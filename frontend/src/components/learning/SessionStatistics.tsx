import React, { useMemo, useState, useEffect } from "react";
import { motion } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { LearningSession, LearningWord } from "@/types/learning";
import { cn } from "@/lib/utils";
import {
  Trophy,
  Target,
  Clock,
  TrendingUp,
  TrendingDown,
  Zap,
  Brain,
  CheckCircle,
  XCircle,
  ArrowLeft,
  BarChart3,
  PieChart,
  Activity,
  Star,
  TimerIcon,
  PlayCircle
} from "lucide-react";

interface SessionStatisticsProps {
  session: LearningSession | null;
  onBackToLearning: () => void;
  onStartNewSession: () => void;
  className?: string;
}

interface WordAnalysis {
  word: LearningWord;
  attempts: number;
  successRate: number;
  averageTime: number;
  difficulty: 'easy' | 'medium' | 'hard';
}

interface PerformanceMetrics {
  overallAccuracy: number;
  averageTimePerWord: number;
  totalTime: number;
  wordsPerMinute: number;
  difficultyBreakdown: Record<string, { correct: number; total: number }>;
  modePerformance: Record<string, { correct: number; total: number }>;
  improvementAreas: string[];
  strengths: string[];
  learningVelocity?: number;
}

export const SessionStatistics: React.FC<SessionStatisticsProps> = ({
  session,
  onBackToLearning,
  onStartNewSession,
  className
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'detailed' | 'progress'>('overview');
  const [realTimeStats, setRealTimeStats] = useState<PerformanceMetrics | null>(null);

  // Real-time statistics updates
  useEffect(() => {
    if (session && !session.isCompleted) {
      const interval = setInterval(() => {
        const updatedMetrics = calculateRealTimeMetrics(session);
        setRealTimeStats(updatedMetrics);
      }, 1000); // Update every second

      return () => clearInterval(interval);
    }
  }, [session]);

  const calculateRealTimeMetrics = (currentSession: LearningSession): PerformanceMetrics => {
    const completedWords = currentSession.words.filter(word => word.isCompleted);
    const currentTime = currentSession.duration || 0;
    const averageTimePerWord = completedWords.length > 0 ? currentTime / completedWords.length : 0;
    const wordsPerMinute = currentTime > 0 ? (completedWords.length / currentTime) * 60 : 0;

    const accuracy = currentSession.completedWords > 0
      ? (currentSession.correctAnswers / currentSession.completedWords) * 100
      : 0;

    // Calculate learning velocity (improvement over time)
    const learningVelocity = calculateLearningVelocity(currentSession.words);

    // Difficulty breakdown
    const difficultyBreakdown: Record<string, { correct: number; total: number }> = {};
    completedWords.forEach(word => {
      if (!difficultyBreakdown[word.difficulty]) {
        difficultyBreakdown[word.difficulty] = { correct: 0, total: 0 };
      }
      difficultyBreakdown[word.difficulty].total++;
      if (word.correctAttempts > 0) {
        difficultyBreakdown[word.difficulty].correct++;
      }
    });

    return {
      overallAccuracy: accuracy,
      averageTimePerWord,
      totalTime: currentTime,
      wordsPerMinute,
      difficultyBreakdown,
      modePerformance: {
        [currentSession.mode]: {
          correct: currentSession.correctAnswers,
          total: currentSession.completedWords
        }
      },
      improvementAreas: calculateImprovementAreas(accuracy, averageTimePerWord, difficultyBreakdown),
      strengths: calculateStrengths(accuracy, averageTimePerWord, difficultyBreakdown),
      learningVelocity
    };
  };

  const calculateLearningVelocity = (words: LearningWord[]): number => {
    const completedWords = words.filter(w => w.isCompleted);
    if (completedWords.length < 3) return 0;

    const recentWords = completedWords.slice(-5); // Last 5 words
    const earlierWords = completedWords.slice(-10, -5); // Previous 5 words

    const recentAccuracy = recentWords.reduce((sum, w) => sum + (w.correctAttempts > 0 ? 1 : 0), 0) / recentWords.length;
    const earlierAccuracy = earlierWords.length > 0
      ? earlierWords.reduce((sum, w) => sum + (w.correctAttempts > 0 ? 1 : 0), 0) / earlierWords.length
      : 0;

    return (recentAccuracy - earlierAccuracy) * 100; // Percentage improvement
  };

  const calculateImprovementAreas = (accuracy: number, avgTime: number, diffBreakdown: Record<string, any>): string[] => {
    const areas: string[] = [];

    if (accuracy < 60) areas.push("Overall accuracy needs improvement");
    if (avgTime > 45) areas.push("Consider improving response time");

    Object.entries(diffBreakdown).forEach(([difficulty, stats]) => {
      const difficultyAccuracy = stats.total > 0 ? (stats.correct / stats.total) * 100 : 0;
      if (difficultyAccuracy < 50) {
        areas.push(`${difficulty} level words need more practice`);
      }
    });

    return areas;
  };

  const calculateStrengths = (accuracy: number, avgTime: number, diffBreakdown: Record<string, any>): string[] => {
    const strengths: string[] = [];

    if (accuracy >= 80) strengths.push("Excellent overall accuracy");
    if (avgTime < 20) strengths.push("Great response speed");

    Object.entries(diffBreakdown).forEach(([difficulty, stats]) => {
      const difficultyAccuracy = stats.total > 0 ? (stats.correct / stats.total) * 100 : 0;
      if (difficultyAccuracy >= 80) {
        strengths.push(`Strong performance on ${difficulty} level words`);
      }
    });

    return strengths;
  };

  // Calculate comprehensive performance metrics
  const performanceMetrics = useMemo((): PerformanceMetrics => {
    // Use real-time stats if available (for active sessions)
    if (realTimeStats && session && !session.isCompleted) {
      return realTimeStats;
    }

    if (!session) {
      return {
        overallAccuracy: 0,
        averageTimePerWord: 0,
        totalTime: 0,
        wordsPerMinute: 0,
        difficultyBreakdown: {},
        modePerformance: {},
        improvementAreas: [],
        strengths: []
      };
    }

    const completedWords = session.words.filter(word => word.isCompleted);
    const totalTime = session.duration || 0;
    const averageTimePerWord = completedWords.length > 0 ? totalTime / completedWords.length : 0;
    const wordsPerMinute = totalTime > 0 ? (completedWords.length / totalTime) * 60 : 0;

    // Difficulty breakdown
    const difficultyBreakdown: Record<string, { correct: number; total: number }> = {};
    completedWords.forEach(word => {
      if (!difficultyBreakdown[word.difficulty]) {
        difficultyBreakdown[word.difficulty] = { correct: 0, total: 0 };
      }
      difficultyBreakdown[word.difficulty].total++;
      if (word.correctAttempts > 0) {
        difficultyBreakdown[word.difficulty].correct++;
      }
    });

    // Mode performance (for mixed mode)
    const modePerformance: Record<string, { correct: number; total: number }> = {
      [session.mode]: {
        correct: session.correctAnswers,
        total: session.completedWords
      }
    };

    // Identify improvement areas and strengths
    const improvementAreas: string[] = [];
    const strengths: string[] = [];

    const accuracy = session.completedWords > 0 ? (session.correctAnswers / session.completedWords) * 100 : 0;

    if (accuracy < 60) {
      improvementAreas.push("Overall accuracy needs improvement");
    } else if (accuracy >= 80) {
      strengths.push("Excellent overall accuracy");
    }

    if (averageTimePerWord > 45) {
      improvementAreas.push("Consider improving response time");
    } else if (averageTimePerWord < 20) {
      strengths.push("Great response speed");
    }

    // Check difficulty-specific performance
    Object.entries(difficultyBreakdown).forEach(([difficulty, stats]) => {
      const difficultyAccuracy = stats.total > 0 ? (stats.correct / stats.total) * 100 : 0;
      if (difficultyAccuracy < 50) {
        improvementAreas.push(`${difficulty} level words need more practice`);
      } else if (difficultyAccuracy >= 80) {
        strengths.push(`Strong performance on ${difficulty} level words`);
      }
    });

    return {
      overallAccuracy: accuracy,
      averageTimePerWord,
      totalTime,
      wordsPerMinute,
      difficultyBreakdown,
      modePerformance,
      improvementAreas,
      strengths
    };
  }, [session]);

  // Analyze individual word performance
  const wordAnalysis = useMemo((): WordAnalysis[] => {
    if (!session) return [];

    return session.words
      .filter(word => word.isCompleted)
      .map(word => {
        const successRate = word.attempts > 0 ? (word.correctAttempts / word.attempts) * 100 : 0;
        let difficulty: 'easy' | 'medium' | 'hard' = 'medium';

        if (successRate >= 80) difficulty = 'easy';
        else if (successRate < 50) difficulty = 'hard';

        return {
          word,
          attempts: word.attempts,
          successRate,
          averageTime: word.timeSpent,
          difficulty
        };
      })
      .sort((a, b) => b.successRate - a.successRate);
  }, [session]);

  const formatTime = (seconds: number): string => {
    if (seconds < 60) return `${Math.round(seconds)}s`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.round(seconds % 60);
    return `${minutes}m ${remainingSeconds}s`;
  };

  const getPerformanceColor = (percentage: number): string => {
    if (percentage >= 80) return "text-green-600";
    if (percentage >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  const getPerformanceBadgeVariant = (percentage: number): "default" | "secondary" | "destructive" => {
    if (percentage >= 80) return "default";
    if (percentage >= 60) return "secondary";
    return "destructive";
  };

  if (!session) {
    return (
      <div className={cn("max-w-4xl mx-auto", className)}>
        <Card>
          <CardContent className="p-8 text-center">
            <BarChart3 className="h-16 w-16 mx-auto text-muted-foreground mb-4" />
            <h2 className="text-2xl font-bold mb-2">No Session Data</h2>
            <p className="text-muted-foreground mb-6">
              Start a learning session to see your statistics and progress.
            </p>
            <Button onClick={onStartNewSession} className="flex items-center gap-2">
              <PlayCircle className="h-4 w-4" />
              Start New Session
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className={cn("max-w-6xl mx-auto space-y-6", className)}>
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between"
      >
        <div>
          <h1 className="text-3xl font-bold">Session Statistics</h1>
          <p className="text-muted-foreground">
            {session.mode.replace('_', ' ').toUpperCase()} • {session.difficulty.toUpperCase()} Level
          </p>
        </div>
        <div className="flex gap-2">
          {!session.isCompleted && (
            <Button variant="outline" onClick={onBackToLearning} className="flex items-center gap-2">
              <ArrowLeft className="h-4 w-4" />
              Back to Learning
            </Button>
          )}
          <Button onClick={onStartNewSession} className="flex items-center gap-2">
            <PlayCircle className="h-4 w-4" />
            New Session
          </Button>
        </div>
      </motion.div>

      {/* Tab Navigation */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="flex space-x-1 bg-muted p-1 rounded-lg"
      >
        {[
          { key: 'overview', label: 'Overview', icon: BarChart3 },
          { key: 'detailed', label: 'Detailed Analysis', icon: PieChart },
          { key: 'progress', label: 'Word Progress', icon: Activity }
        ].map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              className={cn(
                "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-all",
                activeTab === tab.key
                  ? "bg-background text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              )}
            >
              <Icon className="h-4 w-4" />
              {tab.label}
            </button>
          );
        })}
      </motion.div>

      {/* Tab Content */}
      <motion.div
        key={activeTab}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Real-time Learning Velocity Indicator */}
            {session && !session.isCompleted && performanceMetrics.learningVelocity !== undefined && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 rounded-lg p-4"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-green-100 rounded-lg">
                    <TrendingUp className="h-5 w-5 text-green-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-green-800">
                      Learning Velocity: {performanceMetrics.learningVelocity > 0 ? 'Improving' :
                                         performanceMetrics.learningVelocity < 0 ? 'Declining' : 'Stable'}
                    </h3>
                    <p className="text-sm text-green-600">
                      {Math.abs(performanceMetrics.learningVelocity).toFixed(1)}% change in recent performance
                    </p>
                  </div>
                  <div className="ml-auto">
                    <Badge variant={performanceMetrics.learningVelocity > 0 ? 'default' : 'secondary'}>
                      {session && !session.isCompleted ? 'Live' : 'Final'}
                    </Badge>
                  </div>
                </div>
              </motion.div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {/* Overall Performance */}
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center gap-4">
                    <div className="p-3 bg-blue-100 rounded-lg">
                      <Target className="h-6 w-6 text-blue-600" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="text-sm font-medium text-muted-foreground">Accuracy</p>
                        {session && !session.isCompleted && (
                          <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
                        )}
                      </div>
                      <p className={cn("text-2xl font-bold", getPerformanceColor(performanceMetrics.overallAccuracy))}>
                        {Math.round(performanceMetrics.overallAccuracy)}%
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Session Progress */}
              <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-4">
                  <div className="p-3 bg-green-100 rounded-lg">
                    <CheckCircle className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Completed</p>
                    <p className="text-2xl font-bold">
                      {session.completedWords}/{session.totalWords}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Time Metrics */}
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-4">
                  <div className="p-3 bg-purple-100 rounded-lg">
                    <Clock className="h-6 w-6 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Avg. Time</p>
                    <p className="text-2xl font-bold">
                      {formatTime(performanceMetrics.averageTimePerWord)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Speed */}
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-4">
                  <div className="p-3 bg-orange-100 rounded-lg">
                    <Zap className="h-6 w-6 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Words/Min</p>
                    <p className="text-2xl font-bold">
                      {Math.round(performanceMetrics.wordsPerMinute)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Session Overview */}
            <Card className="md:col-span-2 lg:col-span-4">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Trophy className="h-5 w-5" />
                  Session Overview
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="font-semibold mb-3 flex items-center gap-2">
                      <TrendingUp className="h-4 w-4 text-green-500" />
                      Strengths
                    </h4>
                    {performanceMetrics.strengths.length > 0 ? (
                      <ul className="space-y-2">
                        {performanceMetrics.strengths.map((strength, index) => (
                          <li key={index} className="flex items-center gap-2 text-sm">
                            <CheckCircle className="h-4 w-4 text-green-500" />
                            {strength}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-sm text-muted-foreground">Complete more words to see your strengths</p>
                    )}
                  </div>

                  <div>
                    <h4 className="font-semibold mb-3 flex items-center gap-2">
                      <TrendingDown className="h-4 w-4 text-orange-500" />
                      Areas for Improvement
                    </h4>
                    {performanceMetrics.improvementAreas.length > 0 ? (
                      <ul className="space-y-2">
                        {performanceMetrics.improvementAreas.map((area, index) => (
                          <li key={index} className="flex items-center gap-2 text-sm">
                            <Brain className="h-4 w-4 text-orange-500" />
                            {area}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-sm text-muted-foreground">Great job! No major areas needing improvement</p>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
            </div>
          </div>
        )}

        {activeTab === 'detailed' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Difficulty Breakdown */}
            <Card>
              <CardHeader>
                <CardTitle>Performance by Difficulty</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {Object.entries(performanceMetrics.difficultyBreakdown).map(([difficulty, stats]) => {
                  const percentage = stats.total > 0 ? (stats.correct / stats.total) * 100 : 0;
                  return (
                    <div key={difficulty} className="space-y-2">
                      <div className="flex justify-between items-center">
                        <span className="font-medium capitalize">{difficulty}</span>
                        <Badge variant={getPerformanceBadgeVariant(percentage)}>
                          {stats.correct}/{stats.total} ({Math.round(percentage)}%)
                        </Badge>
                      </div>
                      <Progress value={percentage} className="h-2" />
                    </div>
                  );
                })}
              </CardContent>
            </Card>

            {/* Session Details */}
            <Card>
              <CardHeader>
                <CardTitle>Session Details</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Learning Mode</span>
                  <Badge>{session.mode.replace('_', ' ').toUpperCase()}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Difficulty Level</span>
                  <Badge variant="secondary">{session.difficulty.toUpperCase()}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Total Time</span>
                  <span className="font-medium">{formatTime(performanceMetrics.totalTime)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Start Time</span>
                  <span className="font-medium">
                    {new Date(session.startTime).toLocaleTimeString()}
                  </span>
                </div>
                {session.endTime && (
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">End Time</span>
                    <span className="font-medium">
                      {new Date(session.endTime).toLocaleTimeString()}
                    </span>
                  </div>
                )}
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Status</span>
                  <Badge variant={session.isCompleted ? "default" : session.isPaused ? "secondary" : "default"}>
                    {session.isCompleted ? "Completed" : session.isPaused ? "Paused" : "Active"}
                  </Badge>
                </div>
              </CardContent>
            </Card>

            {/* Performance Trends */}
            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle>Performance Insights</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <Star className="h-8 w-8 mx-auto mb-2 text-yellow-500" />
                    <p className="font-semibold">Grade</p>
                    <p className="text-2xl font-bold">
                      {performanceMetrics.overallAccuracy >= 90 ? 'A+' :
                       performanceMetrics.overallAccuracy >= 80 ? 'A' :
                       performanceMetrics.overallAccuracy >= 70 ? 'B' :
                       performanceMetrics.overallAccuracy >= 60 ? 'C' : 'D'}
                    </p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <TimerIcon className="h-8 w-8 mx-auto mb-2 text-blue-500" />
                    <p className="font-semibold">Pace</p>
                    <p className="text-2xl font-bold">
                      {performanceMetrics.averageTimePerWord < 20 ? 'Fast' :
                       performanceMetrics.averageTimePerWord < 35 ? 'Good' : 'Steady'}
                    </p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <Brain className="h-8 w-8 mx-auto mb-2 text-purple-500" />
                    <p className="font-semibold">Focus</p>
                    <p className="text-2xl font-bold">
                      {performanceMetrics.overallAccuracy >= 85 ? 'Sharp' :
                       performanceMetrics.overallAccuracy >= 70 ? 'Good' : 'Building'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {activeTab === 'progress' && (
          <div className="space-y-6">
            {/* Word-by-word Analysis */}
            <Card>
              <CardHeader>
                <CardTitle>Individual Word Performance</CardTitle>
                <p className="text-sm text-muted-foreground">
                  Showing {wordAnalysis.length} completed words
                </p>
              </CardHeader>
              <CardContent>
                <div className="space-y-3 max-h-96 overflow-y-auto">
                  {wordAnalysis.map((analysis, index) => (
                    <motion.div
                      key={analysis.word.id}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                      className="flex items-center justify-between p-3 bg-muted rounded-lg"
                    >
                      <div className="flex-1">
                        <div className="flex items-center gap-3">
                          <h4 className="font-semibold">{analysis.word.word}</h4>
                          <Badge
                            variant={
                              analysis.difficulty === 'easy' ? 'default' :
                              analysis.difficulty === 'medium' ? 'secondary' : 'destructive'
                            }
                            className="text-xs"
                          >
                            {analysis.difficulty}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground">{analysis.word.definition}</p>
                      </div>
                      <div className="flex items-center gap-4 text-sm">
                        <div className="text-center">
                          <p className="font-medium">{Math.round(analysis.successRate)}%</p>
                          <p className="text-muted-foreground">Success</p>
                        </div>
                        <div className="text-center">
                          <p className="font-medium">{analysis.attempts}</p>
                          <p className="text-muted-foreground">Attempts</p>
                        </div>
                        <div className="text-center">
                          <p className="font-medium">{formatTime(analysis.averageTime)}</p>
                          <p className="text-muted-foreground">Time</p>
                        </div>
                      </div>
                    </motion.div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </motion.div>
    </div>
  );
};