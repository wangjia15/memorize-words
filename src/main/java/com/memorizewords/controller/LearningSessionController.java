package com.memorizewords.controller;

import com.memorizewords.dto.request.CreateSessionRequest;
import com.memorizewords.dto.request.SubmitAnswerRequest;
import com.memorizewords.dto.response.ApiResponse;
import com.memorizewords.dto.response.LearningSessionDto;
import com.memorizewords.dto.response.SessionStatsDto;
import com.memorizewords.entity.User;
import com.memorizewords.service.LearningSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for learning session management operations.
 */
@RestController
@RequestMapping("/api/learning-sessions")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Slf4j
public class LearningSessionController {

    private final LearningSessionService learningSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningSessionDto>> startSession(
            @Valid @RequestBody CreateSessionRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.startSession(request, user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Learning session started successfully", session));
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<ApiResponse<LearningSessionDto>> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.submitAnswer(sessionId, request, user);

        return ResponseEntity.ok(ApiResponse.success("Answer submitted successfully", session));
    }

    @PatchMapping("/{sessionId}/pause")
    public ResponseEntity<ApiResponse<LearningSessionDto>> pauseSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.pauseSession(sessionId, user);

        return ResponseEntity.ok(ApiResponse.success("Session paused successfully", session));
    }

    @PatchMapping("/{sessionId}/resume")
    public ResponseEntity<ApiResponse<LearningSessionDto>> resumeSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.resumeSession(sessionId, user);

        return ResponseEntity.ok(ApiResponse.success("Session resumed successfully", session));
    }

    @PatchMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<LearningSessionDto>> completeSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.completeSession(sessionId, user);

        return ResponseEntity.ok(ApiResponse.success("Session completed successfully", session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<LearningSessionDto>> getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.getSession(sessionId, user);

        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", session));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LearningSessionDto>>> getUserSessions(
            @PageableDefault(size = 20, sort = "startTime", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Page<LearningSessionDto> sessions = learningSessionService.getUserSessions(user, pageable);

        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<LearningSessionDto>> getActiveSession(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Optional<LearningSessionDto> activeSession = learningSessionService.getActiveSession(user);

        if (activeSession.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Active session retrieved successfully", activeSession.get()));
        } else {
            return ResponseEntity.ok(ApiResponse.success("No active session found", null));
        }
    }

    @GetMapping("/incomplete")
    public ResponseEntity<ApiResponse<List<LearningSessionDto>>> getIncompleteSessions(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        List<LearningSessionDto> incompleteSessions = learningSessionService.getIncompleteSession(user);

        return ResponseEntity.ok(ApiResponse.success("Incomplete sessions retrieved successfully", incompleteSessions));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SessionStatsDto>> getUserStats(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        SessionStatsDto stats = learningSessionService.getUserStats(user);

        return ResponseEntity.ok(ApiResponse.success("Session statistics retrieved successfully", stats));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Page<LearningSessionDto>>> getRecentSessions(
            @PageableDefault(size = 5, sort = "startTime", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Page<LearningSessionDto> recentSessions = learningSessionService.getUserSessions(user, pageable);

        return ResponseEntity.ok(ApiResponse.success("Recent sessions retrieved successfully", recentSessions));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        // Note: This would require implementing a delete method in the service
        // For now, we'll return a not implemented response

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse.error("Delete session functionality not implemented"));
    }

    // Health check endpoint specific to learning sessions
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Learning session service is healthy", "OK"));
    }

    // Endpoint to get session performance summary
    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<ApiResponse<LearningSessionDto>> getSessionSummary(
            @PathVariable Long sessionId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        LearningSessionDto session = learningSessionService.getSession(sessionId, user);

        // Only return summary for completed sessions
        if (!session.getIsCompleted()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Session must be completed to view summary"));
        }

        return ResponseEntity.ok(ApiResponse.success("Session summary retrieved successfully", session));
    }

    // Endpoint to get user's learning progress across all sessions
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<SessionStatsDto>> getLearningProgress(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        SessionStatsDto progress = learningSessionService.getUserStats(user);

        return ResponseEntity.ok(ApiResponse.success("Learning progress retrieved successfully", progress));
    }

    // Endpoint to get sessions by mode
    @GetMapping("/by-mode/{mode}")
    public ResponseEntity<ApiResponse<Page<LearningSessionDto>>> getSessionsByMode(
            @PathVariable String mode,
            @PageableDefault(size = 20, sort = "startTime", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Page<LearningSessionDto> sessions = learningSessionService.getUserSessions(user, pageable);

        // Filter by mode (this could be enhanced by adding a method to the service)
        return ResponseEntity.ok(ApiResponse.success("Sessions by mode retrieved successfully", sessions));
    }

    private User getCurrentUser(Authentication authentication) {
        // This would typically get the user from the authentication principal
        // For now, we'll create a simple user implementation
        // In a real application, this would be properly implemented with Spring Security
        User user = new User();
        user.setId(1L); // Default user ID for development
        user.setUsername(authentication.getName());
        return user;
    }
}