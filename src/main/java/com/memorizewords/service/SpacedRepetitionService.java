package com.memorizewords.service;

import com.memorizewords.entity.SpacedRepetitionCard;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.ReviewOutcome;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.SpacedRepetitionCardRepository;
import com.memorizewords.repository.UserRepository;
import com.memorizewords.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementing the spaced repetition algorithm.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SpacedRepetitionService {

    private final SpacedRepetitionCardRepository cardRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    // Algorithm parameters
    private static final BigDecimal MINIMUM_EASE_FACTOR = new BigDecimal("1.3");
    private static final BigDecimal MAXIMUM_EASE_FACTOR = new BigDecimal("2.5");
    private static final int INITIAL_INTERVAL = 1;
    private static final int MAXIMUM_INTERVAL = 365;

    public SpacedRepetitionCard createCard(User user, Word word) {
        log.info("Creating spaced repetition card for user {} and word {}", user.getId(), word.getId());

        SpacedRepetitionCard card = new SpacedRepetitionCard();
        card.setUser(user);
        card.setWord(word);
        card.setIntervalDays(INITIAL_INTERVAL);
        card.setEaseFactor(new BigDecimal("2.5"));
        card.setDueDate(LocalDateTime.now());
        card.setActive(true);

        return cardRepository.save(card);
    }

    public SpacedRepetitionCard getCard(Long cardId) {
        return cardRepository.findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("SpacedRepetitionCard", "id", cardId));
    }

    public SpacedRepetitionCard getCardByUserAndWord(User user, Word word) {
        return cardRepository.findByUserAndWordId(user, word.getId())
            .orElseGet(() -> createCard(user, word));
    }

    public SpacedRepetitionCard updateCardWithOutcome(SpacedRepetitionCard card, ReviewOutcome outcome, int responseTime) {
        log.debug("Updating card {} with outcome {} and response time {}", card.getId(), outcome, responseTime);

        // Record the review
        card.recordReview(outcome, responseTime);

        // Update spaced repetition parameters
        updateSpacedRepetitionParameters(card, outcome);

        // Calculate next review date
        calculateNextReviewDate(card);

        // Update card age
        updateCardAge(card);

        return cardRepository.save(card);
    }

    public List<SpacedRepetitionCard> getDueCards(User user) {
        LocalDateTime now = LocalDateTime.now();
        return cardRepository.findDueCardsForUser(user, now);
    }

    public List<SpacedRepetitionCard> getDueCards(User user, int limit) {
        LocalDateTime now = LocalDateTime.now();
        return cardRepository.findDueCardsForUser(user, now).stream()
            .limit(limit)
            .toList();
    }

    public List<SpacedRepetitionCard> getNewCards(User user, int limit) {
        return cardRepository.findNewCardsForUser(user).stream()
            .limit(limit)
            .toList();
    }

    public List<SpacedRepetitionCard> getDifficultCards(User user, int limit) {
        return cardRepository.findDifficultCardsForUser(user, org.springframework.data.domain.PageRequest.of(0, limit))
            .getContent();
    }

    public List<SpacedRepetitionCard> getRandomCards(User user, int limit) {
        return cardRepository.findRandomCardsForUser(user, org.springframework.data.domain.PageRequest.of(0, limit))
            .getContent();
    }

    public long countDueCards(User user) {
        LocalDateTime now = LocalDateTime.now();
        return cardRepository.countDueCardsForUser(user, now);
    }

    public long countNewCards(User user) {
        return cardRepository.countNewCardsForUser(user);
    }

    public long countActiveCards(User user) {
        return cardRepository.countActiveCardsForUser(user);
    }

    public List<SpacedRepetitionCard> getActiveCards(User user) {
        return cardRepository.findActiveCardsForUser(user);
    }

    public List<SpacedRepetitionCard> getLowPerformingCards(User user, int minReviews) {
        return cardRepository.findLowPerformingCards(user, minReviews);
    }

    public List<SpacedRepetitionCard> getHighPerformingCards(User user, double threshold) {
        return cardRepository.findHighPerformingCards(user, threshold);
    }

    public void suspendCard(Long cardId) {
        SpacedRepetitionCard card = getCard(cardId);
        card.setSuspended(true);
        cardRepository.save(card);
    }

    public void unsuspendCard(Long cardId) {
        SpacedRepetitionCard card = getCard(cardId);
        card.setSuspended(false);
        cardRepository.save(card);
    }

    public void resetCard(Long cardId) {
        SpacedRepetitionCard card = getCard(cardId);
        card.setIntervalDays(INITIAL_INTERVAL);
        card.setEaseFactor(new BigDecimal("2.5"));
        card.setDueDate(LocalDateTime.now());
        card.setTotalReviews(0);
        card.setCorrectReviews(0);
        card.setConsecutiveCorrect(0);
        card.setConsecutiveIncorrect(0);
        card.setDifficultyRating(null);
        card.setPerformanceIndex(null);
        card.setAverageResponseTime(null);
        card.setStabilityFactor(BigDecimal.ONE);
        card.setActive(true);
        card.setSuspended(false);
        card.setTotalStudyTime(0L);
        card.setLastReviewOutcome(null);
        card.setReviewCountAgain(0);
        card.setReviewCountHard(0);
        card.setReviewCountGood(0);
        card.setReviewCountEasy(0);
        card.setCardAgeDays(0);
        card.setRetentionRate(null);
        card.getReviewHistory().clear();
        cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        SpacedRepetitionCard card = getCard(cardId);
        cardRepository.delete(card);
    }

    public void bulkCreateCards(User user, List<Word> words) {
        List<SpacedRepetitionCard> cards = words.stream()
            .map(word -> {
                SpacedRepetitionCard card = new SpacedRepetitionCard();
                card.setUser(user);
                card.setWord(word);
                card.setIntervalDays(INITIAL_INTERVAL);
                card.setEaseFactor(new BigDecimal("2.5"));
                card.setDueDate(LocalDateTime.now());
                card.setActive(true);
                return card;
            })
            .toList();

        cardRepository.saveAll(cards);
        log.info("Created {} spaced repetition cards for user {}", cards.size(), user.getId());
    }

    private void updateSpacedRepetitionParameters(SpacedRepetitionCard card, ReviewOutcome outcome) {
        BigDecimal currentEaseFactor = card.getEaseFactor();
        BigDecimal newEaseFactor = currentEaseFactor;

        switch (outcome) {
            case AGAIN:
                // Reset interval, decrease ease factor significantly
                card.setIntervalDays(1);
                newEaseFactor = currentEaseFactor.subtract(new BigDecimal("0.2"));
                break;
            case HARD:
                // Small increase in interval, slight decrease in ease factor
                card.setIntervalDays((int) (card.getIntervalDays() * 1.2));
                newEaseFactor = currentEaseFactor.subtract(new BigDecimal("0.15"));
                break;
            case GOOD:
                // Standard interval increase
                card.setIntervalDays((int) (card.getIntervalDays() * currentEaseFactor.doubleValue()));
                newEaseFactor = currentEaseFactor;
                break;
            case EASY:
                // Large interval increase, slight increase in ease factor
                card.setIntervalDays((int) (card.getIntervalDays() * currentEaseFactor.doubleValue() * 1.3));
                newEaseFactor = currentEaseFactor.add(new BigDecimal("0.1"));
                break;
        }

        // Apply bounds to ease factor
        newEaseFactor = newEaseFactor.max(MINIMUM_EASE_FACTOR);
        newEaseFactor = newEaseFactor.min(MAXIMUM_EASE_FACTOR);
        card.setEaseFactor(newEaseFactor);

        // Apply bounds to interval
        card.setIntervalDays(Math.min(card.getIntervalDays(), MAXIMUM_INTERVAL));
        card.setIntervalDays(Math.max(card.getIntervalDays(), 1));

        // Update stability factor
        updateStabilityFactor(card, outcome);
    }

    private void updateStabilityFactor(SpacedRepetitionCard card, ReviewOutcome outcome) {
        BigDecimal currentStability = card.getStabilityFactor();
        BigDecimal newStability = currentStability;

        switch (outcome) {
            case AGAIN:
                newStability = currentStability.multiply(new BigDecimal("0.5"));
                break;
            case HARD:
                newStability = currentStability.multiply(new BigDecimal("0.8"));
                break;
            case GOOD:
                newStability = currentStability.multiply(new BigDecimal("1.1"));
                break;
            case EASY:
                newStability = currentStability.multiply(new BigDecimal("1.3"));
                break;
        }

        // Ensure stability doesn't go below minimum
        newStability = newStability.max(new BigDecimal("0.1"));
        card.setStabilityFactor(newStability.setScale(2, RoundingMode.HALF_UP));
    }

    private void calculateNextReviewDate(SpacedRepetitionCard card) {
        LocalDateTime nextReview = LocalDateTime.now().plusDays(card.getIntervalDays());
        card.setNextReview(nextReview);
        card.setDueDate(nextReview);
    }

    private void updateCardAge(SpacedRepetitionCard card) {
        if (card.getCreatedAt() != null) {
            long ageInDays = java.time.Duration.between(card.getCreatedAt(), LocalDateTime.now()).toDays();
            card.setCardAgeDays((int) ageInDays);
        }
    }

    public Optional<SpacedRepetitionCard> findCardByUserAndWord(User user, Word word) {
        return cardRepository.findByUserAndWordId(user, word.getId());
    }

    public List<SpacedRepetitionCard> getCardsNeedingAttention(User user) {
        // Get cards with consecutive incorrect answers or low performance
        List<SpacedRepetitionCard> cards = cardRepository.findCardsWithConsecutiveIncorrect(user, 2);
        cards.addAll(cardRepository.findLowPerformingCards(user, 5));
        return cards.stream().distinct().toList();
    }

    public void adjustDifficultyBasedOnPerformance(User user) {
        List<SpacedRepetitionCard> cards = cardRepository.findActiveCardsForUser(user);

        for (SpacedRepetitionCard card : cards) {
            if (card.getPerformanceIndex() != null) {
                double performance = card.getPerformanceIndex().doubleValue();

                // Adjust ease factor based on performance
                if (performance < 30.0) {
                    // Poor performance - make reviews more frequent
                    BigDecimal newEaseFactor = card.getEaseFactor().subtract(new BigDecimal("0.1"));
                    card.setEaseFactor(newEaseFactor.max(MINIMUM_EASE_FACTOR));
                } else if (performance > 80.0) {
                    // Excellent performance - allow longer intervals
                    BigDecimal newEaseFactor = card.getEaseFactor().add(new BigDecimal("0.05"));
                    card.setEaseFactor(newEaseFactor.min(MAXIMUM_EASE_FACTOR));
                }
            }
        }

        cardRepository.saveAll(cards);
    }

    public BigDecimal getUserAverageRetentionRate(User user) {
        Double avgRetention = cardRepository.getAverageRetentionRate(user);
        return avgRetention != null ? new BigDecimal(avgRetention).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getUserAveragePerformanceIndex(User user) {
        Double avgPerformance = cardRepository.getAveragePerformanceIndex(user);
        return avgPerformance != null ? new BigDecimal(avgPerformance).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public Long getUserTotalStudyTime(User user) {
        return cardRepository.getTotalStudyTime(user);
    }

    public Double getUserAverageResponseTime(User user) {
        return cardRepository.getAverageResponseTime(user);
    }

    public Double getUserOverallAccuracy(User user) {
        Long totalReviews = cardRepository.getTotalReviews(user);
        Long correctReviews = cardRepository.getTotalCorrectReviews(user);

        if (totalReviews == null || totalReviews == 0 || correctReviews == null) {
            return 0.0;
        }

        return (double) correctReviews / totalReviews * 100.0;
    }
}