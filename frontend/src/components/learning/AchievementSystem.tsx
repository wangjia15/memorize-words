import React, { useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { LearningSession } from "@/types/learning";
import { cn } from "@/lib/utils";
import {
  Trophy,
  Star,
  Target,
  Zap,
  Crown,
  Medal,
  Award,
  Flame,
  Brain,
  ArrowLeft,
  TrendingUp,
  CheckCircle,
  Calendar,
  Lock,
  Unlock,
  Gift,
  Sparkles,
  Rocket,
  Gem,
  Activity
} from "lucide-react";

interface AchievementSystemProps {
  session: LearningSession | null;
  onBackToLearning: () => void;
  className?: string;
}

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: React.ComponentType<{ className?: string }>;
  iconColor: string;
  category: 'accuracy' | 'speed' | 'consistency' | 'milestone' | 'special';
  requirement: {
    type: 'percentage' | 'count' | 'streak' | 'time' | 'special';
    value: number;
    description: string;
  };
  tier: 'bronze' | 'silver' | 'gold' | 'platinum' | 'diamond';
  points: number;
  isUnlocked: boolean;
  unlockedAt?: Date;
  progress: number; // 0-100
}

interface UserStats {
  totalSessions: number;
  totalWords: number;
  totalCorrect: number;
  currentStreak: number;
  longestStreak: number;
  averageAccuracy: number;
  totalTime: number;
  achievements: Achievement[];
  level: number;
  experience: number;
  experienceToNext: number;
}

