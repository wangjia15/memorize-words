import {
  ReviewSession,
  StartReviewSessionRequest,
  SubmitReviewRequest,
  DueCardsResponse,
  ReviewStatistics,
  ReviewInsights,
  UserReviewPreferences,
  SpacedRepetitionCard,
  ReviewModeInfo,
  ReviewOutcome,
  ReviewMode,
  ApiResponse,
  ReviewApiService
} from '@/types/review';

// Base API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Default headers
const getHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('auth_token') || ''}`
});

// Helper function for API calls
async function apiCall<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;

  const config: RequestInit = {
    headers: getHeaders(),
    ...options
  };

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    const result: ApiResponse<T> = await response.json();

    if (!result.success) {
      throw new Error(result.message || 'API request failed');
    }

    return result.data;
  } catch (error) {
    console.error(`API call failed: ${endpoint}`, error);
    throw error;
  }
}

// Retry logic with exponential backoff
async function retryWithBackoff<T>(
  fn: () => Promise<T>,
  maxRetries = 3,
  baseDelay = 1000
): Promise<T> {
  let lastError: Error;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error as Error;

      if (attempt === maxRetries) {
        break;
      }

      const delay = baseDelay * Math.pow(2, attempt - 1) + Math.random() * 1000;
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }

  throw lastError!;
}

// Review API Service Implementation
export const reviewApi: ReviewApiService = {
  // Session Management
  async getActiveSession(): Promise<ReviewSession> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewSession>('/spaced-repetition/sessions/active');
    });
  },

  async startReviewSession(request: StartReviewSessionRequest): Promise<ReviewSession> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewSession>('/spaced-repetition/sessions/start', {
        method: 'POST',
        body: JSON.stringify(request)
      });
    });
  },

  async submitReview(request: SubmitReviewRequest): Promise<ReviewSession> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewSession>(`/spaced-repetition/sessions/${request.sessionId}/submit`, {
        method: 'POST',
        body: JSON.stringify(request)
      });
    });
  },

  async completeSession(sessionId: string): Promise<ReviewSession> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewSession>(`/spaced-repetition/sessions/${sessionId}/complete`, {
        method: 'POST'
      });
    });
  },

  // Card Management
  async getDueCards(limit: number = 20): Promise<DueCardsResponse> {
    return retryWithBackoff(async () => {
      return apiCall<DueCardsResponse>(`/spaced-repetition/cards/due?limit=${limit}`);
    });
  },

  async getNewCards(limit: number = 20): Promise<SpacedRepetitionCard[]> {
    return retryWithBackoff(async () => {
      return apiCall<SpacedRepetitionCard[]>(`/spaced-repetition/cards/new?limit=${limit}`);
    });
  },

  async getDifficultCards(limit: number = 20): Promise<SpacedRepetitionCard[]> {
    return retryWithBackoff(async () => {
      return apiCall<SpacedRepetitionCard[]>(`/spaced-repetition/cards/difficult?limit=${limit}`);
    });
  },

  async getRandomCards(limit: number = 20): Promise<SpacedRepetitionCard[]> {
    return retryWithBackoff(async () => {
      return apiCall<SpacedRepetitionCard[]>(`/spaced-repetition/cards/random?limit=${limit}`);
    });
  },

  // Statistics and Analytics
  async getStatistics(from: Date, to: Date): Promise<ReviewStatistics> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<ReviewStatistics>(`/spaced-repetition/statistics?from=${fromStr}&to=${toStr}`);
    });
  },

  async getCurrentMonthStatistics(): Promise<ReviewStatistics> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewStatistics>('/spaced-repetition/statistics/overview');
    });
  },

  async getReviewInsights(): Promise<ReviewInsights> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewInsights>('/spaced-repetition/analytics/insights');
    });
  },

  // Preferences
  async getPreferences(): Promise<UserReviewPreferences> {
    return retryWithBackoff(async () => {
      return apiCall<UserReviewPreferences>('/spaced-repetition/preferences');
    });
  },

  async updatePreferences(preferences: Partial<UserReviewPreferences>): Promise<UserReviewPreferences> {
    return retryWithBackoff(async () => {
      return apiCall<UserReviewPreferences>('/spaced-repetition/preferences', {
        method: 'PUT',
        body: JSON.stringify(preferences)
      });
    });
  },

  // Review Modes
  async getAvailableReviewModes(): Promise<ReviewModeInfo[]> {
    return retryWithBackoff(async () => {
      return apiCall<ReviewModeInfo[]>('/spaced-repetition/modes/available');
    });
  },

  // Card Operations
  async suspendCard(cardId: string): Promise<void> {
    return retryWithBackoff(async () => {
      return apiCall<void>(`/spaced-repetition/cards/${cardId}/suspend`, {
        method: 'POST'
      });
    });
  },

  async unsuspendCard(cardId: string): Promise<void> {
    return retryWithBackoff(async () => {
      return apiCall<void>(`/spaced-repetition/cards/${cardId}/unsuspend`, {
        method: 'POST'
      });
    });
  },

  async resetCard(cardId: string): Promise<void> {
    return retryWithBackoff(async () => {
      return apiCall<void>(`/spaced-repetition/cards/${cardId}/reset`, {
        method: 'POST'
      });
    });
  },

  async deleteCard(cardId: string): Promise<void> {
    return retryWithBackoff(async () => {
      return apiCall<void>(`/spaced-repetition/cards/${cardId}`, {
        method: 'DELETE'
      });
    });
  }
};

// Utility functions
export const reviewUtils = {
  // Calculate session progress
  calculateProgress(session: ReviewSession) {
    const completed = session.completedCards;
    const total = session.totalCards;
    const percentage = total > 0 ? (completed / total) * 100 : 0;

    return {
      completed,
      total,
      percentage,
      remaining: total - completed,
      accuracy: completed > 0 ? (session.correctAnswers / completed) * 100 : 0
    };
  },

  // Format response time for display
  formatResponseTime(ms: number): string {
    if (ms < 1000) {
      return `${ms}ms`;
    } else if (ms < 60000) {
      return `${(ms / 1000).toFixed(1)}s`;
    } else {
      return `${(ms / 60000).toFixed(1)}m`;
    }
  },

  // Get outcome color and styling
  getOutcomeStyle(outcome: ReviewOutcome) {
    switch (outcome) {
      case ReviewOutcome.AGAIN:
        return {
          color: 'red',
          bgColor: 'bg-red-500',
          hoverColor: 'hover:bg-red-600',
          textColor: 'text-red-500',
          label: 'Again',
          description: "Didn't know"
        };
      case ReviewOutcome.HARD:
        return {
          color: 'orange',
          bgColor: 'bg-orange-500',
          hoverColor: 'hover:bg-orange-600',
          textColor: 'text-orange-500',
          label: 'Hard',
          description: 'Barely knew'
        };
      case ReviewOutcome.GOOD:
        return {
          color: 'green',
          bgColor: 'bg-green-500',
          hoverColor: 'hover:bg-green-600',
          textColor: 'text-green-500',
          label: 'Good',
          description: 'Knew it'
        };
      case ReviewOutcome.EASY:
        return {
          color: 'blue',
          bgColor: 'bg-blue-500',
          hoverColor: 'hover:bg-blue-600',
          textColor: 'text-blue-500',
          label: 'Easy',
          description: 'Knew perfectly'
        };
      default:
        return {
          color: 'gray',
          bgColor: 'bg-gray-500',
          hoverColor: 'hover:bg-gray-600',
          textColor: 'text-gray-500',
          label: 'Unknown',
          description: 'Unknown'
        };
    }
  },

  // Format date for display
  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  },

  // Calculate due date status
  getDueStatus(dueDate: Date): {
    isOverdue: boolean;
    isDueToday: boolean;
    daysUntilDue: number;
    statusText: string;
    statusColor: string;
  } {
    const now = new Date();
    const due = new Date(dueDate);
    const diffTime = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    const isOverdue = diffDays < 0;
    const isDueToday = diffDays === 0;

    let statusText: string;
    let statusColor: string;

    if (isOverdue) {
      statusText = `${Math.abs(diffDays)} day${Math.abs(diffDays) > 1 ? 's' : ''} overdue`;
      statusColor = 'text-red-600';
    } else if (isDueToday) {
      statusText = 'Due today';
      statusColor = 'text-yellow-600';
    } else if (diffDays === 1) {
      statusText = 'Due tomorrow';
      statusColor = 'text-blue-600';
    } else {
      statusText = `Due in ${diffDays} days`;
      statusColor = 'text-green-600';
    }

    return {
      isOverdue,
      isDueToday,
      daysUntilDue: diffDays,
      statusText,
      statusColor
    };
  },

  // Calculate study session duration
  calculateDuration(startTime: Date, endTime?: Date): number {
    const end = endTime || new Date();
    return Math.floor((end.getTime() - startTime.getTime()) / 1000);
  },

  // Get card state color
  getCardStateColor(state: string): string {
    switch (state.toUpperCase()) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800';
      case 'LEARNING':
        return 'bg-yellow-100 text-yellow-800';
      case 'REVIEW':
        return 'bg-green-100 text-green-800';
      case 'RELEARNING':
        return 'bg-orange-100 text-orange-800';
      case 'SUSPENDED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  },

  // Validate session request
  validateSessionRequest(request: StartReviewSessionRequest): string[] {
    const errors: string[] = [];

    if (!request.mode || !Object.values(ReviewMode).includes(request.mode)) {
      errors.push('Invalid review mode');
    }

    if (!request.limit || request.limit < 1 || request.limit > 100) {
      errors.push('Limit must be between 1 and 100');
    }

    if (request.includeWordListIds && request.excludeWordListIds) {
      errors.push('Cannot specify both include and exclude word lists');
    }

    return errors;
  },

  // Local storage helpers
  storage: {
    saveSession(session: ReviewSession): void {
      try {
        localStorage.setItem('active_review_session', JSON.stringify(session));
      } catch (error) {
        console.error('Failed to save session to localStorage:', error);
      }
    },

    loadSession(): ReviewSession | null {
      try {
        const stored = localStorage.getItem('active_review_session');
        if (stored) {
          const session = JSON.parse(stored);
          // Convert date strings back to Date objects
          session.startTime = new Date(session.startTime);
          if (session.endTime) {
            session.endTime = new Date(session.endTime);
          }
          return session;
        }
      } catch (error) {
        console.error('Failed to load session from localStorage:', error);
      }
      return null;
    },

    clearSession(): void {
      try {
        localStorage.removeItem('active_review_session');
      } catch (error) {
        console.error('Failed to clear session from localStorage:', error);
      }
    }
  }
};

export default reviewApi;