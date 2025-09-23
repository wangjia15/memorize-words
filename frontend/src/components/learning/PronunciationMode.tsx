import React, { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@/components/ui/button";
import { LearningWord } from "@/types/learning";
import { cn } from "@/lib/utils";
import {
  Volume2,
  Mic,
  MicOff,
  Play,
  Pause,
  RotateCcw,
  CheckCircle,
  XCircle,
  AlertCircle
} from "lucide-react";

interface PronunciationModeProps {
  word: LearningWord;
  onAnswer: (isCorrect: boolean, confidence?: number) => void;
  showResult: boolean;
  isCorrect: boolean;
  disabled?: boolean;
  enableSpeechRecognition?: boolean;
}

interface RecognitionResult {
  transcript: string;
  confidence: number;
  isListening: boolean;
}

declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

export const PronunciationMode: React.FC<PronunciationModeProps> = ({
  word,
  onAnswer,
  showResult,
  isCorrect,
  disabled = false,
  enableSpeechRecognition = true
}) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [recognition, setRecognition] = useState<any>(null);
  const [recognitionResult, setRecognitionResult] = useState<RecognitionResult | null>(null);
  const [audioSupported, setAudioSupported] = useState(true);
  const [speechRecognitionSupported, setSpeechRecognitionSupported] = useState(false);
  const [userChoice, setUserChoice] = useState<'confident' | 'needs-practice' | null>(null);
  const [attempts, setAttempts] = useState(0);
  const maxAttempts = 3;

  const synthRef = useRef<SpeechSynthesisUtterance | null>(null);

  useEffect(() => {
    // Check if speech synthesis is supported
    if (!('speechSynthesis' in window)) {
      setAudioSupported(false);
    }

    // Initialize speech recognition if supported
    if (enableSpeechRecognition && ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)) {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      const recognitionInstance = new SpeechRecognition();

      recognitionInstance.continuous = false;
      recognitionInstance.interimResults = false;
      recognitionInstance.lang = 'en-US';
      recognitionInstance.maxAlternatives = 1;

      recognitionInstance.onstart = () => {
        setIsListening(true);
      };

      recognitionInstance.onresult = (event: any) => {
        const result = event.results[0];
        const transcript = result[0].transcript.toLowerCase().trim();
        const confidence = result[0].confidence;

        setRecognitionResult({
          transcript,
          confidence,
          isListening: false
        });

        // Analyze the pronunciation
        analyzePronunciation(transcript, confidence);
      };

      recognitionInstance.onerror = (event: any) => {
        console.error('Speech recognition error:', event.error);
        setIsListening(false);
        setRecognitionResult({
          transcript: '',
          confidence: 0,
          isListening: false
        });
      };

      recognitionInstance.onend = () => {
        setIsListening(false);
      };

      setRecognition(recognitionInstance);
      setSpeechRecognitionSupported(true);
    }

    return () => {
      if (synthRef.current) {
        speechSynthesis.cancel();
      }
      if (recognition) {
        recognition.abort();
      }
    };
  }, [enableSpeechRecognition]);

  const playPronunciation = async () => {
    if (!audioSupported || disabled) return;

    try {
      // Cancel any ongoing speech
      speechSynthesis.cancel();

      setIsPlaying(true);

      const utterance = new SpeechSynthesisUtterance(word.word);
      utterance.rate = 0.7; // Slower for learning
      utterance.pitch = 1;
      utterance.volume = 1;

      // Try to get a native English voice
      const voices = speechSynthesis.getVoices();
      const englishVoice = voices.find(voice =>
        voice.lang.startsWith('en-') && voice.localService
      ) || voices.find(voice => voice.lang.startsWith('en-'));

      if (englishVoice) {
        utterance.voice = englishVoice;
      }

      utterance.onend = () => {
        setIsPlaying(false);
      };

      utterance.onerror = () => {
        setIsPlaying(false);
      };

      synthRef.current = utterance;
      speechSynthesis.speak(utterance);
    } catch (error) {
      console.error('Pronunciation playback failed:', error);
      setIsPlaying(false);
    }
  };

  const startListening = () => {
    if (!speechRecognitionSupported || !recognition || disabled || isListening) return;

    setAttempts(prev => prev + 1);
    setRecognitionResult(null);

    try {
      recognition.start();
    } catch (error) {
      console.error('Failed to start speech recognition:', error);
      setIsListening(false);
    }
  };

  const stopListening = () => {
    if (recognition && isListening) {
      recognition.stop();
    }
  };

  const analyzePronunciation = (transcript: string, confidence: number) => {
    const wordLower = word.word.toLowerCase().trim();
    const transcriptLower = transcript.toLowerCase().trim();

    // Simple pronunciation analysis
    const isCorrect = transcriptLower === wordLower ||
                     transcriptLower.includes(wordLower) ||
                     wordLower.includes(transcriptLower);

    // Adjust confidence based on accuracy
    let adjustedConfidence = confidence;
    if (isCorrect) {
      adjustedConfidence = Math.max(confidence, 0.8); // Boost confidence for correct answers
    } else {
      adjustedConfidence = Math.min(confidence, 0.6); // Reduce confidence for incorrect answers
    }

    setUserChoice(isCorrect && adjustedConfidence > 0.7 ? 'confident' : 'needs-practice');
  };

  const handleManualAnswer = (confident: boolean) => {
    setUserChoice(confident ? 'confident' : 'needs-practice');
    onAnswer(confident, confident ? 0.9 : 0.3);
  };

  const handleSpeechAnswer = () => {
    if (!recognitionResult) return;

    const wordLower = word.word.toLowerCase().trim();
    const transcriptLower = recognitionResult.transcript.toLowerCase().trim();
    const isCorrect = transcriptLower === wordLower ||
                     transcriptLower.includes(wordLower) ||
                     wordLower.includes(transcriptLower);

    onAnswer(isCorrect, recognitionResult.confidence);
  };

  const resetAttempt = () => {
    setRecognitionResult(null);
    setUserChoice(null);
    setAttempts(0);
  };

  const getSpeechAccuracy = (): string => {
    if (!recognitionResult) return '';

    const wordLower = word.word.toLowerCase().trim();
    const transcriptLower = recognitionResult.transcript.toLowerCase().trim();

    if (transcriptLower === wordLower) {
      return 'Perfect!';
    } else if (transcriptLower.includes(wordLower) || wordLower.includes(transcriptLower)) {
      return 'Very close!';
    } else {
      return 'Keep practicing!';
    }
  };

  return (
    <div className="h-full flex flex-col justify-center items-center">
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="text-center mb-8"
      >
        <h2 className="text-2xl font-bold mb-6">Pronunciation Practice</h2>

        {/* Word display */}
        <motion.div
          initial={{ scale: 0.9 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="mb-6"
        >
          <p className="text-5xl font-bold text-primary mb-4">{word.word}</p>
          {word.pronunciation && (
            <p className="text-xl text-muted-foreground mb-2">
              /{word.pronunciation}/
            </p>
          )}
          <p className="text-lg text-muted-foreground">{word.definition}</p>
        </motion.div>

        {/* Audio controls */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.2 }}
          className="mb-8"
        >
          <Button
            onClick={playPronunciation}
            disabled={!audioSupported || disabled || isPlaying}
            size="lg"
            className="px-8 py-4 text-lg"
          >
            {isPlaying ? (
              <>
                <Pause className="h-5 w-5 mr-2" />
                Playing...
              </>
            ) : (
              <>
                <Volume2 className="h-5 w-5 mr-2" />
                Listen
              </>
            )}
          </Button>

          {!audioSupported && (
            <p className="text-sm text-muted-foreground mt-2">
              Audio not supported in this browser
            </p>
          )}
        </motion.div>
      </motion.div>

      {/* Speech recognition section */}
      {speechRecognitionSupported && !showResult && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.3 }}
          className="w-full max-w-md mb-6"
        >
          <div className="text-center mb-4">
            <h3 className="text-lg font-semibold mb-2">Try pronouncing it!</h3>

            <div className="flex justify-center gap-4">
              <Button
                onClick={isListening ? stopListening : startListening}
                disabled={disabled || attempts >= maxAttempts}
                variant={isListening ? "destructive" : "default"}
                size="lg"
                className={cn(
                  "transition-all duration-200",
                  isListening && "animate-pulse"
                )}
              >
                {isListening ? (
                  <>
                    <MicOff className="h-5 w-5 mr-2" />
                    Stop
                  </>
                ) : (
                  <>
                    <Mic className="h-5 w-5 mr-2" />
                    Record
                  </>
                )}
              </Button>

              {recognitionResult && (
                <Button
                  onClick={resetAttempt}
                  variant="outline"
                  size="lg"
                >
                  <RotateCcw className="h-4 w-4 mr-2" />
                  Reset
                </Button>
              )}
            </div>

            <p className="text-sm text-muted-foreground mt-2">
              Attempts: {attempts}/{maxAttempts}
            </p>
          </div>

          {/* Recognition result */}
          <AnimatePresence>
            {recognitionResult && (
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                className="bg-muted p-4 rounded-lg border"
              >
                <div className="text-center">
                  <p className="text-sm text-muted-foreground mb-2">You said:</p>
                  <p className="text-lg font-medium mb-2">"{recognitionResult.transcript}"</p>
                  <p className="text-sm text-muted-foreground mb-3">
                    Confidence: {Math.round(recognitionResult.confidence * 100)}%
                  </p>

                  <div className={cn(
                    "inline-block px-3 py-1 rounded text-sm font-medium",
                    getSpeechAccuracy() === 'Perfect!' ? "bg-green-100 text-green-700" :
                    getSpeechAccuracy() === 'Very close!' ? "bg-yellow-100 text-yellow-700" :
                    "bg-red-100 text-red-700"
                  )}>
                    {getSpeechAccuracy()}
                  </div>

                  <div className="mt-4">
                    <Button
                      onClick={handleSpeechAnswer}
                      className="w-full"
                    >
                      Submit Pronunciation
                    </Button>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {isListening && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center mt-4"
            >
              <div className="flex justify-center items-center gap-2 text-primary">
                <motion.div
                  animate={{ scale: [1, 1.2, 1] }}
                  transition={{ repeat: Infinity, duration: 1 }}
                  className="w-3 h-3 bg-red-500 rounded-full"
                />
                <span className="text-sm font-medium">Listening...</span>
              </div>
            </motion.div>
          )}
        </motion.div>
      )}

      {/* Manual assessment (fallback) */}
      {!showResult && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.4 }}
          className="text-center"
        >
          <h3 className="text-lg font-semibold mb-4">
            {speechRecognitionSupported ? "Or rate yourself:" : "How did you do?"}
          </h3>

          <div className="flex gap-4 justify-center">
            <Button
              variant="outline"
              className="bg-red-50 hover:bg-red-100 border-red-200 text-red-700"
              onClick={() => handleManualAnswer(false)}
              disabled={disabled}
            >
              <XCircle className="h-4 w-4 mr-2" />
              Need Practice
            </Button>
            <Button
              className="bg-green-50 hover:bg-green-100 border-green-200 text-green-700"
              onClick={() => handleManualAnswer(true)}
              disabled={disabled}
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Got It!
            </Button>
          </div>
        </motion.div>
      )}

      {/* Result display */}
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
              : "bg-orange-100 text-orange-700 border border-orange-300"
          )}>
            {isCorrect ? (
              <>
                <CheckCircle className="h-6 w-6" />
                Excellent pronunciation!
              </>
            ) : (
              <>
                <AlertCircle className="h-6 w-6" />
                Keep practicing - you'll get it!
              </>
            )}
          </div>

          {word.example && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
              className="mt-4 p-4 bg-muted rounded-lg"
            >
              <p className="text-sm text-muted-foreground mb-1">Example usage:</p>
              <p className="italic">"{word.example}"</p>
            </motion.div>
          )}
        </motion.div>
      )}

      {/* Browser compatibility notice */}
      {!speechRecognitionSupported && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="mt-4 text-center"
        >
          <p className="text-xs text-muted-foreground">
            Speech recognition requires Chrome, Edge, or Safari
          </p>
        </motion.div>
      )}
    </div>
  );
};