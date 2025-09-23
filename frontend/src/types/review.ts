// Review System TypeScript Types

export interface ReviewSession {
  id: string;
  userId: string;
  mode: ReviewMode;
  startTime: Date;
  endTime?: Date;
  totalCards: number;
  completedCards: number;
  correctAnswers: number;
  averageResponseTime: number;
  isCompleted: boolean;
  sessionAccuracy?: number;
  sessionDuration?: number;
  cardsPerMinute?: number;
  totalSessionScore?: number;
  efficiencyScore?: number;
  newCardsLearned?: number;
  difficultCardsMastered?: number;
  learningVelocity?: number;
  cards: ReviewSessionCard[];
  currentCardIndex: number;
  remainingCards?: number;
  progressPercentage?: number;
}

export interface ReviewSessionCard {
  id: string;
  sessionId: string;
  card: SpacedRepetitionCard;
  responseTime: number;
  reviewOutcome?: ReviewOutcome;
  reviewedAt?: Date;
  intervalBeforeReview?: number;
  intervalAfterReview?: number;
  easeFactorBeforeReview?: number;
  easeFactorAfterReview?: number;
  reviewNumber?: number;
  consecutiveCorrectBefore?: number;
  wasCorrect?: boolean;
  score?: number;
  userAnswer?: string;
  hintUsed?: boolean;
  confidenceLevel?: number;
  markedAsDifficult?: boolean;
  notes?: string;
  isNewCard?: boolean;
  wasDifficult?: boolean;
  isCompleted?: boolean;
}

export interface SpacedRepetitionCard {
  id: string;
  userId: string;
  word: Word;
  intervalDays: number;
  easeFactor: number;
  dueDate: Date;
  nextReview?: Date;
  lastReviewed?: Date;
  totalReviews: number;
  correctReviews: number;
  consecutiveCorrect: number;
  consecutiveIncorrect: number;
  difficultyLevel: number;
  performanceIndex: number;
  averageResponseTime: number;
  isActive: boolean;
  isSuspended: boolean;
  stabilityFactor: number;
  totalStudyTime: number;
  lastReviewOutcome?: ReviewOutcome;
  reviewCountAgain: number;
  reviewCountHard: number;
  reviewCountGood: number;
  reviewCountEasy: number;
  cardAgeDays: number;
  retentionRate: number;
  isDue: boolean;
  isNew: boolean;
  isDifficult: boolean;
  difficultyRating: number;
  reviewHistory: ReviewHistory[];
}

export interface Word {
  id: string;
  text: string;
  translation: string;
  pronunciation?: string;
  type: WordType;
  category?: string;
  difficultyLevel: number;
  exampleSentence?: string;
  exampleTranslation?: string;
}

export interface ReviewHistory {
  id: string;
  reviewOutcome: ReviewOutcome;
  responseTime: number;
  reviewedAt: Date;
  intervalBefore: number;
  intervalAfter: number;
  easeFactorBefore: number;
  easeFactorAfter: number;
  confidenceLevel?: number;
  wasCorrect: boolean;
  score: number;
}

