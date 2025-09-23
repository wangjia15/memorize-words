import React, { useState, useEffect } from 'react';
import { useReviewSession } from '@/hooks/useReviewSession';
import { useQuery } from '@tanstack/react-query';
import { reviewApi } from '@/services/reviewApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { ReviewCard } from '@/components/review/ReviewCard';
import { ReviewProgress } from '@/components/review/ReviewProgress';
import { ReviewMode, ReviewOutcome } from '@/types/review';
import {
  BookOpen,
  Clock,
  Target,
  TrendingUp,
  Flame,
  Brain,
  Calendar,
  BarChart3,
  Zap,
  Award,
  Users,
  Play,
  Pause,
  RotateCcw,
  Square
} from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

export const ReviewDashboard: React.FC = () => {
  const {
    session,
    currentCard,
    currentCardIndex,
    showAnswer,
    isSubmitting,
    isPaused,
    progress,
    startReview,
    submitReview,
    toggleAnswer,
    pauseSession,
    resumeSession,
    restartSession,
    endSession,
    statistics,
    preferences,
    isStarting,
    isLoadingActiveSession
  } = useReviewSession();

  const [selectedMode, setSelectedMode] = useState<ReviewMode>(ReviewMode.DUE_CARDS);
  const [showDetailedStats, setShowDetailedStats] = useState(false);

  // Get available review modes
  const { data: availableModes } = useQuery({
    queryKey: ['review-modes'],
    queryFn: () => reviewApi.getAvailableReviewModes(),
    staleTime: 5 * 60 * 1000 // 5 minutes
  });

  // Get due cards count
  const { data: dueCardsData } = useQuery({
    queryKey: ['due-cards'],
    queryFn: () => reviewApi.getDueCards(1), // Just get count
    staleTime: 2 * 60 * 1000 // 2 minutes
  });

  // Review modes configuration
  const reviewModes = [
    {
      mode: ReviewMode.DUE_CARDS,
      title: 'Due Cards',
      description: 'Review cards that are due for practice',
      icon: Clock,
      count: dueCardsData?.totalDue || 0,
      color: 'bg-blue-500',
      recommended: true
    },
    {
      mode: ReviewMode.DIFFICULT_CARDS,
      title: 'Difficult Cards',
      description: 'Focus on challenging words',
      icon: Brain,
      count: dueCardsData?.totalDifficult || 0,
      color: 'bg-orange-500',
      recommended: false
    },
    {
      mode: ReviewMode.NEW_CARDS,
      title: 'New Cards',
      description: 'Learn new vocabulary',
      icon: BookOpen,
      count: dueCardsData?.totalNew || 0,
      color: 'bg-green-500',
      recommended: false
    },
    {
      mode: ReviewMode.RANDOM_REVIEW,
      title: 'Random Review',
      description: 'Practice random words from your collection',
      icon: BarChart3,
      count: statistics?.totalCards || 0,
      color: 'bg-purple-500',
      recommended: false
    }
  ];

  // Loading state
  if (isLoadingActiveSession || isStarting) {
    return (
      <div className="container mx-auto py-8 max-w-6xl">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-muted-foreground">Loading review session...</p>
          </div>
        </div>
      </div>
    );
  }

  // Active session view
  if (session && currentCard) {
    return (
      <div className="container mx-auto py-8 max-w-6xl">
        <div className="mb-6">
          <ReviewProgress
            session={session}
            currentIndex={currentCardIndex}
            showDetailed={showDetailedStats}
            onPause={pauseSession}
            onResume={resumeSession}
            onRestart={restartSession}
            onEnd={endSession}
            isPaused={isPaused}
          />
        </div>

        <ReviewCard
          card={currentCard}
          onSubmit={submitReview}
          showAnswer={showAnswer}
          onToggleAnswer={toggleAnswer}
          isSubmitting={isSubmitting}
          enablePronunciation={preferences?.enablePronunciation}
          showHint={preferences?.enableHints}
        />
      </div>
    );
  }

  // Dashboard view
  return (
    <div className="container mx-auto py-8 max-w-6xl">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-2">Review Dashboard</h1>
        <p className="text-muted-foreground">
          Practice your vocabulary with spaced repetition
        </p>
      </div>

      {/* Statistics Overview */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Current Streak</p>
                  <p className="text-2xl font-bold">{statistics.streakDays} days</p>
                  {statistics.longestStreak > statistics.streakDays && (
                    <p className="text-xs text-muted-foreground">
                      Best: {statistics.longestStreak} days
                    </p>
                  )}
                </div>
                <Flame className="h-8 w-8 text-orange-500" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Accuracy</p>
                  <p className="text-2xl font-bold">{statistics.averageAccuracy.toFixed(1)}%</p>
                  <p className="text-xs text-muted-foreground">
                    {statistics.totalReviews} total reviews
                  </p>
                </div>
                <Target className="h-8 w-8 text-green-500" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Reviews Today</p>
                  <p className="text-2xl font-bold">{statistics.todayReviews || 0}</p>
                  <p className="text-xs text-muted-foreground">
                    This week: {statistics.weeklyReviews || 0}
                  </p>
                </div>
                <Calendar className="h-8 w-8 text-blue-500" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Total Cards</p>
                  <p className="text-2xl font-bold">{statistics.totalCards}</p>
                  <p className="text-xs text-muted-foreground">
                    Retention: {(statistics.retentionRate * 100).toFixed(1)}%
                  </p>
                </div>
                <BookOpen className="h-8 w-8 text-purple-500" />
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Review Mode Selection */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-semibold">Choose Review Mode</h2>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowDetailedStats(!showDetailedStats)}
          >
            {showDetailedStats ? 'Hide Stats' : 'Show Stats'}
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {reviewModes.map((modeInfo) => (
            <Card
              key={modeInfo.mode}
              className={`cursor-pointer transition-all hover:shadow-md ${
                selectedMode === modeInfo.mode ? 'ring-2 ring-primary' : ''
              } ${modeInfo.recommended ? 'border-2 border-blue-200' : ''}`}
              onClick={() => setSelectedMode(modeInfo.mode)}
            >
              <CardContent className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className={`p-2 rounded-lg ${modeInfo.color}`}>
                    <modeInfo.icon className="h-6 w-6 text-white" />
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant="outline">
                      {modeInfo.count} cards
                    </Badge>
                    {modeInfo.recommended && (
                      <Badge className="bg-blue-500">
                        Recommended
                      </Badge>
                    )}
                  </div>
                </div>

                <h3 className="text-lg font-semibold mb-2">{modeInfo.title}</h3>
                <p className="text-sm text-muted-foreground mb-4">
                  {modeInfo.description}
                </p>

                <Button
                  className="w-full"
                  onClick={(e) => {
                    e.stopPropagation();
                    startReview(modeInfo.mode, 20);
                  }}
                  disabled={modeInfo.count === 0}
                >
                  <Play className="h-4 w-4 mr-2" />
                  Start Review
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Daily Goal</p>
                <p className="text-lg font-semibold">{preferences?.sessionGoal || 20} cards</p>
              </div>
              <Target className="h-6 w-6 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Daily Limit</p>
                <p className="text-lg font-semibold">{preferences?.dailyReviewLimit || 50}</p>
              </div>
              <Zap className="h-6 w-6 text-yellow-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">New Cards</p>
                <p className="text-lg font-semibold">{preferences?.dailyNewCardLimit || 10}</p>
              </div>
              <BookOpen className="h-6 w-6 text-green-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      {statistics?.recentActivity && statistics.recentActivity.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {statistics.recentActivity.slice(0, 5).map((activity, index) => (
                <div key={index} className="flex items-center justify-between py-2 border-b last:border-b-0">
                  <div className="flex items-center gap-3">
                    <div className={`w-2 h-2 rounded-full ${
                      activity.accuracy >= 80 ? 'bg-green-500' :
                      activity.accuracy >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                    }`} />
                    <span className="text-sm">{activity.date}</span>
                    <Badge variant="outline" className="text-xs">
                      {activity.sessionMode}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-sm text-muted-foreground">
                      {activity.cardsReviewed} cards
                    </span>
                    <Badge variant={activity.accuracy >= 80 ? 'default' : 'secondary'}>
                      {activity.accuracy.toFixed(1)}%
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Achievements */}
      {statistics?.achievements && statistics.achievements.length > 0 && (
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Recent Achievements</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {statistics.achievements
                .filter(achievement => achievement.isUnlocked)
                .slice(0, 6)
                .map((achievement) => (
                  <div key={achievement.id} className="flex items-center gap-3 p-3 bg-muted rounded-lg">
                    <div className="text-2xl">{achievement.icon}</div>
                    <div>
                      <h4 className="font-semibold text-sm">{achievement.title}</h4>
                      <p className="text-xs text-muted-foreground">{achievement.description}</p>
                    </div>
                  </div>
                ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};