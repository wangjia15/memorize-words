import {
  ProgressStatistics,
  DailyMetric,
  WeeklyMetric,
  MonthlyMetric,
  PerformanceTrend,
  OptimalStudyTime,
  Achievement,
  LearningPrediction,
  DifficultyDistribution,
  UserPreferences,
  Timeframe,
  MetricType,
  ApiResponse,
  AnalyticsApiService,
  ProgressStatisticsService
} from '@/types/analytics';

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

// Progress Statistics Service Implementation
const progressStatisticsService: ProgressStatisticsService = {
  async getProgressStatistics(from: Date, to: Date): Promise<ProgressStatistics> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<ProgressStatistics>(`/progress/statistics?from=${fromStr}&to=${toStr}`);
    });
  },

  async getCurrentStreak(): Promise<number> {
    return retryWithBackoff(async () => {
      return apiCall<number>('/progress/statistics/current-streak');
    });
  },

  async getLongestStreak(): Promise<number> {
    return retryWithBackoff(async () => {
      return apiCall<number>('/progress/statistics/longest-streak');
    });
  },

  async getDailyMetrics(from: Date, to: Date): Promise<DailyMetric[]> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<DailyMetric[]>(`/progress/statistics/daily?from=${fromStr}&to=${toStr}`);
    });
  },

  async getWeeklyMetrics(from: Date, to: Date): Promise<WeeklyMetric[]> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<WeeklyMetric[]>(`/progress/statistics/weekly?from=${fromStr}&to=${toStr}`);
    });
  },

  async getMonthlyMetrics(from: Date, to: Date): Promise<MonthlyMetric[]> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<MonthlyMetric[]>(`/progress/statistics/monthly?from=${fromStr}&to=${toStr}`);
    });
  },

  async getPerformanceTrends(from: Date, to: Date): Promise<PerformanceTrend> {
    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];

    return retryWithBackoff(async () => {
      return apiCall<PerformanceTrend>(`/progress/statistics/trends?from=${fromStr}&to=${toStr}`);
    });
  },

  async getOptimalStudyTimes(): Promise<OptimalStudyTime[]> {
    return retryWithBackoff(async () => {
      return apiCall<OptimalStudyTime[]>('/progress/statistics/optimal-study-times');
    });
  },

  async getAchievements(): Promise<Achievement[]> {
    return retryWithBackoff(async () => {
      return apiCall<Achievement[]>('/progress/statistics/achievements');
    });
  },

  async getPredictions(): Promise<LearningPrediction> {
    return retryWithBackoff(async () => {
      return apiCall<LearningPrediction>('/progress/statistics/predictions');
    });
  },

  async getDifficultyDistribution(): Promise<DifficultyDistribution> {
    return retryWithBackoff(async () => {
      return apiCall<DifficultyDistribution>('/progress/statistics/difficulty-distribution');
    });
  }
};

// Analytics API Service Implementation
export const analyticsApi: AnalyticsApiService = {
  statistics: progressStatisticsService,

  async exportProgressReport(format: 'csv' | 'json' | 'pdf'): Promise<Blob> {
    return retryWithBackoff(async () => {
      const response = await fetch(`${API_BASE_URL}/progress/export/${format}`, {
        headers: getHeaders(),
        method: 'GET'
      });

      if (!response.ok) {
        throw new Error(`Failed to export progress report: ${response.status}`);
      }

      return response.blob();
    });
  },

  async getPersonalizedInsights(): Promise<string[]> {
    return retryWithBackoff(async () => {
      return apiCall<string[]>('/progress/analytics/insights');
    });
  },

  async updateDashboardPreferences(preferences: UserPreferences): Promise<UserPreferences> {
    return retryWithBackoff(async () => {
      return apiCall<UserPreferences>('/progress/preferences', {
        method: 'PUT',
        body: JSON.stringify(preferences)
      });
    });
  }
};

// Cache utilities for better performance
const cache = new Map<string, { data: any; timestamp: number; ttl: number }>();

