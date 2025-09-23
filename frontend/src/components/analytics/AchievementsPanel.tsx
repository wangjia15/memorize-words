import React, { useMemo, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { ProgressStatistics, Achievement, AchievementCategory, AchievementRarity } from '@/types/analytics';
import { format } from 'date-fns';
import { Trophy, Star, Zap, Target, Calendar, TrendingUp, Award } from 'lucide-react';

interface AchievementsPanelProps {
  achievements: Achievement[];
  className?: string;
}

interface AchievementCardProps {
  achievement: Achievement;
  progress?: number;
}

export const AchievementsPanel: React.FC<AchievementsPanelProps> = ({
  achievements,
  className
}) => {
  const [selectedCategory, setSelectedCategory] = useState<AchievementCategory | 'all'>('all');
  const [sortBy, setSortBy] = useState<'recent' | 'rarity' | 'category'>('recent');

  const filteredAndSortedAchievements = useMemo(() => {
    let filtered = selectedCategory === 'all'
      ? achievements
      : achievements.filter(a => a.category === selectedCategory);

    // Sort achievements
    filtered = [...filtered].sort((a, b) => {
      switch (sortBy) {
        case 'recent':
          return new Date(b.unlockedAt).getTime() - new Date(a.unlockedAt).getTime();
        case 'rarity':
          const rarityOrder = {
            [AchievementRarity.LEGENDARY]: 4,
            [AchievementRarity.EPIC]: 3,
            [AchievementRarity.RARE]: 2,
            [AchievementRarity.COMMON]: 1
          };
          return rarityOrder[b.rarity] - rarityOrder[a.rarity];
        case 'category':
          return a.category.localeCompare(b.category);
        default:
          return 0;
      }
    });

    return filtered;
  }, [achievements, selectedCategory, sortBy]);

  const categoryStats = useMemo(() => {
    const stats = new Map<AchievementCategory, number>();

    achievements.forEach(achievement => {
      const current = stats.get(achievement.category) || 0;
      stats.set(achievement.category, current + 1);
    });

    return Array.from(stats.entries()).map(([category, count]) => ({
      category,
      count,
      icon: getCategoryIcon(category)
    }));
  }, [achievements]);

  const rarityStats = useMemo(() => {
    const stats = new Map<AchievementRarity, number>();

    achievements.forEach(achievement => {
      const current = stats.get(achievement.rarity) || 0;
      stats.set(achievement.rarity, current + 1);
    });

    return Array.from(stats.entries()).map(([rarity, count]) => ({
      rarity,
      count
    }));
  }, [achievements]);

  const totalAchievements = achievements.length;
  const unlockedAchievements = achievements.filter(a => a.progress === undefined || a.progress === (a.maxProgress || 1)).length;
  const overallProgress = totalAchievements > 0 ? (unlockedAchievements / totalAchievements) * 100 : 0;

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span className="flex items-center gap-2">
            <Trophy className="w-5 h-5" />
            Achievements
          </span>
          <Badge variant="outline">
            {unlockedAchievements}/{totalAchievements} Unlocked
          </Badge>
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          Track your learning milestones and accomplishments
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Overall Progress */}
          <div className="p-3 bg-muted rounded-lg">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium">Overall Progress</span>
              <span className="text-sm text-muted-foreground">{overallProgress.toFixed(0)}%</span>
            </div>
            <Progress value={overallProgress} className="h-2" />
            <div className="text-xs text-muted-foreground mt-1">
              {totalAchievements - unlockedAchievements} achievements remaining
            </div>
          </div>

          {/* Category Filter */}
          <div className="flex flex-wrap gap-2">
            <Button
              variant={selectedCategory === 'all' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedCategory('all')}
            >
              All ({totalAchievements})
            </Button>
            {categoryStats.map(({ category, count, icon: Icon }) => (
              <Button
                key={category}
                variant={selectedCategory === category ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedCategory(category)}
              >
                <Icon className="w-4 h-4 mr-1" />
                {formatCategoryName(category)} ({count})
              </Button>
            ))}
          </div>

          {/* Sort Options */}
          <div className="flex gap-2">
            <Button
              variant={sortBy === 'recent' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSortBy('recent')}
            >
              Recent
            </Button>
            <Button
              variant={sortBy === 'rarity' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSortBy('rarity')}
            >
              Rarity
            </Button>
            <Button
              variant={sortBy === 'category' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSortBy('category')}
            >
              Category
            </Button>
          </div>

          {/* Achievement Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {filteredAndSortedAchievements.map((achievement) => (
              <AchievementCard
                key={achievement.id}
                achievement={achievement}
                progress={achievement.progress}
              />
            ))}
          </div>

          {/* Rarity Summary */}
          <div className="border-t pt-4">
            <h4 className="text-sm font-medium mb-3">Achievement Rarity</h4>
            <div className="grid grid-cols-4 gap-2">
              {rarityStats.map(({ rarity, count }) => (
                <div key={rarity} className="text-center">
                  <div className={`w-8 h-8 rounded-full mx-auto mb-1 ${getRarityColor(rarity)}`}>
                    <Award className="w-4 h-4 m-2" />
                  </div>
                  <div className="text-xs font-medium">{formatRarityName(rarity)}</div>
                  <div className="text-xs text-muted-foreground">{count}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

const AchievementCard: React.FC<AchievementCardProps> = ({ achievement, progress }) => {
  const isUnlocked = progress === undefined || progress === (achievement.maxProgress || 1);
  const progressPercentage = achievement.maxProgress
    ? ((progress || 0) / achievement.maxProgress) * 100
    : 100;

  return (
    <div className={`p-3 rounded-lg border transition-all duration-200 hover:shadow-md ${
      isUnlocked
        ? getRarityBgColor(achievement.rarity)
        : 'bg-muted/50 border-muted'
    }`}>
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-2">
          <div className={`w-8 h-8 rounded-full flex items-center justify-center ${getRarityColor(achievement.rarity)}`}>
            {getAchievementIcon(achievement.category)}
          </div>
          <div>
            <h4 className={`text-sm font-medium ${isUnlocked ? '' : 'text-muted-foreground'}`}>
              {achievement.name}
            </h4>
            <Badge variant="outline" className="text-xs mt-1">
              {formatRarityName(achievement.rarity)}
            </Badge>
          </div>
        </div>
        {isUnlocked && (
          <Star className="w-4 h-4 text-yellow-500 fill-current" />
        )}
      </div>

      <p className="text-xs text-muted-foreground mb-2">
        {achievement.description}
      </p>

      {/* Progress Bar */}
      {!isUnlocked && achievement.maxProgress && (
        <div className="space-y-1">
          <div className="flex justify-between text-xs">
            <span>Progress</span>
            <span>{progress || 0}/{achievement.maxProgress}</span>
          </div>
          <Progress value={progressPercentage} className="h-2" />
        </div>
      )}

      {/* Unlocked Date */}
      {isUnlocked && (
        <div className="text-xs text-muted-foreground mt-2">
          Unlocked on {format(new Date(achievement.unlockedAt), 'MMM dd, yyyy')}
        </div>
      )}
    </div>
  );
};

// Helper functions
function getCategoryIcon(category: AchievementCategory): React.ComponentType<{ className?: string }> {
  switch (category) {
    case AchievementCategory.STREAK:
      return Calendar;
    case AchievementCategory.ACCURACY:
      return Target;
    case AchievementCategory.VOLUME:
      return TrendingUp;
    case AchievementCategory.SPEED:
      return Zap;
    case AchievementCategory.CONSISTENCY:
      return Calendar;
    case AchievementCategory.MASTERY:
      return Award;
    default:
      return Trophy;
  }
}

function getAchievementIcon(category: AchievementCategory): React.ReactNode {
  switch (category) {
    case AchievementCategory.STREAK:
      return <Calendar className="w-4 h-4" />;
    case AchievementCategory.ACCURACY:
      return <Target className="w-4 h-4" />;
    case AchievementCategory.VOLUME:
      return <TrendingUp className="w-4 h-4" />;
    case AchievementCategory.SPEED:
      return <Zap className="w-4 h-4" />;
    case AchievementCategory.CONSISTENCY:
      return <Calendar className="w-4 h-4" />;
    case AchievementCategory.MASTERY:
      return <Award className="w-4 h-4" />;
    default:
      return <Trophy className="w-4 h-4" />;
  }
}

function getRarityColor(rarity: AchievementRarity): string {
  switch (rarity) {
    case AchievementRarity.COMMON:
      return 'bg-gray-100 text-gray-800';
    case AchievementRarity.RARE:
      return 'bg-blue-100 text-blue-800';
    case AchievementRarity.EPIC:
      return 'bg-purple-100 text-purple-800';
    case AchievementRarity.LEGENDARY:
      return 'bg-yellow-100 text-yellow-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

function getRarityBgColor(rarity: AchievementRarity): string {
  switch (rarity) {
    case AchievementRarity.COMMON:
      return 'bg-gray-50 border-gray-200';
    case AchievementRarity.RARE:
      return 'bg-blue-50 border-blue-200';
    case AchievementRarity.EPIC:
      return 'bg-purple-50 border-purple-200';
    case AchievementRarity.LEGENDARY:
      return 'bg-yellow-50 border-yellow-200';
    default:
      return 'bg-gray-50 border-gray-200';
  }
}

function formatCategoryName(category: AchievementCategory): string {
  return category.charAt(0).toUpperCase() + category.slice(1).replace('_', ' ');
}

function formatRarityName(rarity: AchievementRarity): string {
  return rarity.charAt(0).toUpperCase() + rarity.slice(1).toLowerCase();
}

export default AchievementsPanel;