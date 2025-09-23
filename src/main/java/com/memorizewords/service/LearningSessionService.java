package com.memorizewords.service;

import com.memorizewords.dto.request.CreateSessionRequest;
import com.memorizewords.dto.request.SubmitAnswerRequest;
import com.memorizewords.dto.response.LearningSessionDto;
import com.memorizewords.dto.response.LearningAnswerDto;
import com.memorizewords.dto.response.LearningWordDto;
import com.memorizewords.dto.response.SessionStatsDto;
import com.memorizewords.entity.*;
import com.memorizewords.enums.LearningMode;
import com.memorizewords.enums.SessionStatus;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.exception.AccessDeniedException;
import com.memorizewords.repository.LearningSessionRepository;
import com.memorizewords.repository.WordRepository;
import com.memorizewords.repository.VocabularyListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for learning session management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LearningSessionService {

    private final LearningSessionRepository sessionRepository;
    private final WordRepository wordRepository;
    private final VocabularyListRepository vocabularyListRepository;

    public LearningSessionDto startSession(CreateSessionRequest request, User user) {
        log.info("Starting new learning session for user: {} with mode: {}", user.getUsername(), request.getMode());

        // Check if user has any active sessions
        if (sessionRepository.hasActiveOrPausedSession(user)) {
            log.warn("User {} already has an active or paused session", user.getUsername());
            // Could either throw exception or auto-complete previous session
            // For now, we'll complete any existing sessions
            completeExistingSessions(user);
        }

        LearningSession session = new LearningSession();
        session.setUser(user);
        session.setMode(request.getMode());
        session.setDifficulty(request.getDifficulty());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.ACTIVE);

        // Session settings
        session.setAutoAdvance(request.getAutoAdvance());
        session.setShowDefinitionFirst(request.getShowDefinitionFirst());
        session.setEnablePronunciation(request.getEnablePronunciation());
        session.setEnableHints(request.getEnableHints());
        session.setTimeLimit(request.getTimeLimit());
        session.setShuffleWords(request.getShuffleWords());
        session.setRepeatIncorrect(request.getRepeatIncorrect());

        // Set vocabulary list if specified
        if (request.getVocabularyListId() != null) {
            VocabularyList vocabularyList = vocabularyListRepository.findById(request.getVocabularyListId())
                .orElseThrow(() -> new ResourceNotFoundException("VocabularyList", "id", request.getVocabularyListId()));
            session.setVocabularyList(vocabularyList);
        }

        // Select words for the session
        Set<Word> words = selectWordsForSession(request, user);
        session.setWords(words);
        session.setTotalWords(words.size());

        LearningSession savedSession = sessionRepository.save(session);
        log.info("Successfully created learning session with ID: {}", savedSession.getId());

        return mapToDto(savedSession);
    }

    public LearningSessionDto submitAnswer(Long sessionId, SubmitAnswerRequest request, User user) {
        log.debug("Submitting answer for session: {} by user: {}", sessionId, user.getUsername());

        LearningSession session = getSessionWithValidation(sessionId, user);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        Word word = wordRepository.findById(request.getWordId())
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", request.getWordId()));

        // Create and save the answer
        LearningAnswer answer = new LearningAnswer();
        answer.setSession(session);
        answer.setWord(word);
        answer.setIsCorrect(request.getIsCorrect());
        answer.setUserAnswer(request.getUserAnswer());
        answer.setTimeSpent(request.getTimeSpent());
        answer.setAttemptNumber(request.getAttemptNumber());
        answer.setHintUsed(request.getHintUsed());
        answer.setPronunciationPlayed(request.getPronunciationPlayed());
        answer.setConfidenceLevel(request.getConfidenceLevel());
        answer.setDifficultyRating(request.getDifficultyRating());
        answer.setAnsweredAt(LocalDateTime.now());

        session.addAnswer(answer);

        // Update session progress
        if (request.getIsCorrect()) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }

        // Move to next word
        session.setCurrentWordIndex(session.getCurrentWordIndex() + 1);
        session.setCompletedWords(session.getCompletedWords() + 1);

        // Check if session is complete
        if (session.getCurrentWordIndex() >= session.getTotalWords()) {
            completeSession(session);
        }

        LearningSession updatedSession = sessionRepository.save(session);
        log.debug("Successfully submitted answer for session: {}", sessionId);

        return mapToDto(updatedSession);
    }

    public LearningSessionDto pauseSession(Long sessionId, User user) {
        log.info("Pausing session: {} for user: {}", sessionId, user.getUsername());

        LearningSession session = getSessionWithValidation(sessionId, user);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active and cannot be paused");
        }

        session.setStatus(SessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());

        LearningSession updatedSession = sessionRepository.save(session);
        log.info("Successfully paused session: {}", sessionId);

        return mapToDto(updatedSession);
    }

    public LearningSessionDto resumeSession(Long sessionId, User user) {
        log.info("Resuming session: {} for user: {}", sessionId, user.getUsername());

        LearningSession session = getSessionWithValidation(sessionId, user);

        if (session.getStatus() != SessionStatus.PAUSED) {
            throw new IllegalStateException("Session is not paused and cannot be resumed");
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setResumedAt(LocalDateTime.now());

        LearningSession updatedSession = sessionRepository.save(session);
        log.info("Successfully resumed session: {}", sessionId);

        return mapToDto(updatedSession);
    }

    public LearningSessionDto completeSession(Long sessionId, User user) {
        log.info("Completing session: {} for user: {}", sessionId, user.getUsername());

        LearningSession session = getSessionWithValidation(sessionId, user);
        completeSession(session);

        LearningSession updatedSession = sessionRepository.save(session);
        log.info("Successfully completed session: {}", sessionId);

        return mapToDto(updatedSession);
    }

    @Transactional(readOnly = true)
    public LearningSessionDto getSession(Long sessionId, User user) {
        log.debug("Getting session: {} for user: {}", sessionId, user.getUsername());

        LearningSession session = getSessionWithValidation(sessionId, user);
        return mapToDto(session);
    }

    @Transactional(readOnly = true)
    public Page<LearningSessionDto> getUserSessions(User user, Pageable pageable) {
        log.debug("Getting sessions for user: {}", user.getUsername());

        Page<LearningSession> sessions = sessionRepository.findByUser(user, pageable);
        return sessions.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Optional<LearningSessionDto> getActiveSession(User user) {
        log.debug("Getting active session for user: {}", user.getUsername());

        Optional<LearningSession> activeSession = sessionRepository.findByUserAndStatus(user, SessionStatus.ACTIVE);
        return activeSession.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<LearningSessionDto> getIncompleteSession(User user) {
        log.debug("Getting incomplete sessions for user: {}", user.getUsername());

        List<LearningSession> incompleteSessions = sessionRepository.findIncompleteSessionsByUser(user);
        return incompleteSessions.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionStatsDto getUserStats(User user) {
        log.debug("Getting session statistics for user: {}", user.getUsername());

        SessionStatsDto stats = new SessionStatsDto();

        // Basic counts
        stats.setTotalSessions(sessionRepository.countByUserAndStatus(user, SessionStatus.COMPLETED));
        stats.setTotalCompletedSessions(sessionRepository.countByUserAndStatus(user, SessionStatus.COMPLETED));
        stats.setTotalActiveSessions(sessionRepository.countByUserAndStatus(user, SessionStatus.ACTIVE));
        stats.setTotalPausedSessions(sessionRepository.countByUserAndStatus(user, SessionStatus.PAUSED));

        // Time and accuracy metrics
        stats.setTotalTime(sessionRepository.getTotalLearningTimeByUser(user));
        stats.setAverageAccuracy(sessionRepository.getAverageAccuracyByUser(user));

        // Recent session info
        Optional<LearningSession> lastSession = sessionRepository.findTopByUserOrderByStartTimeDesc(user);
        if (lastSession.isPresent()) {
            stats.setLastSessionDate(lastSession.get().getStartTime());
        }

        // Sessions by mode
        List<Object[]> sessionsByMode = sessionRepository.countSessionsByModeForUser(user);
        Map<LearningMode, Long> modeStats = new HashMap<>();
        for (Object[] result : sessionsByMode) {
            modeStats.put((LearningMode) result[0], (Long) result[1]);
        }
        stats.setSessionsByMode(modeStats);

        return stats;
    }

    private void completeSession(LearningSession session) {
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setIsCompleted(true);

        // Calculate duration
        if (session.getStartTime() != null && session.getEndTime() != null) {
            Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
            session.setDuration(duration.getSeconds());
        }

        // Calculate accuracy
        if (!session.getAnswers().isEmpty()) {
            double accuracy = session.getAccuracyPercentage();
            session.setAccuracy(accuracy);
        }
    }

    private void completeExistingSessions(User user) {
        List<LearningSession> incompleteSessions = sessionRepository.findIncompleteSessionsByUser(user);
        for (LearningSession session : incompleteSessions) {
            completeSession(session);
            sessionRepository.save(session);
        }
    }

    private Set<Word> selectWordsForSession(CreateSessionRequest request, User user) {
        Set<Word> selectedWords = new HashSet<>();

        if (request.getSpecificWordIds() != null && !request.getSpecificWordIds().isEmpty()) {
            // Use specific words
            selectedWords.addAll(wordRepository.findAllById(request.getSpecificWordIds()));
        } else if (request.getVocabularyListId() != null) {
            // Use words from vocabulary list
            VocabularyList vocabularyList = vocabularyListRepository.findById(request.getVocabularyListId())
                .orElseThrow(() -> new ResourceNotFoundException("VocabularyList", "id", request.getVocabularyListId()));
            selectedWords.addAll(vocabularyList.getWords());
        } else {
            // Use words by difficulty and language
            List<Word> candidateWords = wordRepository.findByLanguageAndDifficulty("en", request.getDifficulty());

            // Shuffle and limit the words
            Collections.shuffle(candidateWords);
            int wordCount = Math.min(request.getWordCount(), candidateWords.size());
            selectedWords.addAll(candidateWords.subList(0, wordCount));
        }

        // Shuffle words if requested
        if (request.getShuffleWords() != null && request.getShuffleWords()) {
            List<Word> wordsList = new ArrayList<>(selectedWords);
            Collections.shuffle(wordsList);
            selectedWords = new LinkedHashSet<>(wordsList);
        }

        return selectedWords;
    }

    private LearningSession getSessionWithValidation(Long sessionId, User user) {
        LearningSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("User does not have access to this session");
        }

        return session;
    }

    private LearningSessionDto mapToDto(LearningSession session) {
        LearningSessionDto dto = new LearningSessionDto();
        dto.setId(session.getId());
        dto.setUserId(session.getUser().getId());
        dto.setUsername(session.getUser().getUsername());

        if (session.getVocabularyList() != null) {
            dto.setVocabularyListId(session.getVocabularyList().getId());
            dto.setVocabularyListName(session.getVocabularyList().getName());
        }

        dto.setMode(session.getMode());
        dto.setDifficulty(session.getDifficulty());
        dto.setStatus(session.getStatus());
        dto.setTotalWords(session.getTotalWords());
        dto.setCompletedWords(session.getCompletedWords());
        dto.setCorrectAnswers(session.getCorrectAnswers());
        dto.setCurrentWordIndex(session.getCurrentWordIndex());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setPausedAt(session.getPausedAt());
        dto.setResumedAt(session.getResumedAt());
        dto.setDuration(session.getDuration());
        dto.setAccuracy(session.getAccuracy());
        dto.setIsCompleted(session.getIsCompleted());
        dto.setIsPaused(session.getIsPaused());

        // Session settings
        dto.setAutoAdvance(session.getAutoAdvance());
        dto.setShowDefinitionFirst(session.getShowDefinitionFirst());
        dto.setEnablePronunciation(session.getEnablePronunciation());
        dto.setEnableHints(session.getEnableHints());
        dto.setTimeLimit(session.getTimeLimit());
        dto.setShuffleWords(session.getShuffleWords());
        dto.setRepeatIncorrect(session.getRepeatIncorrect());

        // Calculate derived metrics
        dto.setProgressPercentage(dto.getProgressPercentage());
        dto.setTotalTimeSpent(session.getTotalTimeSpent());
        dto.setAverageTimePerWord(session.getAverageTimePerWord());

        // Map words
        List<LearningWordDto> wordDtos = session.getWords().stream()
            .map(this::mapWordToDto)
            .collect(Collectors.toList());
        dto.setWords(wordDtos);

        return dto;
    }

    private LearningWordDto mapWordToDto(Word word) {
        LearningWordDto dto = new LearningWordDto();
        dto.setId(word.getId());
        dto.setWord(word.getWord());
        dto.setDefinition(word.getDefinition());
        dto.setPronunciation(word.getPronunciation());
        dto.setExample(word.getExample());
        dto.setDifficulty(word.getDifficulty());

        // Initialize learning-specific fields with default values
        dto.setAttempts(0);
        dto.setCorrectAttempts(0);
        dto.setIsCompleted(false);
        dto.setTimeSpent(0L);
        dto.setPerformanceCategory("NOT_ATTEMPTED");
        dto.setAccuracyScore(0.0);

        return dto;
    }

    private LearningAnswerDto mapAnswerToDto(LearningAnswer answer) {
        LearningAnswerDto dto = new LearningAnswerDto();
        dto.setId(answer.getId());
        dto.setSessionId(answer.getSession().getId());
        dto.setWordId(answer.getWord().getId());
        dto.setWordText(answer.getWord().getWord());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setUserAnswer(answer.getUserAnswer());
        dto.setCorrectAnswer(answer.getCorrectAnswer());
        dto.setTimeSpent(answer.getTimeSpent());
        dto.setAnsweredAt(answer.getAnsweredAt());
        dto.setAttemptNumber(answer.getAttemptNumber());
        dto.setHintUsed(answer.getHintUsed());
        dto.setPronunciationPlayed(answer.getPronunciationPlayed());
        dto.setConfidenceLevel(answer.getConfidenceLevel());
        dto.setDifficultyRating(answer.getDifficultyRating());
        dto.setAccuracyScore(answer.getAccuracyScore());
        dto.setPerformanceCategory(answer.getPerformanceCategory());

        return dto;
    }
}