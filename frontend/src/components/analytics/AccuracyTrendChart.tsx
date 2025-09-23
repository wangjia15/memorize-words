import React, { useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { format } from 'date-fns';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ProgressStatistics, TrendDirection } from '@/types/analytics';
import { analyticsUtils } from '@/services/analyticsApi';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface AccuracyTrendChartProps {
  data: ProgressStatistics;
  showMovingAverage?: boolean;
  showConfidenceBands?: boolean;
  className?: string;
}

export const AccuracyTrendChart: React.FC<AccuracyTrendChartProps> = ({
  data,
  showMovingAverage = true,
  showConfidenceBands = false,
  className
}) => {
  const chartData = useMemo(() => {
    const dailyMetrics = data.dailyMetrics || [];
    const labels = dailyMetrics.map(m => format(new Date(m.date), 'MMM dd'));

    const accuracyData = dailyMetrics.map(m => m.accuracy);
    const movingAverage = calculateMovingAverage(accuracyData, 7);
    const upperBand = showConfidenceBands ? calculateUpperConfidenceBand(accuracyData) : [];
    const lowerBand = showConfidenceBands ? calculateLowerConfidenceBand(accuracyData) : [];

    const datasets = [
      {
        label: 'Daily Accuracy',
        data: accuracyData,
        borderColor: 'rgb(34, 197, 94)',
        backgroundColor: 'rgba(34, 197, 94, 0.1)',
        borderWidth: 2,
        fill: showConfidenceBands ? false : true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 5,
        pointBackgroundColor: 'rgb(34, 197, 94)',
        pointBorderColor: '#fff',
        pointBorderWidth: 2
      }
    ];

    if (showMovingAverage && movingAverage.length > 0) {
      datasets.push({
        label: '7-Day Moving Average',
        data: movingAverage,
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'transparent',
        borderWidth: 3,
        fill: false,
        tension: 0.4,
        pointRadius: 0,
        pointHoverRadius: 0,
        borderDash: [8, 4]
      });
    }

    if (showConfidenceBands && upperBand.length > 0 && lowerBand.length > 0) {
      datasets.push(
        {
          label: 'Upper Confidence',
          data: upperBand,
          borderColor: 'rgba(156, 163, 175, 0.5)',
          backgroundColor: 'rgba(156, 163, 175, 0.1)',
          borderWidth: 1,
          fill: '+1',
          pointRadius: 0,
          pointHoverRadius: 0
        },
        {
          label: 'Lower Confidence',
          data: lowerBand,
          borderColor: 'rgba(156, 163, 175, 0.5)',
          backgroundColor: 'rgba(156, 163, 175, 0.1)',
          borderWidth: 1,
          fill: false,
          pointRadius: 0,
          pointHoverRadius: 0
        }
      );
    }

    return { labels, datasets };
  }, [data, showMovingAverage, showConfidenceBands]);

  const options = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      intersect: false,
      mode: 'index' as const
    },
    plugins: {
      legend: {
        position: 'top' as const,
        labels: {
          usePointStyle: true,
          padding: 20,
          font: {
            size: 12,
            weight: '500'
          }
        }
      },
      title: {
        display: false
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        borderWidth: 1,
        cornerRadius: 8,
        padding: 12,
        callbacks: {
          title: (context: any) => {
            const metric = data.dailyMetrics[context[0].dataIndex];
            return format(new Date(metric.date), 'MMMM dd, yyyy');
          },
          label: (context: any) => {
            const value = context.parsed.y;
            const label = context.dataset.label;

            if (label.includes('Accuracy') || label.includes('Average')) {
              return `${label}: ${value.toFixed(1)}%`;
            }
            return `${label}: ${value.toFixed(1)}%`;
          },
          afterLabel: (context: any) => {
            if (context.datasetIndex === 0) {
              const value = context.parsed.y;
              const prevValue = context.dataIndex > 0
                ? context.dataset.data[context.dataIndex - 1]
                : value;

              const change = value - prevValue;

              if (Math.abs(change) > 1) {
                const direction = change >= 0 ? '↑' : '↓';
                return [
                  '',
                  `${direction} ${Math.abs(change).toFixed(1)}% from previous day`
                ];
              }
            }
            return [];
          }
        }
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        ticks: {
          maxRotation: 45,
          minRotation: 0,
          font: {
            size: 11
          }
        }
      },
      y: {
        beginAtZero: true,
        max: 100,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        },
        ticks: {
          font: {
            size: 11
          },
          callback: (value: any) => `${value}%`
        },
        title: {
          display: true,
          text: 'Accuracy (%)',
          font: {
            size: 12,
            weight: '500'
          }
        }
      }
    },
    animation: {
      duration: 1000,
      easing: 'easeInOutQuart'
    }
  }), [data]);

  const trendAnalysis = useMemo(() => {
    if (!data.performanceTrends) return null;

    const trend = data.performanceTrends;
    const confidence = trend.confidenceScore || 0;
    const direction = trend.trendDirection || TrendDirection.STABLE;

    return {
      direction,
      confidence,
      trendValue: trend.accuracyTrend || 0,
      description: getTrendDescription(direction, trend.accuracyTrend || 0, confidence)
    };
  }, [data]);

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Accuracy Trend Analysis</span>
          {trendAnalysis && (
            <Badge variant={
              trendAnalysis.direction === TrendDirection.IMPROVING ? 'default' :
              trendAnalysis.direction === TrendDirection.DECLINING ? 'destructive' : 'secondary'
            }>
              {trendAnalysis.direction.replace('_', ' ').toUpperCase()}
            </Badge>
          )}
        </CardTitle>
        <div className="space-y-2">
          <p className="text-sm text-muted-foreground">
            Track your learning accuracy over time with trend analysis
          </p>
          {trendAnalysis && (
            <div className="text-xs text-muted-foreground">
              {trendAnalysis.description}
            </div>
          )}
        </div>
      </CardHeader>
      <CardContent>
        <div className="h-80 w-full">
          <Line data={chartData} options={options} />
        </div>

        {trendAnalysis && (
          <div className="mt-4 grid grid-cols-3 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-green-600">
                {analyticsUtils.formatAccuracy(data.averageAccuracy)}
              </div>
              <div className="text-xs text-muted-foreground">Current Average</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-blue-600">
                {trendAnalysis.trendValue >= 0 ? '+' : ''}{(trendAnalysis.trendValue * 100).toFixed(2)}%
              </div>
              <div className="text-xs text-muted-foreground">Trend Rate</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">
                {(trendAnalysis.confidence * 100).toFixed(0)}%
              </div>
              <div className="text-xs text-muted-foreground">Confidence</div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

// Helper functions
function calculateMovingAverage(data: number[], window: number): number[] {
  const result: number[] = [];
  for (let i = 0; i < data.length; i++) {
    const start = Math.max(0, i - Math.floor(window / 2));
    const end = Math.min(data.length, i + Math.ceil(window / 2));
    const windowData = data.slice(start, end);
    const average = windowData.reduce((sum, val) => sum + val, 0) / windowData.length;
    result.push(average);
  }
  return result;
}

function calculateUpperConfidenceBand(data: number[]): number[] {
  const movingAverage = calculateMovingAverage(data, 7);
  const standardDeviation = calculateStandardDeviation(data);

  return movingAverage.map((avg, i) => {
    // 95% confidence interval (2 standard deviations)
    const bandWidth = (2 * standardDeviation) / Math.sqrt(Math.min(i + 1, 7));
    return Math.min(100, avg + bandWidth);
  });
}

function calculateLowerConfidenceBand(data: number[]): number[] {
  const movingAverage = calculateMovingAverage(data, 7);
  const standardDeviation = calculateStandardDeviation(data);

  return movingAverage.map((avg, i) => {
    // 95% confidence interval (2 standard deviations)
    const bandWidth = (2 * standardDeviation) / Math.sqrt(Math.min(i + 1, 7));
    return Math.max(0, avg - bandWidth);
  });
}

function calculateStandardDeviation(data: number[]): number {
  const mean = data.reduce((sum, val) => sum + val, 0) / data.length;
  const squaredDifferences = data.map(val => Math.pow(val - mean, 2));
  const avgSquaredDiff = squaredDifferences.reduce((sum, val) => sum + val, 0) / data.length;
  return Math.sqrt(avgSquaredDiff);
}

function getTrendDescription(
  direction: TrendDirection,
  trendValue: number,
  confidence: number
): string {
  if (confidence < 0.5) {
    return 'Insufficient data for reliable trend analysis';
  }

  const magnitude = Math.abs(trendValue * 100);
  const confidenceLevel = confidence >= 0.8 ? 'High' : confidence >= 0.6 ? 'Medium' : 'Low';

  switch (direction) {
    case TrendDirection.IMPROVING:
      if (magnitude > 2) {
        return `Strong improvement trend (${magnitude.toFixed(1)}% increase) with ${confidenceLevel} confidence`;
      } else if (magnitude > 0.5) {
        return `Moderate improvement trend (${magnitude.toFixed(1)}% increase) with ${confidenceLevel} confidence`;
      } else {
        return `Slight improvement trend (${magnitude.toFixed(1)}% increase) with ${confidenceLevel} confidence`;
      }

    case TrendDirection.DECLINING:
      if (magnitude > 2) {
        return `Concerning decline trend (${magnitude.toFixed(1)}% decrease) with ${confidenceLevel} confidence`;
      } else if (magnitude > 0.5) {
        return `Moderate decline trend (${magnitude.toFixed(1)}% decrease) with ${confidenceLevel} confidence`;
      } else {
        return `Slight decline trend (${magnitude.toFixed(1)}% decrease) with ${confidenceLevel} confidence`;
      }

    case TrendDirection.STABLE:
    default:
      return `Stable performance with ${confidenceLevel} confidence`;
  }
}

export default AccuracyTrendChart;