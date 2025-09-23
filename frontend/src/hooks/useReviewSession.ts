import { useState, useEffect, useCallback, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reviewApi, reviewUtils } from '@/services/reviewApi';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/useToast';
import {
  ReviewSession,
  ReviewMode,
  ReviewOutcome,
  WordType,
  UseReviewSessionOptions,
  ReviewSessionEvent,
  StartReviewSessionRequest
} from '@/types/review';

export const useReviewSession = (options?: UseReviewSessionOptions) => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const toast = useToast();
  const responseTimeRef = useRef<number>(0);
  const eventHandlersRef = useRef<Set<ReviewSessionEventHandler>>(new Set());

  // State
  const [session, setSession] = useState<ReviewSession | null>(null);
  const [currentCardIndex, setCurrentCardIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);

  // Current card derived from session and index
  const currentCard = session?.cards[currentCardIndex]?.card;

  // Get active session query
  const { data: activeSession, isLoading: isLoadingActiveSession } = useQuery({
    queryKey: ['review-session', 'active', user?.id],
    queryFn: () => reviewApi.getActiveSession(),
    enabled: !!user && !isInitialized,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: (failureCount, error) => {
      // Don't retry if user is not authenticated
      if (error.message.includes('401') || error.message.includes('403')) {
        return false;
      }
      return failureCount < 3;
    }
  });

  // Get review statistics
  const { data: statistics } = useQuery({
    queryKey: ['review-statistics', user?.id],
    queryFn: () => reviewApi.getCurrentMonthStatistics(),
    enabled: !!user,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });

  // Get user preferences
  const { data: preferences } = useQuery({
    queryKey: ['review-preferences', user?.id],
    queryFn: () => reviewApi.getPreferences(),
    enabled: !!user,
    staleTime: 30 * 60 * 1000, // 30 minutes
  });

  // Start session mutation
  const startSessionMutation = useMutation({
    mutationFn: (request: StartReviewSessionRequest) => reviewApi.startReviewSession(request),
    onSuccess: (data) => {
      setSession(data);
      setCurrentCardIndex(0);
      setShowAnswer(false);
      setIsPaused(false);
      responseTimeRef.current = Date.now();

      // Save to localStorage for persistence
      reviewUtils.storage.saveSession(data);

      queryClient.setQueryData(['review-session', 'active', user?.id], data);
      queryClient.invalidateQueries(['review-statistics', user?.id]);

      emitEvent({
        type: 'start',
        sessionId: data.id,
        timestamp: new Date(),
        data: { mode: data.mode, totalCards: data.totalCards }
      });

      toast.success('Review session started! Good luck! 🎯');
      options?.onSuccess?.(data);
    },
    onError: (error: Error) => {
      toast.error('Failed to start review session. Please try again.');
      options?.onError?.(error);
      emitEvent({
        type: 'error',
        sessionId: '',
        timestamp: new Date(),
        data: { error: error.message, action: 'start' }
      });
    }
  });

  // Submit review mutation
  const submitReviewMutation = useMutation({
    mutationFn: (params: {
      sessionId: string;
      cardId: string;
      outcome: ReviewOutcome;
      responseTime: number;
    }) => reviewApi.submitReview({
      sessionId: params.sessionId,
      cardId: params.cardId,
      outcome: params.outcome,
      responseTime: params.responseTime
    }),
    onSuccess: (updatedSession) => {
      setSession(updatedSession);

      // Move to next card or complete session
      if (currentCardIndex < updatedSession.cards.length - 1) {
        setCurrentCardIndex(prev => prev + 1);
        setShowAnswer(false);
        responseTimeRef.current = Date.now();
      } else {
        // Auto-complete session
        completeSessionMutation.mutate(updatedSession.id);
      }

      queryClient.setQueryData(['review-session', 'active', user?.id], updatedSession);
      queryClient.invalidateQueries(['review-statistics', user?.id]);

      emitEvent({
        type: 'submit',
        sessionId: updatedSession.id,
        timestamp: new Date(),
        data: {
          cardIndex: currentCardIndex,
          outcome: updatedSession.cards[currentCardIndex]?.reviewOutcome,
          accuracy: updatedSession.sessionAccuracy
        }
      });
    },
    onError: (error: Error) => {
      toast.error('Failed to submit review. Please try again.');
      setIsSubmitting(false);
      options?.onError?.(error);
      emitEvent({
        type: 'error',
        sessionId: session?.id || '',
        timestamp: new Date(),
        data: { error: error.message, action: 'submit' }
      });
    }
  });

  // Complete session mutation
  const completeSessionMutation = useMutation({
    mutationFn: (sessionId: string) => reviewApi.completeSession(sessionId),
    onSuccess: (completedSession) => {
      const finalSession = { ...completedSession, isCompleted: true };
      setSession(null);
      setCurrentCardIndex(0);
      setShowAnswer(false);
      setIsPaused(false);

      // Clear localStorage
      reviewUtils.storage.clearSession();

      queryClient.setQueryData(['review-session', 'active', user?.id], null);
      queryClient.invalidateQueries(['review-statistics', user?.id]);

      emitEvent({
        type: 'complete',
        sessionId: finalSession.id,
        timestamp: new Date(),
        data: {
          totalCards: finalSession.totalCards,
          correctAnswers: finalSession.correctAnswers,
          accuracy: finalSession.sessionAccuracy,
          duration: finalSession.sessionDuration
        }
      });

      // Show completion toast with stats
      const accuracy = finalSession.sessionAccuracy || 0;
      const message = `Session completed! ${accuracy.toFixed(1)}% accuracy 🎉`;
      toast.success(message);

      options?.onSessionComplete?.(finalSession);
    },
    onError: (error: Error) => {
      toast.error('Failed to complete session. Please try again.');
      options?.onError?.(error);
      emitEvent({
        type: 'error',
        sessionId: session?.id || '',
        timestamp: new Date(),
        data: { error: error.message, action: 'complete' }
      });
    }
  });

  // Initialize with active session if exists
  useEffect(() => {
    if (activeSession && !session && !isInitialized) {
      setSession(activeSession);
      setCurrentCardIndex(activeSession.currentCardIndex || 0);
      setIsPaused(false);
      responseTimeRef.current = Date.now();
      setIsInitialized(true);
    }
  }, [activeSession, session, isInitialized]);

  // Load session from localStorage if no active session from API
  useEffect(() => {
    if (!activeSession && !session && !isInitialized && user) {
      const savedSession = reviewUtils.storage.loadSession();
      if (savedSession) {
        setSession(savedSession);
        setCurrentCardIndex(savedSession.currentCardIndex || 0);
        setIsPaused(false);
        responseTimeRef.current = Date.now();
        setIsInitialized(true);
      }
    }
  }, [activeSession, session, isInitialized, user]);

  // Event emitter
  const emitEvent = useCallback((event: ReviewSessionEvent) => {
    eventHandlersRef.current.forEach(handler => {
      try {
        handler(event);
      } catch (error) {
        console.error('Error in review session event handler:', error);
      }
    });
  }, []);

  // Session actions
  const startReview = useCallback(async (
    mode: ReviewMode,
    limit: number = 20,
    options?: {
      includeWordListIds?: string[];
      excludeWordListIds?: string[];
      includeWordTypes?: WordType[];
      difficultyRange?: [number, number];
      shuffle?: boolean;
      prioritizeNewCards?: boolean;
    }
  ) => {
    const request: StartReviewSessionRequest = {
      mode,
      limit,
      ...options
    };

    // Validate request
    const validationErrors = reviewUtils.validateSessionRequest(request);
    if (validationErrors.length > 0) {
      toast.error('Invalid session configuration: ' + validationErrors.join(', '));
      return;
    }

    startSessionMutation.mutate(request);
  }, [startSessionMutation, toast]);

  const submitReview = useCallback(async (outcome: ReviewOutcome) => {
    if (!session || !currentCard || isSubmitting) return;

    setIsSubmitting(true);

    const responseTime = Date.now() - responseTimeRef.current;

    submitReviewMutation.mutate({
      sessionId: session.id,
      cardId: currentCard.id,
      outcome,
      responseTime
    });
  }, [session, currentCard, isSubmitting, submitReviewMutation]);

  const completeSession = useCallback(async () => {
    if (!session) return;

    completeSessionMutation.mutate(session.id);
  }, [session, completeSessionMutation]);

  const toggleAnswer = useCallback(() => {
    setShowAnswer(prev => !prev);
  }, []);

  const skipCard = useCallback(() => {
    if (!session || !currentCard || isSubmitting) return;

    setIsSubmitting(true);

    setTimeout(() => {
      if (currentCardIndex < session.cards.length - 1) {
        setCurrentCardIndex(prev => prev + 1);
        setShowAnswer(false);
        responseTimeRef.current = Date.now();
      } else {
        completeSession();
      }
      setIsSubmitting(false);
    }, 300);
  }, [session, currentCard, currentCardIndex, isSubmitting, completeSession]);

  const pauseSession = useCallback(() => {
    setIsPaused(true);
    toast.info('Session paused. Click resume to continue.');

    if (session) {
      emitEvent({
        type: 'pause',
        sessionId: session.id,
        timestamp: new Date()
      });
    }
  }, [session, toast, emitEvent]);

  const resumeSession = useCallback(() => {
    setIsPaused(false);
    responseTimeRef.current = Date.now();
    toast.info('Session resumed.');

    if (session) {
      emitEvent({
        type: 'resume',
        sessionId: session.id,
        timestamp: new Date()
      });
    }
  }, [session, toast, emitEvent]);

  const restartSession = useCallback(() => {
    if (!session) return;

    // Reset session to initial state
    const restartedSession = {
      ...session,
      completedCards: 0,
      correctAnswers: 0,
      currentCardIndex: 0,
      startTime: new Date(),
      endTime: undefined,
      sessionDuration: undefined,
      sessionAccuracy: undefined,
      cards: session.cards.map(sessionCard => ({
        ...sessionCard,
        reviewOutcome: undefined,
        reviewedAt: undefined,
        responseTime: 0
      }))
    };

    setSession(restartedSession);
    setCurrentCardIndex(0);
    setShowAnswer(false);
    setIsPaused(false);
    responseTimeRef.current = Date.now();

    reviewUtils.storage.saveSession(restartedSession);
    toast.info('Session restarted.');
  }, [session]);

  const endSession = useCallback(() => {
    if (!session) return;

    setSession(null);
    setCurrentCardIndex(0);
    setShowAnswer(false);
    setIsPaused(false);
    reviewUtils.storage.clearSession();

    emitEvent({
      type: 'complete',
      sessionId: session.id,
      timestamp: new Date(),
      data: { manualEnd: true }
    });

    queryClient.setQueryData(['review-session', 'active', user?.id], null);
    toast.info('Session ended.');
  }, [session, queryClient, user?.id, emitEvent]);

  const addEventListener = useCallback((handler: ReviewSessionEventHandler) => {
    eventHandlersRef.current.add(handler);
    return () => {
      eventHandlersRef.current.delete(handler);
    };
  }, []);

  // Calculate progress
  const progress = session ? {
    current: currentCardIndex + 1,
    total: session.totalCards,
    percentage: ((currentCardIndex + 1) / session.totalCards) * 100,
    completed: session.completedCards,
    correct: session.correctAnswers,
    accuracy: session.completedCards > 0
      ? (session.correctAnswers / session.completedCards) * 100
      : 0,
    remaining: session.remainingCards || (session.totalCards - session.completedCards),
    cardsPerMinute: session.cardsPerMinute || 0,
    efficiencyScore: session.efficiencyScore || 0,
    sessionScore: session.totalSessionScore || 0
  } : null;

  // Current response time
  const currentResponseTime = responseTimeRef.current ? Date.now() - responseTimeRef.current : 0;

  return {
    // State
    session,
    currentCard,
    currentCardIndex,
    showAnswer,
    isSubmitting,
    isPaused,
    progress,
    currentResponseTime,
    statistics,
    preferences,

    // Actions
    startReview,
    submitReview,
    completeSession,
    toggleAnswer,
    skipCard,
    pauseSession,
    resumeSession,
    restartSession,
    endSession,
    addEventListener,

    // Loading states
    isStarting: startSessionMutation.isPending,
    isLoadingActiveSession,
    isCompleting: completeSessionMutation.isPending,

    // Error states
    error: startSessionMutation.error || submitReviewMutation.error || completeSessionMutation.error,

    // Helpers
    isSessionActive: !!session && !session.isCompleted,
    isCurrentCardAnswered: showAnswer,
    formattedResponseTime: reviewUtils.formatResponseTime(currentResponseTime),
    sessionMode: session?.mode,
    sessionStartTime: session?.startTime
  };
};

export default useReviewSession;