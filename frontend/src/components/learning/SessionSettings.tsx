import React, { useState } from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Slider } from "@/components/ui/slider";
import {
  LearningMode,
  DifficultyLevel,
  SessionSettings as SessionSettingsType
} from "@/types/learning";
import { cn } from "@/lib/utils";
import {
  BookOpen,
  CheckSquare,
  Keyboard,
  Mic,
  Shuffle,
  Clock,
  Volume2,
  Lightbulb,
  RotateCcw,
  Settings,
  Zap,
  Target,
  Timer
} from "lucide-react";

interface SessionSettingsProps {
  settings: SessionSettingsType;
  onSettingsChange: (settings: SessionSettingsType) => void;
  selectedMode: LearningMode;
  onModeChange: (mode: LearningMode) => void;
  selectedDifficulty: DifficultyLevel;
  onDifficultyChange: (difficulty: DifficultyLevel) => void;
  wordCount: number;
  onWordCountChange: (count: number) => void;
  onStartSession: () => void;
  isLoading?: boolean;
  className?: string;
}

const learningModes = [
  {
    mode: LearningMode.FLASHCARDS,
    title: "Flashcards",
    description: "Classic card-flipping for vocabulary review",
    icon: BookOpen,
    color: "bg-blue-100 text-blue-700 border-blue-200"
  },
  {
    mode: LearningMode.MULTIPLE_CHOICE,
    title: "Multiple Choice",
    description: "Choose the correct word from options",
    icon: CheckSquare,
    color: "bg-green-100 text-green-700 border-green-200"
  },
  {
    mode: LearningMode.TYPING,
    title: "Typing Practice",
    description: "Type the word based on definition",
    icon: Keyboard,
    color: "bg-purple-100 text-purple-700 border-purple-200"
  },
  {
    mode: LearningMode.PRONUNCIATION,
    title: "Pronunciation",
    description: "Practice speaking and pronunciation",
    icon: Mic,
    color: "bg-orange-100 text-orange-700 border-orange-200"
  },
  {
    mode: LearningMode.MIXED,
    title: "Mixed Mode",
    description: "Combination of all learning modes",
    icon: Shuffle,
    color: "bg-indigo-100 text-indigo-700 border-indigo-200"
  }
];

const difficultyLevels = [
  {
    level: DifficultyLevel.BEGINNER,
    title: "Beginner",
    description: "Basic vocabulary, more time per word",
    color: "bg-green-100 text-green-700"
  },
  {
    level: DifficultyLevel.INTERMEDIATE,
    title: "Intermediate",
    description: "Moderate difficulty, balanced pace",
    color: "bg-yellow-100 text-yellow-700"
  },
  {
    level: DifficultyLevel.ADVANCED,
    title: "Advanced",
    description: "Complex words, faster pace",
    color: "bg-red-100 text-red-700"
  },
  {
    level: DifficultyLevel.MIXED,
    title: "Mixed",
    description: "All difficulty levels combined",
    color: "bg-purple-100 text-purple-700"
  }
];

