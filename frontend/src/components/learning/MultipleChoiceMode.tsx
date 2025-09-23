import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { LearningWord } from "@/types/learning";
import { cn } from "@/lib/utils";

interface MultipleChoiceModeProps {
  word: LearningWord;
  onAnswer: (isCorrect: boolean, answer: string) => void;
  showResult: boolean;
  isCorrect: boolean;
  disabled?: boolean;
}

// Utility function to shuffle an array
const shuffleArray = <T,>(array: T[]): T[] => {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
};

// Generate distractors based on word characteristics
const generateDistractors = (correctWord: string): string[] => {
  // In a real app, this would call a backend service to get semantically similar words
  // For now, we'll generate some mock distractors based on the word
  const baseDistractors = [
    'abandon', 'ability', 'absence', 'academic', 'account', 'achieve', 'acquire', 'address',
    'adequate', 'adjust', 'advance', 'advantage', 'adventure', 'advertisement', 'advice',
    'advocate', 'affect', 'afford', 'agency', 'agenda', 'agreement', 'agriculture', 'aircraft',
    'alcohol', 'already', 'alternative', 'although', 'amazing', 'ambition', 'analysis', 'ancient',
    'announce', 'annual', 'anxiety', 'anywhere', 'apparent', 'appeal', 'appear', 'application',
    'approach', 'appropriate', 'approval', 'approximately', 'architect', 'argument', 'arrange',
    'article', 'artificial', 'artist', 'aspect', 'assault', 'assembly', 'assessment', 'assist',
    'associate', 'assumption', 'atmosphere', 'attack', 'attempt', 'attend', 'attention',
    'attitude', 'attorney', 'attract', 'attribute', 'audience', 'authority', 'available',
    'average', 'awareness', 'background', 'balance', 'barrier', 'baseball', 'basketball',
    'bathroom', 'battery', 'beautiful', 'because', 'bedroom', 'behavior', 'benefit', 'bicycle',
    'birthday', 'boundary', 'breakfast', 'brilliant', 'brother', 'building', 'business',
    'calendar', 'campaign', 'candidate', 'capacity', 'capital', 'captain', 'capture', 'carbon',
    'career', 'careful', 'category', 'celebrate', 'ceremony', 'certainly', 'chairman',
    'challenge', 'champion', 'channel', 'chapter', 'character', 'charge', 'charity', 'chemical',
    'chicken', 'childhood', 'chocolate', 'choice', 'choose', 'citizen', 'classroom', 'climate',
    'clothes', 'coffee', 'collect', 'college', 'comment', 'commerce', 'commission', 'committee',
    'community', 'company', 'compare', 'compete', 'complaint', 'complete', 'complex', 'computer',
    'concept', 'concern', 'condition', 'conduct', 'conference', 'confidence', 'confirm',
    'conflict', 'congress', 'connect', 'consider', 'consistent', 'constant', 'constitute',
    'construct', 'contain', 'content', 'contest', 'context', 'continue', 'contract', 'control',
    'convert', 'convince', 'cooking', 'correct', 'council', 'counter', 'country', 'couple',
    'courage', 'course', 'coverage', 'creative', 'credit', 'crisis', 'criteria', 'critical',
    'culture', 'curious', 'current', 'customer', 'database', 'daughter', 'decision', 'decline',
    'decrease', 'default', 'defend', 'deficit', 'deliver', 'demand', 'democracy', 'department',
    'dependent', 'deposit', 'depression', 'design', 'desktop', 'destroy', 'detail', 'detect',
    'determine', 'develop', 'device', 'dialogue', 'diamond', 'digital', 'dinner', 'direct',
    'director', 'disability', 'disappear', 'disaster', 'discipline', 'discount', 'discover',
    'discuss', 'disease', 'display', 'distance', 'distribute', 'district', 'diverse', 'divide',
    'document', 'domestic', 'dominant', 'download', 'dramatic', 'drawing', 'driver', 'dynamic',
    'economy', 'education', 'effect', 'effective', 'efficient', 'effort', 'electric', 'element',
    'eliminate', 'emergency', 'emission', 'emotion', 'emphasis', 'employee', 'employer', 'empty',
    'enable', 'encounter', 'encourage', 'energy', 'engage', 'engine', 'engineer', 'enhance',
    'enormous', 'enough', 'enterprise', 'entire', 'entrance', 'environment', 'episode', 'equal',
    'equipment', 'escape', 'especially', 'essential', 'establish', 'estimate', 'evaluate',
    'evening', 'event', 'eventually', 'evidence', 'exactly', 'examine', 'example', 'exceed',
    'excellent', 'except', 'exchange', 'exciting', 'exclude', 'execute', 'exercise', 'exhibit',
    'exist', 'expand', 'expect', 'expense', 'experience', 'experiment', 'expert', 'explain',
    'explore', 'export', 'expose', 'express', 'extend', 'external', 'extreme', 'facility',
    'factory', 'faculty', 'failure', 'familiar', 'family', 'famous', 'fantasy', 'fashion',
    'father', 'feature', 'federal', 'feedback', 'female', 'festival', 'fiction', 'figure',
    'finance', 'finding', 'finger', 'finish', 'fishing', 'fitness', 'flight', 'flower',
    'football', 'foreign', 'forever', 'format', 'former', 'forward', 'foundation', 'freedom',
    'frequent', 'fresh', 'friend', 'function', 'fundamental', 'funeral', 'future', 'gallery',
    'garden', 'gather', 'gender', 'general', 'generate', 'genetic', 'gentle', 'global',
    'government', 'graduate', 'graphic', 'greater', 'grocery', 'ground', 'growth', 'guarantee',
    'guard', 'guest', 'guidance', 'guitar', 'handle', 'happen', 'happy', 'hardware', 'health',
    'hearing', 'height', 'heritage', 'highlight', 'himself', 'history', 'holiday', 'honest',
    'horrible', 'hospital', 'housing', 'however', 'hundred', 'husband', 'identity', 'illegal',
    'illness', 'image', 'imagine', 'impact', 'implement', 'imply', 'import', 'important',
    'improve', 'include', 'income', 'increase', 'indeed', 'independence', 'indicate', 'individual',
    'industry', 'infection', 'inflation', 'influence', 'inform', 'initial', 'initiative',
    'injury', 'innovation', 'input', 'inquiry', 'insight', 'inspire', 'install', 'instance',
    'instead', 'institute', 'instruction', 'instrument', 'insurance', 'integrate', 'intelligence',
    'intend', 'intense', 'intention', 'interact', 'interest', 'interface', 'internal',
    'international', 'internet', 'interpret', 'interview', 'introduce', 'invasion', 'invest',
    'investigate', 'investment', 'invite', 'involve', 'island', 'issue', 'itself', 'jacket',
    'joint', 'journal', 'journey', 'judge', 'junior', 'justice', 'justify', 'keyboard',
    'kitchen', 'knowledge', 'laboratory', 'language', 'laptop', 'launch', 'lawyer', 'leader',
    'leadership', 'league', 'learn', 'leather', 'leave', 'lecture', 'legacy', 'legal',
    'legislation', 'length', 'lesson', 'letter', 'level', 'liberal', 'library', 'license',
    'lifestyle', 'limited', 'literature', 'living', 'local', 'location', 'machine', 'magazine',
    'maintain', 'major', 'maker', 'manage', 'manager', 'manner', 'manufacture', 'margin',
    'market', 'marriage', 'material', 'mathematics', 'matter', 'maximum', 'meaning', 'measure',
    'mechanism', 'media', 'medical', 'medicine', 'medium', 'meeting', 'member', 'memory',
    'mental', 'mention', 'message', 'method', 'middle', 'military', 'million', 'minimum',
    'minister', 'minor', 'minority', 'minute', 'mirror', 'mission', 'mistake', 'mixture',
    'mobile', 'mode', 'model', 'moderate', 'modern', 'modify', 'moment', 'monitor', 'month',
    'moral', 'morning', 'mortgage', 'mother', 'motion', 'motivation', 'motor', 'mountain',
    'mouse', 'movement', 'movie', 'multiple', 'muscle', 'museum', 'music', 'musician',
    'mutual', 'myself', 'mystery', 'national', 'native', 'natural', 'nature', 'nearby',
    'necessary', 'negative', 'neighbor', 'neither', 'network', 'neutral', 'newspaper',
    'night', 'nobody', 'normal', 'northern', 'nothing', 'notice', 'notion', 'novel',
    'number', 'numerous', 'object', 'objective', 'obligation', 'observation', 'observe',
    'obtain', 'obvious', 'occasion', 'occupy', 'occur', 'ocean', 'offer', 'office',
    'officer', 'official', 'often', 'ongoing', 'online', 'opening', 'operate', 'operation',
    'operator', 'opinion', 'opportunity', 'oppose', 'opposite', 'option', 'orange', 'order',
    'ordinary', 'organic', 'organization', 'organize', 'origin', 'original', 'other',
    'outcome', 'output', 'outside', 'overall', 'overcome', 'overlap', 'overseas', 'owner',
    'package', 'painting', 'panel', 'paper', 'parent', 'parking', 'partner', 'party',
    'passage', 'passion', 'passive', 'password', 'patent', 'patient', 'pattern', 'payment',
    'peace', 'people', 'percent', 'perfect', 'perform', 'performance', 'perhaps', 'period',
    'permission', 'person', 'personal', 'personality', 'perspective', 'phase', 'phenomenon',
    'philosophy', 'phone', 'photo', 'phrase', 'physical', 'piano', 'picture', 'piece',
    'place', 'plane', 'planet', 'planning', 'plant', 'plastic', 'platform', 'player',
    'please', 'pleasure', 'plenty', 'pocket', 'poetry', 'point', 'police', 'policy',
    'political', 'politics', 'pollution', 'popular', 'population', 'portrait', 'position',
    'positive', 'possibility', 'possible', 'potential', 'poverty', 'power', 'powerful',
    'practical', 'practice', 'prayer', 'precise', 'predict', 'prefer', 'pregnant', 'premium',
    'prepare', 'present', 'preserve', 'president', 'pressure', 'prevent', 'previous',
    'price', 'pride', 'primary', 'principal', 'principle', 'prior', 'priority', 'privacy',
    'private', 'probably', 'problem', 'procedure', 'process', 'produce', 'product',
    'production', 'profession', 'professional', 'professor', 'profile', 'profit', 'program',
    'project', 'promise', 'promote', 'property', 'proposal', 'propose', 'prospect', 'protect',
    'protocol', 'proud', 'provide', 'province', 'provision', 'psychology', 'public',
    'publication', 'publish', 'purchase', 'purpose', 'pursue', 'quality', 'quarter',
    'question', 'quick', 'quiet', 'quite', 'quote', 'radio', 'raise', 'random', 'range',
    'rapid', 'rarely', 'rather', 'rating', 'ratio', 'reach', 'react', 'reading', 'ready',
    'reality', 'realize', 'really', 'reason', 'reasonable', 'receive', 'recent', 'recognize',
    'recommend', 'record', 'recover', 'reduce', 'refer', 'reflect', 'reform', 'refuse',
    'regard', 'region', 'register', 'regular', 'regulation', 'reject', 'relate', 'relation',
    'relationship', 'relative', 'release', 'relevant', 'reliable', 'relief', 'religion',
    'religious', 'remain', 'remember', 'remind', 'remove', 'repair', 'repeat', 'replace',
    'reply', 'report', 'represent', 'reputation', 'request', 'require', 'research', 'reserve',
    'resident', 'resolve', 'resource', 'respect', 'respond', 'response', 'responsibility',
    'responsible', 'restaurant', 'restore', 'restrict', 'result', 'retail', 'retire',
    'return', 'reveal', 'revenue', 'review', 'revolution', 'reward', 'rhythm', 'right',
    'river', 'robot', 'romantic', 'routine', 'royal', 'rural', 'safety', 'salary',
    'sample', 'satellite', 'satisfy', 'sauce', 'scale', 'scandal', 'scenario', 'scene',
    'schedule', 'scheme', 'scholarship', 'school', 'science', 'scientific', 'scope', 'score',
    'screen', 'script', 'search', 'season', 'second', 'secret', 'secretary', 'section',
    'sector', 'secure', 'security', 'select', 'senior', 'sense', 'sensitive', 'sentence',
    'separate', 'sequence', 'series', 'serious', 'serve', 'service', 'session', 'setting',
    'settle', 'several', 'severe', 'sexual', 'shadow', 'shake', 'shame', 'shape',
    'share', 'shelter', 'shift', 'shine', 'shirt', 'shock', 'shoot', 'shopping',
    'short', 'should', 'shoulder', 'shower', 'sight', 'signal', 'significant', 'silence',
    'similar', 'simple', 'since', 'single', 'sister', 'situation', 'skill', 'sleep',
    'slight', 'small', 'smart', 'smile', 'smoke', 'smooth', 'snake', 'snow',
    'soccer', 'social', 'society', 'software', 'solar', 'solid', 'solution', 'solve',
    'someone', 'something', 'sometimes', 'somewhat', 'somewhere', 'sound', 'source', 'south',
    'southern', 'space', 'spare', 'speak', 'special', 'specialist', 'specific', 'speech',
    'speed', 'spend', 'spirit', 'split', 'sport', 'spread', 'spring', 'square',
    'stable', 'staff', 'stage', 'stake', 'standard', 'standing', 'start', 'state',
    'statement', 'station', 'status', 'steal', 'steel', 'step', 'stick', 'still',
    'stock', 'stomach', 'stone', 'stop', 'storage', 'store', 'storm', 'story',
    'straight', 'strange', 'stranger', 'strategy', 'stream', 'street', 'strength', 'stress',
    'strike', 'string', 'strip', 'stroke', 'strong', 'structure', 'struggle', 'student',
    'studio', 'study', 'stuff', 'stupid', 'style', 'subject', 'submit', 'subsequent',
    'substance', 'substantial', 'succeed', 'success', 'successful', 'sudden', 'suffer',
    'sufficient', 'sugar', 'suggest', 'suggestion', 'suitable', 'summer', 'summit', 'super',
    'superior', 'supply', 'support', 'suppose', 'suppress', 'surface', 'surgery', 'surprise',
    'survey', 'survive', 'suspect', 'sustain', 'switch', 'symbol', 'symptom', 'system',
    'table', 'tackle', 'talent', 'target', 'taste', 'teach', 'teacher', 'teaching',
    'team', 'technical', 'technique', 'technology', 'telephone', 'television', 'temperature',
    'temple', 'temporary', 'tennis', 'tension', 'terminal', 'terms', 'terrible', 'territory',
    'terror', 'test', 'text', 'thank', 'theater', 'theme', 'theory', 'therapy',
    'there', 'therefore', 'these', 'thick', 'thing', 'think', 'third', 'thirty',
    'thought', 'thousand', 'threat', 'three', 'through', 'throw', 'thumb', 'ticket',
    'tight', 'timber', 'tired', 'tissue', 'title', 'today', 'together', 'tomorrow',
    'tongue', 'tonight', 'tooth', 'topic', 'total', 'touch', 'tough', 'tourism',
    'tourist', 'toward', 'track', 'trade', 'tradition', 'traditional', 'traffic', 'train',
    'training', 'transfer', 'transform', 'transition', 'translate', 'transport', 'travel',
    'treat', 'treatment', 'trend', 'trial', 'trick', 'trigger', 'trip', 'truck',
    'truly', 'trust', 'truth', 'tunnel', 'turkey', 'turning', 'twelve', 'twenty',
    'twice', 'typical', 'ultimate', 'unable', 'uncle', 'under', 'understand', 'undertake',
    'unemployment', 'unexpected', 'union', 'unique', 'unit', 'united', 'universal', 'university',
    'unknown', 'unless', 'unlike', 'unlikely', 'until', 'unusual', 'update', 'upgrade',
    'upload', 'upper', 'urban', 'urgent', 'usage', 'useful', 'user', 'usual',
    'utility', 'vacation', 'valley', 'valuable', 'value', 'variable', 'variety', 'various',
    'vegetable', 'vehicle', 'venture', 'version', 'versus', 'vertical', 'victim', 'video',
    'village', 'violence', 'violent', 'virtual', 'virus', 'visible', 'vision', 'visit',
    'visual', 'vital', 'voice', 'volume', 'volunteer', 'waiting', 'walking', 'wallet',
    'warning', 'waste', 'watch', 'water', 'wave', 'wealth', 'weapon', 'wear',
    'weather', 'website', 'wedding', 'weekend', 'weekly', 'weight', 'welcome', 'welfare',
    'western', 'whatever', 'wheel', 'whenever', 'where', 'whereas', 'whereby', 'wherever',
    'whether', 'which', 'while', 'white', 'whole', 'whose', 'widely', 'wife',
    'willing', 'window', 'wine', 'wing', 'winner', 'winter', 'wisdom', 'wise',
    'wish', 'within', 'without', 'woman', 'wonder', 'wooden', 'worker', 'working',
    'workshop', 'world', 'worry', 'worth', 'would', 'wound', 'write', 'writer',
    'writing', 'wrong', 'yellow', 'yesterday', 'young', 'youth'
  ];

  // Filter out words that are too similar to the correct answer
  const filtered = baseDistractors.filter(word =>
    word.toLowerCase() !== correctWord.toLowerCase() &&
    !word.toLowerCase().includes(correctWord.toLowerCase().substring(0, 3)) &&
    !correctWord.toLowerCase().includes(word.toLowerCase().substring(0, 3))
  );

  // Take random selection
  return shuffleArray(filtered).slice(0, 3);
};

