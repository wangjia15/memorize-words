# Task 007: Review System - GitHub Issue #7

## Metadata
```yaml
epic: memorize-words
task_number: 007-github
title: Review System - GitHub Issue #7
status: completed
priority: high
effort_estimate: 20
parallel: true
dependencies: [005, 006]
github_issue: 7
github_url: https://github.com/wangjia15/memorize-words/issues/7
created: 2025-09-23T10:15:00Z
updated: 2025-09-23T10:30:00Z
completed: 2025-09-23T10:30:00Z
assignee: developer
tags: [spaced-repetition, review-system, algorithm, scheduling, analytics, memory-retention, user-experience]
```

## Summary
Implement a comprehensive review system with advanced spaced repetition algorithms, multiple review modes, and intelligent scheduling to optimize long-term memory retention. The system will track learning progress, adapt to individual performance patterns, and provide personalized review schedules for maximum learning efficiency.

## GitHub Issue Mapping
- **Issue Number**: #7
- **Issue Title**: Review System
- **Issue State**: OPEN
- **Author**: wangjia15
- **Issue URL**: https://github.com/wangjia15/memorize-words/issues/7

## Acceptance Criteria (from GitHub Issue)
- [ ] Spaced repetition algorithm implementation (SM-2, Anki-style, or custom)
- [ ] Multiple review modes (due words, difficult words, random review)
- [ ] Intelligent scheduling based on performance and forgetting curve
- [ ] Review streak tracking and motivation features
- [ ] Performance analytics and learning insights
- [ ] Customizable review parameters (interval modifiers, ease factors)
- [ ] Review notification system and reminders
- [ ] Adaptive difficulty adjustment during reviews
- [ ] Review session resume functionality
- [ ] Export/import of review data and statistics
- [ ] Integration with learning progress tracking
- [ ] Review optimization suggestions based on performance

## Technical Implementation Plan

### Backend Components

#### 1. Spaced Repetition Entities
```java
@Entity
@Table(name = "spaced_repetition_cards")
@Data
@EqualsAndHashCode(callSuper = true)
public class SpacedRepetitionCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "ease_factor", precision = 4, scale = 2)
    private BigDecimal easeFactor = new BigDecimal("2.50");

    @Column(name = "interval_days")
    private Integer interval = 1;

    @Column(name = "repetition_count")
    private Integer repetitionCount = 0;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_state")
    private CardState state = CardState.NEW;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "correct_reviews")
    private Integer correctReviews = 0;

    @Column(name = "average_response_time")
    private Integer averageResponseTime = 0;

    @Column(name = "difficulty_rating", precision = 3, scale = 2)
    private BigDecimal difficultyRating = BigDecimal.ZERO;

    @Column(name = "stability", precision = 4, scale = 2)
    private BigDecimal stability = new BigDecimal("1.00");

    @Column(name = "retrievability", precision = 4, scale = 2)
    private BigDecimal retrievability = new BigDecimal("1.00");

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<ReviewOutcome> reviewHistory = new ArrayList<>();
}

@Entity
@Table(name = "review_sessions")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_mode")
    private ReviewMode mode;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_cards")
    private Integer totalCards = 0;

    @Column(name = "completed_cards")
    private Integer completedCards = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "average_response_time")
    private Integer averageResponseTime = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<ReviewSessionCard> cards = new ArrayList<>();
}

@Entity
@Table(name = "review_session_cards")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewSessionCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private SpacedRepetitionCard card;

    @Column(name = "response_time")
    private Integer responseTime = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private ReviewOutcome outcome;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
```

