import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ReviewSession } from '@/types/review';
import { reviewUtils } from '@/services/reviewApi';
import {
  Clock,
  Target,
  TrendingUp,
  Pause,
  Play,
  RotateCcw,
  Square,
  Zap,
  Award
} from 'lucide-react';

interface ReviewProgressProps {
  session: ReviewSession;
  currentIndex: number;
  showDetailed?: boolean;
  compact?: boolean;
  onPause?: () => void;
  onResume?: () => void;
  onRestart?: () => void;
  onEnd?: () => void;
  isPaused?: boolean;
}

export const ReviewProgress: React.FC<ReviewProgressProps> = ({
  session,
  currentIndex,
  showDetailed = false,
  compact = false,
  onPause,
  onResume,
  onRestart,
  onEnd,
  isPaused = false
}) => {
  const progress = ((currentIndex + 1) / session.totalCards) * 100;
  const accuracy = session.completedCards > 0
    ? (session.correctAnswers / session.completedCards) * 100
    : 0;
  const averageTime = session.averageResponseTime / 1000; // Convert to seconds
  const cardsPerMinute = session.cardsPerMinute || 0;
  const efficiencyScore = session.efficiencyScore || 0;

  const getAccuracyColor = (acc: number) => {
    if (acc >= 90) return 'text-green-600';
    if (acc >= 70) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getProgressColor = (progress: number) => {
    if (progress >= 80) return 'bg-green-500';
    if (progress >= 50) return 'bg-yellow-500';
    return 'bg-blue-500';
  };

  const getEfficiencyColor = (score: number) => {
    if (score >= 0.8) return 'text-green-600';
    if (score >= 0.6) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getModeIcon = (mode: string) => {
    switch (mode.toLowerCase()) {
      case 'due_cards':
        return <Clock className="h-4 w-4" />;
      case 'difficult_cards':
        return <Target className="h-4 w-4" />;
      case 'new_cards':
        return <Award className="h-4 w-4" />;
      case 'random_review':
        return <RotateCcw className="h-4 w-4" />;
      default:
        return <TrendingUp className="h-4 w-4" />;
    }
  };

  if (compact) {
    return (
      <Card className="w-full">
        <CardContent className="p-4">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              {getModeIcon(session.mode)}
              <span className="text-sm font-medium">
                {session.currentCardIndex + 1} / {session.totalCards}
              </span>
            </div>
            <Badge variant="outline">
              {progress.toFixed(0)}%
            </Badge>
          </div>
          <Progress
            value={progress}
            className="h-2 mb-2"
          />
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>Accuracy: <span className={getAccuracyColor(accuracy)}>{accuracy.toFixed(1)}%</span></span>
            <span>Time: {averageTime.toFixed(1)}s</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full">
      <CardContent className="p-6">
        {/* Main Progress Section */}
        <div className="space-y-4">
          {/* Progress Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              {getModeIcon(session.mode)}
              <div>
                <h3 className="text-lg font-semibold">
                  Session Progress
                </h3>
                <p className="text-sm text-muted-foreground">
                  {session.mode.replace('_', ' ').toLowerCase()}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline">
                {session.currentCardIndex + 1} / {session.totalCards}
              </Badge>
              <Badge variant={progress >= 80 ? 'default' : 'secondary'}>
                {progress.toFixed(0)}% Complete
              </Badge>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium">Progress</span>
              <span className="text-sm text-muted-foreground">
                {session.completedCards} of {session.totalCards} cards completed
              </span>
            </div>
            <Progress
              value={progress}
              className="h-3"
            />
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {/* Accuracy */}
            <div className="flex items-center gap-3 p-3 bg-muted rounded-lg">
              <Target className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Accuracy</p>
                <p className={`text-lg font-semibold ${getAccuracyColor(accuracy)}`}>
                  {accuracy.toFixed(1)}%
                </p>
              </div>
            </div>

            {/* Average Time */}
            <div className="flex items-center gap-3 p-3 bg-muted rounded-lg">
              <Clock className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Avg Time</p>
                <p className="text-lg font-semibold">
                  {averageTime.toFixed(1)}s
                </p>
              </div>
            </div>

            {/* Cards Per Minute */}
            <div className="flex items-center gap-3 p-3 bg-muted rounded-lg">
              <Zap className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Speed</p>
                <p className="text-lg font-semibold">
                  {cardsPerMinute.toFixed(1)}/min
                </p>
              </div>
            </div>

            {/* Efficiency Score */}
            <div className="flex items-center gap-3 p-3 bg-muted rounded-lg">
              <TrendingUp className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Efficiency</p>
                <p className={`text-lg font-semibold ${getEfficiencyColor(efficiencyScore)}`}>
                  {(efficiencyScore * 100).toFixed(0)}%
                </p>
              </div>
            </div>
          </div>

          {/* Session Controls */}
          <div className="flex items-center justify-between pt-4 border-t">
            <div className="flex items-center gap-2">
              <Badge variant="outline">
                Remaining: {session.remainingCards || (session.totalCards - session.completedCards)}
              </Badge>
              <Badge variant="outline">
                Correct: {session.correctAnswers}
              </Badge>
              {session.sessionScore && (
                <Badge variant="outline">
                  Score: {session.sessionScore.toFixed(0)}
                </Badge>
              )}
            </div>

            <div className="flex items-center gap-2">
              {isPaused ? (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={onResume}
                >
                  <Play className="h-4 w-4 mr-2" />
                  Resume
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={onPause}
                >
                  <Pause className="h-4 w-4 mr-2" />
                  Pause
                </Button>
              )}

              <Button
                variant="outline"
                size="sm"
                onClick={onRestart}
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Restart
              </Button>

              <Button
                variant="destructive"
                size="sm"
                onClick={onEnd}
              >
                <Square className="h-4 w-4 mr-2" />
                End Session
              </Button>
            </div>
          </div>

          {/* Detailed Stats */}
          {showDetailed && (
            <div className="mt-4 pt-4 border-t">
              <h4 className="text-sm font-semibold mb-3">Detailed Statistics</h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">New Cards:</span>
                  <span className="ml-2 font-semibold">{session.newCardsLearned || 0}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Difficult Cards:</span>
                  <span className="ml-2 font-semibold">{session.difficultCardsMastered || 0}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Learning Velocity:</span>
                  <span className="ml-2 font-semibold">{session.learningVelocity?.toFixed(2) || 0}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Session Duration:</span>
                  <span className="ml-2 font-semibold">
                    {session.sessionDuration
                      ? reviewUtils.formatResponseTime(session.sessionDuration * 1000)
                      : '0s'
                    }
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};