const CACHE_TTL = {
  PROGRESS_STATS: 5 * 60 * 1000, // 5 minutes
  ACHIEVEMENTS: 30 * 60 * 1000, // 30 minutes
  PREDICTIONS: 60 * 60 * 1000, // 1 hour
  TRENDS: 10 * 60 * 1000, // 10 minutes
  OPTIMAL_TIMES: 24 * 60 * 60 * 1000 // 24 hours
};

function getCacheKey(service: string, method: string, params: any[]): string {
  return `${service}:${method}:${JSON.stringify(params)}`;
}

function getCachedData<T>(key: string, ttl: number): T | null {
  const cached = cache.get(key);
  if (!cached) return null;

  if (Date.now() - cached.timestamp > ttl) {
    cache.delete(key);
    return null;
  }

  return cached.data as T;
}

function setCachedData<T>(key: string, data: T, ttl: number): void {
  cache.set(key, {
    data,
    timestamp: Date.now(),
    ttl
  });
}

// Cached API wrapper
export const cachedAnalyticsApi = {
  ...analyticsApi,
  statistics: {
    ...progressStatisticsService,

    async getProgressStatistics(from: Date, to: Date): Promise<ProgressStatistics> {
      const key = getCacheKey('statistics', 'getProgressStatistics', [from.toISOString(), to.toISOString()]);
      const cached = getCachedData<ProgressStatistics>(key, CACHE_TTL.PROGRESS_STATS);

      if (cached) return cached;

      const data = await progressStatisticsService.getProgressStatistics(from, to);
      setCachedData(key, data, CACHE_TTL.PROGRESS_STATS);
      return data;
    },

    async getAchievements(): Promise<Achievement[]> {
      const key = getCacheKey('statistics', 'getAchievements', []);
      const cached = getCachedData<Achievement[]>(key, CACHE_TTL.ACHIEVEMENTS);

      if (cached) return cached;

      const data = await progressStatisticsService.getAchievements();
      setCachedData(key, data, CACHE_TTL.ACHIEVEMENTS);
      return data;
    },

    async getPredictions(): Promise<LearningPrediction> {
      const key = getCacheKey('statistics', 'getPredictions', []);
      const cached = getCachedData<LearningPrediction>(key, CACHE_TTL.PREDICTIONS);

      if (cached) return cached;

      const data = await progressStatisticsService.getPredictions();
      setCachedData(key, data, CACHE_TTL.PREDICTIONS);
      return data;
    },

    async getPerformanceTrends(from: Date, to: Date): Promise<PerformanceTrend> {
      const key = getCacheKey('statistics', 'getPerformanceTrends', [from.toISOString(), to.toISOString()]);
      const cached = getCachedData<PerformanceTrend>(key, CACHE_TTL.TRENDS);

      if (cached) return cached;

      const data = await progressStatisticsService.getPerformanceTrends(from, to);
      setCachedData(key, data, CACHE_TTL.TRENDS);
      return data;
    },

    async getOptimalStudyTimes(): Promise<OptimalStudyTime[]> {
      const key = getCacheKey('statistics', 'getOptimalStudyTimes', []);
      const cached = getCachedData<OptimalStudyTime[]>(key, CACHE_TTL.OPTIMAL_TIMES);

      if (cached) return cached;

      const data = await progressStatisticsService.getOptimalStudyTimes();
      setCachedData(key, data, CACHE_TTL.OPTIMAL_TIMES);
      return data;
    }
  }
};

