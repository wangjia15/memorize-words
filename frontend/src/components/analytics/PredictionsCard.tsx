import React, { useMemo } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { LearningPrediction, RiskFactor, RiskSeverity } from '@/types/analytics';
import { format } from 'date-fns';
import {
  Brain,
  TrendingUp,
  Calendar,
  Target,
  AlertTriangle,
  CheckCircle,
  Clock,
  Zap
} from 'lucide-react';

interface PredictionsCardProps {
  predictions: LearningPrediction;
  className?: string;
}

export const PredictionsCard: React.FC<PredictionsCardProps> = ({
  predictions,
  className
}) => {
  const assessment = useMemo(() => {
    const { confidenceLevel, predictedAccuracy, predictedWordsInMonth } = predictions;

    let assessment = {
      level: 'Conservative',
      color: 'blue',
      description: 'Steady, sustainable progress',
      icon: TrendingUp
    };

    if (confidenceLevel >= 0.8 && predictedAccuracy >= 85 && predictedWordsInMonth >= 200) {
      assessment = {
        level: 'Optimistic',
        color: 'green',
        description: 'Excellent growth potential',
        icon: Zap
      };
    } else if (confidenceLevel >= 0.6 && predictedAccuracy >= 75 && predictedWordsInMonth >= 100) {
      assessment = {
        level: 'Realistic',
        color: 'purple',
        description: 'Achievable with consistent effort',
        icon: Target
      };
    } else if (confidenceLevel < 0.5 || predictedAccuracy < 60) {
      assessment = {
        level: 'Needs Attention',
        color: 'orange',
        description: 'Requires strategy adjustment',
        icon: AlertTriangle
      };
    }

    return assessment;
  }, [predictions]);

  const riskLevel = useMemo(() => {
    const highRisks = predictions.riskFactors?.filter(r => r.severity === RiskSeverity.HIGH) || [];
    const mediumRisks = predictions.riskFactors?.filter(r => r.severity === RiskSeverity.MEDIUM) || [];

    if (highRisks.length >= 2) {
      return { level: 'High', color: 'red', count: highRisks.length };
    } else if (highRisks.length === 1 || mediumRisks.length >= 2) {
      return { level: 'Medium', color: 'orange', count: highRisks.length + mediumRisks.length };
    } else if (mediumRisks.length === 1) {
      return { level: 'Low', color: 'yellow', count: 1 };
    } else {
      return { level: 'Minimal', color: 'green', count: 0 };
    }
  }, [predictions]);

  const timeToMastery = useMemo(() => {
    const days = predictions.estimatedMasteryTime;
    if (days < 30) {
      return { text: `${days} days`, color: 'green' };
    } else if (days < 90) {
      return { text: `${Math.round(days / 7)} weeks`, color: 'blue' };
    } else if (days < 365) {
      return { text: `${Math.round(days / 30)} months`, color: 'orange' };
    } else {
      return { text: `${(days / 365).toFixed(1)} years`, color: 'red' };
    }
  }, [predictions]);

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span className="flex items-center gap-2">
            <Brain className="w-5 h-5" />
            Learning Predictions
          </span>
          <div className="flex items-center gap-2">
            <Badge variant={assessment.color as any}>
              {assessment.level}
            </Badge>
            <Badge variant="outline">
              {(predictions.confidenceLevel * 100).toFixed(0)}% confidence
            </Badge>
          </div>
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          AI-powered insights based on your learning patterns and performance
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          {/* Prediction Overview */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center p-3 bg-muted rounded-lg">
              <div className="text-2xl font-bold text-blue-600">
                {predictions.predictedWordsInMonth}
              </div>
              <div className="text-xs text-muted-foreground">Words This Month</div>
              <Progress
                value={Math.min(100, (predictions.predictedWordsInMonth / 300) * 100)}
                className="mt-2 h-2"
              />
            </div>
            <div className="text-center p-3 bg-muted rounded-lg">
              <div className="text-2xl font-bold text-green-600">
                {predictions.predictedAccuracy.toFixed(1)}%
              </div>
              <div className="text-xs text-muted-foreground">Predicted Accuracy</div>
              <Progress
                value={predictions.predictedAccuracy}
                className="mt-2 h-2"
              />
            </div>
            <div className="text-center p-3 bg-muted rounded-lg">
              <div className={`text-2xl font-bold text-${timeToMastery.color}-600`}>
                {timeToMastery.text}
              </div>
              <div className="text-xs text-muted-foreground">To Mastery</div>
              <div className="text-xs text-muted-foreground mt-1">
                {predictions.estimatedMasteryTime} days
              </div>
            </div>
          </div>

          {/* Assessment */}
          <div className="p-3 border rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <assessment.icon className="w-4 h-4" />
              <span className="text-sm font-medium">Growth Assessment</span>
            </div>
            <p className="text-sm text-muted-foreground">
              {assessment.description}
            </p>
          </div>

          {/* Daily Goal */}
          <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium">Recommended Daily Goal</span>
              <span className="text-lg font-bold text-blue-600">
                {predictions.recommendedDailyGoal} words
              </span>
            </div>
            <p className="text-xs text-muted-foreground">
              Based on your current pace and optimal learning efficiency
            </p>
            <Button size="sm" className="mt-2 w-full">
              <Target className="w-4 h-4 mr-2" />
              Set as Goal
            </Button>
          </div>

          {/* Risk Factors */}
          {predictions.riskFactors && predictions.riskFactors.length > 0 && (
            <div>
              <div className="flex items-center justify-between mb-3">
                <h4 className="text-sm font-medium">Risk Factors</h4>
                <Badge variant={riskLevel.color as any}>
                  {riskLevel.level} Risk ({riskLevel.count} factors)
                </Badge>
              </div>
              <div className="space-y-2">
                {predictions.riskFactors.map((risk, index) => (
                  <RiskFactorCard key={index} risk={risk} />
                ))}
              </div>
            </div>
          )}

          {/* Action Items */}
          <div>
            <h4 className="text-sm font-medium mb-3">Recommended Actions</h4>
            <div className="space-y-2">
              <ActionItem
                icon={Clock}
                title="Maintain Consistency"
                description="Study at the same time daily to build habits"
              />
              <ActionItem
                icon={TrendingUp}
                title="Review Regularly"
                description="Schedule review sessions to improve retention"
              />
              {riskLevel.level === 'High' && (
                <ActionItem
                  icon={AlertTriangle}
                  title="Address Risk Factors"
                  description="Focus on mitigating high-priority risks"
                />
              )}
            </div>
          </div>

          {/* Confidence Disclaimer */}
          <div className="text-xs text-muted-foreground text-center p-2 border-t">
            Predictions are based on historical data and may vary. Continue tracking your progress for more accurate forecasts.
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

const RiskFactorCard: React.FC<{ risk: RiskFactor }> = ({ risk }) => {
  const getSeverityColor = (severity: RiskSeverity) => {
    switch (severity) {
      case RiskSeverity.HIGH:
        return 'text-red-600 bg-red-50 border-red-200';
      case RiskSeverity.MEDIUM:
        return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      case RiskSeverity.LOW:
        return 'text-blue-600 bg-blue-50 border-blue-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const getSeverityIcon = (severity: RiskSeverity) => {
    switch (severity) {
      case RiskSeverity.HIGH:
        return <AlertTriangle className="w-4 h-4" />;
      case RiskSeverity.MEDIUM:
        return <Clock className="w-4 h-4" />;
      case RiskSeverity.LOW:
        return <CheckCircle className="w-4 h-4" />;
      default:
        return <AlertTriangle className="w-4 h-4" />;
    }
  };

  return (
    <div className={`p-3 rounded-lg border ${getSeverityColor(risk.severity)}`}>
      <div className="flex items-start gap-2">
        <div className="flex-shrink-0 mt-0.5">
          {getSeverityIcon(risk.severity)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="text-sm font-medium mb-1">
            {formatRiskTypeName(risk.type)}
          </div>
          <p className="text-xs mb-2">{risk.description}</p>
          <div className="text-xs font-medium">
            💡 {risk.recommendation}
          </div>
        </div>
        <Badge variant="outline" className="text-xs">
          {risk.severity.toUpperCase()}
        </Badge>
      </div>
    </div>
  );
};

const ActionItem: React.FC<{
  icon: React.ComponentType<{ className?: string }>;
  title: string;
  description: string;
}> = ({ icon: Icon, title, description }) => {
  return (
    <div className="flex items-start gap-2 p-2 bg-muted rounded-lg">
      <Icon className="w-4 h-4 mt-0.5 text-muted-foreground" />
      <div className="flex-1">
        <div className="text-sm font-medium">{title}</div>
        <div className="text-xs text-muted-foreground">{description}</div>
      </div>
    </div>
  );
};

function formatRiskTypeName(type: string): string {
  switch (type) {
    case 'streak_break':
      return 'Streak Break Risk';
    case 'accuracy_decline':
      return 'Accuracy Decline';
    case 'burnout':
      return 'Burnout Risk';
    case 'inconsistent_schedule':
      return 'Inconsistent Schedule';
    case 'high_difficulty_load':
      return 'High Difficulty Load';
    default:
      return type.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase());
  }
}

export default PredictionsCard;