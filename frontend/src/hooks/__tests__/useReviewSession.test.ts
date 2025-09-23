import { renderHook, act, waitFor } from '@testing-library/react';
import { useReviewSession } from '../useReviewSession';
import { ReviewMode, ReviewOutcome } from '@/types/review';

// Mock dependencies
jest.mock('@/services/reviewApi', () => ({
  reviewApi: {
    getActiveSession: jest.fn(),
    startReviewSession: jest.fn(),
    submitReview: jest.fn(),
    completeSession: jest.fn(),
    getDueCards: jest.fn(),
    getCurrentMonthStatistics: jest.fn(),
    getPreferences: jest.fn(),
    getAvailableReviewModes: jest.fn(),
  },
  reviewUtils: {
    validateSessionRequest: () => [],
    storage: {
      saveSession: jest.fn(),
      loadSession: () => null,
      clearSession: jest.fn(),
    },
    calculateProgress: (session: any) => ({
      completed: session.completedCards,
      total: session.totalCards,
      percentage: (session.completedCards / session.totalCards) * 100,
      remaining: session.totalCards - session.completedCards,
      accuracy: session.completedCards > 0 ? (session.correctAnswers / session.completedCards) * 100 : 0,
    }),
    formatResponseTime: (ms: number) => `${ms}ms`,
  }
}));

jest.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({
    user: { id: 'user-1', email: 'test@example.com', name: 'Test User' },
    isLoading: false,
    isAuthenticated: true,
  })
}));

jest.mock('@/hooks/useToast', () => ({
  useToast: () => ({
    success: jest.fn(),
    error: jest.fn(),
    info: jest.fn(),
    warning: jest.fn(),
  })
}));

// Mock React Query
jest.mock('@tanstack/react-query', () => ({
  useQuery: jest.fn(),
  useMutation: jest.fn(),
  useQueryClient: () => ({
    setQueryData: jest.fn(),
    invalidateQueries: jest.fn(),
  }),
}));

const mockReviewApi = require('@/services/reviewApi').reviewApi;
const mockReactQuery = require('@tanstack/react-query');

