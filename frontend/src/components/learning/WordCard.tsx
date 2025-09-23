import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Volume2, CheckCircle, XCircle } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { LearningWord, LearningMode } from "@/types/learning";
import { FlashcardMode } from "./FlashcardMode";
import { MultipleChoiceMode } from "./MultipleChoiceMode";
import { TypingMode } from "./TypingMode";
import { PronunciationMode } from "./PronunciationMode";

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
        return (
          <MultipleChoiceMode
            word={word}
            onAnswer={(isCorrect, answer) => onAnswer(isCorrect, answer)}
            showResult={showResult}
            isCorrect={isCorrect}
          />
        );

      case LearningMode.TYPING:
        return (
          <TypingMode
            word={word}
            onAnswer={(isCorrect, answer) => onAnswer(isCorrect, answer)}
            showResult={showResult}
            isCorrect={isCorrect}
            showHints={showHints}
          />
        );

      case LearningMode.PRONUNCIATION:
        return (
          <PronunciationMode
            word={word}
            onAnswer={(isCorrect) => onAnswer(isCorrect)}
            showResult={showResult}
            isCorrect={isCorrect}
            enableSpeechRecognition={enableAudio}
          />
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