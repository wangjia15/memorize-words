import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { Clock, Target, Trophy, Pause, Play, RotateCcw } from "lucide-react";
import { LearningSession } from "@/types/learning";

interface LearningProgressProps {
  session: LearningSession;
  currentWordIndex: number;
  onPauseResume: () => void;
  onRestart: () => void;
  className?: string;
}

export const LearningProgress: React.FC<LearningProgressProps> = ({
  session,
  currentWordIndex,
  onPauseResume,
  onRestart,
  className
}) => {
  const progressPercentage = (currentWordIndex / session.totalWords) * 100;
  const accuracyPercentage = session.completedWords > 0
    ? (session.correctAnswers / session.completedWords) * 100
    : 0;

  const formatTime = (minutes: number): string => {
    const hours = Math.floor(minutes / 60);
    const mins = Math.floor(minutes % 60);
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  };

  const calculateElapsedTime = (): number => {
    if (!session.startTime) return 0;
    const now = new Date();
    const start = new Date(session.startTime);
    return Math.floor((now.getTime() - start.getTime()) / (1000 * 60));
  };

  const elapsedMinutes = calculateElapsedTime();

  return (
    <Card className={className}>
      <CardHeader className="pb-4">
        <div className="flex justify-between items-center">
          <CardTitle className="text-lg">Learning Progress</CardTitle>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={onPauseResume}
              className="flex items-center gap-2"
            >
              {session.isPaused ? (
                <>
                  <Play className="h-4 w-4" />
                  Resume
                </>
              ) : (
                <>
                  <Pause className="h-4 w-4" />
                  Pause
                </>
              )}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={onRestart}
              className="flex items-center gap-2"
            >
              <RotateCcw className="h-4 w-4" />
              Restart
            </Button>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        {/* Main Progress Bar */}
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span>Progress</span>
            <span>{currentWordIndex} / {session.totalWords} words</span>
          </div>
          <Progress value={progressPercentage} className="h-3" />
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${progressPercentage}%` }}
            transition={{ duration: 0.5 }}
            className="text-right text-sm text-muted-foreground"
          >
            {Math.round(progressPercentage)}%
          </motion.div>
        </div>

        {/* Session Stats */}
        <div className="grid grid-cols-2 gap-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg"
          >
            <Target className="h-5 w-5 text-blue-500" />
            <div>
              <p className="text-sm font-medium">Accuracy</p>
              <p className="text-lg font-bold text-blue-600">
                {Math.round(accuracyPercentage)}%
              </p>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg"
          >
            <Clock className="h-5 w-5 text-green-500" />
            <div>
              <p className="text-sm font-medium">Time</p>
              <p className="text-lg font-bold text-green-600">
                {formatTime(elapsedMinutes)}
              </p>
            </div>
          </motion.div>
        </div>

        {/* Detailed Stats */}
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-sm text-muted-foreground">Correct Answers</span>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-green-600">{session.correctAnswers}</span>
              <Trophy className="h-4 w-4 text-green-500" />
            </div>
          </div>

          <div className="flex justify-between items-center">
            <span className="text-sm text-muted-foreground">Incorrect Answers</span>
            <span className="font-semibold text-red-600">
              {session.completedWords - session.correctAnswers}
            </span>
          </div>

          <div className="flex justify-between items-center">
            <span className="text-sm text-muted-foreground">Learning Mode</span>
            <Badge variant="secondary">
              {session.mode.replace('_', ' ').toUpperCase()}
            </Badge>
          </div>

          <div className="flex justify-between items-center">
            <span className="text-sm text-muted-foreground">Difficulty</span>
            <Badge
              variant={session.difficulty === 'advanced' ? 'destructive' : 'secondary'}
            >
              {session.difficulty.toUpperCase()}
            </Badge>
          </div>
        </div>

        {/* Session Settings Info */}
        {session.settings && (
          <div className="space-y-2 pt-4 border-t">
            <h4 className="text-sm font-medium text-muted-foreground">Session Settings</h4>
            <div className="flex flex-wrap gap-2">
              {session.settings.autoAdvance && (
                <Badge variant="outline" className="text-xs">Auto Advance</Badge>
              )}
              {session.settings.enablePronunciation && (
                <Badge variant="outline" className="text-xs">Audio Enabled</Badge>
              )}
              {session.settings.enableHints && (
                <Badge variant="outline" className="text-xs">Hints Enabled</Badge>
              )}
              {session.settings.shuffleWords && (
                <Badge variant="outline" className="text-xs">Shuffled</Badge>
              )}
              {session.settings.repeatIncorrect && (
                <Badge variant="outline" className="text-xs">Repeat Incorrect</Badge>
              )}
            </div>
          </div>
        )}

        {/* Performance Indicator */}
        {session.completedWords >= 5 && (
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.3 }}
            className="p-3 rounded-lg border"
            style={{
              backgroundColor: accuracyPercentage >= 80 ? '#f0f9ff' :
                            accuracyPercentage >= 60 ? '#fffbeb' : '#fef2f2',
              borderColor: accuracyPercentage >= 80 ? '#3b82f6' :
                          accuracyPercentage >= 60 ? '#f59e0b' : '#ef4444'
            }}
          >
            <div className="flex items-center gap-2">
              <Trophy className={`h-4 w-4 ${
                accuracyPercentage >= 80 ? 'text-blue-500' :
                accuracyPercentage >= 60 ? 'text-yellow-500' : 'text-red-500'
              }`} />
              <span className={`text-sm font-medium ${
                accuracyPercentage >= 80 ? 'text-blue-700' :
                accuracyPercentage >= 60 ? 'text-yellow-700' : 'text-red-700'
              }`}>
                {accuracyPercentage >= 80 ? 'Excellent Performance!' :
                 accuracyPercentage >= 60 ? 'Good Progress!' : 'Keep Practicing!'}
              </span>
            </div>
          </motion.div>
        )}

        {/* Session Status */}
        {session.isPaused && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-center p-2 bg-yellow-100 text-yellow-800 rounded-lg text-sm"
          >
            Session is paused. Click Resume to continue.
          </motion.div>
        )}

        {session.isCompleted && (
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="text-center p-4 bg-green-100 text-green-800 rounded-lg"
          >
            <Trophy className="h-6 w-6 mx-auto mb-2" />
            <p className="font-semibold">Session Completed!</p>
            <p className="text-sm">
              Final Score: {session.correctAnswers}/{session.totalWords} ({Math.round(accuracyPercentage)}%)
            </p>
          </motion.div>
        )}
      </CardContent>
    </Card>
  );
};