const achievementDefinitions: Omit<Achievement, 'isUnlocked' | 'unlockedAt' | 'progress'>[] = [
  // Accuracy Achievements
  {
    id: 'perfect-session',
    title: 'Perfect Score',
    description: 'Complete a session with 100% accuracy',
    icon: Crown,
    iconColor: 'text-yellow-500',
    category: 'accuracy',
    requirement: {
      type: 'percentage',
      value: 100,
      description: '100% accuracy in one session'
    },
    tier: 'gold',
    points: 100
  },
  {
    id: 'accuracy-master',
    title: 'Accuracy Master',
    description: 'Achieve 90% accuracy or higher',
    icon: Target,
    iconColor: 'text-blue-500',
    category: 'accuracy',
    requirement: {
      type: 'percentage',
      value: 90,
      description: '90%+ accuracy in session'
    },
    tier: 'silver',
    points: 50
  },
  {
    id: 'sharpshooter',
    title: 'Sharpshooter',
    description: 'Achieve 80% accuracy or higher',
    icon: Target,
    iconColor: 'text-green-500',
    category: 'accuracy',
    requirement: {
      type: 'percentage',
      value: 80,
      description: '80%+ accuracy in session'
    },
    tier: 'bronze',
    points: 25
  },

  // Speed Achievements
  {
    id: 'lightning-fast',
    title: 'Lightning Fast',
    description: 'Complete words at 5+ words per minute',
    icon: Zap,
    iconColor: 'text-yellow-500',
    category: 'speed',
    requirement: {
      type: 'count',
      value: 5,
      description: '5+ words per minute'
    },
    tier: 'gold',
    points: 75
  },
  {
    id: 'speed-demon',
    title: 'Speed Demon',
    description: 'Complete words at 3+ words per minute',
    icon: Rocket,
    iconColor: 'text-orange-500',
    category: 'speed',
    requirement: {
      type: 'count',
      value: 3,
      description: '3+ words per minute'
    },
    tier: 'silver',
    points: 40
  },
  {
    id: 'quick-learner',
    title: 'Quick Learner',
    description: 'Complete words at 2+ words per minute',
    icon: Brain,
    iconColor: 'text-purple-500',
    category: 'speed',
    requirement: {
      type: 'count',
      value: 2,
      description: '2+ words per minute'
    },
    tier: 'bronze',
    points: 20
  },

  // Milestone Achievements
  {
    id: 'century-club',
    title: 'Century Club',
    description: 'Learn 100 words correctly',
    icon: Medal,
    iconColor: 'text-amber-500',
    category: 'milestone',
    requirement: {
      type: 'count',
      value: 100,
      description: '100 correct words'
    },
    tier: 'gold',
    points: 150
  },
  {
    id: 'half-century',
    title: 'Half Century',
    description: 'Learn 50 words correctly',
    icon: Award,
    iconColor: 'text-blue-500',
    category: 'milestone',
    requirement: {
      type: 'count',
      value: 50,
      description: '50 correct words'
    },
    tier: 'silver',
    points: 75
  },
  {
    id: 'first-ten',
    title: 'Getting Started',
    description: 'Learn your first 10 words correctly',
    icon: Star,
    iconColor: 'text-green-500',
    category: 'milestone',
    requirement: {
      type: 'count',
      value: 10,
      description: '10 correct words'
    },
    tier: 'bronze',
    points: 25
  },

  // Consistency Achievements
  {
    id: 'dedication',
    title: 'Dedication',
    description: 'Complete 10 learning sessions',
    icon: Calendar,
    iconColor: 'text-indigo-500',
    category: 'consistency',
    requirement: {
      type: 'count',
      value: 10,
      description: '10 completed sessions'
    },
    tier: 'silver',
    points: 60
  },
  {
    id: 'committed',
    title: 'Committed Learner',
    description: 'Complete 5 learning sessions',
    icon: CheckCircle,
    iconColor: 'text-green-500',
    category: 'consistency',
    requirement: {
      type: 'count',
      value: 5,
      description: '5 completed sessions'
    },
    tier: 'bronze',
    points: 30
  },
  {
    id: 'marathon-session',
    title: 'Marathon Session',
    description: 'Complete a 30+ word session',
    icon: Activity,
    iconColor: 'text-red-500',
    category: 'consistency',
    requirement: {
      type: 'count',
      value: 30,
      description: '30+ words in one session'
    },
    tier: 'gold',
    points: 80
  },

  // Special Achievements
  {
    id: 'first-session',
    title: 'First Steps',
    description: 'Complete your first learning session',
    icon: Gift,
    iconColor: 'text-pink-500',
    category: 'special',
    requirement: {
      type: 'count',
      value: 1,
      description: 'Complete first session'
    },
    tier: 'bronze',
    points: 20
  },
  {
    id: 'perfectionist',
    title: 'Perfectionist',
    description: 'Get 5 words correct in a row',
    icon: Gem,
    iconColor: 'text-cyan-500',
    category: 'special',
    requirement: {
      type: 'streak',
      value: 5,
      description: '5 correct answers in a row'
    },
    tier: 'silver',
    points: 45
  },
  {
    id: 'unstoppable',
    title: 'Unstoppable',
    description: 'Get 10 words correct in a row',
    icon: Flame,
    iconColor: 'text-orange-500',
    category: 'special',
    requirement: {
      type: 'streak',
      value: 10,
      description: '10 correct answers in a row'
    },
    tier: 'gold',
    points: 100
  }
];

