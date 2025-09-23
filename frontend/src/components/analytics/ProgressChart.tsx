import React, { useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line, Bar } from 'react-chartjs-2';
import { format } from 'date-fns';
import { cn } from '@/lib/utils';
import {
  ProgressStatistics,
  Timeframe,
  MetricType,
  ChartProps
} from '@/types/analytics';
import { analyticsUtils } from '@/services/analyticsApi';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface ProgressChartProps extends ChartProps {
  data: ProgressStatistics;
  chartType?: 'line' | 'bar';
  showTrendLine?: boolean;
  showAnnotations?: boolean;
}

export const ProgressChart: React.FC<ProgressChartProps> = ({
  data,
  timeframe,
  metric,
  chartType = 'line',
  showTrendLine = false,
  showAnnotations = false,
  className
}) => {
  const chartData = useMemo(() => {
    const metrics = timeframe === Timeframe.DAILY
      ? data.dailyMetrics
      : timeframe === Timeframe.WEEKLY
        ? data.weeklyMetrics
        : data.monthlyMetrics;

    const labels = metrics.map(m => {
      if (timeframe === Timeframe.DAILY) {
        return format(new Date(m.date), 'MMM dd');
      } else if (timeframe === Timeframe.WEEKLY) {
        return `Week ${format(new Date(m.weekStart), 'MMM dd')}`;
      } else {
        return format(new Date(m.month || m.date), 'MMM yyyy');
      }
    });

    const values = metrics.map(m => {
      switch (metric) {
        case MetricType.ACCURACY:
          return m.accuracy || m.averageAccuracy || 0;
        case MetricType.REVIEWS:
          return m.reviewsCompleted || m.totalReviews || 0;
        case MetricType.STUDY_TIME:
          return m.studyTime || m.totalStudyTime || 0;
        case MetricType.WORDS_ADDED:
          return m.wordsAdded || m.totalWords || 0;
        case MetricType.WORDS_LEARNED:
          return m.wordsLearned || 0;
        case MetricType.WORDS_MASTERED:
          return m.wordsMastered || 0;
        default:
          return 0;
      }
    });

    const colors = analyticsUtils.getChartColors(metric);

    const datasets = [
      {
        label: getMetricLabel(metric),
        data: values,
        borderColor: colors.primary,
        backgroundColor: chartType === 'line'
          ? `${colors.primary}20`
          : `${colors.primary}80`,
        borderWidth: 2,
        fill: chartType === 'line',
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: colors.primary,
        pointBorderColor: '#fff',
        pointBorderWidth: 2
      }
    ];

    // Add trend line if requested
    if (showTrendLine && values.length > 1) {
      const trendLine = calculateTrendLine(values);
      datasets.push({
        label: 'Trend',
        data: trendLine,
        borderColor: 'rgb(239, 68, 68)',
        backgroundColor: 'transparent',
        borderWidth: 2,
        borderDash: [5, 5],
        fill: false,
        tension: 0,
        pointRadius: 0,
        pointHoverRadius: 0
      });
    }

    return {
      labels,
      datasets
    };
  }, [data, timeframe, metric, chartType, showTrendLine]);

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
        display: true,
        text: `${getMetricLabel(metric)} Over Time (${timeframe})`,
        font: {
          size: 16,
          weight: '600'
        },
        padding: {
          top: 10,
          bottom: 20
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        borderWidth: 1,
        cornerRadius: 8,
        padding: 12,
        displayColors: true,
        callbacks: {
          title: (context: any) => {
            return context[0].label;
          },
          label: (context: any) => {
            const value = context.parsed.y;
            const unit = getMetricUnit(metric);
            const label = context.dataset.label;

            if (metric === MetricType.ACCURACY) {
              return `${label}: ${value.toFixed(1)}${unit}`;
            }

            return `${label}: ${value}${unit}`;
          },
          afterLabel: (context: any) => {
            if (showAnnotations && context.datasetIndex === 0) {
              const value = context.parsed.y;
              const prevValue = context.dataIndex > 0
                ? context.dataset.data[context.dataIndex - 1]
                : value;

              const change = value - prevValue;
              const changePercent = prevValue > 0 ? (change / prevValue) * 100 : 0;

              if (Math.abs(changePercent) > 1) {
                return [
                  '',
                  `${changePercent >= 0 ? '↑' : '↓'} ${Math.abs(changePercent).toFixed(1)}% from previous`
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
        beginAtZero: metric !== MetricType.ACCURACY,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        },
        ticks: {
          font: {
            size: 11
          },
          callback: (value: any) => {
            if (metric === MetricType.ACCURACY) {
              return `${value}%`;
            }
            return value;
          }
        },
        title: {
          display: true,
          text: getMetricLabel(metric),
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
  }), [metric, timeframe, showAnnotations]);

  const ChartComponent = chartType === 'line' ? Line : Bar;

  return (
    <div className={cn('w-full h-96', className)}>
      <ChartComponent data={chartData} options={options} />
    </div>
  );
};

// Helper functions
function getMetricLabel(metric: MetricType): string {
  const labels = {
    [MetricType.ACCURACY]: 'Accuracy',
    [MetricType.REVIEWS]: 'Reviews Completed',
    [MetricType.STUDY_TIME]: 'Study Time (minutes)',
    [MetricType.WORDS_ADDED]: 'Words Added',
    [MetricType.WORDS_LEARNED]: 'Words Learned',
    [MetricType.WORDS_MASTERED]: 'Words Mastered',
    [MetricType.STREAK]: 'Streak Days',
    [MetricType.RETENTION]: 'Retention Rate'
  };

  return labels[metric] || 'Metric';
}

function getMetricUnit(metric: MetricType): string {
  const units = {
    [MetricType.ACCURACY]: '%',
    [MetricType.REVIEWS]: '',
    [MetricType.STUDY_TIME]: 'min',
    [MetricType.WORDS_ADDED]: '',
    [MetricType.WORDS_LEARNED]: '',
    [MetricType.WORDS_MASTERED]: '',
    [MetricType.STREAK]: ' days',
    [MetricType.RETENTION]: '%'
  };

  return units[metric] || '';
}

function calculateTrendLine(data: number[]): number[] {
  if (data.length < 2) return data;

  const n = data.length;
  const sumX = (n * (n - 1)) / 2;
  const sumY = data.reduce((sum, y) => sum + y, 0);
  const sumXY = data.reduce((sum, y, i) => sum + (i * y), 0);
  const sumXX = (n * (n - 1) * (2 * n - 1)) / 6;

  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
  const intercept = (sumY - slope * sumX) / n;

  return data.map((_, i) => slope * i + intercept);
}

export default ProgressChart;