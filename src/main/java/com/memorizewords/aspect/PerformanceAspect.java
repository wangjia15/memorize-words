package com.memorizewords.aspect;

import com.memorizewords.service.PerformanceMonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final PerformanceMonitoringService monitoringService;
    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, AtomicLong> methodCallCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodErrorCounts = new ConcurrentHashMap<>();

    @Autowired
    public PerformanceAspect(PerformanceMonitoringService monitoringService, MeterRegistry meterRegistry) {
        this.monitoringService = monitoringService;
        this.meterRegistry = meterRegistry;
    }

    // Pointcut for all controller methods
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    // Pointcut for all service methods
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}

    // Pointcut for all repository methods
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethods() {}

    // Pointcut for methods annotated with @Timed
    @Pointcut("@annotation(io.micrometer.core.annotation.Timed)")
    public void timedMethods() {}

    // Pointcut for slow query detection (methods that might execute database queries)
    @Pointcut("execution(* com.memorizewords.repository.*.*(..)) || " +
               "execution(* com.memorizewords.service.*.*(..))")
    public void databaseOperations() {}

    // Monitor all controller methods
    @Around("controllerMethods()")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Record successful API call
            recordApiCall(methodName, "success", duration);

            // Log slow controller methods
            if (duration.toMillis() > 1000) {
                log.warn("Slow controller method: {} took {}ms", methodName, duration.toMillis());
            }

            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Record failed API call
            recordApiCall(methodName, "error", duration);

            // Increment error counter
            methodErrorCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();

            log.error("Controller method failed: {} after {}ms", methodName, duration.toMillis(), e);
            throw e;
        }
    }

    // Monitor service methods with detailed metrics
    @Around("serviceMethods()")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        Timer.Sample sample = Timer.start();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Stop timer with tags
            sample.stop(Timer.builder("service.method.execution")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));

            // Record method call count
            methodCallCounts.computeIfAbsent(fullMethodName, k -> new AtomicLong(0)).incrementAndGet();

            // Log slow service methods
            if (duration.toMillis() > 500) {
                log.warn("Slow service method: {} took {}ms", fullMethodName, duration.toMillis());

                // Record slow service method metric
                Counter.builder("service.methods.slow")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .increment();
            }

            // Record result size metrics for collections
            if (result instanceof Collection) {
                int resultSize = ((Collection<?>) result).size();
                Gauge.builder("service.method.result.size")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .set(resultSize);

                // Log large result sets
                if (resultSize > 1000) {
                    log.warn("Large result set from service method: {} returned {} items",
                        fullMethodName, resultSize);
                }
            }

            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Stop timer with error tag
            sample.stop(Timer.builder("service.method.execution")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "error")
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry));

            // Record error
            methodErrorCounts.computeIfAbsent(fullMethodName, k -> new AtomicLong(0)).incrementAndGet();

            log.error("Service method failed: {} after {}ms", fullMethodName, duration.toMillis(), e);
            throw e;
        }
    }

    // Monitor repository methods for database performance
    @Around("repositoryMethods()")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        Timer.Sample sample = Timer.start();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Stop timer
            sample.stop(Timer.builder("repository.query.execution")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));

            // Check for slow queries
            if (duration.toMillis() > 200) {
                log.warn("Slow repository query: {} took {}ms",
                    className + "." + methodName, duration.toMillis());

                // Record slow query
                monitoringService.recordSlowQuery(
                    className + "." + methodName,
                    getQueryString(joinPoint),
                    duration
                );
            }

            // Record result size for collections
            if (result instanceof Collection) {
                int resultSize = ((Collection<?>) result).size();
                Gauge.builder("repository.query.result.size")
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .set(resultSize);
            }

            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Stop timer with error
            sample.stop(Timer.builder("repository.query.execution")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "error")
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry));

            log.error("Repository query failed: {} after {}ms",
                className + "." + methodName, duration.toMillis(), e);
            throw e;
        }
    }

    // Monitor methods annotated with @Timed
    @Around("timedMethods()")
    public Object monitorTimedMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.annotation.Timed timed =
            ((io.micrometer.core.annotation.Timed) joinPoint.getSignature().getAnnotations()[0]);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String description = timed.description().isEmpty() ?
            "Execution time for " + className + "." + methodName : timed.description();

        Timer.Sample sample = Timer.start();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Create timer with custom tags
            Timer.Builder timerBuilder = Timer.builder(description)
                .tag("class", className)
                .tag("method", methodName);

            // Add extra tags from annotation
            if (timed.extraTags().length > 0) {
                for (int i = 0; i < timed.extraTags().length; i += 2) {
                    if (i + 1 < timed.extraTags().length) {
                        timerBuilder.tag(timed.extraTags()[i], timed.extraTags()[i + 1]);
                    }
                }
            }

            sample.stop(timerBuilder.register(meterRegistry));

            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder(description)
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "error")
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry));

            throw e;
        }
    }

    // Monitor all database operations for performance
    @Around("databaseOperations()")
    public Object monitorDatabaseOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationName = joinPoint.getSignature().toShortString();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Record database operation duration
            Timer.builder("database.operation.duration")
                .tag("operation", operationName)
                .tag("status", "success")
                .register(meterRegistry)
                .record(duration);

            // Check for very slow database operations
            if (duration.toMillis() > 1000) {
                log.warn("Very slow database operation: {} took {}ms", operationName, duration.toMillis());

                // Record slow database operation
                Counter.builder("database.operations.slow")
                    .tag("operation", operationName)
                    .register(meterRegistry)
                    .increment();
            }

            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            Timer.builder("database.operation.duration")
                .tag("operation", operationName)
                .tag("status", "error")
                .register(meterRegistry)
                .record(duration);

            throw e;
        }
    }

    // Performance monitoring for specific business operations
    @Around("execution(* com.memorizewords.service..*startReviewSession(..)) || " +
            "execution(* com.memorizewords.service..*submitReview(..)) || " +
            "execution(* com.memorizewords.service..*calculateProgress(..))")
    public Object monitorBusinessOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationName = joinPoint.getSignature().getName();
        Timer.Sample sample = Timer.start();
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Record business operation metrics
            sample.stop(Timer.builder("business.operation.duration")
                .tag("operation", operationName)
                .tag("status", "success")
                .register(meterRegistry));

            // Record business event
            monitoringService.recordReviewSessionEvent(operationName + "_completed", duration);

            return result;
        } catch (Exception e) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            sample.stop(Timer.builder("business.operation.duration")
                .tag("operation", operationName)
                .tag("status", "error")
                .register(meterRegistry));

            throw e;
        }
    }

    // Helper method to get current HTTP request
    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to record API calls
    private void recordApiCall(String methodName, String status, Duration duration) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            int statusCode = status.equals("success") ? 200 : 500;

            monitoringService.recordApiCall(endpoint, method, statusCode, duration);
        }
    }

    // Helper method to extract query string (simplified)
    private String getQueryString(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.getSignature().toShortString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    // Get performance statistics
    public long getMethodCallCount(String methodName) {
        return methodCallCounts.getOrDefault(methodName, new AtomicLong(0)).get();
    }

    public long getMethodErrorCount(String methodName) {
        return methodErrorCounts.getOrDefault(methodName, new AtomicLong(0)).get();
    }

    public double getMethodErrorRate(String methodName) {
        long calls = getMethodCallCount(methodName);
        long errors = getMethodErrorCount(methodName);
        return calls > 0 ? (double) errors / calls * 100 : 0.0;
    }
}