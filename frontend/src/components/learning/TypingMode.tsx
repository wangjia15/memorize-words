import React, { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LearningWord } from "@/types/learning";
import { cn } from "@/lib/utils";
import { Lightbulb, CheckCircle, XCircle, RotateCcw } from "lucide-react";

interface TypingModeProps {
  word: LearningWord;
  onAnswer: (isCorrect: boolean, answer: string) => void;
  showResult: boolean;
  isCorrect: boolean;
  showHints?: boolean;
  disabled?: boolean;
  autoFocus?: boolean;
}

interface ValidationResult {
  isCorrect: boolean;
  score: number; // 0-100, how close the answer is
  mistakes: string[];
}

// Enhanced validation function
const validateAnswer = (userAnswer: string, correctAnswer: string): ValidationResult => {
  const userTrimmed = userAnswer.toLowerCase().trim();
  const correctTrimmed = correctAnswer.toLowerCase().trim();

  if (userTrimmed === correctTrimmed) {
    return {
      isCorrect: true,
      score: 100,
      mistakes: []
    };
  }

  const mistakes: string[] = [];
  let score = 0;

  // Check for common typos and variations
  const levenshteinDistance = calculateLevenshteinDistance(userTrimmed, correctTrimmed);
  const maxLength = Math.max(userTrimmed.length, correctTrimmed.length);
  const similarity = 1 - (levenshteinDistance / maxLength);
  score = Math.round(similarity * 100);

  // Specific mistake analysis
  if (userTrimmed.length !== correctTrimmed.length) {
    if (userTrimmed.length < correctTrimmed.length) {
      mistakes.push("missing_letters");
    } else {
      mistakes.push("extra_letters");
    }
  }

  if (userTrimmed.charAt(0) !== correctTrimmed.charAt(0)) {
    mistakes.push("wrong_first_letter");
  }

  if (userTrimmed.charAt(userTrimmed.length - 1) !== correctTrimmed.charAt(correctTrimmed.length - 1)) {
    mistakes.push("wrong_last_letter");
  }

  // Check for letter transposition
  if (levenshteinDistance <= 2 && userTrimmed.length === correctTrimmed.length) {
    mistakes.push("letter_order");
  }

  // Check for phonetic similarities
  if (soundsLike(userTrimmed, correctTrimmed)) {
    mistakes.push("phonetic_error");
  }

  return {
    isCorrect: false,
    score,
    mistakes
  };
};

// Simple Levenshtein distance calculation
const calculateLevenshteinDistance = (str1: string, str2: string): number => {
  const matrix: number[][] = [];

  for (let i = 0; i <= str2.length; i++) {
    matrix[i] = [i];
  }

  for (let j = 0; j <= str1.length; j++) {
    matrix[0][j] = j;
  }

  for (let i = 1; i <= str2.length; i++) {
    for (let j = 1; j <= str1.length; j++) {
      if (str2.charAt(i - 1) === str1.charAt(j - 1)) {
        matrix[i][j] = matrix[i - 1][j - 1];
      } else {
        matrix[i][j] = Math.min(
          matrix[i - 1][j - 1] + 1, // substitution
          matrix[i][j - 1] + 1,     // insertion
          matrix[i - 1][j] + 1      // deletion
        );
      }
    }
  }

  return matrix[str2.length][str1.length];
};

// Simple phonetic similarity check
const soundsLike = (str1: string, str2: string): boolean => {
  // Simple implementation - in a real app, you'd use a phonetic algorithm like Soundex or Metaphone
  const phonemes: { [key: string]: string } = {
    'ph': 'f',
    'gh': 'f',
    'c': 'k',
    'q': 'k',
    'x': 'ks',
    'z': 's',
  };

  let processed1 = str1;
  let processed2 = str2;

  Object.entries(phonemes).forEach(([pattern, replacement]) => {
    processed1 = processed1.replace(new RegExp(pattern, 'g'), replacement);
    processed2 = processed2.replace(new RegExp(pattern, 'g'), replacement);
  });

  return calculateLevenshteinDistance(processed1, processed2) <= 1;
};