export const MultipleChoiceMode: React.FC<MultipleChoiceModeProps> = ({
  word,
  onAnswer,
  showResult,
  isCorrect,
  disabled = false
}) => {
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [options, setOptions] = useState<string[]>([]);

  useEffect(() => {
    generateOptions();
  }, [word]);

  const generateOptions = () => {
    const distractors = generateDistractors(word.word);
    const allOptions = [word.word, ...distractors];
    setOptions(shuffleArray(allOptions));
    setSelectedOption(null);
  };

  const handleOptionSelect = (option: string) => {
    if (disabled || showResult) return;

    setSelectedOption(option);
    const correct = option.toLowerCase().trim() === word.word.toLowerCase().trim();
    onAnswer(correct, option);
  };

  const getOptionClassName = (option: string) => {
    const baseClasses = "h-16 text-lg transition-all duration-200 hover:scale-105";

    if (!showResult) {
      return cn(
        baseClasses,
        selectedOption === option
          ? "bg-primary text-primary-foreground"
          : "bg-background border-2 border-border hover:border-primary/50"
      );
    }

    // Show results
    if (option === word.word) {
      return cn(baseClasses, "bg-green-100 border-2 border-green-500 text-green-700");
    }

    if (selectedOption === option && option !== word.word) {
      return cn(baseClasses, "bg-red-100 border-2 border-red-500 text-red-700");
    }

    return cn(baseClasses, "bg-muted text-muted-foreground opacity-60");
  };

  return (
    <div className="h-full flex flex-col justify-center">
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="text-center mb-8"
      >
        <h2 className="text-2xl font-bold mb-4">What does this mean?</h2>
        <motion.div
          initial={{ scale: 0.95 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="text-xl bg-muted p-6 rounded-lg border-2 border-border"
        >
          <p className="font-medium">{word.definition}</p>
          {word.example && (
            <p className="text-sm text-muted-foreground mt-3 italic">
              Example: "{word.example}"
            </p>
          )}
        </motion.div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.2 }}
        className="grid grid-cols-2 gap-4"
      >
        {options.map((option, index) => (
          <motion.div
            key={option}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.2, delay: 0.3 + index * 0.1 }}
          >
            <Button
              variant="outline"
              className={getOptionClassName(option)}
              onClick={() => handleOptionSelect(option)}
              disabled={disabled || showResult}
              aria-label={`Option: ${option}`}
            >
              {option}
            </Button>
          </motion.div>
        ))}
      </motion.div>

      {showResult && (
        <motion.div
          initial={{ opacity: 0, y: 20, scale: 0.9 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.4, delay: 0.2 }}
          className="mt-6 text-center"
        >
          <div className={cn(
            "inline-flex items-center gap-2 px-4 py-2 rounded-lg text-lg font-semibold",
            isCorrect
              ? "bg-green-100 text-green-700 border border-green-300"
              : "bg-red-100 text-red-700 border border-red-300"
          )}>
            {isCorrect ? (
              <>
                <span className="text-2xl">✓</span>
                Correct!
              </>
            ) : (
              <>
                <span className="text-2xl">✗</span>
                Incorrect. The answer is: <strong>{word.word}</strong>
              </>
            )}
          </div>

          {!isCorrect && word.pronunciation && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
              className="text-sm text-muted-foreground mt-2"
            >
              Pronunciation: /{word.pronunciation}/
            </motion.p>
          )}
        </motion.div>
      )}
    </div>
  );
};