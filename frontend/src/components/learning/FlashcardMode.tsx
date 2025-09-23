import React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@/components/ui/button";
import { LearningWord } from "@/types/learning";

interface FlashcardModeProps {
  word: LearningWord;
  isFlipped: boolean;
  onFlip: () => void;
  onAnswer: (isCorrect: boolean) => void;
  playPronunciation: () => void;
  enableAudio: boolean;
}

export const FlashcardMode: React.FC<FlashcardModeProps> = ({
  word,
  isFlipped,
  onFlip,
  onAnswer,
  playPronunciation,
  enableAudio
}) => {
  return (
    <motion.div
      className="h-full flex flex-col justify-center items-center cursor-pointer"
      onClick={onFlip}
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
    >
      <AnimatePresence mode="wait">
        {!isFlipped ? (
          <motion.div
            key="front"
            initial={{ rotateY: 0 }}
            animate={{ rotateY: 0 }}
            exit={{ rotateY: 90 }}
            transition={{ duration: 0.3 }}
            className="text-center"
          >
            <h2 className="text-4xl font-bold mb-4">{word.word}</h2>
            {word.pronunciation && (
              <p className="text-lg text-muted-foreground mb-6">
                /{word.pronunciation}/
              </p>
            )}
            <p className="text-sm text-muted-foreground">
              Click to reveal definition
            </p>
          </motion.div>
        ) : (
          <motion.div
            key="back"
            initial={{ rotateY: -90 }}
            animate={{ rotateY: 0 }}
            exit={{ rotateY: 90 }}
            transition={{ duration: 0.3 }}
            className="text-center"
          >
            <h3 className="text-2xl font-semibold mb-4">{word.definition}</h3>
            {word.example && (
              <blockquote className="text-lg italic text-muted-foreground mb-6">
                "{word.example}"
              </blockquote>
            )}
            <div className="flex gap-4 justify-center">
              <Button
                variant="outline"
                className="bg-red-50 hover:bg-red-100 border-red-200"
                onClick={(e) => {
                  e.stopPropagation();
                  onAnswer(false);
                }}
              >
                Don't Know
              </Button>
              <Button
                className="bg-green-50 hover:bg-green-100 border-green-200 text-green-700"
                onClick={(e) => {
                  e.stopPropagation();
                  onAnswer(true);
                }}
              >
                Know It
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};