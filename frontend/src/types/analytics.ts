import { DifficultyLevel } from './learning';

// Base analytics types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: Date;
}

// Progress Statistics Types
export interface ProgressStatistics {
  userId: string;
  periodStart: Date;
  periodEnd: Date;
  totalWordsAdded: number;
  totalReviews: number;
  correctReviews: number;
  averageAccuracy: number;
  totalStudyTime: number; // in minutes
  averageSessionLength: number; // in minutes
  streakDays: number;
  longestStreak: number;
  wordsLearned: number;
  wordsMastered: number;
  learningVelocity: LearningVelocity;
  retentionRate: number;
  difficultyDistribution: DifficultyDistribution;
  dailyMetrics: DailyMetric[];
  weeklyMetrics: WeeklyMetric[];
  monthlyMetrics: MonthlyMetric[];
  performanceTrends: PerformanceTrend;
  optimalStudyTimes: OptimalStudyTime[];
  achievements: Achievement[];
  predictions: LearningPrediction;
}

export interface DailyMetric {
  date: Date;
  wordsAdded: number;
  reviewsCompleted: number;
  accuracy: number;
  studyTime: number; // in minutes
  hasActivity: boolean;
  streakDay: number;
}

export interface WeeklyMetric {
  weekStart: Date;
  weekEnd: Date;
  totalWords: number;
  totalReviews: number;
  averageAccuracy: number;
  totalStudyTime: number; // in minutes
  activeDays: number;
}

export interface MonthlyMetric {
  month: Date;
  totalWords: number;
  totalReviews: number;
  averageAccuracy: number;
  totalStudyTime: number; // in minutes
  activeDays: number;
  wordsLearned: number;
  wordsMastered: number;
}

export interface LearningVelocity {
  newWordsPerDay: number;
  learnedWordsPerDay: number;
  masteredWordsPerDay: number;
  reviewVelocity: number; // reviews per day
}

export interface DifficultyDistribution {
  [key in DifficultyLevel]: number;
}

export interface PerformanceTrend {
  accuracyTrend: number; // slope
  velocityTrend: number; // slope
  studyTimeTrend: number; // slope
  trendDirection: TrendDirection;
  confidenceScore: number; // 0-1
}

export enum TrendDirection {
  IMPROVING = 'improving',
  STABLE = 'stable',
  DECLINING = 'declining'
}

export interface OptimalStudyTime {
  dayOfWeek: number; // 0-6 (Sunday-Saturday)
  hour: number; // 0-23
  averageAccuracy: number;
  completionRate: number;
  sessionCount: number;
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: AchievementCategory;
  rarity: AchievementRarity;
  unlockedAt: Date;
  progress?: number; // for progressive achievements
  maxProgress?: number;
}

export enum AchievementCategory {
  STREAK = 'streak',
  ACCURACY = 'accuracy',
  VOLUME = 'volume',
  SPEED = 'speed',
  CONSISTENCY = 'consistency',
  MASTERY = 'mastery'
}

export enum AchievementRarity {
  COMMON = 'common',
  RARE = 'rare',
  EPIC = 'epic',
  LEGENDARY = 'legendary'
}

export interface LearningPrediction {
  predictedWordsInMonth: number;
  predictedAccuracy: number;
  estimatedMasteryTime: number; // in days
  recommendedDailyGoal: number;
  confidenceLevel: number; // 0-1
  riskFactors: RiskFactor[];
}

export interface RiskFactor {
  type: RiskType;
  description: string;
  severity: RiskSeverity;
  recommendation: string;
}

export enum RiskType {
  STREAK_BREAK = 'streak_break',
  ACCURACY_DECLINE = 'accuracy_decline',
  BURNOUT = 'burnout',
  INCONSISTENT_SCHEDULE = 'inconsistent_schedule',
  HIGH_DIFFICULTY_LOAD = 'high_difficulty_load'
}

export enum RiskSeverity {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high'
}

// Chart Data Types
export interface ChartData {
  labels: string[];
  datasets: ChartDataset[];
}

export interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor?: string | string[];
  borderColor?: string | string[];
  borderWidth?: number;
  fill?: boolean;
  tension?: number;
  pointRadius?: number;
  pointHoverRadius?: number;
  borderDash?: number[];
}

export interface HeatmapData {
  dayOfWeek: number;
  hour: number;
  intensity: number;
  label: string;
}

// Dashboard Types
export interface DashboardWidget {
  id: string;
  title: string;
  type: 'chart' | 'metric' | 'heatmap' | 'list';
  size: 'small' | 'medium' | 'large';
  position: { x: number; y: number; width: number; height: number };
  data: any;
}

export interface UserPreferences {
  dashboardLayout: DashboardWidget[];
  chartTheme: 'light' | 'dark' | 'auto';
  defaultTimeframe: Timeframe;
  defaultMetrics: string[];
  notifications: {
    achievements: boolean;
    streakReminders: boolean;
    weeklyReports: boolean;
  };
}

export enum Timeframe {
  DAILY = 'daily',
  WEEKLY = 'weekly',
  MONTHLY = 'monthly',
  YEARLY = 'yearly'
}

export enum MetricType {
  ACCURACY = 'accuracy',
  REVIEWS = 'reviews',
  STUDY_TIME = 'studyTime',
  WORDS_ADDED = 'wordsAdded',
  WORDS_LEARNED = 'wordsLearned',
  WORDS_MASTERED = 'wordsMastered',
  STREAK = 'streak',
  RETENTION = 'retention'
}

// API Service Types
export interface ProgressStatisticsService {
  getProgressStatistics(from: Date, to: Date): Promise<ProgressStatistics>;
  getCurrentStreak(): Promise<number>;
  getLongestStreak(): Promise<number>;
  getDailyMetrics(from: Date, to: Date): Promise<DailyMetric[]>;
  getWeeklyMetrics(from: Date, to: Date): Promise<WeeklyMetric[]>;
  getMonthlyMetrics(from: Date, to: Date): Promise<MonthlyMetric[]>;
  getPerformanceTrends(from: Date, to: Date): Promise<PerformanceTrend>;
  getOptimalStudyTimes(): Promise<OptimalStudyTime[]>;
  getAchievements(): Promise<Achievement[]>;
  getPredictions(): Promise<LearningPrediction>;
  getDifficultyDistribution(): Promise<DifficultyDistribution>;
}

export interface AnalyticsApiService {
  statistics: ProgressStatisticsService;
  exportProgressReport(format: 'csv' | 'json' | 'pdf'): Promise<Blob>;
  getPersonalizedInsights(): Promise<string[]>;
  updateDashboardPreferences(preferences: UserPreferences): Promise<UserPreferences>;
}

// UI Component Types
export interface ChartProps {
  data: any;
  timeframe: Timeframe;
  metric: MetricType;
  className?: string;
}

export interface StatCardProps {
  title: string;
  value: number | string;
  unit?: string;
  trend?: 'up' | 'down' | 'neutral';
  trendValue?: number;
  icon?: React.ComponentType<{ className?: string }>;
  className?: string;
}

export interface AchievementCardProps {
  achievement: Achievement;
  progress?: number;
  className?: string;
}

// Utility Types
export interface DateRange {
  from: Date;
  to: Date;
}

export interface FilterOptions {
  timeframe: Timeframe;
  metric: MetricType;
  difficulty?: DifficultyLevel;
  category?: AchievementCategory;
  dateRange?: DateRange;
}

// Export all types for easy importing
export type {
  ApiResponse,
  ProgressStatistics,
  DailyMetric,
  WeeklyMetric,
  MonthlyMetric,
  LearningVelocity,
  DifficultyDistribution,
  PerformanceTrend,
  OptimalStudyTime,
  Achievement,
  LearningPrediction,
  RiskFactor,
  ChartData,
  ChartDataset,
  HeatmapData,
  DashboardWidget,
  UserPreferences,
  ChartProps,
  StatCardProps,
  AchievementCardProps,
  DateRange,
  FilterOptions
};