export enum LearningMode {
  FLASHCARDS = 'flashcards',
  MULTIPLE_CHOICE = 'multiple_choice',
  TYPING = 'typing',
  PRONUNCIATION = 'pronunciation',
  MIXED = 'mixed'
}

export enum DifficultyLevel {
  BEGINNER = 'beginner',
  INTERMEDIATE = 'intermediate',
  ADVANCED = 'advanced',
  MIXED = 'mixed'
}

export interface LearningSession {
  id: string;
  userId: string;
  vocabularyListId?: string;
  mode: LearningMode;
  difficulty: DifficultyLevel;
  totalWords: number;
  completedWords: number;
  correctAnswers: number;
  startTime: Date;
  endTime?: Date;
  duration?: number;
  isCompleted: boolean;
  isPaused: boolean;
  currentWordIndex: number;
  words: LearningWord[];
  settings: SessionSettings;
}

export interface LearningWord {
  id: string;
  word: string;
  definition: string;
  pronunciation?: string;
  example?: string;
  difficulty: DifficultyLevel;
  attempts: number;
  correctAttempts: number;
  isCompleted: boolean;
  timeSpent: number;
  lastAttemptAt?: Date;
}

export interface SessionSettings {
  autoAdvance: boolean;
  showDefinitionFirst: boolean;
  enablePronunciation: boolean;
  enableHints: boolean;
  timeLimit?: number;
  shuffleWords: boolean;
  repeatIncorrect: boolean;
}

export interface LearningSessionConfig {
  mode: LearningMode;
  difficulty: DifficultyLevel;
  vocabularyListId?: string;
  wordCount?: number;
  settings: SessionSettings;
}

export interface SubmitAnswerRequest {
  wordId: string;
  isCorrect: boolean;
  answer?: string;
  timeSpent: number;
}

export interface CreateSessionRequest {
  mode: LearningMode;
  difficulty: DifficultyLevel;
  vocabularyListId?: string;
  wordCount?: number;
  settings: SessionSettings;
}

export interface LearningAnswer {
  id: string;
  sessionId: string;
  wordId: string;
  isCorrect: boolean;
  userAnswer?: string;
  timeSpent: number;
  answeredAt: Date;
}

export enum SessionStatus {
  ACTIVE = 'active',
  PAUSED = 'paused',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled'
}