// Generate contextual hints
const generateHints = (word: string, userAnswer: string, showHints: boolean): string[] => {
  if (!showHints) return [];

  const hints: string[] = [];
  const wordLower = word.toLowerCase();
  const userLower = userAnswer.toLowerCase();

  if (userAnswer.length === 0) {
    hints.push(`First letter: ${word.charAt(0).toUpperCase()}`);
    hints.push(`Length: ${word.length} letters`);
  } else if (userAnswer.length > 0) {
    // Progressive hints based on input
    if (userLower.charAt(0) !== wordLower.charAt(0)) {
      hints.push(`First letter should be: ${word.charAt(0).toUpperCase()}`);
    }

    if (userAnswer.length >= 2 && userLower.substring(0, 2) !== wordLower.substring(0, 2)) {
      hints.push(`First two letters: ${word.substring(0, 2).toUpperCase()}`);
    }

    if (Math.abs(userAnswer.length - word.length) > 2) {
      hints.push(`Word length: ${word.length} letters`);
    }

    if (userAnswer.length >= word.length / 2) {
      hints.push(`Last letter: ${word.charAt(word.length - 1).toUpperCase()}`);
    }
  }

  return hints.slice(0, 2); // Limit to 2 hints to avoid overwhelming
};

export const TypingMode: React.FC<TypingModeProps> = ({
  word,
  onAnswer,
  showResult,
  isCorrect,
  showHints = true,
  disabled = false,
  autoFocus = true
}) => {
  const [userAnswer, setUserAnswer] = useState('');
  const [hints, setHints] = useState<string[]>([]);
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);
  const [showValidation, setShowValidation] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (autoFocus && inputRef.current && !disabled) {
      inputRef.current.focus();
    }
  }, [autoFocus, disabled]);

  useEffect(() => {
    if (showHints && userAnswer.length > 0 && !showResult) {
      const newHints = generateHints(word.word, userAnswer, showHints);
      setHints(newHints);
    } else {
      setHints([]);
    }
  }, [userAnswer, word.word, showHints, showResult]);

  const handleInputChange = (value: string) => {
    if (disabled || showResult) return;
    setUserAnswer(value);
    setShowValidation(false);

    // Real-time validation for feedback (optional)
    if (value.length > 2) {
      const result = validateAnswer(value, word.word);
      setValidationResult(result);
    }
  };

  const handleSubmit = () => {
    if (!userAnswer.trim() || disabled || showResult) return;

    const result = validateAnswer(userAnswer, word.word);
    setValidationResult(result);
    setShowValidation(true);
    onAnswer(result.isCorrect, userAnswer);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };

  const handleReset = () => {
    setUserAnswer('');
    setHints([]);
    setValidationResult(null);
    setShowValidation(false);
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  const getInputClassName = () => {
    if (showResult) {
      return cn(
        "text-xl text-center transition-all duration-200",
        isCorrect
          ? "border-green-500 bg-green-50 text-green-700"
          : "border-red-500 bg-red-50 text-red-700"
      );
    }

    if (validationResult && userAnswer.length > 2) {
      const score = validationResult.score;
      if (score >= 80) {
        return "border-yellow-400 bg-yellow-50 text-xl text-center transition-all duration-200";
      } else if (score >= 60) {
        return "border-orange-400 bg-orange-50 text-xl text-center transition-all duration-200";
      }
    }

    return "text-xl text-center transition-all duration-200";
  };

  const getMistakeFeedback = (mistakes: string[]): string => {
    if (mistakes.includes("wrong_first_letter")) {
      return "Check the first letter";
    }
    if (mistakes.includes("missing_letters")) {
      return "You're missing some letters";
    }
    if (mistakes.includes("extra_letters")) {
      return "Too many letters";
    }
    if (mistakes.includes("letter_order")) {
      return "Check the letter order";
    }
    if (mistakes.includes("phonetic_error")) {
      return "Close! Check the spelling";
    }
    return "Keep trying!";
  };

  return (
    <div className="h-full flex flex-col justify-center">
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="text-center mb-8"
      >
        <h2 className="text-2xl font-bold mb-4">Type the word</h2>
        <motion.div
          initial={{ scale: 0.95 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="text-xl bg-muted p-6 rounded-lg border-2 border-border"
        >
          <p className="font-medium mb-2">{word.definition}</p>
          {word.example && (
            <p className="text-sm text-muted-foreground italic">
              Example: "{word.example}"
            </p>
          )}
        </motion.div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.2 }}
        className="space-y-4"
      >
        <div className="relative">
          <Input
            ref={inputRef}
            type="text"
            value={userAnswer}
            onChange={(e) => handleInputChange(e.target.value)}
            onKeyPress={handleKeyPress}
            className={getInputClassName()}
            placeholder="Type your answer..."
            disabled={disabled || showResult}
            autoComplete="off"
            spellCheck="false"
            aria-label="Type your answer"
          />

          {!showResult && userAnswer.trim() && (
            <motion.button
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              onClick={handleReset}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 text-muted-foreground hover:text-foreground transition-colors"
              aria-label="Clear input"
            >
              <RotateCcw className="h-4 w-4" />
            </motion.button>
          )}
        </div>

        {!showResult && (
          <Button
            onClick={handleSubmit}
            disabled={!userAnswer.trim() || disabled}
            className="w-full"
            size="lg"
          >
            Submit Answer
          </Button>
        )}

        {/* Real-time validation feedback */}
        {validationResult && userAnswer.length > 2 && !showResult && !validationResult.isCorrect && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center text-sm text-muted-foreground"
          >
            <div className="flex items-center justify-center gap-2">
              <span>Similarity: {validationResult.score}%</span>
              {validationResult.score >= 70 && <span className="text-yellow-600">Getting close!</span>}
            </div>
            {validationResult.mistakes.length > 0 && (
              <p className="mt-1 text-xs">
                {getMistakeFeedback(validationResult.mistakes)}
              </p>
            )}
          </motion.div>
        )}

        {/* Hints */}
        <AnimatePresence>
          {hints.length > 0 && !showResult && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.3 }}
              className="bg-blue-50 border border-blue-200 rounded-lg p-3"
            >
              <div className="flex items-center gap-2 mb-2 text-blue-700">
                <Lightbulb className="h-4 w-4" />
                <span className="text-sm font-medium">Hints</span>
              </div>
              <div className="space-y-1">
                {hints.map((hint, index) => (
                  <motion.p
                    key={index}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: index * 0.1 }}
                    className="text-sm text-blue-600"
                  >
                    • {hint}
                  </motion.p>
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      {/* Result feedback */}
      {showResult && (
        <motion.div
          initial={{ opacity: 0, y: 20, scale: 0.9 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.4, delay: 0.2 }}
          className="mt-6 text-center"
        >
          <div className={cn(
            "inline-flex items-center gap-3 px-6 py-3 rounded-lg text-lg font-semibold",
            isCorrect
              ? "bg-green-100 text-green-700 border border-green-300"
              : "bg-red-100 text-red-700 border border-red-300"
          )}>
            {isCorrect ? (
              <>
                <CheckCircle className="h-6 w-6" />
                Excellent! Perfect spelling!
              </>
            ) : (
              <>
                <XCircle className="h-6 w-6" />
                <div className="text-left">
                  <div>Incorrect spelling</div>
                  <div className="text-sm font-normal">
                    Correct answer: <strong>{word.word}</strong>
                  </div>
                  {validationResult && validationResult.score >= 70 && (
                    <div className="text-xs font-normal mt-1">
                      You were {validationResult.score}% correct - great effort!
                    </div>
                  )}
                </div>
              </>
            )}
          </div>

          {!isCorrect && word.pronunciation && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
              className="text-sm text-muted-foreground mt-3"
            >
              Pronunciation: /{word.pronunciation}/
            </motion.p>
          )}
        </motion.div>
      )}
    </div>
  );
};