#### 2. Spaced Repetition Service
```java
@Service
@Transactional
public class SpacedRepetitionService {
    private final SpacedRepetitionCardRepository cardRepository;
    private final ReviewSessionRepository sessionRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final UserPreferencesService preferencesService;

    public SpacedRepetitionCard createCard(User user, Word word) {
        SpacedRepetitionCard card = new SpacedRepetitionCard();
        card.setUser(user);
        card.setWord(word);
        card.setDueDate(LocalDateTime.now());
        card.setState(CardState.NEW);

        return cardRepository.save(card);
    }

    public ReviewSession startReviewSession(User user, ReviewMode mode, int limit) {
        List<SpacedRepetitionCard> cards = getCardsForReview(user, mode, limit);

        ReviewSession session = new ReviewSession();
        session.setUser(user);
        session.setMode(mode);
        session.setStartTime(LocalDateTime.now());
        session.setTotalCards(cards.size());

        session = sessionRepository.save(session);

        // Create session cards
        List<ReviewSessionCard> sessionCards = cards.stream()
            .map(card -> {
                ReviewSessionCard sessionCard = new ReviewSessionCard();
                sessionCard.setSession(session);
                sessionCard.setCard(card);
                return sessionCard;
            })
            .collect(Collectors.toList());

        session.setCards(sessionCards);

        return sessionRepository.save(session);
    }

    public ReviewSession submitReview(ReviewSession session, SpacedRepetitionCard card,
                                   ReviewOutcome outcome, int responseTime) {
        // Update the spaced repetition card
        updateCardAlgorithm(card, outcome, responseTime);

        // Find and update the session card
        ReviewSessionCard sessionCard = session.getCards().stream()
            .filter(sc -> sc.getCard().equals(card))
            .findFirst()
            .orElseThrow();

        sessionCard.setOutcome(outcome);
        sessionCard.setResponseTime(responseTime);
        sessionCard.setReviewedAt(LocalDateTime.now());

        // Update session statistics
        session.setCompletedCards(session.getCompletedCards() + 1);
        if (outcome == ReviewOutcome.GOOD || outcome == ReviewOutcome.EASY) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }

        // Calculate new average response time
        int totalResponseTime = session.getCards().stream()
            .filter(sc -> sc.getResponseTime() > 0)
            .mapToInt(ReviewSessionCard::getResponseTime)
            .sum();
        int completedCount = (int) session.getCards().stream()
            .filter(sc -> sc.getResponseTime() > 0)
            .count();

        session.setAverageResponseTime(completedCount > 0 ? totalResponseTime / completedCount : 0);

        return sessionRepository.save(session);
    }

    private void updateCardAlgorithm(SpacedRepetitionCard card, ReviewOutcome outcome, int responseTime) {
        card.setLastReviewed(LocalDateTime.now());
        card.setTotalReviews(card.getTotalReviews() + 1);
        card.getReviewHistory().add(outcome);

        // Update average response time
        if (card.getAverageResponseTime() == 0) {
            card.setAverageResponseTime(responseTime);
        } else {
            card.setAverageResponseTime((card.getAverageResponseTime() + responseTime) / 2);
        }

        // Apply SM-2 algorithm
        switch (outcome) {
            case AGAIN:
                handleFailedReview(card);
                break;
            case HARD:
                handleHardReview(card);
                break;
            case GOOD:
                handleGoodReview(card);
                break;
            case EASY:
                handleEasyReview(card);
                break;
        }

        // Update memory model
        updateMemoryModel(card, outcome);

        // Calculate next due date
        calculateNextDueDate(card);

        cardRepository.save(card);
    }

    private List<SpacedRepetitionCard> getCardsForReview(User user, ReviewMode mode, int limit) {
        switch (mode) {
            case DUE_CARDS:
                return cardRepository.findDueCardsForUser(user, LocalDateTime.now(),
                    PageRequest.of(0, limit));
            case DIFFICULT_CARDS:
                return cardRepository.findDifficultCardsForUser(user,
                    PageRequest.of(0, limit));
            case RANDOM_REVIEW:
                return cardRepository.findRandomCardsForUser(user,
                    PageRequest.of(0, limit));
            case NEW_CARDS:
                return cardRepository.findNewCardsForUser(user,
                    PageRequest.of(0, limit));
            default:
                return cardRepository.findDueCardsForUser(user, LocalDateTime.now(),
                    PageRequest.of(0, limit));
        }
    }
}
```

#### 3. REST Controllers
```java
@RestController
@RequestMapping("/api/spaced-repetition")
public class SpacedRepetitionController {
    private final SpacedRepetitionService spacedRepetitionService;
    private final ReviewAnalyticsService analyticsService;

    @PostMapping("/cards")
    public ResponseEntity<ApiResponse<SpacedRepetitionCardDTO>> createCard(
            @RequestBody CreateCardRequest request) {
        SpacedRepetitionCard card = spacedRepetitionService.createCard(
            getCurrentUser(), request.getWordId());

        return ResponseEntity.ok(ApiResponse.success(toDTO(card)));
    }

    @PostMapping("/reviews/start")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> startReview(
            @RequestBody StartReviewRequest request) {
        ReviewSession session = spacedRepetitionService.startReviewSession(
            getCurrentUser(), request.getMode(), request.getLimit());

        return ResponseEntity.ok(ApiResponse.success(toDTO(session)));
    }

    @PostMapping("/reviews/{sessionId}/submit")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> submitReview(
            @PathVariable Long sessionId,
            @RequestBody SubmitReviewRequest request) {

        ReviewSession session = spacedRepetitionService.submitReview(
            getSession(sessionId),
            getCard(request.getCardId()),
            request.getOutcome(),
            request.getResponseTime());

        return ResponseEntity.ok(ApiResponse.success(toDTO(session)));
    }

    @GetMapping("/cards/due")
    public ResponseEntity<ApiResponse<List<SpacedRepetitionCardDTO>>> getDueCards(
            @RequestParam(defaultValue = "20") int limit) {
        List<SpacedRepetitionCard> cards = spacedRepetitionService.getDueCards(
            getCurrentUser(), limit);

        return ResponseEntity.ok(ApiResponse.success(toDTOList(cards)));
    }

    @GetMapping("/reviews/statistics")
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ReviewStatistics stats = analyticsService.getReviewStatistics(
            getCurrentUser(), from, to);

        return ResponseEntity.ok(ApiResponse.success(toDTO(stats)));
    }
}
```