describe('useReviewSession', () => {
  const mockToast = require('@/hooks/useToast').useToast();

  beforeEach(() => {
    jest.clearAllMocks();

    // Default mock implementations
    mockReactQuery.useQuery.mockImplementation(({ queryKey, enabled }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: null,
          isLoading: false,
          error: null,
        };
      }
      if (queryKey[0] === 'review-statistics') {
        return {
          data: {
            totalReviews: 10,
            correctReviews: 8,
            averageAccuracy: 80,
            streakDays: 5,
            totalCards: 50,
          },
          isLoading: false,
          error: null,
        };
      }
      if (queryKey[0] === 'review-preferences') {
        return {
          data: {
            dailyReviewLimit: 50,
            sessionGoal: 20,
            enablePronunciation: true,
            enableHints: true,
          },
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    mockReactQuery.useMutation.mockImplementation(() => ({
      mutate: jest.fn(),
      mutateAsync: jest.fn(),
      isPending: false,
      error: null,
    }));
  });

  it('initializes with default state', () => {
    const { result } = renderHook(() => useReviewSession());

    expect(result.current.session).toBeNull();
    expect(result.current.currentCard).toBeNull();
    expect(result.current.currentCardIndex).toBe(0);
    expect(result.current.showAnswer).toBe(false);
    expect(result.current.isSubmitting).toBe(false);
    expect(result.current.isPaused).toBe(false);
    expect(result.current.progress).toBeNull();
  });

  it('starts a review session successfully', async () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 10,
      completedCards: 0,
      correctAnswers: 0,
      averageResponseTime: 0,
      isCompleted: false,
      cards: [
        {
          id: 'card-1',
          sessionId: 'session-1',
          card: {
            id: 'word-1',
            userId: 'user-1',
            word: {
              id: 'word-1',
              text: 'test',
              translation: 'test definition',
              type: 'NOUN',
              difficultyLevel: 0.5,
            },
            intervalDays: 1,
            easeFactor: 2.5,
            dueDate: new Date(),
            totalReviews: 0,
            correctReviews: 0,
            consecutiveCorrect: 0,
            consecutiveIncorrect: 0,
            difficultyLevel: 0.5,
            performanceIndex: 0.8,
            averageResponseTime: 3000,
            isActive: true,
            isSuspended: false,
            stabilityFactor: 1.0,
            totalStudyTime: 30000,
            reviewCountAgain: 0,
            reviewCountHard: 0,
            reviewCountGood: 0,
            reviewCountEasy: 0,
            cardAgeDays: 1,
            retentionRate: 0.8,
            isDue: true,
            isNew: true,
            isDifficult: false,
            difficultyRating: 0.3,
            reviewHistory: [],
          },
          responseTime: 0,
        },
      ],
      currentCardIndex: 0,
    };

    const mockMutation = {
      mutate: jest.fn((data, options) => {
        options.onSuccess(mockSession);
      }),
      isPending: false,
      error: null,
    };

    mockReactQuery.useMutation.mockReturnValue(mockMutation);

    const { result } = renderHook(() => useReviewSession());

    await act(async () => {
      result.current.startReview(ReviewMode.DUE_CARDS, 10);
    });

    expect(mockMutation.mutate).toHaveBeenCalledWith({
      mode: ReviewMode.DUE_CARDS,
      limit: 10,
    });
    expect(result.current.session).toEqual(mockSession);
    expect(result.current.currentCard).toEqual(mockSession.cards[0].card);
    expect(result.current.currentCardIndex).toBe(0);
    expect(mockToast.success).toHaveBeenCalledWith('Review session started! Good luck! 🎯');
  });

  it('submits a review successfully', async () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 2,
      completedCards: 0,
      correctAnswers: 0,
      averageResponseTime: 0,
      isCompleted: false,
      cards: [
        {
          id: 'card-1',
          sessionId: 'session-1',
          card: {
            id: 'word-1',
            userId: 'user-1',
            word: {
              id: 'word-1',
              text: 'test',
              translation: 'test definition',
              type: 'NOUN',
              difficultyLevel: 0.5,
            },
            intervalDays: 1,
            easeFactor: 2.5,
            dueDate: new Date(),
            totalReviews: 0,
            correctReviews: 0,
            consecutiveCorrect: 0,
            consecutiveIncorrect: 0,
            difficultyLevel: 0.5,
            performanceIndex: 0.8,
            averageResponseTime: 3000,
            isActive: true,
            isSuspended: false,
            stabilityFactor: 1.0,
            totalStudyTime: 30000,
            reviewCountAgain: 0,
            reviewCountHard: 0,
            reviewCountGood: 0,
            reviewCountEasy: 0,
            cardAgeDays: 1,
            retentionRate: 0.8,
            isDue: true,
            isNew: true,
            isDifficult: false,
            difficultyRating: 0.3,
            reviewHistory: [],
          },
          responseTime: 0,
        },
        {
          id: 'card-2',
          sessionId: 'session-1',
          card: {
            id: 'word-2',
            userId: 'user-1',
            word: {
              id: 'word-2',
              text: 'test2',
              translation: 'test definition 2',
              type: 'NOUN',
              difficultyLevel: 0.5,
            },
            intervalDays: 1,
            easeFactor: 2.5,
            dueDate: new Date(),
            totalReviews: 0,
            correctReviews: 0,
            consecutiveCorrect: 0,
            consecutiveIncorrect: 0,
            difficultyLevel: 0.5,
            performanceIndex: 0.8,
            averageResponseTime: 3000,
            isActive: true,
            isSuspended: false,
            stabilityFactor: 1.0,
            totalStudyTime: 30000,
            reviewCountAgain: 0,
            reviewCountHard: 0,
            reviewCountGood: 0,
            reviewCountEasy: 0,
            cardAgeDays: 1,
            retentionRate: 0.8,
            isDue: true,
            isNew: true,
            isDifficult: false,
            difficultyRating: 0.3,
            reviewHistory: [],
          },
          responseTime: 0,
        },
      ],
      currentCardIndex: 0,
    };

    const updatedSession = {
      ...mockSession,
      completedCards: 1,
      correctAnswers: 1,
      currentCardIndex: 1,
    };

    // Set up initial state
    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const submitMutation = {
      mutate: jest.fn((data, options) => {
        options.onSuccess(updatedSession);
      }),
      isPending: false,
      error: null,
    };

    mockReactQuery.useMutation.mockReturnValue(submitMutation);

    const { result } = renderHook(() => useReviewSession());

    // Wait for session to load
    await waitFor(() => {
      expect(result.current.session).toEqual(mockSession);
    });

    await act(async () => {
      result.current.submitReview(ReviewOutcome.GOOD);
    });

    expect(submitMutation.mutate).toHaveBeenCalledWith({
      sessionId: 'session-1',
      cardId: 'word-1',
      outcome: ReviewOutcome.GOOD,
      responseTime: expect.any(Number),
    });
    expect(result.current.isSubmitting).toBe(false);
  });

  it('handles session completion when last card is reviewed', async () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 1,
      completedCards: 0,
      correctAnswers: 0,
      averageResponseTime: 0,
      isCompleted: false,
      cards: [
        {
          id: 'card-1',
          sessionId: 'session-1',
          card: {
            id: 'word-1',
            userId: 'user-1',
            word: {
              id: 'word-1',
              text: 'test',
              translation: 'test definition',
              type: 'NOUN',
              difficultyLevel: 0.5,
            },
            intervalDays: 1,
            easeFactor: 2.5,
            dueDate: new Date(),
            totalReviews: 0,
            correctReviews: 0,
            consecutiveCorrect: 0,
            consecutiveIncorrect: 0,
            difficultyLevel: 0.5,
            performanceIndex: 0.8,
            averageResponseTime: 3000,
            isActive: true,
            isSuspended: false,
            stabilityFactor: 1.0,
            totalStudyTime: 30000,
            reviewCountAgain: 0,
            reviewCountHard: 0,
            reviewCountGood: 0,
            reviewCountEasy: 0,
            cardAgeDays: 1,
            retentionRate: 0.8,
            isDue: true,
            isNew: true,
            isDifficult: false,
            difficultyRating: 0.3,
            reviewHistory: [],
          },
          responseTime: 0,
        },
      ],
      currentCardIndex: 0,
    };

    const completedSession = {
      ...mockSession,
      isCompleted: true,
      completedCards: 1,
      correctAnswers: 1,
    };

    // Set up initial state
    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const submitMutation = {
      mutate: jest.fn((data, options) => {
        options.onSuccess(completedSession);
      }),
      isPending: false,
      error: null,
    };

    const completeMutation = {
      mutate: jest.fn((data, options) => {
        options.onSuccess(completedSession);
      }),
      isPending: false,
      error: null,
    };

    mockReactQuery.useMutation.mockImplementation((fn) => {
      if (fn.name.includes('submitReview')) {
        return submitMutation;
      }
      if (fn.name.includes('completeSession')) {
        return completeMutation;
      }
      return { mutate: jest.fn(), isPending: false, error: null };
    });

    const { result } = renderHook(() => useReviewSession());

    // Wait for session to load
    await waitFor(() => {
      expect(result.current.session).toEqual(mockSession);
    });

    await act(async () => {
      result.current.submitReview(ReviewOutcome.GOOD);
    });

    expect(completeMutation.mutate).toHaveBeenCalledWith('session-1');
  });

  it('toggles answer display', () => {
    const { result } = renderHook(() => useReviewSession());

    expect(result.current.showAnswer).toBe(false);

    act(() => {
      result.current.toggleAnswer();
    });

    expect(result.current.showAnswer).toBe(true);

    act(() => {
      result.current.toggleAnswer();
    });

    expect(result.current.showAnswer).toBe(false);
  });

  it('calculates progress correctly', () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 10,
      completedCards: 3,
      correctAnswers: 2,
      averageResponseTime: 2500,
      isCompleted: false,
      cards: [],
      currentCardIndex: 5,
    };

    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const { result } = renderHook(() => useReviewSession());

    const progress = result.current.progress;

    expect(progress).not.toBeNull();
    expect(progress!.current).toBe(6);
    expect(progress!.total).toBe(10);
    expect(progress!.percentage).toBe(60);
    expect(progress!.completed).toBe(3);
    expect(progress!.correct).toBe(2);
    expect(progress!.accuracy).toBeCloseTo(66.67);
    expect(progress!.remaining).toBe(7);
  });

  it('handles session pause and resume', () => {
    const { result } = renderHook(() => useReviewSession());

    act(() => {
      result.current.pauseSession();
    });

    expect(result.current.isPaused).toBe(true);
    expect(mockToast.info).toHaveBeenCalledWith('Session paused. Click resume to continue.');

    act(() => {
      result.current.resumeSession();
    });

    expect(result.current.isPaused).toBe(false);
    expect(mockToast.info).toHaveBeenCalledWith('Session resumed.');
  });

  it('handles session restart', () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 10,
      completedCards: 3,
      correctAnswers: 2,
      averageResponseTime: 2500,
      isCompleted: false,
      cards: [],
      currentCardIndex: 5,
    };

    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const { result } = renderHook(() => useReviewSession());

    act(() => {
      result.current.restartSession();
    });

    expect(result.current.session!.completedCards).toBe(0);
    expect(result.current.session!.correctAnswers).toBe(0);
    expect(result.current.session!.currentCardIndex).toBe(0);
    expect(result.current.currentCardIndex).toBe(0);
    expect(result.current.showAnswer).toBe(false);
    expect(result.current.isPaused).toBe(false);
  });

  it('handles session end', () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 10,
      completedCards: 3,
      correctAnswers: 2,
      averageResponseTime: 2500,
      isCompleted: false,
      cards: [],
      currentCardIndex: 5,
    };

    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const { result } = renderHook(() => useReviewSession());

    act(() => {
      result.current.endSession();
    });

    expect(result.current.session).toBeNull();
    expect(result.current.currentCardIndex).toBe(0);
    expect(result.current.showAnswer).toBe(false);
    expect(result.current.isPaused).toBe(false);
    expect(mockToast.info).toHaveBeenCalledWith('Session ended.');
  });

  it('provides formatted response time', () => {
    const { result } = renderHook(() => useReviewSession());

    expect(result.current.formattedResponseTime).toBe('0ms');
  });

  it('provides session metadata', () => {
    const mockSession = {
      id: 'session-1',
      userId: 'user-1',
      mode: ReviewMode.DUE_CARDS,
      startTime: new Date(),
      totalCards: 10,
      completedCards: 3,
      correctAnswers: 2,
      averageResponseTime: 2500,
      isCompleted: false,
      cards: [],
      currentCardIndex: 5,
    };

    mockReactQuery.useQuery.mockImplementation(({ queryKey }) => {
      if (queryKey[0] === 'review-session' && queryKey[1] === 'active') {
        return {
          data: mockSession,
          isLoading: false,
          error: null,
        };
      }
      return { data: null, isLoading: false, error: null };
    });

    const { result } = renderHook(() => useReviewSession());

    expect(result.current.sessionMode).toBe(ReviewMode.DUE_CARDS);
    expect(result.current.sessionStartTime).toBeInstanceOf(Date);
    expect(result.current.isSessionActive).toBe(true);
    expect(result.current.isCurrentCardAnswered).toBe(false);
  });
});