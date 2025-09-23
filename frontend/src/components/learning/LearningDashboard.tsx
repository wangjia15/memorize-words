import React, { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  LearningSession,
  LearningMode,
  DifficultyLevel,
  SessionSettings as SessionSettingsType,
  LearningSessionConfig
} from "@/types/learning";
import { useLearningSession } from "@/hooks/useLearningSession";
import { WordCard } from "./WordCard";
import { LearningProgress } from "./LearningProgress";
import { SessionSettings } from "./SessionSettings";
import { SessionStatistics } from "./SessionStatistics";
import { AchievementSystem } from "./AchievementSystem";
import { cn } from "@/lib/utils";
import {
  Play,
  Pause,
  RotateCcw,
  Settings,
  BarChart3,
  Trophy,
  Home,
  Clock,
  Target
} from "lucide-react";

interface LearningDashboardProps {
  initialMode?: LearningMode;
  initialDifficulty?: DifficultyLevel;
  vocabularyListId?: string;
  className?: string;
  onSessionComplete?: (session: LearningSession) => void;
  onExit?: () => void;
}

type DashboardView = 'settings' | 'learning' | 'statistics' | 'achievements';

export const LearningDashboard: React.FC<LearningDashboardProps> = ({
  initialMode = LearningMode.FLASHCARDS,
  initialDifficulty = DifficultyLevel.INTERMEDIATE,
  vocabularyListId,
  className,
  onSessionComplete,
  onExit
}) => {
  // Dashboard state
  const [currentView, setCurrentView] = useState<DashboardView>('settings');
  const [isCardFlipped, setIsCardFlipped] = useState(false);
  const [showKeyboardHelp, setShowKeyboardHelp] = useState(false);

  // Session configuration
  const [selectedMode, setSelectedMode] = useState<LearningMode>(initialMode);
  const [selectedDifficulty, setSelectedDifficulty] = useState<DifficultyLevel>(initialDifficulty);
  const [wordCount, setWordCount] = useState(10);
  const [sessionSettings, setSessionSettings] = useState<SessionSettingsType>({
    autoAdvance: false,
    showDefinitionFirst: true,
    enablePronunciation: true,
    enableHints: true,
    timeLimit: undefined,
    shuffleWords: true,
    repeatIncorrect: true
  });

  // Learning session hook
  const {
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
  } = useLearningSession();

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      // Ignore if user is typing in an input field
      if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
        return;
      }

      switch (event.key) {
        case ' ':
          event.preventDefault();
          if (currentView === 'learning' && session && !session.isCompleted) {
            if (selectedMode === LearningMode.FLASHCARDS) {
              handleFlipCard();
            }
          }
          break;

        case 'ArrowLeft':
          event.preventDefault();
          if (currentView === 'learning' && session) {
            handlePreviousWord();
          }
          break;

        case 'ArrowRight':
        case 'Enter':
          event.preventDefault();
          if (currentView === 'learning' && session) {
            handleNextWord();
          }
          break;

        case 'p':
        case 'P':
          event.preventDefault();
          if (currentView === 'learning' && session && !session.isCompleted) {
            handlePauseResume();
          }
          break;

        case 'r':
        case 'R':
          event.preventDefault();
          if (currentView === 'learning' && session) {
            handleRestartSession();
          }
          break;

        case 's':
        case 'S':
          event.preventDefault();
          setCurrentView('statistics');
          break;

        case 'a':
        case 'A':
          event.preventDefault();
          setCurrentView('achievements');
          break;

        case 'h':
        case 'H':
          event.preventDefault();
          setShowKeyboardHelp(!showKeyboardHelp);
          break;

        case 'Escape':
          event.preventDefault();
          if (currentView === 'learning') {
            setCurrentView('settings');
          } else if (onExit) {
            onExit();
          }
          break;

        case 'y':
        case 'Y':
          event.preventDefault();
          if (currentView === 'learning' && selectedMode === LearningMode.FLASHCARDS && isCardFlipped) {
            handleAnswer(true);
          }
          break;

        case 'n':
        case 'N':
          event.preventDefault();
          if (currentView === 'learning' && selectedMode === LearningMode.FLASHCARDS && isCardFlipped) {
            handleAnswer(false);
          }
          break;

        case 'Tab':
          event.preventDefault();
          // Cycle through views
          const views: DashboardView[] = ['settings', 'learning', 'statistics', 'achievements'];
          const currentIndex = views.indexOf(currentView);
          const nextIndex = (currentIndex + 1) % views.length;
          if (session || views[nextIndex] === 'settings') {
            setCurrentView(views[nextIndex]);
          }
          break;

        case '1':
        case '2':
        case '3':
        case '4':
          event.preventDefault();
          // Quick answer shortcuts for multiple choice mode
          if (currentView === 'learning' && selectedMode === LearningMode.MULTIPLE_CHOICE) {
            const optionIndex = parseInt(event.key) - 1;
            // Dispatch custom event for MultipleChoiceMode to handle
            const customEvent = new CustomEvent('quickAnswer', { detail: { optionIndex } });
            window.dispatchEvent(customEvent);
          }
          break;

        default:
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentView, session, selectedMode, showKeyboardHelp]);

  // Session management
  const handleStartSession = useCallback(async () => {
    const config: LearningSessionConfig = {
      mode: selectedMode,
      difficulty: selectedDifficulty,
      vocabularyListId,
      wordCount,
      settings: sessionSettings
    };

    await startSession(config);
    setCurrentView('learning');
    setIsCardFlipped(false);
  }, [selectedMode, selectedDifficulty, vocabularyListId, wordCount, sessionSettings, startSession]);

  const handlePauseResume = useCallback(async () => {
    if (!session) return;

    if (session.isPaused) {
      await resumeSession();
    } else {
      await pauseSession();
    }
  }, [session, pauseSession, resumeSession]);

  const handleRestartSession = useCallback(async () => {
    await restartSession();
    setIsCardFlipped(false);
  }, [restartSession]);

  const handleCompleteSession = useCallback(async () => {
    if (!session) return;

    const completedSession = await completeSession();
    if (completedSession && onSessionComplete) {
      onSessionComplete(completedSession);
    }
    setCurrentView('statistics');
  }, [session, completeSession, onSessionComplete]);

  // Word navigation
  const handleFlipCard = useCallback(() => {
    setIsCardFlipped(!isCardFlipped);
  }, [isCardFlipped]);

  const handleAnswer = useCallback(async (isCorrect: boolean, answer?: string) => {
    await submitAnswer(isCorrect, answer);

    if (sessionSettings.autoAdvance && isCorrect) {
      // Auto-advance after a short delay
      setTimeout(() => {
        setIsCardFlipped(false);
      }, 1500);
    } else {
      setIsCardFlipped(false);
    }
  }, [submitAnswer, sessionSettings.autoAdvance]);

  const handleNextWord = useCallback(() => {
    if (!session || !currentWord) return;

    // This is handled automatically by the hook when an answer is submitted
    // For manual navigation, we would need additional logic
    setIsCardFlipped(false);
  }, [session, currentWord]);

  const handlePreviousWord = useCallback(() => {
    if (!session || session.currentWordIndex <= 0) return;

    // In a full implementation, this would require backend support
    // For now, we'll just flip the card back
    setIsCardFlipped(false);
  }, [session]);

  // Render keyboard help overlay
  const renderKeyboardHelp = () => {
    if (!showKeyboardHelp) return null;

    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
        onClick={() => setShowKeyboardHelp(false)}
      >
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          className="bg-background border rounded-lg p-6 max-w-md mx-4"
          onClick={(e) => e.stopPropagation()}
        >
          <h3 className="text-lg font-semibold mb-4">Keyboard Shortcuts</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span>Space</span>
              <span>Flip card (flashcards)</span>
            </div>
            <div className="flex justify-between">
              <span>Y / N</span>
              <span>Know it / Don't know (flashcards)</span>
            </div>
            <div className="flex justify-between">
              <span>1-4</span>
              <span>Quick select (multiple choice)</span>
            </div>
            <div className="flex justify-between">
              <span>←/→ Arrow keys</span>
              <span>Navigate words</span>
            </div>
            <div className="flex justify-between">
              <span>Enter</span>
              <span>Next word</span>
            </div>
            <div className="flex justify-between">
              <span>Tab</span>
              <span>Cycle through views</span>
            </div>
            <div className="flex justify-between">
              <span>P</span>
              <span>Pause/Resume</span>
            </div>
            <div className="flex justify-between">
              <span>R</span>
              <span>Restart session</span>
            </div>
            <div className="flex justify-between">
              <span>S</span>
              <span>Show statistics</span>
            </div>
            <div className="flex justify-between">
              <span>A</span>
              <span>Show achievements</span>
            </div>
            <div className="flex justify-between">
              <span>H</span>
              <span>Toggle this help</span>
            </div>
            <div className="flex justify-between">
              <span>Escape</span>
              <span>Back/Exit</span>
            </div>
          </div>
          <Button
            onClick={() => setShowKeyboardHelp(false)}
            className="w-full mt-4"
          >
            Close
          </Button>
        </motion.div>
      </motion.div>
    );
  };

  // Render navigation bar
  const renderNavigationBar = () => {
    return (
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-background border-b p-4"
      >
        <div className="flex items-center justify-between max-w-6xl mx-auto">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onExit ? onExit() : setCurrentView('settings')}
              className="flex items-center gap-2"
            >
              <Home className="h-4 w-4" />
              Home
            </Button>

            <div className="flex items-center gap-2">
              <Button
                variant={currentView === 'settings' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setCurrentView('settings')}
                className="flex items-center gap-2"
              >
                <Settings className="h-4 w-4" />
                Settings
              </Button>

              {session && (
                <>
                  <Button
                    variant={currentView === 'learning' ? 'default' : 'ghost'}
                    size="sm"
                    onClick={() => setCurrentView('learning')}
                    className="flex items-center gap-2"
                  >
                    <Target className="h-4 w-4" />
                    Learning
                  </Button>

                  <Button
                    variant={currentView === 'statistics' ? 'default' : 'ghost'}
                    size="sm"
                    onClick={() => setCurrentView('statistics')}
                    className="flex items-center gap-2"
                  >
                    <BarChart3 className="h-4 w-4" />
                    Statistics
                  </Button>

                  <Button
                    variant={currentView === 'achievements' ? 'default' : 'ghost'}
                    size="sm"
                    onClick={() => setCurrentView('achievements')}
                    className="flex items-center gap-2"
                  >
                    <Trophy className="h-4 w-4" />
                    Achievements
                  </Button>
                </>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setShowKeyboardHelp(true)}
              className="text-muted-foreground"
            >
              Press H for shortcuts
            </Button>

            {session && !session.isCompleted && (
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {session.isPaused ? 'Paused' : 'Active'}
                </Badge>
              </div>
            )}
          </div>
        </div>
      </motion.div>
    );
  };

  // Render main content based on current view
  const renderMainContent = () => {
    switch (currentView) {
      case 'settings':
        return (
          <SessionSettings
            settings={sessionSettings}
            onSettingsChange={setSessionSettings}
            selectedMode={selectedMode}
            onModeChange={setSelectedMode}
            selectedDifficulty={selectedDifficulty}
            onDifficultyChange={setSelectedDifficulty}
            wordCount={wordCount}
            onWordCountChange={setWordCount}
            onStartSession={handleStartSession}
            isLoading={isLoading}
            className="p-6"
          />
        );

      case 'learning':
        if (!session || !currentWord) {
          return (
            <div className="flex-1 flex items-center justify-center">
              <Card className="max-w-md">
                <CardContent className="p-6 text-center">
                  <p className="text-muted-foreground mb-4">
                    {isLoading ? 'Loading session...' : 'No active session'}
                  </p>
                  <Button onClick={() => setCurrentView('settings')}>
                    Start New Session
                  </Button>
                </CardContent>
              </Card>
            </div>
          );
        }

        return (
          <div className="flex-1 p-6">
            <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-4 gap-6 h-full">
              {/* Learning Progress Sidebar */}
              <div className="lg:col-span-1">
                <LearningProgress
                  session={session}
                  currentWordIndex={session.currentWordIndex}
                  onPauseResume={handlePauseResume}
                  onRestart={handleRestartSession}
                />
              </div>

              {/* Main Learning Area */}
              <div className="lg:col-span-3 flex flex-col justify-center">
                <WordCard
                  word={currentWord}
                  mode={selectedMode}
                  isFlipped={isCardFlipped}
                  onFlip={handleFlipCard}
                  onAnswer={handleAnswer}
                  onNext={handleNextWord}
                  onPrevious={handlePreviousWord}
                  enableAudio={sessionSettings.enablePronunciation}
                  showHints={sessionSettings.enableHints}
                />

                {/* Session Controls */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.3 }}
                  className="flex justify-center gap-4 mt-6"
                >
                  <Button
                    variant="outline"
                    onClick={handlePauseResume}
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
                    onClick={handleRestartSession}
                    className="flex items-center gap-2"
                  >
                    <RotateCcw className="h-4 w-4" />
                    Restart
                  </Button>

                  {session.isCompleted && (
                    <Button
                      onClick={handleCompleteSession}
                      className="flex items-center gap-2"
                    >
                      <Trophy className="h-4 w-4" />
                      View Results
                    </Button>
                  )}
                </motion.div>
              </div>
            </div>
          </div>
        );

      case 'statistics':
        return (
          <div className="flex-1 p-6">
            <SessionStatistics
              session={session}
              onBackToLearning={() => setCurrentView('learning')}
              onStartNewSession={() => setCurrentView('settings')}
            />
          </div>
        );

      case 'achievements':
        return (
          <div className="flex-1 p-6">
            <AchievementSystem
              session={session}
              onBackToLearning={() => setCurrentView('learning')}
            />
          </div>
        );

      default:
        return null;
    }
  };

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle className="text-red-600">Error</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground mb-4">{error}</p>
            <Button onClick={() => window.location.reload()}>
              Reload Page
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className={cn("min-h-screen bg-background flex flex-col", className)}>
      {renderNavigationBar()}

      <AnimatePresence mode="wait">
        <motion.div
          key={currentView}
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{ duration: 0.3 }}
          className="flex-1 flex flex-col"
        >
          {renderMainContent()}
        </motion.div>
      </AnimatePresence>

      <AnimatePresence>
        {renderKeyboardHelp()}
      </AnimatePresence>
    </div>
  );
};