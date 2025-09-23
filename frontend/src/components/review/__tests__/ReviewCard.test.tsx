import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ReviewCard } from '../ReviewCard';
import { ReviewOutcome, SpacedRepetitionCard } from '@/types/review';

// Mock the framer-motion library
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
  },
  AnimatePresence: ({ children }: any) => <>{children}</>,
}));

// Mock the date-fns library
jest.mock('date-fns', () => ({
  formatDistanceToNow: () => '2 hours ago',
}));

// Mock the reviewUtils
jest.mock('@/services/reviewApi', () => ({
  reviewUtils: {
    getCardStateColor: () => 'bg-blue-100 text-blue-800',
    getOutcomeStyle: (outcome: ReviewOutcome) => ({
      color: 'red',
      bgColor: 'bg-red-500',
      hoverColor: 'hover:bg-red-600',
      textColor: 'text-red-500',
      label: outcome,
      description: 'Test description'
    }),
    getDueStatus: () => ({
      isOverdue: false,
      isDueToday: true,
      daysUntilDue: 0,
      statusText: 'Due today',
      statusColor: 'text-yellow-600'
    }),
    formatResponseTime: (ms: number) => `${ms}ms`
  }
}));

const mockCard: SpacedRepetitionCard = {
  id: '1',
  userId: 'user-1',
  word: {
    id: 'word-1',
    text: 'serendipity',
    translation: 'The occurrence and development of events by chance in a happy way',
    pronunciation: 'ser-ən-ˈdi-pə-tē',
    type: 'NOUN',
    difficultyLevel: 0.5,
    exampleSentence: 'It was pure serendipity that we met at the coffee shop.',
    exampleTranslation: 'Fue pura serendipia que nos conociéramos en la cafetería.'
  },
  intervalDays: 1,
  easeFactor: 2.5,
  dueDate: new Date(),
  nextReview: new Date(),
  lastReviewed: new Date(),
  totalReviews: 0,
  correctReviews: 0,
  consecutiveCorrect: 0,
  consecutiveIncorrect: 0,
  difficultyLevel: 0.5,
  performanceIndex: 0.8,
  averageResponseTime: 3000,
  isActive: true,
  isSuspended: false,
  stabilityFactor: 1.0,
  totalStudyTime: 30000,
  reviewCountAgain: 0,
  reviewCountHard: 0,
  reviewCountGood: 0,
  reviewCountEasy: 0,
  cardAgeDays: 1,
  retentionRate: 0.8,
  isDue: true,
  isNew: true,
  isDifficult: false,
  difficultyRating: 0.3,
  reviewHistory: []
};

describe('ReviewCard', () => {
  const mockOnSubmit = jest.fn();
  const mockOnToggleAnswer = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders word initially when showAnswer is false', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('serendipity')).toBeInTheDocument();
    expect(screen.queryByText('The occurrence and development of events by chance in a happy way')).not.toBeInTheDocument();
  });

  it('shows pronunciation when available', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('/ser-ən-ˈdi-pə-tē/')).toBeInTheDocument();
  });

  it('shows definition when showAnswer is true', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('The occurrence and development of events by chance in a happy way')).toBeInTheDocument();
    expect(screen.getByText('It was pure serendipity that we met at the coffee shop.')).toBeInTheDocument();
  });

  it('calls onToggleAnswer when card is clicked', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    fireEvent.click(screen.getByText('serendipity'));
    expect(mockOnToggleAnswer).toHaveBeenCalled();
  });

  it('calls onSubmit with correct outcome when button clicked', async () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    const goodButton = screen.getByText('Good');
    fireEvent.click(goodButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith(ReviewOutcome.GOOD);
    });
  });

  it('calls onSubmit with all outcome buttons', async () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    // Test each outcome button
    const outcomes = [ReviewOutcome.AGAIN, ReviewOutcome.HARD, ReviewOutcome.GOOD, ReviewOutcome.EASY];

    for (const outcome of outcomes) {
      const button = screen.getByText(outcome);
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(outcome);
      });

      mockOnSubmit.mockClear();
    }
  });

  it('shows loading overlay when isSubmitting is true', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
        isSubmitting={true}
      />
    );

    expect(screen.getByText('Submitting review...')).toBeInTheDocument();
  });

  it('displays card statistics when answer is shown', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('Reviews:')).toBeInTheDocument();
    expect(screen.getByText('Success:')).toBeInTheDocument();
    expect(screen.getByText('Streak:')).toBeInTheDocument();
  });

  it('handles hint functionality', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
        showHint={true}
      />
    );

    const hintButton = screen.getByText('Get Hint');
    fireEvent.click(hintButton);

    // The hint should be shown when the answer is revealed
    expect(mockOnToggleAnswer).toHaveBeenCalled();
  });

  it('handles compact mode', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
        compact={true}
      />
    );

    // In compact mode, should have smaller height
    const card = screen.getByRole('article');
    expect(card).toHaveClass('h-[400px]');
  });

  it('displays due status correctly', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('Due today')).toBeInTheDocument();
  });

  it('shows example sentence translation when available', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={true}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('Fue pura serendipia que nos conociéramos en la cafetería.')).toBeInTheDocument();
  });

  it('displays card difficulty and performance metrics', () => {
    render(
      <ReviewCard
        card={mockCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('Interval: 1d')).toBeInTheDocument();
    expect(screen.getByText('Ease: 2.50')).toBeInTheDocument();
    expect(screen.getByText('0.30')).toBeInTheDocument(); // difficulty rating
    expect(screen.getByText('80%')).toBeInTheDocument(); // retention rate
  });

  it('handles new card display', () => {
    const newCard = { ...mockCard, isNew: true, isDifficult: false };

    render(
      <ReviewCard
        card={newCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('New')).toBeInTheDocument();
  });

  it('handles difficult card display', () => {
    const difficultCard = { ...mockCard, isNew: false, isDifficult: true };

    render(
      <ReviewCard
        card={difficultCard}
        onSubmit={mockOnSubmit}
        showAnswer={false}
        onToggleAnswer={mockOnToggleAnswer}
      />
    );

    expect(screen.getByText('Difficult')).toBeInTheDocument();
  });
});