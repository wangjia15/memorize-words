import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Volume2, CheckCircle, XCircle } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { LearningWord, LearningMode } from "@/types/learning";
import { FlashcardMode } from "./FlashcardMode";
import { cn } from "@/lib/utils";

interface WordCardProps {
  word: LearningWord;
  mode: LearningMode;
  isFlipped: boolean;
  onFlip: () => void;
  onAnswer: (isCorrect: boolean, answer?: string) => void;
  onNext: () => void;
  onPrevious: () => void;
  enableAudio: boolean;
  showHints: boolean;
}

export const WordCard: React.FC<WordCardProps> = ({
  word,
  mode,
  isFlipped,
  onFlip,
  onAnswer,
  onNext,
  onPrevious,
  enableAudio,
  showHints
}) => {
  const [userAnswer, setUserAnswer] = useState('');
  const [showResult, setShowResult] = useState(false);
  const [isCorrect, setIsCorrect] = useState(false);

  const playPronunciation = async () => {
    if (word.pronunciation && enableAudio) {
      try {
        // Use Web Speech API for pronunciation
        const utterance = new SpeechSynthesisUtterance(word.word);
        utterance.rate = 0.8;
        utterance.pitch = 1;
        speechSynthesis.speak(utterance);
      } catch (error) {
        console.error('Pronunciation playback failed:', error);
      }
    }
  };

  const handleSubmitAnswer = (answer: string) => {
    const correct = answer.toLowerCase().trim() === word.word.toLowerCase().trim();
    setIsCorrect(correct);
    setShowResult(true);
    onAnswer(correct, answer);
  };

  const renderMultipleChoiceMode = () => {
    const [selectedOption, setSelectedOption] = useState<string | null>(null);
    // Generate mock options for demo - in real app this would come from backend
    const options = [word.word, 'option1', 'option2', 'option3'];

    const handleOptionSelect = (option: string) => {
      setSelectedOption(option);
      handleSubmitAnswer(option);
    };

    return (
      <div className="h-full flex flex-col justify-center">
        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold mb-4">What does this mean?</h2>
          <p className="text-xl bg-muted p-4 rounded-lg">{word.definition}</p>
        </div>

        <div className="grid grid-cols-2 gap-4">
          {options.map((option, index) => (
            <Button
              key={index}
              variant={selectedOption === option ? "default" : "outline"}
              className={cn(
                "h-16 text-lg",
                showResult && option === word.word && "bg-green-100 border-green-500",
                showResult && selectedOption === option && option !== word.word && "bg-red-100 border-red-500"
              )}
              onClick={() => handleOptionSelect(option)}
              disabled={showResult}
            >
              {option}
            </Button>
          ))}
        </div>

        {showResult && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-6 text-center"
          >
            <p className={cn(
              "text-lg font-semibold",
              isCorrect ? "text-green-600" : "text-red-600"
            )}>
              {isCorrect ? "Correct!" : `Incorrect. The answer is: ${word.word}`}
            </p>
          </motion.div>
        )}
      </div>
    );
  };

  const renderTypingMode = () => {
    return (
      <div className="h-full flex flex-col justify-center">
        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold mb-4">Type the word</h2>
          <p className="text-xl bg-muted p-4 rounded-lg">{word.definition}</p>
          {word.example && (
            <p className="text-sm text-muted-foreground mt-4 italic">
              Example: "{word.example}"
            </p>
          )}
        </div>

        <div className="space-y-4">
          <input
            type="text"
            value={userAnswer}
            onChange={(e) => setUserAnswer(e.target.value)}
            className="w-full p-4 text-xl border rounded-lg text-center"
            placeholder="Type your answer..."
            disabled={showResult}
            onKeyPress={(e) => {
              if (e.key === 'Enter' && userAnswer.trim()) {
                handleSubmitAnswer(userAnswer);
              }
            }}
          />

          {!showResult && (
            <Button
              onClick={() => handleSubmitAnswer(userAnswer)}
              disabled={!userAnswer.trim()}
              className="w-full"
            >
              Submit Answer
            </Button>
          )}

          {showHints && !showResult && userAnswer.length > 0 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center text-sm text-muted-foreground"
            >
              First letter: {word.word.charAt(0).toUpperCase()}
            </motion.div>
          )}
        </div>

        {showResult && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-6 text-center"
          >
            <p className={cn(
              "text-lg font-semibold",
              isCorrect ? "text-green-600" : "text-red-600"
            )}>
              {isCorrect ? "Correct!" : `Incorrect. The answer is: ${word.word}`}
            </p>
          </motion.div>
        )}
      </div>
    );
  };

  const renderLearningMode = () => {
    switch (mode) {
      case LearningMode.FLASHCARDS:
        return (
          <FlashcardMode
            word={word}
            isFlipped={isFlipped}
            onFlip={onFlip}
            onAnswer={onAnswer}
            playPronunciation={playPronunciation}
            enableAudio={enableAudio}
          />
        );

      case LearningMode.MULTIPLE_CHOICE:
        return renderMultipleChoiceMode();

      case LearningMode.TYPING:
        return renderTypingMode();

      case LearningMode.PRONUNCIATION:
        return (
          <div className="h-full flex flex-col justify-center items-center">
            <h2 className="text-2xl font-bold mb-4">Pronunciation Practice</h2>
            <p className="text-4xl font-bold mb-4">{word.word}</p>
            <p className="text-lg text-muted-foreground mb-6">/{word.pronunciation || word.word}/</p>
            <Button
              onClick={playPronunciation}
              className="mb-6"
              disabled={!enableAudio}
            >
              <Volume2 className="h-4 w-4 mr-2" />
              Play Pronunciation
            </Button>
            <p className="text-lg mb-6">{word.definition}</p>
            <div className="flex gap-4">
              <Button variant="outline" onClick={() => onAnswer(false)}>
                Need Practice
              </Button>
              <Button onClick={() => onAnswer(true)}>
                Got It!
              </Button>
            </div>
          </div>
        );

      default:
        return (
          <FlashcardMode
            word={word}
            isFlipped={isFlipped}
            onFlip={onFlip}
            onAnswer={onAnswer}
            playPronunciation={playPronunciation}
            enableAudio={enableAudio}
          />
        );
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{ duration: 0.3 }}
      className="w-full max-w-2xl mx-auto"
    >
      <Card className="h-96 relative overflow-hidden">
        <CardHeader className="pb-2">
          <div className="flex justify-between items-center">
            <CardTitle className="text-lg">
              Learning Mode: {mode.replace('_', ' ').toUpperCase()}
            </CardTitle>
            <div className="flex gap-2">
              <Badge variant={word.difficulty === 'advanced' ? 'destructive' : 'secondary'}>
                {word.difficulty}
              </Badge>
              {enableAudio && word.pronunciation && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={playPronunciation}
                  className="p-2"
                >
                  <Volume2 className="h-4 w-4" />
                </Button>
              )}
            </div>
          </div>
        </CardHeader>

        <CardContent className="h-full pb-20">
          <AnimatePresence mode="wait">
            {renderLearningMode()}
          </AnimatePresence>
        </CardContent>

        <div className="absolute bottom-0 left-0 right-0 p-4 bg-background border-t">
          <div className="flex justify-between items-center">
            <Button variant="outline" onClick={onPrevious}>
              Previous
            </Button>

            <div className="flex gap-2">
              {showResult && (
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  className="flex items-center gap-1"
                >
                  {isCorrect ? (
                    <CheckCircle className="h-5 w-5 text-green-500" />
                  ) : (
                    <XCircle className="h-5 w-5 text-red-500" />
                  )}
                </motion.div>
              )}
            </div>

            <Button onClick={onNext}>
              Next
            </Button>
          </div>
        </div>
      </Card>
    </motion.div>
  );
};