// Utility functions for analytics
export const analyticsUtils = {
  // Format date ranges for display
  formatDateRange(from: Date, to: Date): string {
    const formatDate = (date: Date) => {
      return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      }).format(date);
    };

    if (from.toDateString() === to.toDateString()) {
      return formatDate(from);
    }

    return `${formatDate(from)} - ${formatDate(to)}`;
  },

  // Format duration for display
  formatDuration(minutes: number): string {
    if (minutes < 60) {
      return `${minutes}min`;
    } else if (minutes < 1440) {
      const hours = Math.floor(minutes / 60);
      const remainingMinutes = minutes % 60;
      return `${hours}h ${remainingMinutes}min`;
    } else {
      const days = Math.floor(minutes / 1440);
      const remainingHours = Math.floor((minutes % 1440) / 60);
      return `${days}d ${remainingHours}h`;
    }
  },

  // Format accuracy percentage
  formatAccuracy(accuracy: number): string {
    return `${accuracy.toFixed(1)}%`;
  },

  // Get trend direction and icon
  getTrendDirection(trend: number): {
    direction: 'up' | 'down' | 'neutral';
    icon: string;
    color: string;
  } {
    const threshold = 0.01; // 1% threshold for meaningful change

    if (Math.abs(trend) < threshold) {
      return { direction: 'neutral', icon: 'minus', color: 'gray' };
    } else if (trend > 0) {
      return { direction: 'up', icon: 'trending-up', color: 'green' };
    } else {
      return { direction: 'down', icon: 'trending-down', color: 'red' };
    }
  },

  // Calculate percentage change
  calculatePercentageChange(oldValue: number, newValue: number): number {
    if (oldValue === 0) return newValue > 0 ? 100 : 0;
    return ((newValue - oldValue) / oldValue) * 100;
  },

  // Get achievement color based on rarity
  getAchievementColor(rarity: string): string {
    switch (rarity) {
      case 'common': return 'bg-gray-100 text-gray-800';
      case 'rare': return 'bg-blue-100 text-blue-800';
      case 'epic': return 'bg-purple-100 text-purple-800';
      case 'legendary': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  },

  // Get risk severity color
  getRiskSeverityColor(severity: string): string {
    switch (severity) {
      case 'low': return 'text-green-600';
      case 'medium': return 'text-yellow-600';
      case 'high': return 'text-red-600';
      default: return 'text-gray-600';
    }
  },

  // Generate chart colors based on metric
  getChartColors(metric: MetricType): {
    primary: string;
    secondary: string;
    background: string;
  } {
    const colorMap = {
      [MetricType.ACCURACY]: {
        primary: 'rgb(34, 197, 94)',
        secondary: 'rgb(22, 163, 74)',
        background: 'rgba(34, 197, 94, 0.1)'
      },
      [MetricType.REVIEWS]: {
        primary: 'rgb(59, 130, 246)',
        secondary: 'rgb(37, 99, 235)',
        background: 'rgba(59, 130, 246, 0.1)'
      },
      [MetricType.STUDY_TIME]: {
        primary: 'rgb(168, 85, 247)',
        secondary: 'rgb(147, 51, 234)',
        background: 'rgba(168, 85, 247, 0.1)'
      },
      [MetricType.WORDS_ADDED]: {
        primary: 'rgb(251, 146, 60)',
        secondary: 'rgb(249, 115, 22)',
        background: 'rgba(251, 146, 60, 0.1)'
      },
      [MetricType.WORDS_LEARNED]: {
        primary: 'rgb(14, 165, 233)',
        secondary: 'rgb(2, 132, 199)',
        background: 'rgba(14, 165, 233, 0.1)'
      },
      [MetricType.WORDS_MASTERED]: {
        primary: 'rgb(245, 158, 11)',
        secondary: 'rgb(217, 119, 6)',
        background: 'rgba(245, 158, 11, 0.1)'
      },
      [MetricType.STREAK]: {
        primary: 'rgb(239, 68, 68)',
        secondary: 'rgb(220, 38, 38)',
        background: 'rgba(239, 68, 68, 0.1)'
      },
      [MetricType.RETENTION]: {
        primary: 'rgb(236, 72, 153)',
        secondary: 'rgb(219, 39, 119)',
        background: 'rgba(236, 72, 153, 0.1)'
      }
    };

    return colorMap[metric] || colorMap[MetricType.ACCURACY];
  },

  // Clear cache
  clearCache(): void {
    cache.clear();
  },

  // Get cache statistics
  getCacheStats(): {
    size: number;
    entries: Array<{ key: string; timestamp: number; ttl: number }>;
  } {
    const entries = Array.from(cache.entries()).map(([key, value]) => ({
      key,
      timestamp: value.timestamp,
      ttl: value.ttl
    }));

    return {
      size: cache.size,
      entries
    };
  }
};

export default analyticsApi;