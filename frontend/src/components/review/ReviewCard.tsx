import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { useTimer } from '@/hooks/useTimer';
import { ReviewOutcome, SpacedRepetitionCard } from '@/types/review';
import { reviewUtils } from '@/services/reviewApi';
import { formatDistanceToNow } from 'date-fns';
import { animation, responsive, memory, performanceMonitor } from '@/utils/performance';
import {
  Brain,
  Clock,
  Target,
  TrendingUp,
  Volume2,
  RotateCcw,
  Lightbulb,
  Flag
} from 'lucide-react';

interface ReviewCardProps {
  card: SpacedRepetitionCard;
  onSubmit: (outcome: ReviewOutcome) => void;
  showAnswer: boolean;
  onToggleAnswer: () => void;
  isSubmitting?: boolean;
  className?: string;
  showHint?: boolean;
  enablePronunciation?: boolean;
  compact?: boolean;
}

export const ReviewCard: React.FC<ReviewCardProps> = ({
  card,
  onSubmit,
  showAnswer,
  onToggleAnswer,
  isSubmitting = false,
  className = '',
  showHint = false,
  enablePronunciation = true,
  compact = false
}) => {
  const [responseTime, setResponseTime] = useState(0);
  const [hintUsed, setHintUsed] = useState(false);
  const [markedAsDifficult, setMarkedAsDifficult] = useState(false);
  const [showPronunciation, setShowPronunciation] = useState(false);
  const timer = useTimer({ autoStart: true, interval: 100 });
  const cardRef = useRef<HTMLDivElement>(null);
  const prefersReducedMotion = animation.useReducedMotion();
  const isMobile = responsive.useBreakpoint() === 'xs' || responsive.useBreakpoint() === 'sm';

  useEffect(() => {
    setResponseTime(timer.time);
  }, [timer.time]);

  // Performance monitoring
  useEffect(() => {
    performanceMonitor.startMeasure('ReviewCard-render');
    return () => {
      performanceMonitor.endMeasure('ReviewCard-render');
    };
  }, []);

  // Memoized expensive calculations
  const dueStatus = useMemo(() => reviewUtils.getDueStatus(card.dueDate), [card.dueDate]);
  const cardStateColor = useMemo(() => reviewUtils.getCardStateColor(
    card.difficultyLevel > 0.5 ? 'RELEARNING' : 'REVIEW'
  ), [card.difficultyLevel]);

  const outcomeStyles = useMemo(() => {
    const styles: Record<ReviewOutcome, any> = {};
    Object.values(ReviewOutcome).forEach(outcome => {
      styles[outcome] = reviewUtils.getOutcomeStyle(outcome);
    });
    return styles;
  }, []);

  // Memoized event handlers
  const handleSubmit = useCallback((outcome: ReviewOutcome) => {
    performanceMonitor.startMeasure('ReviewCard-submit');
    timer.stop();
    onSubmit(outcome);
    performanceMonitor.endMeasure('ReviewCard-submit');
  }, [timer, onSubmit]);

  const handleHintClick = useCallback(() => {
    setHintUsed(true);
  }, []);

  const handleDifficultClick = useCallback(() => {
    setMarkedAsDifficult(prev => !prev);
  }, []);

  const handlePronounce = useCallback(() => {
    if (enablePronunciation && card.word.pronunciation) {
      setShowPronunciation(true);
      setTimeout(() => setShowPronunciation(false), 2000);
    }
  }, [enablePronunciation, card.word.pronunciation]);

  const getCardStateColor = (state: string) => {
    return reviewUtils.getCardStateColor(state);
  };

  const getOutcomeButtonStyle = useMemo(() => (outcome: ReviewOutcome) => {
    const style = outcomeStyles[outcome];
    return {
      className: `${style.bgColor} ${style.hoverColor} text-white h-14 text-lg font-semibold`,
      label: style.label,
      description: style.description
    };
  }, [outcomeStyles]);

  const getDifficultyColor = useCallback((difficulty: number) => {
    if (difficulty >= 0.7) return 'text-red-600';
    if (difficulty >= 0.4) return 'text-yellow-600';
    return 'text-green-600';
  }, []);

  const getRetentionColor = useCallback((retention: number) => {
    if (retention >= 0.8) return 'text-green-600';
    if (retention >= 0.6) return 'text-yellow-600';
    return 'text-red-600';
  }, []);

  if (compact) {
    return (
      <Card ref={cardRef} className={`w-full max-w-2xl mx-auto h-[400px] relative ${className}`}>
        <CardContent className="p-4 h-full flex flex-col">
          <div className="flex-1 flex flex-col justify-center items-center cursor-pointer" onClick={onToggleAnswer}>
            <AnimatePresence mode="wait">
              {!showAnswer ? (
                <motion.div
                  key="question"
                  initial={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: 20 }}
                  animate={prefersReducedMotion ? { opacity: 1 } : { opacity: 1, y: 0 }}
                  exit={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: -20 }}
                  transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
                  className="text-center px-4"
                >
                  <h2 className={`text-3xl font-bold mb-4 text-primary ${isMobile ? 'text-2xl' : ''}`}>
                    {card.word.text}
                  </h2>
                  {card.word.pronunciation && (
                    <p className="text-sm text-muted-foreground mb-2">
                      /{card.word.pronunciation}/
                    </p>
                  )}
                  <p className="text-sm text-muted-foreground">
                    Click to reveal
                  </p>
                </motion.div>
              ) : (
                <motion.div
                  key="answer"
                  initial={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: 20 }}
                  animate={prefersReducedMotion ? { opacity: 1 } : { opacity: 1, y: 0 }}
                  exit={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: -20 }}
                  transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
                  className="text-center px-4"
                >
                  <h3 className={`text-xl font-semibold mb-4 text-primary ${isMobile ? 'text-lg' : ''}`}>
                    {card.word.translation}
                  </h3>
                  {hintUsed && card.word.exampleSentence && (
                    <p className="text-sm text-muted-foreground italic mb-4">
                      "{card.word.exampleSentence}"
                    </p>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Action Buttons */}
          <div className={`grid gap-2 ${isMobile ? 'grid-cols-2' : 'grid-cols-4'}`}>
            {Object.values(ReviewOutcome).map((outcome) => {
              const style = getOutcomeButtonStyle(outcome);
              return (
                <Button
                  key={outcome}
                  variant={outcome === ReviewOutcome.GOOD ? 'default' : 'outline'}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleSubmit(outcome);
                  }}
                  disabled={isSubmitting}
                  className={`h-12 text-sm ${style.className} ${isMobile ? 'text-xs' : ''}`}
                >
                  {style.label}
                  {isMobile && (
                    <span className="block text-xs opacity-75 mt-1">
                      {style.description}
                    </span>
                  )}
                </Button>
              );
            })}
          </div>
        </CardContent>

        {/* Loading Overlay */}
        {isSubmitting && (
          <div className="absolute inset-0 bg-background/80 flex items-center justify-center backdrop-blur-sm">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
              <p className="text-sm text-muted-foreground">Submitting...</p>
            </div>
          </div>
        )}
      </Card>
    );
  }

  return (
    <Card ref={cardRef} className={`w-full max-w-4xl mx-auto relative ${className} ${isMobile ? 'h-[500px]' : 'h-[600px]'}`}>
      {/* Progress Header */}
      <CardHeader className={`pb-4 ${isMobile ? 'pb-2' : ''}`}>
        <div className="flex flex-col space-y-2">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge className={`${cardStateColor} text-xs ${isMobile ? 'text-xs' : ''}`}>
                {card.isNew ? 'New' : card.isDifficult ? 'Difficult' : 'Review'}
              </Badge>
              <Badge variant="outline" className="text-xs">
                {card.intervalDays}d
              </Badge>
              {!isMobile && (
                <>
                  <Badge variant="secondary" className="text-xs">
                    Ease: {card.easeFactor.toFixed(2)}
                  </Badge>
                  <Badge variant="outline" className={`text-xs ${dueStatus.statusColor}`}>
                    {dueStatus.statusText}
                  </Badge>
                </>
              )}
            </div>
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <div className="flex items-center gap-1">
                <Clock className="h-3 w-3" />
                <span>{reviewUtils.formatResponseTime(responseTime)}</span>
              </div>
              {!isMobile && (
                <>
                  <div className="flex items-center gap-1">
                    <Target className="h-3 w-3" />
                    <span className={getDifficultyColor(card.difficultyRating)}>
                      {card.difficultyRating.toFixed(2)}
                    </span>
                  </div>
                  <div className="flex items-center gap-1">
                    <TrendingUp className="h-3 w-3" />
                    <span className={getRetentionColor(card.retentionRate)}>
                      {(card.retentionRate * 100).toFixed(0)}%
                    </span>
                  </div>
                </>
              )}
            </div>
          </div>
          {isMobile && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Badge variant="secondary" className="text-xs">
                Ease: {card.easeFactor.toFixed(2)}
              </Badge>
              <Badge variant="outline" className={`text-xs ${dueStatus.statusColor}`}>
                {dueStatus.statusText}
              </Badge>
            </div>
          )}
        </div>
      </CardHeader>

      {/* Main Content Area */}
      <CardContent className={`h-full ${isMobile ? 'pb-24' : 'pb-32'}`}>
        <motion.div
          className="h-full flex flex-col justify-center items-center cursor-pointer"
          onClick={onToggleAnswer}
          whileHover={!prefersReducedMotion ? { scale: showAnswer ? 1 : 1.02 } : undefined}
          whileTap={!prefersReducedMotion ? { scale: showAnswer ? 1 : 0.98 } : undefined}
          transition={{ duration: prefersReducedMotion ? 0 : 0.2 }}
        >
          <AnimatePresence mode="wait">
            {!showAnswer ? (
              <motion.div
                key="question"
                initial={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: 20 }}
                animate={prefersReducedMotion ? { opacity: 1 } : { opacity: 1, y: 0 }}
                exit={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: -20 }}
                transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
                className="text-center px-8"
              >
                <h2 className={`font-bold mb-6 text-primary ${isMobile ? 'text-3xl' : 'text-5xl'}`}>
                  {card.word.text}
                </h2>

                {/* Pronunciation */}
                {card.word.pronunciation && (
                  <div className="flex items-center justify-center gap-2 mb-4">
                    <p className="text-lg text-muted-foreground">
                      /{card.word.pronunciation}/
                    </p>
                    {enablePronunciation && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          handlePronounce();
                        }}
                        className="h-8 w-8 p-0"
                      >
                        <Volume2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                )}

                {/* Word Type */}
                {card.word.type && (
                  <Badge variant="outline" className="mb-4">
                    {card.word.type}
                  </Badge>
                )}

                {/* Pronunciation Animation */}
                <AnimatePresence>
                  {showPronunciation && (
                    <motion.div
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.8 }}
                      className="text-sm text-green-600 mb-4"
                    >
                      🔊 Pronouncing...
                    </motion.div>
                  )}
                </AnimatePresence>

                <p className="text-muted-foreground">
                  Click to reveal definition
                </p>

                {/* Hint Button */}
                {showHint && !hintUsed && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleHintClick();
                    }}
                    className="mt-4"
                  >
                    <Lightbulb className="h-4 w-4 mr-2" />
                    Get Hint
                  </Button>
                )}

                {/* Mark as Difficult */}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDifficultClick();
                  }}
                  className={`mt-2 ${markedAsDifficult ? 'text-red-600' : ''}`}
                >
                  <Flag className="h-4 w-4 mr-2" />
                  {markedAsDifficult ? 'Marked as Difficult' : 'Mark as Difficult'}
                </Button>
              </motion.div>
            ) : (
              <motion.div
                key="answer"
                initial={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: 20 }}
                animate={prefersReducedMotion ? { opacity: 1 } : { opacity: 1, y: 0 }}
                exit={prefersReducedMotion ? { opacity: 0 } : { opacity: 0, y: -20 }}
                transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
                className="text-center px-8"
              >
                <h3 className="text-3xl font-semibold mb-6 text-primary">
                  {card.word.translation}
                </h3>

                {/* Example Sentence */}
                {card.word.exampleSentence && (
                  <div className="mb-6">
                    <p className="text-lg text-muted-foreground italic">
                      "{card.word.exampleSentence}"
                    </p>
                    {card.word.exampleTranslation && (
                      <p className="text-sm text-muted-foreground mt-2">
                        {card.word.exampleTranslation}
                      </p>
                    )}
                  </div>
                )}

                {/* Category */}
                {card.word.category && (
                  <Badge variant="secondary" className="mb-6">
                    {card.word.category}
                  </Badge>
                )}

                {/* Action Buttons */}
                <div className="flex flex-col gap-3 mt-8">
                  <div className={`grid gap-3 ${isMobile ? 'grid-cols-2' : 'grid-cols-4'}`}>
                    {Object.values(ReviewOutcome).map((outcome) => {
                      const style = getOutcomeButtonStyle(outcome);
                      return (
                        <Button
                          key={outcome}
                          variant={outcome === ReviewOutcome.GOOD ? 'default' : 'outline'}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSubmit(outcome);
                          }}
                          disabled={isSubmitting}
                          className={`${isMobile ? 'h-12 text-sm' : 'h-14 text-lg'} font-semibold ${style.className}`}
                        >
                          {style.label}
                          <span className={`block ${isMobile ? 'text-[10px]' : 'text-xs'} opacity-75`}>
                            {style.description}
                          </span>
                        </Button>
                      );
                    })}
                  </div>

                  {/* Additional Info */}
                  <div className={`mt-6 grid gap-4 ${isMobile ? 'grid-cols-2 text-xs' : 'grid-cols-3 text-sm'}`}>
                    <div className="text-center">
                      <span className="text-muted-foreground">Reviews:</span>
                      <span className="ml-1 font-semibold">{card.totalReviews}</span>
                    </div>
                    <div className="text-center">
                      <span className="text-muted-foreground">Success:</span>
                      <span className="ml-1 font-semibold">
                        {card.totalReviews > 0
                          ? Math.round((card.correctReviews / card.totalReviews) * 100)
                          : 0}%
                      </span>
                    </div>
                    {!isMobile && (
                      <div className="text-center">
                        <span className="text-muted-foreground">Streak:</span>
                        <span className="ml-1 font-semibold">{card.consecutiveCorrect}</span>
                      </div>
                    )}
                  </div>

                  {/* Card Stats */}
                  <div className={`mt-4 grid gap-4 ${isMobile ? 'grid-cols-1' : 'grid-cols-2'} text-xs text-muted-foreground`}>
                    <div className="flex justify-between">
                      <span>Performance:</span>
                      <span className="font-semibold">{card.performanceIndex.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Stability:</span>
                      <span className="font-semibold">{card.stabilityFactor.toFixed(2)}</span>
                    </div>
                    {!isMobile && (
                      <>
                        <div className="flex justify-between">
                          <span>Avg Response:</span>
                          <span className="font-semibold">{reviewUtils.formatResponseTime(card.averageResponseTime)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span>Total Study:</span>
                          <span className="font-semibold">{Math.floor(card.totalStudyTime / 60000)}m</span>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </CardContent>

      {/* Fixed Footer */}
      <div className="absolute bottom-0 left-0 right-0 p-4 bg-background border-t">
        <div className="flex justify-between items-center text-sm text-muted-foreground">
          <div className="flex items-center gap-4">
            <span>Card Age: {card.cardAgeDays}d</span>
            <span>Next Review: {formatDistanceToNow(new Date(card.nextReview || card.dueDate), { addSuffix: true })}</span>
          </div>
          <div className="flex items-center gap-4">
            <span>Response Time: {reviewUtils.formatResponseTime(responseTime)}</span>
            {hintUsed && (
              <Badge variant="secondary">
                <Lightbulb className="h-3 w-3 mr-1" />
                Hint Used
              </Badge>
            )}
            {markedAsDifficult && (
              <Badge variant="destructive">
                <Flag className="h-3 w-3 mr-1" />
                Difficult
              </Badge>
            )}
          </div>
        </div>
      </div>

      {/* Loading Overlay */}
      {isSubmitting && (
        <div className="absolute inset-0 bg-background/80 flex items-center justify-center backdrop-blur-sm">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-sm text-muted-foreground">Submitting review...</p>
          </div>
        </div>
      )}
    </Card>
  );
};