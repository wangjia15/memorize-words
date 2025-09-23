package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for review trend analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTrendDTO {

    private BigDecimal accuracyTrend;

    private BigDecimal retentionTrend;

    private BigDecimal speedTrend;

    private BigDecimal consistencyTrend;

    private String overallTrend; // IMPROVING, STABLE, DECLINING

    private Integer trendPeriodDays;

    private BigDecimal trendStrength;

    private String prediction; // CONTINUE_IMPROVE, MAINTAIN, NEEDS_ATTENTION
}