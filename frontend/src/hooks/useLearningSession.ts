import { useState, useCallback } from 'react';
import {
  LearningSession,
  LearningWord,
  LearningSessionConfig,
  SubmitAnswerRequest
} from '@/types/learning';

// Mock API service - in real app this would be replaced with actual API calls
const mockApi = {
  post: async (url: string, data: any) => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));

    if (url === '/api/learning-sessions') {
      // Create mock session
      const mockWords: LearningWord[] = [
        {
          id: '1',
          word: 'serendipity',
          definition: 'The occurrence and development of events by chance in a happy way',
          pronunciation: 'ser-ən-ˈdi-pə-tē',
          example: 'It was pure serendipity that we met at the coffee shop.',
          difficulty: data.difficulty,
          attempts: 0,
          correctAttempts: 0,
          isCompleted: false,
          timeSpent: 0
        },
        {
          id: '2',
          word: 'ephemeral',
          definition: 'Lasting for a very short time',
          pronunciation: 'ə-ˈfe-mə-rəl',
          example: 'The beauty of the sunset was ephemeral but unforgettable.',
          difficulty: data.difficulty,
          attempts: 0,
          correctAttempts: 0,
          isCompleted: false,
          timeSpent: 0
        },
        {
          id: '3',
          word: 'ubiquitous',
          definition: 'Present, appearing, or found everywhere',
          pronunciation: 'yu̇-ˈbi-kwə-təs',
          example: 'Smartphones have become ubiquitous in modern society.',
          difficulty: data.difficulty,
          attempts: 0,
          correctAttempts: 0,
          isCompleted: false,
          timeSpent: 0
        }
      ];

      const session: LearningSession = {
        id: `session-${Date.now()}`,
        userId: 'user-1',
        vocabularyListId: data.vocabularyListId,
        mode: data.mode,
        difficulty: data.difficulty,
        totalWords: mockWords.length,
        completedWords: 0,
        correctAnswers: 0,
        startTime: new Date(),
        isCompleted: false,
        isPaused: false,
        currentWordIndex: 0,
        words: mockWords,
        settings: data.settings
      };

      return { data: { data: session } };
    }

    if (url.includes('/answers')) {
      // Mock answer submission
      return { data: { data: {} } };
    }

    return { data: { data: {} } };
  },

  patch: async (url: string) => {
    await new Promise(resolve => setTimeout(resolve, 200));
    return { data: { data: {} } };
  }
};

export const useLearningSession = (sessionConfig?: LearningSessionConfig) => {
  const [session, setSession] = useState<LearningSession | null>(null);
  const [currentWord, setCurrentWord] = useState<LearningWord | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const startSession = useCallback(async (config: LearningSessionConfig) => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await mockApi.post('/api/learning-sessions', config);
      const newSession = response.data.data;

      setSession(newSession);
      setCurrentWord(newSession.words[0]);
    } catch (err) {
      setError('Failed to start learning session');
      console.error('Start session error:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const submitAnswer = useCallback(async (isCorrect: boolean, answer?: string) => {
    if (!session || !currentWord) return;

    try {
      const request: SubmitAnswerRequest = {
        wordId: currentWord.id,
        isCorrect,
        answer,
        timeSpent: calculateTimeSpent()
      };

      await mockApi.post(`/api/learning-sessions/${session.id}/answers`, request);

      // Update session state
      const updatedSession = { ...session };
      updatedSession.completedWords += 1;

      if (isCorrect) {
        updatedSession.correctAnswers += 1;
      }

      // Update current word
      const updatedWord = { ...currentWord };
      updatedWord.attempts += 1;
      updatedWord.isCompleted = true;
      if (isCorrect) {
        updatedWord.correctAttempts += 1;
      }

      // Update words array
      updatedSession.words = updatedSession.words.map(word =>
        word.id === currentWord.id ? updatedWord : word
      );

      // Move to next word
      const nextIndex = updatedSession.currentWordIndex + 1;
      updatedSession.currentWordIndex = nextIndex;

      if (nextIndex < updatedSession.words.length) {
        setCurrentWord(updatedSession.words[nextIndex]);
        setSession(updatedSession);
      } else {
        // Session completed
        await completeSession(updatedSession);
      }
    } catch (err) {
      setError('Failed to submit answer');
      console.error('Submit answer error:', err);
    }
  }, [session, currentWord]);

  const pauseSession = useCallback(async () => {
    if (!session) return;

    try {
      await mockApi.patch(`/api/learning-sessions/${session.id}/pause`);
      setSession(prev => prev ? { ...prev, isPaused: true } : null);
    } catch (err) {
      setError('Failed to pause session');
      console.error('Pause session error:', err);
    }
  }, [session]);

  const resumeSession = useCallback(async () => {
    if (!session) return;

    try {
      await mockApi.patch(`/api/learning-sessions/${session.id}/resume`);
      setSession(prev => prev ? { ...prev, isPaused: false } : null);
    } catch (err) {
      setError('Failed to resume session');
      console.error('Resume session error:', err);
    }
  }, [session]);

  const completeSession = useCallback(async (sessionToComplete?: LearningSession) => {
    const targetSession = sessionToComplete || session;
    if (!targetSession) return;

    try {
      const completedSession = {
        ...targetSession,
        isCompleted: true,
        endTime: new Date(),
        duration: calculateSessionDuration(targetSession.startTime, new Date())
      };

      await mockApi.patch(`/api/learning-sessions/${targetSession.id}/complete`);
      setSession(completedSession);
      return completedSession;
    } catch (err) {
      setError('Failed to complete session');
      console.error('Complete session error:', err);
    }
  }, [session]);

  const restartSession = useCallback(async () => {
    if (!session) return;

    try {
      // Reset session to initial state
      const restartedSession = {
        ...session,
        completedWords: 0,
        correctAnswers: 0,
        currentWordIndex: 0,
        isCompleted: false,
        isPaused: false,
        startTime: new Date(),
        endTime: undefined,
        duration: undefined,
        words: session.words.map(word => ({
          ...word,
          attempts: 0,
          correctAttempts: 0,
          isCompleted: false,
          timeSpent: 0,
          lastAttemptAt: undefined
        }))
      };

      setSession(restartedSession);
      setCurrentWord(restartedSession.words[0]);
    } catch (err) {
      setError('Failed to restart session');
      console.error('Restart session error:', err);
    }
  }, [session]);

  const calculateTimeSpent = (): number => {
    // Return time spent on current word in seconds
    return Math.floor(Math.random() * 30) + 5; // Mock implementation
  };

  const calculateSessionDuration = (startTime: Date, endTime: Date): number => {
    return Math.floor((endTime.getTime() - startTime.getTime()) / 1000);
  };

  return {
    session,
    currentWord,
    isLoading,
    error,
    startSession,
    submitAnswer,
    pauseSession,
    resumeSession,
    completeSession,
    restartSession
  };
};