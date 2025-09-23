import { render, screen, fireEvent } from '@testing-library/react';
import { ReviewProgress } from '../ReviewProgress';
import { ReviewSession, ReviewMode } from '@/types/review';

const mockSession: ReviewSession = {
  id: 'session-1',
  userId: 'user-1',
  mode: ReviewMode.DUE_CARDS,
  startTime: new Date(),
  totalCards: 10,
  completedCards: 3,
  correctAnswers: 2,
  averageResponseTime: 2500,
  isCompleted: false,
  sessionAccuracy: 66.67,
  sessionDuration: 180,
  cardsPerMinute: 1.67,
  efficiencyScore: 0.75,
  totalSessionScore: 85,
  newCardsLearned: 1,
  difficultCardsMastered: 1,
  learningVelocity: 1.2,
  cards: [],
  currentCardIndex: 5,
  remainingCards: 7,
  progressPercentage: 60,
};

describe('ReviewProgress', () => {
  const mockOnPause = jest.fn();
  const mockOnResume = jest.fn();
  const mockOnRestart = jest.fn();
  const mockOnEnd = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders progress information correctly', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
        onPause={mockOnPause}
        onResume={mockOnResume}
        onRestart={mockOnRestart}
        onEnd={mockOnEnd}
        isPaused={false}
      />
    );

    expect(screen.getByText('Session Progress')).toBeInTheDocument();
    expect(screen.getByText('6 / 10')).toBeInTheDocument();
    expect(screen.getByText('60% Complete')).toBeInTheDocument();
    expect(screen.getByText('3 of 10 cards completed')).toBeInTheDocument();
  });

  it('displays accuracy with correct color', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    const accuracyElement = screen.getByText('66.7%');
    expect(accuracyElement).toBeInTheDocument();
    expect(accuracyElement).toHaveClass('text-yellow-600'); // 66.67% is in the yellow range
  });

  it('displays average response time', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('2.5s')).toBeInTheDocument();
  });

  it('displays cards per minute', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('1.7/min')).toBeInTheDocument();
  });

  it('displays efficiency score', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('75%')).toBeInTheDocument(); // 0.75 * 100
  });

  it('shows remaining cards and correct answers', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('Remaining: 7')).toBeInTheDocument();
    expect(screen.getByText('Correct: 2')).toBeInTheDocument();
  });

  it('shows session score when available', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('Score: 85')).toBeInTheDocument();
  });

  it('shows control buttons when not paused', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
        onPause={mockOnPause}
        onResume={mockOnResume}
        onRestart={mockOnRestart}
        onEnd={mockOnEnd}
        isPaused={false}
      />
    );

    expect(screen.getByText('Pause')).toBeInTheDocument();
    expect(screen.getByText('Restart')).toBeInTheDocument();
    expect(screen.getByText('End Session')).toBeInTheDocument();
    expect(screen.queryByText('Resume')).not.toBeInTheDocument();
  });

  it('shows resume button when paused', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
        onPause={mockOnPause}
        onResume={mockOnResume}
        onRestart={mockOnRestart}
        onEnd={mockOnEnd}
        isPaused={true}
      />
    );

    expect(screen.getByText('Resume')).toBeInTheDocument();
    expect(screen.queryByText('Pause')).not.toBeInTheDocument();
  });

  it('calls control handlers when buttons are clicked', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
        onPause={mockOnPause}
        onResume={mockOnResume}
        onRestart={mockOnRestart}
        onEnd={mockOnEnd}
        isPaused={false}
      />
    );

    fireEvent.click(screen.getByText('Pause'));
    expect(mockOnPause).toHaveBeenCalled();

    fireEvent.click(screen.getByText('Restart'));
    expect(mockOnRestart).toHaveBeenCalled();

    fireEvent.click(screen.getByText('End Session'));
    expect(mockOnEnd).toHaveBeenCalled();
  });

  it('renders compact mode correctly', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={true}
      />
    );

    expect(screen.getByText('6 / 10')).toBeInTheDocument();
    expect(screen.getByText('60%')).toBeInTheDocument();
    expect(screen.getByText('Accuracy: 66.7%')).toBeInTheDocument();
    expect(screen.getByText('Time: 2.5s')).toBeInTheDocument();
  });

  it('shows detailed statistics when showDetailed is true', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={true}
        compact={false}
      />
    );

    expect(screen.getByText('Detailed Statistics')).toBeInTheDocument();
    expect(screen.getByText('New Cards: 1')).toBeInTheDocument();
    expect(screen.getByText('Difficult Cards: 1')).toBeInTheDocument();
    expect(screen.getByText('Learning Velocity: 1.20')).toBeInTheDocument();
    expect(screen.getByText('Session Duration: 3m')).toBeInTheDocument();
  });

  it('handles session mode icons correctly', () => {
    const modes = [
      ReviewMode.DUE_CARDS,
      ReviewMode.DIFFICULT_CARDS,
      ReviewMode.NEW_CARDS,
      ReviewMode.RANDOM_REVIEW,
    ];

    modes.forEach(mode => {
      const sessionWithMode = { ...mockSession, mode };

      const { rerender } = render(
        <ReviewProgress
          session={sessionWithMode}
          currentIndex={5}
          showDetailed={false}
          compact={false}
        />
      );

      expect(screen.getByText('Session Progress')).toBeInTheDocument();

      rerender(null);
    });
  });

  it('displays mode name in lowercase', () => {
    render(
      <ReviewProgress
        session={mockSession}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('due cards')).toBeInTheDocument();
  });

  it('handles undefined optional props gracefully', () => {
    const sessionWithoutOptionalProps = {
      ...mockSession,
      sessionScore: undefined,
      cardsPerMinute: undefined,
      efficiencyScore: undefined,
      newCardsLearned: undefined,
      difficultCardsMastered: undefined,
      learningVelocity: undefined,
      sessionDuration: undefined,
    };

    render(
      <ReviewProgress
        session={sessionWithoutOptionalProps}
        currentIndex={5}
        showDetailed={true}
        compact={false}
      />
    );

    // Should still render without errors
    expect(screen.getByText('Session Progress')).toBeInTheDocument();
    expect(screen.getByText('0%')).toBeInTheDocument(); // efficiency score default
  });

  it('handles zero completed cards', () => {
    const sessionWithZeroCompleted = {
      ...mockSession,
      completedCards: 0,
      correctAnswers: 0,
    };

    render(
      <ReviewProgress
        session={sessionWithZeroCompleted}
        currentIndex={5}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('0%')).toBeInTheDocument(); // accuracy
  });

  it('handles 100% completion', () => {
    const completedSession = {
      ...mockSession,
      totalCards: 5,
      completedCards: 5,
      currentCardIndex: 4,
    };

    render(
      <ReviewProgress
        session={completedSession}
        currentIndex={4}
        showDetailed={false}
        compact={false}
      />
    );

    expect(screen.getByText('5 / 5')).toBeInTheDocument();
    expect(screen.getByText('100% Complete')).toBeInTheDocument();
  });
});