### Frontend Components

#### 1. Review Session Hook
```typescript
export interface ReviewSession {
  id: string;
  userId: string;
  mode: ReviewMode;
  startTime: Date;
  endTime?: Date;
  totalCards: number;
  completedCards: number;
  correctAnswers: number;
  averageResponseTime: number;
  isCompleted: boolean;
  cards: ReviewCard[];
  currentCardIndex: number;
}

export interface ReviewCard {
  id: string;
  wordId: string;
  word: string;
  definition: string;
  easeFactor: number;
  interval: number;
  repetitionCount: number;
  dueDate: Date;
  state: CardState;
  difficultyRating: number;
}

export enum ReviewMode {
  DUE_CARDS = 'due_cards',
  DIFFICULT_CARDS = 'difficult_cards',
  RANDOM_REVIEW = 'random_review',
  NEW_CARDS = 'new_cards'
}

export const useReviewSession = () => {
  const [session, setSession] = useState<ReviewSession | null>(null);
  const [currentCard, setCurrentCard] = useState<ReviewCard | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const startReview = async (mode: ReviewMode, limit: number = 20) => {
    setIsLoading(true);
    try {
      const response = await api.post('/api/spaced-repetition/reviews/start', { mode, limit });
      const newSession = response.data.data;
      setSession(newSession);
      setCurrentCard(newSession.cards[0]);
    } catch (error) {
      console.error('Failed to start review session:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const submitReview = async (outcome: ReviewOutcome, responseTime: number) => {
    if (!session || !currentCard) return;

    try {
      const response = await api.post(`/api/spaced-repetition/reviews/${session.id}/submit`, {
        cardId: currentCard.id,
        outcome,
        responseTime
      });

      const updatedSession = response.data.data;
      setSession(updatedSession);

      // Move to next card
      const nextIndex = session.currentCardIndex + 1;
      if (nextIndex < session.cards.length) {
        setCurrentCard(session.cards[nextIndex]);
      } else {
        // Review session completed
        await completeReview();
      }
    } catch (error) {
      console.error('Failed to submit review:', error);
    }
  };

  const completeReview = async () => {
    if (!session) return;

    try {
      const response = await api.post(`/api/spaced-repetition/reviews/${session.id}/complete`);
      const completedSession = response.data.data;
      setSession(completedSession);
      return completedSession;
    } catch (error) {
      console.error('Failed to complete review:', error);
    }
  };

  return {
    session,
    currentCard,
    isLoading,
    startReview,
    submitReview,
    completeReview
  };
};
```