export const SessionSettings: React.FC<SessionSettingsProps> = ({
  settings,
  onSettingsChange,
  selectedMode,
  onModeChange,
  selectedDifficulty,
  onDifficultyChange,
  wordCount,
  onWordCountChange,
  onStartSession,
  isLoading = false,
  className
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const updateSetting = (key: keyof SessionSettingsType, value: any) => {
    onSettingsChange({
      ...settings,
      [key]: value
    });
  };

  const resetToDefaults = () => {
    const defaultSettings: SessionSettingsType = {
      autoAdvance: false,
      showDefinitionFirst: true,
      enablePronunciation: true,
      enableHints: true,
      timeLimit: undefined,
      shuffleWords: true,
      repeatIncorrect: true
    };
    onSettingsChange(defaultSettings);
    onModeChange(LearningMode.FLASHCARDS);
    onDifficultyChange(DifficultyLevel.INTERMEDIATE);
    onWordCountChange(10);
  };

  const getEstimatedDuration = (): string => {
    const baseTimePerWord = {
      [LearningMode.FLASHCARDS]: 15,
      [LearningMode.MULTIPLE_CHOICE]: 20,
      [LearningMode.TYPING]: 30,
      [LearningMode.PRONUNCIATION]: 25,
      [LearningMode.MIXED]: 25
    };

    const difficultyMultiplier = {
      [DifficultyLevel.BEGINNER]: 1.3,
      [DifficultyLevel.INTERMEDIATE]: 1.0,
      [DifficultyLevel.ADVANCED]: 0.8,
      [DifficultyLevel.MIXED]: 1.1
    };

    const baseTime = baseTimePerWord[selectedMode] || 20;
    const totalSeconds = wordCount * baseTime * difficultyMultiplier[selectedDifficulty];
    const minutes = Math.round(totalSeconds / 60);

    return minutes < 60 ? `${minutes} min` : `${Math.round(minutes / 60)}h ${minutes % 60}m`;
  };

  return (
    <div className={cn("w-full max-w-4xl mx-auto space-y-6", className)}>
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center"
      >
        <h1 className="text-3xl font-bold mb-2">Start Learning Session</h1>
        <p className="text-muted-foreground">
          Choose your learning mode and customize your session
        </p>
      </motion.div>

      {/* Learning Mode Selection */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Target className="h-5 w-5" />
              Learning Mode
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {learningModes.map((modeConfig) => {
                const Icon = modeConfig.icon;
                const isSelected = selectedMode === modeConfig.mode;

                return (
                  <motion.div
                    key={modeConfig.mode}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    <Card
                      className={cn(
                        "cursor-pointer transition-all duration-200 hover:shadow-md",
                        isSelected
                          ? "ring-2 ring-primary shadow-md"
                          : "hover:border-primary/50"
                      )}
                      onClick={() => onModeChange(modeConfig.mode)}
                    >
                      <CardContent className="p-4">
                        <div className="flex items-start gap-3">
                          <div className={cn(
                            "p-2 rounded-lg",
                            modeConfig.color
                          )}>
                            <Icon className="h-5 w-5" />
                          </div>
                          <div className="flex-1">
                            <h3 className="font-semibold text-sm">
                              {modeConfig.title}
                            </h3>
                            <p className="text-xs text-muted-foreground mt-1">
                              {modeConfig.description}
                            </p>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </motion.div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </motion.div>

      {/* Difficulty & Word Count */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="grid grid-cols-1 lg:grid-cols-2 gap-6"
      >
        {/* Difficulty Selection */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Zap className="h-5 w-5" />
              Difficulty Level
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {difficultyLevels.map((level) => (
                <div
                  key={level.level}
                  className={cn(
                    "p-3 rounded-lg border-2 cursor-pointer transition-all duration-200",
                    selectedDifficulty === level.level
                      ? "border-primary bg-primary/5"
                      : "border-border hover:border-primary/50"
                  )}
                  onClick={() => onDifficultyChange(level.level)}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <Badge className={level.color}>
                        {level.title}
                      </Badge>
                      <p className="text-sm text-muted-foreground mt-1">
                        {level.description}
                      </p>
                    </div>
                    <div className={cn(
                      "w-4 h-4 rounded-full border-2",
                      selectedDifficulty === level.level
                        ? "bg-primary border-primary"
                        : "border-border"
                    )} />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Word Count & Duration */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Timer className="h-5 w-5" />
              Session Length
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div>
              <div className="flex justify-between items-center mb-3">
                <label className="text-sm font-medium">
                  Number of Words
                </label>
                <Badge variant="outline">
                  {wordCount} words
                </Badge>
              </div>
              <Slider
                value={[wordCount]}
                onValueChange={(value) => onWordCountChange(value[0])}
                min={5}
                max={50}
                step={5}
                className="w-full"
              />
              <div className="flex justify-between text-xs text-muted-foreground mt-2">
                <span>5</span>
                <span>25</span>
                <span>50</span>
              </div>
            </div>

            <div className="bg-muted p-4 rounded-lg">
              <div className="flex items-center gap-2 text-sm">
                <Clock className="h-4 w-4" />
                <span className="font-medium">Estimated duration:</span>
                <Badge>{getEstimatedDuration()}</Badge>
              </div>
            </div>
          </CardContent>
        </Card>
      </motion.div>

      {/* Advanced Settings */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <Card>
          <CardHeader className="cursor-pointer" onClick={() => setIsExpanded(!isExpanded)}>
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Settings className="h-5 w-5" />
                Advanced Settings
              </div>
              <motion.div
                animate={{ rotate: isExpanded ? 180 : 0 }}
                transition={{ duration: 0.2 }}
              >
                ▼
              </motion.div>
            </CardTitle>
          </CardHeader>

          <motion.div
            initial={false}
            animate={{
              height: isExpanded ? "auto" : 0,
              opacity: isExpanded ? 1 : 0
            }}
            transition={{ duration: 0.3 }}
            style={{ overflow: "hidden" }}
          >
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Learning Preferences */}
                <div className="space-y-4">
                  <h3 className="font-semibold text-sm">Learning Preferences</h3>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Zap className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Auto-advance after correct answer</label>
                    </div>
                    <Switch
                      checked={settings.autoAdvance}
                      onCheckedChange={(checked) => updateSetting('autoAdvance', checked)}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <BookOpen className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Show definition first (flashcards)</label>
                    </div>
                    <Switch
                      checked={settings.showDefinitionFirst}
                      onCheckedChange={(checked) => updateSetting('showDefinitionFirst', checked)}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Shuffle className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Shuffle word order</label>
                    </div>
                    <Switch
                      checked={settings.shuffleWords}
                      onCheckedChange={(checked) => updateSetting('shuffleWords', checked)}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <RotateCcw className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Repeat incorrect answers</label>
                    </div>
                    <Switch
                      checked={settings.repeatIncorrect}
                      onCheckedChange={(checked) => updateSetting('repeatIncorrect', checked)}
                    />
                  </div>
                </div>

                {/* Assistance Features */}
                <div className="space-y-4">
                  <h3 className="font-semibold text-sm">Assistance Features</h3>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Volume2 className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Enable pronunciation audio</label>
                    </div>
                    <Switch
                      checked={settings.enablePronunciation}
                      onCheckedChange={(checked) => updateSetting('enablePronunciation', checked)}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Lightbulb className="h-4 w-4 text-muted-foreground" />
                      <label className="text-sm">Show hints when struggling</label>
                    </div>
                    <Switch
                      checked={settings.enableHints}
                      onCheckedChange={(checked) => updateSetting('enableHints', checked)}
                    />
                  </div>

                  {/* Time Limit */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Clock className="h-4 w-4 text-muted-foreground" />
                        <label className="text-sm">Time limit per word</label>
                      </div>
                      <Badge variant="outline">
                        {settings.timeLimit ? `${settings.timeLimit}s` : 'None'}
                      </Badge>
                    </div>
                    <Slider
                      value={[settings.timeLimit || 0]}
                      onValueChange={(value) => updateSetting('timeLimit', value[0] === 0 ? undefined : value[0])}
                      min={0}
                      max={60}
                      step={5}
                      className="w-full"
                    />
                    <div className="flex justify-between text-xs text-muted-foreground">
                      <span>No limit</span>
                      <span>30s</span>
                      <span>60s</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Reset Button */}
              <div className="flex justify-end pt-4 border-t">
                <Button
                  variant="outline"
                  onClick={resetToDefaults}
                  className="flex items-center gap-2"
                >
                  <RotateCcw className="h-4 w-4" />
                  Reset to Defaults
                </Button>
              </div>
            </CardContent>
          </motion.div>
        </Card>
      </motion.div>

      {/* Start Button */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="flex justify-center"
      >
        <Button
          onClick={onStartSession}
          disabled={isLoading}
          size="lg"
          className="px-12 py-4 text-lg font-semibold"
        >
          {isLoading ? (
            <>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                className="w-5 h-5 border-2 border-current border-t-transparent rounded-full mr-2"
              />
              Starting Session...
            </>
          ) : (
            <>
              <BookOpen className="h-5 w-5 mr-2" />
              Start Learning Session
            </>
          )}
        </Button>
      </motion.div>
    </div>
  );
};