export interface UserReviewPreferences {
  id: string;
  userId: string;
  dailyReviewLimit: number;
  dailyNewCardLimit: number;
  sessionGoal: number;
  preferredReviewTime?: string;
  enableNotifications: boolean;
  enableEmailReminders: boolean;
  useAdvancedAlgorithm: boolean;
  easeFactorBonus: number;
  intervalModifier: number;
  minimumIntervalDays: number;
  maximumIntervalDays: number;
  defaultReviewMode: ReviewMode;
  autoAdvanceCards: boolean;
  showAnswerDelay: number;
  enableHints: boolean;
  enablePronunciation: boolean;
  enableProgressAnimation: boolean;
  themePreference?: string;
  languagePreference?: string;
  timezone?: string;
  weekendReviewMode: WeekendReviewMode;
  vacationMode: boolean;
  vacationStartDate?: Date;
  vacationEndDate?: Date;
  preferredModes: ReviewMode[];
  includedCardTypes: WordType[];
  excludedCardTypes: WordType[];
  notificationTimes: string[];
  reviewGoals: ReviewGoal[];
  adaptiveDifficulty: boolean;
  focusModeDuration: number;
  breakDuration: number;
  enableStreakProtection: boolean;
  streakProtectionTime?: string;
  enableLearningInsights: boolean;
  weeklyGoal: number;
  monthlyGoal: number;
  enableAchievements: boolean;
  showDetailedStatistics: boolean;
  exportFormatPreference: string;
  backupFrequency: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface ReviewGoal {
  period: GoalPeriod;
  target: number;
}

export interface ReviewStatistics {
  userId: string;
  periodStart: Date;
  periodEnd: Date;
  totalReviews: number;
  correctReviews: number;
  averageAccuracy: number;
  totalStudyTime: number;
  streakDays: number;
  longestStreak: number;
  learningVelocity: LearningVelocity;
  retentionRate: number;
  dailyMetrics: DailyMetric[];
  weeklyMetrics: WeeklyMetric[];
  achievements: Achievement[];
  dueCardsCount?: number;
  newCardsCount?: number;
  difficultCardsCount?: number;
  totalCards?: number;
  todayReviews?: number;
  weeklyReviews?: number;
  recentActivity?: RecentActivity[];
  performanceInsight?: PerformanceInsight;
}

export interface LearningVelocity {
  cardsPerHour: number;
  cardsPerDay: number;
  cardsPerWeek: number;
  cardsPerMonth: number;
  improvementRate: number;
  trend: 'increasing' | 'stable' | 'decreasing';
}

export interface DailyMetric {
  date: string;
  cardsReviewed: number;
  accuracy: number;
  studyTime: number;
  newCards: number;
  difficultCards: number;
  streak: number;
}

export interface WeeklyMetric {
  week: string;
  cardsReviewed: number;
  accuracy: number;
  studyTime: number;
  newCards: number;
  retentionRate: number;
  streak: number;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  category: string;
  earnedAt?: Date;
  progress: number;
  maxProgress: number;
  isUnlocked: boolean;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
}

export interface RecentActivity {
  date: string;
  cardsReviewed: number;
  accuracy: number;
  studyTime: number;
  sessionMode: ReviewMode;
  streak: number;
}

export interface ReviewInsights {
  learningVelocity: number;
  retentionTrend: 'improving' | 'stable' | 'declining';
  optimalStudyTimes: string[];
  difficultyDistribution: DifficultyDistribution;
  recommendations: string[];
  performanceTrends: PerformanceTrend;
}

export interface DifficultyDistribution {
  easy: number;
  medium: number;
  hard: number;
  veryHard: number;
}

export interface PerformanceTrend {
  accuracy: number[];
  responseTime: number[];
  retentionRate: number[];
  timestamps: string[];
}

export interface PerformanceInsight {
  overallAccuracy: number;
  averageResponseTime: number;
  retentionRate: number;
  learningVelocity: number;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  riskFactors: string[];
  growthOpportunities: string[];
}

export interface ReviewModeInfo {
  mode: ReviewMode;
  name: string;
  description: string;
  available: boolean;
  cardCount: number;
  isRecommended: boolean;
  estimatedTime: number;
  difficulty: 'easy' | 'medium' | 'hard';
}

export interface DueCardsResponse {
  dueCards: SpacedRepetitionCard[];
  totalDue: number;
  totalNew: number;
  totalDifficult: number;
  totalActive: number;
  recommendedLimit: number;
  dailyLimit: number;
  exceedsDailyLimit: boolean;
  newCardsToday: number;
  reviewsToday: number;
  availableReviewModes: string[];
}

export interface StartReviewSessionRequest {
  mode: ReviewMode;
  limit: number;
  includeWordListIds?: string[];
  excludeWordListIds?: string[];
  includeWordTypes?: WordType[];
  excludeWordTypes?: WordType[];
  difficultyRange?: [number, number];
  shuffle?: boolean;
  prioritizeNewCards?: boolean;
}

export interface SubmitReviewRequest {
  sessionId: string;
  cardId: string;
  outcome: ReviewOutcome;
  responseTime: number;
  userAnswer?: string;
  confidenceLevel?: number;
  hintUsed?: boolean;
  markedAsDifficult?: boolean;
  notes?: string;
}

// Enums
export enum ReviewMode {
  DUE_CARDS = 'DUE_CARDS',
  DIFFICULT_CARDS = 'DIFFICULT_CARDS',
  RANDOM_REVIEW = 'RANDOM_REVIEW',
  NEW_CARDS = 'NEW_CARDS',
  TARGETED_REVIEW = 'TARGETED_REVIEW',
  ALL_CARDS = 'ALL_CARDS'
}

export enum ReviewOutcome {
  AGAIN = 'AGAIN',
  HARD = 'HARD',
  GOOD = 'GOOD',
  EASY = 'EASY'
}

export enum WordType {
  NOUN = 'NOUN',
  VERB = 'VERB',
  ADJECTIVE = 'ADJECTIVE',
  ADVERB = 'ADVERB',
  PHRASE = 'PHRASE',
  OTHER = 'OTHER'
}

export enum CardState {
  NEW = 'NEW',
  LEARNING = 'LEARNING',
  REVIEW = 'REVIEW',
  RELEARNING = 'RELEARNING',
  SUSPENDED = 'SUSPENDED',
  BURIED = 'BURIED'
}

export enum WeekendReviewMode {
  NORMAL = 'NORMAL',
  REDUCED = 'REDUCED',
  PAUSED = 'PAUSED'
}

export enum GoalPeriod {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY'
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
  timestamp: Date;
}

// Hook and Component Types
export interface UseReviewSessionOptions {
  onError?: (error: Error) => void;
  onSuccess?: (session: ReviewSession) => void;
  onSessionComplete?: (session: ReviewSession) => void;
  autoSave?: boolean;
  enableAnimations?: boolean;
}

export interface ReviewCardProps {
  card: SpacedRepetitionCard;
  onSubmit: (outcome: ReviewOutcome) => void;
  showAnswer: boolean;
  onToggleAnswer: () => void;
  isSubmitting?: boolean;
  className?: string;
  showHint?: boolean;
  enablePronunciation?: boolean;
}

export interface ReviewProgressProps {
  session: ReviewSession;
  currentIndex: number;
  showDetailed?: boolean;
  compact?: boolean;
}

export interface TimerProps {
  isRunning?: boolean;
  startTime?: Date;
  onTimeUpdate?: (time: number) => void;
  format?: 'seconds' | 'minutes' | 'hours';
  className?: string;
}

export interface AchievementNotificationProps {
  achievement: Achievement;
  onClose: () => void;
  autoClose?: boolean;
}

// Service Types
export interface ReviewApiService {
  getActiveSession(): Promise<ReviewSession>;
  startReviewSession(request: StartReviewSessionRequest): Promise<ReviewSession>;
  submitReview(request: SubmitReviewRequest): Promise<ReviewSession>;
  completeSession(sessionId: string): Promise<ReviewSession>;
  getDueCards(limit: number): Promise<DueCardsResponse>;
  getNewCards(limit: number): Promise<SpacedRepetitionCard[]>;
  getDifficultCards(limit: number): Promise<SpacedRepetitionCard[]>;
  getRandomCards(limit: number): Promise<SpacedRepetitionCard[]>;
  getStatistics(from: Date, to: Date): Promise<ReviewStatistics>;
  getCurrentMonthStatistics(): Promise<ReviewStatistics>;
  getReviewInsights(): Promise<ReviewInsights>;
  getPreferences(): Promise<UserReviewPreferences>;
  updatePreferences(preferences: Partial<UserReviewPreferences>): Promise<UserReviewPreferences>;
  getAvailableReviewModes(): Promise<ReviewModeInfo[]>;
  suspendCard(cardId: string): Promise<void>;
  unsuspendCard(cardId: string): Promise<void>;
  resetCard(cardId: string): Promise<void>;
  deleteCard(cardId: string): Promise<void>;
}

// Error Types
export interface ReviewError extends Error {
  code: string;
  details?: any;
  retryable: boolean;
}

export class ReviewSessionError extends Error {
  constructor(message: string, public code: string, public details?: any) {
    super(message);
    this.name = 'ReviewSessionError';
  }
}

export class ApiError extends Error {
  constructor(message: string, public statusCode: number, public details?: any) {
    super(message);
    this.name = 'ApiError';
  }
}

// Event Types
export interface ReviewSessionEvent {
  type: 'start' | 'submit' | 'complete' | 'pause' | 'resume' | 'error';
  sessionId: string;
  timestamp: Date;
  data?: any;
}

export type ReviewSessionEventHandler = (event: ReviewSessionEvent) => void;