#### 2. Review Card Component
```typescript
export const ReviewCard: React.FC<ReviewCardProps> = ({
  card,
  onSubmit,
  showAnswer,
  onToggleAnswer
}) => {
  const [responseTime, setResponseTime] = useState(0);
  const startTime = useRef<number>(Date.now());

  useEffect(() => {
    startTime.current = Date.now();
  }, [card]);

  const handleSubmit = (outcome: ReviewOutcome) => {
    const endTime = Date.now();
    const time = endTime - startTime.current;
    setResponseTime(time);
    onSubmit(outcome, time);
  };

  return (
    <Card className="w-full max-w-2xl mx-auto h-96">
      <CardHeader>
        <div className="flex justify-between items-center">
          <CardTitle>Review</CardTitle>
          <div className="flex gap-2">
            <Badge variant="outline">
              Interval: {card.interval} days
            </Badge>
            <Badge variant="secondary">
              Ease: {card.easeFactor.toFixed(2)}
            </Badge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="h-full pb-20">
        <motion.div
          className="h-full flex flex-col justify-center items-center cursor-pointer"
          onClick={onToggleAnswer}
          whileHover={{ scale: 1.02 }}
        >
          <AnimatePresence mode="wait">
            {!showAnswer ? (
              <motion.div
                key="question"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-center"
              >
                <h2 className="text-4xl font-bold mb-6">{card.word}</h2>
                <p className="text-muted-foreground">Click to reveal definition</p>
              </motion.div>
            ) : (
              <motion.div
                key="answer"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-center"
              >
                <h3 className="text-2xl font-semibold mb-6">{card.definition}</h3>
                <div className="flex gap-3 justify-center">
                  <Button
                    variant="destructive"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSubmit(ReviewOutcome.AGAIN);
                    }}
                  >
                    Again
                  </Button>
                  <Button
                    variant="outline"
                    className="border-orange-300 text-orange-600 hover:bg-orange-50"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSubmit(ReviewOutcome.HARD);
                    }}
                  >
                    Hard
                  </Button>
                  <Button
                    variant="outline"
                    className="border-green-300 text-green-600 hover:bg-green-50"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSubmit(ReviewOutcome.GOOD);
                    }}
                  >
                    Good
                  </Button>
                  <Button
                    className="bg-blue-500 hover:bg-blue-600"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSubmit(ReviewOutcome.EASY);
                    }}
                  >
                    Easy
                  </Button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </CardContent>

      <div className="absolute bottom-0 left-0 right-0 p-4 bg-background border-t">
        <div className="flex justify-between items-center text-sm text-muted-foreground">
          <span>Response Time: {(responseTime / 1000).toFixed(1)}s</span>
          <span>Due: {formatDueDate(card.dueDate)}</span>
        </div>
      </div>
    </Card>
  );
};
```

### Database Schema

#### Required Tables
1. **spaced_repetition_cards** - Main spaced repetition data
2. **review_sessions** - Review session tracking
3. **review_session_cards** - Individual review attempts
4. **user_review_preferences** - User preferences for review settings

#### Key Relationships
- Users → SpacedRepetitionCards (One-to-Many)
- Words → SpacedRepetitionCards (One-to-Many)
- Users → ReviewSessions (One-to-Many)
- ReviewSessions → ReviewSessionCards (One-to-Many)
- SpacedRepetitionCards → ReviewSessionCards (One-to-Many)

### Testing Strategy

#### Unit Tests
- SM-2 algorithm implementation
- Card scheduling logic
- Review session management
- Statistics calculations

#### Integration Tests
- Review session workflow
- API endpoint functionality
- Database operations
- User preference handling

#### Performance Tests
- Large dataset handling
- Concurrent review sessions
- Algorithm efficiency
- Database query optimization

### Performance Considerations

1. **Database Optimization**
   - Indexes on due_date, user_id, and state fields
   - Partitioning for large datasets
   - Query optimization for card selection

2. **Caching Strategy**
   - Redis caching for frequently accessed cards
   - User statistics caching
   - Algorithm parameter caching

3. **Background Processing**
   - Asynchronous card updates
   - Scheduled review calculations
   - Analytics processing

### Success Metrics

#### Algorithm Effectiveness
- Retention rate improvement
- User satisfaction scores
- Learning velocity optimization
- Review session completion rates

#### System Performance
- Response time < 500ms for card operations
- Support 10,000+ concurrent review sessions
- 99.9% uptime for review system
- Database query efficiency

## Dependencies and Prerequisites

### Required Dependencies
- Task 005: Learning Interface (completed)
- Task 006: Spaced Repetition Algorithm (completed)
- User authentication system (completed)
- Vocabulary management system (completed)

### External Dependencies
- Chart.js for analytics visualization
- Redis for caching (optional)
- Database indexing optimization
- Frontend animation libraries

## Risk Assessment

### Technical Risks
- Algorithm complexity and debugging
- Performance with large user bases
- Data consistency across concurrent sessions
- Migration from existing review systems

### Mitigation Strategies
- Comprehensive testing framework
- Performance monitoring and optimization
- Database transaction management
- Gradual rollout and A/B testing

## Rollout Plan

### Phase 1: Core Implementation
- Spaced repetition entities and services
- Basic review session functionality
- SM-2 algorithm implementation

### Phase 2: Enhanced Features
- Multiple review modes
- Analytics and insights
- User preferences and customization

### Phase 3: Optimization
- Performance tuning
- User experience improvements
- Advanced algorithm features

## Definition of Done
- [ ] Spaced repetition algorithm implemented and tested
- [ ] Multiple review modes functional
- [ ] Performance analytics working correctly
- [ ] Review interface polished and responsive
- [ ] Database optimizations in place
- [ ] All tests passing with high coverage
- [ ] Algorithm effectiveness validated
- [ ] User documentation complete
- [ ] Performance benchmarks met
- [ ] GitHub issue marked as complete