export const AchievementSystem: React.FC<AchievementSystemProps> = ({
  session,
  onBackToLearning,
  className
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [showUnlockedOnly, setShowUnlockedOnly] = useState(false);

  // Calculate user stats and achievements
  const userStats = useMemo((): UserStats => {
    // In a real app, this would come from backend user data
    // For now, we'll simulate based on current session with enhanced calculations
    const baseStats: UserStats = {
      totalSessions: session ? (session.isCompleted ? 1 : 0) : 0,
      totalWords: session ? session.completedWords : 0,
      totalCorrect: session ? session.correctAnswers : 0,
      currentStreak: session ? calculateCurrentStreak(session) : 0,
      longestStreak: session ? calculateCurrentStreak(session) : 0,
      averageAccuracy: session && session.completedWords > 0
        ? (session.correctAnswers / session.completedWords) * 100
        : 0,
      totalTime: session ? session.duration || 0 : 0,
      achievements: [],
      level: 1,
      experience: 0,
      experienceToNext: 100
    };

    // Enhanced streak calculation for consecutive sessions
    if (session) {
      baseStats.currentStreak = Math.max(baseStats.currentStreak, calculateSessionStreaks(session));
      baseStats.longestStreak = Math.max(baseStats.longestStreak, baseStats.currentStreak);
    }

    // Calculate achievements based on current session with enhanced logic
    const achievements = achievementDefinitions.map(def => {
      let isUnlocked = false;
      let progress = 0;

      if (!session) {
        return { ...def, isUnlocked: false, progress: 0 };
      }

      switch (def.requirement.type) {
        case 'percentage':
          const accuracy = session.completedWords > 0
            ? (session.correctAnswers / session.completedWords) * 100
            : 0;
          progress = Math.min((accuracy / def.requirement.value) * 100, 100);
          isUnlocked = accuracy >= def.requirement.value;
          break;

        case 'count':
          if (def.category === 'milestone') {
            progress = Math.min((baseStats.totalCorrect / def.requirement.value) * 100, 100);
            isUnlocked = baseStats.totalCorrect >= def.requirement.value;
          } else if (def.category === 'consistency') {
            if (def.id === 'marathon-session') {
              progress = Math.min((session.totalWords / def.requirement.value) * 100, 100);
              isUnlocked = session.totalWords >= def.requirement.value;
            } else {
              progress = Math.min((baseStats.totalSessions / def.requirement.value) * 100, 100);
              isUnlocked = baseStats.totalSessions >= def.requirement.value;
            }
          } else if (def.category === 'speed') {
            const wordsPerMinute = session.duration ? (session.completedWords / session.duration) * 60 : 0;
            progress = Math.min((wordsPerMinute / def.requirement.value) * 100, 100);
            isUnlocked = wordsPerMinute >= def.requirement.value;
          }
          break;

        case 'streak':
          progress = Math.min((baseStats.currentStreak / def.requirement.value) * 100, 100);
          isUnlocked = baseStats.currentStreak >= def.requirement.value;
          break;

        case 'special':
          if (def.id === 'first-session') {
            isUnlocked = session.isCompleted || session.completedWords > 0;
            progress = isUnlocked ? 100 : 0;
          }
          break;
      }

      return {
        ...def,
        isUnlocked,
        progress,
        unlockedAt: isUnlocked ? new Date() : undefined
      };
    });

    // Calculate level and experience based on unlocked achievements
    const totalPoints = achievements
      .filter(a => a.isUnlocked)
      .reduce((sum, a) => sum + a.points, 0);

    const level = Math.floor(totalPoints / 100) + 1;
    const experience = totalPoints % 100;

    return {
      ...baseStats,
      achievements,
      level,
      experience,
      experienceToNext: 100 - experience
    };
  }, [session]);

  // Helper function to calculate current streak
  function calculateCurrentStreak(session: LearningSession): number {
    if (!session || session.words.length === 0) return 0;

    let streak = 0;
    const completedWords = session.words.filter(w => w.isCompleted);

    for (let i = completedWords.length - 1; i >= 0; i--) {
      if (completedWords[i].correctAttempts > 0) {
        streak++;
      } else {
        break;
      }
    }

    return streak;
  }

  // Enhanced streak calculation for session performance
  function calculateSessionStreaks(session: LearningSession): number {
    if (!session) return 0;

    const completedWords = session.words.filter(w => w.isCompleted);
    let maxStreak = 0;
    let currentStreak = 0;

    completedWords.forEach(word => {
      if (word.correctAttempts > 0) {
        currentStreak++;
        maxStreak = Math.max(maxStreak, currentStreak);
      } else {
        currentStreak = 0;
      }
    });

    return maxStreak;
  }

  const categories = [
    { key: 'all', label: 'All', icon: Trophy },
    { key: 'accuracy', label: 'Accuracy', icon: Target },
    { key: 'speed', label: 'Speed', icon: Zap },
    { key: 'milestone', label: 'Milestones', icon: Medal },
    { key: 'consistency', label: 'Consistency', icon: Calendar },
    { key: 'special', label: 'Special', icon: Sparkles }
  ];

  const filteredAchievements = userStats.achievements.filter(achievement => {
    const categoryMatch = selectedCategory === 'all' || achievement.category === selectedCategory;
    const unlockedMatch = !showUnlockedOnly || achievement.isUnlocked;
    return categoryMatch && unlockedMatch;
  });

  const getTierColor = (tier: string): string => {
    switch (tier) {
      case 'bronze': return 'text-amber-600 bg-amber-100';
      case 'silver': return 'text-gray-600 bg-gray-100';
      case 'gold': return 'text-yellow-600 bg-yellow-100';
      case 'platinum': return 'text-indigo-600 bg-indigo-100';
      case 'diamond': return 'text-cyan-600 bg-cyan-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className={cn("max-w-6xl mx-auto space-y-6", className)}>
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between"
      >
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-3">
            <Trophy className="h-8 w-8 text-yellow-500" />
            Achievements
          </h1>
          <p className="text-muted-foreground">
            Track your learning progress and unlock rewards
          </p>
        </div>
        <Button variant="outline" onClick={onBackToLearning} className="flex items-center gap-2">
          <ArrowLeft className="h-4 w-4" />
          Back to Learning
        </Button>
      </motion.div>

      {/* User Level & Progress */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="grid grid-cols-1 md:grid-cols-3 gap-6"
      >
        <Card>
          <CardContent className="p-6 text-center">
            <div className="flex items-center justify-center mb-4">
              <div className="relative">
                <Crown className="h-12 w-12 text-yellow-500" />
                <Badge className="absolute -bottom-2 -right-2" variant="default">
                  {userStats.level}
                </Badge>
              </div>
            </div>
            <h3 className="font-semibold text-lg">Level {userStats.level}</h3>
            <p className="text-sm text-muted-foreground">Learning Master</p>
            <div className="mt-4">
              <Progress value={(userStats.experience / 100) * 100} className="h-2" />
              <p className="text-xs text-muted-foreground mt-2">
                {userStats.experience}/{100} XP to next level
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <Star className="h-12 w-12 mx-auto mb-4 text-blue-500" />
            <h3 className="font-semibold text-lg">
              {userStats.achievements.filter(a => a.isUnlocked).length}
            </h3>
            <p className="text-sm text-muted-foreground">Achievements Unlocked</p>
            <p className="text-xs text-muted-foreground mt-2">
              {userStats.achievements.length - userStats.achievements.filter(a => a.isUnlocked).length} remaining
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 text-center">
            <Flame className="h-12 w-12 mx-auto mb-4 text-orange-500" />
            <h3 className="font-semibold text-lg">{userStats.currentStreak}</h3>
            <p className="text-sm text-muted-foreground">Current Streak</p>
            <p className="text-xs text-muted-foreground mt-2">
              Best: {userStats.longestStreak} correct in a row
            </p>
          </CardContent>
        </Card>
      </motion.div>

      {/* Category Filter */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="flex flex-wrap gap-2"
      >
        {categories.map((category) => {
          const Icon = category.icon;
          return (
            <Button
              key={category.key}
              variant={selectedCategory === category.key ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedCategory(category.key)}
              className="flex items-center gap-2"
            >
              <Icon className="h-4 w-4" />
              {category.label}
            </Button>
          );
        })}
        <Button
          variant={showUnlockedOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setShowUnlockedOnly(!showUnlockedOnly)}
          className="ml-4"
        >
          {showUnlockedOnly ? <Unlock className="h-4 w-4" /> : <Lock className="h-4 w-4" />}
          {showUnlockedOnly ? 'Unlocked Only' : 'Show All'}
        </Button>
      </motion.div>

      {/* Achievements Grid */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
      >
        <AnimatePresence>
          {filteredAchievements.map((achievement, index) => {
            const Icon = achievement.icon;
            return (
              <motion.div
                key={achievement.id}
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card className={cn(
                  "relative overflow-hidden transition-all duration-300",
                  achievement.isUnlocked
                    ? "border-primary shadow-lg hover:shadow-xl"
                    : "opacity-60 hover:opacity-80"
                )}>
                  {achievement.isUnlocked && (
                    <motion.div
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      className="absolute top-2 right-2"
                    >
                      <CheckCircle className="h-5 w-5 text-green-500" />
                    </motion.div>
                  )}

                  <CardContent className="p-6">
                    <div className="flex items-start gap-4">
                      <div className={cn(
                        "p-3 rounded-lg",
                        achievement.isUnlocked ? "bg-primary/10" : "bg-muted"
                      )}>
                        <Icon className={cn(
                          "h-6 w-6",
                          achievement.isUnlocked ? achievement.iconColor : "text-muted-foreground"
                        )} />
                      </div>

                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold">{achievement.title}</h3>
                          <Badge className={getTierColor(achievement.tier)} variant="secondary">
                            {achievement.tier}
                          </Badge>
                        </div>

                        <p className="text-sm text-muted-foreground mb-3">
                          {achievement.description}
                        </p>

                        <div className="space-y-2">
                          <div className="flex justify-between items-center text-xs">
                            <span className="text-muted-foreground">
                              {achievement.requirement.description}
                            </span>
                            <span className="font-medium">
                              {achievement.points} XP
                            </span>
                          </div>

                          {!achievement.isUnlocked && (
                            <div>
                              <Progress value={achievement.progress} className="h-1" />
                              <p className="text-xs text-muted-foreground mt-1">
                                {Math.round(achievement.progress)}% complete
                              </p>
                            </div>
                          )}

                          {achievement.isUnlocked && achievement.unlockedAt && (
                            <p className="text-xs text-green-600 font-medium">
                              ✓ Unlocked {achievement.unlockedAt.toLocaleDateString()}
                            </p>
                          )}
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </motion.div>

      {filteredAchievements.length === 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center py-12"
        >
          <Trophy className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
          <h3 className="text-lg font-semibold mb-2">No achievements found</h3>
          <p className="text-muted-foreground">
            {showUnlockedOnly
              ? "You haven't unlocked any achievements in this category yet."
              : "No achievements in this category."}
          </p>
        </motion.div>
      )}

      {/* Enhanced Motivational Footer */}
      {userStats.achievements.some(a => !a.isUnlocked) && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
        >
          <Card className="bg-gradient-to-r from-blue-50 to-purple-50 border-blue-200">
            <CardContent className="p-6 text-center">
              <Sparkles className="h-8 w-8 mx-auto mb-4 text-purple-500" />
              <h3 className="text-lg font-semibold mb-2">Keep Learning!</h3>
              <p className="text-muted-foreground mb-4">
                Continue your learning journey to unlock more achievements and level up.
                You're doing great!
              </p>

              {/* Next Achievement Preview */}
              {(() => {
                const nextAchievement = userStats.achievements
                  .filter(a => !a.isUnlocked && a.progress > 0)
                  .sort((a, b) => b.progress - a.progress)[0];

                if (nextAchievement) {
                  const Icon = nextAchievement.icon;
                  return (
                    <div className="bg-white/50 rounded-lg p-4 mt-4">
                      <div className="flex items-center gap-3 mb-2">
                        <Icon className={cn("h-5 w-5", nextAchievement.iconColor)} />
                        <span className="font-medium text-sm">Next Achievement: {nextAchievement.title}</span>
                      </div>
                      <Progress value={nextAchievement.progress} className="h-2 mb-2" />
                      <p className="text-xs text-muted-foreground">
                        {Math.round(nextAchievement.progress)}% complete - {nextAchievement.description}
                      </p>
                    </div>
                  );
                }
                return null;
              })()}

              {/* Streak Motivation */}
              {userStats.currentStreak > 0 && (
                <div className="bg-orange-50 rounded-lg p-3 mt-4">
                  <div className="flex items-center gap-2 justify-center">
                    <Flame className="h-4 w-4 text-orange-500" />
                    <span className="text-sm font-medium text-orange-700">
                      You're on a {userStats.currentStreak} word streak! Keep it going!
                    </span>
                  </div>
                </div>
              )}

              {/* Level Progress */}
              <div className="bg-indigo-50 rounded-lg p-3 mt-4">
                <div className="flex items-center gap-2 justify-center mb-2">
                  <Crown className="h-4 w-4 text-indigo-500" />
                  <span className="text-sm font-medium text-indigo-700">
                    Level {userStats.level} - {userStats.experienceToNext} XP to next level
                  </span>
                </div>
                <Progress
                  value={(userStats.experience / 100) * 100}
                  className="h-1.5"
                />
              </div>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </div>
  );
};