import React, { useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress, Progress as ProgressComponent } from '@/components/ui/progress';
import { ProgressStatistics } from '@/types/analytics';
import { analyticsUtils } from '@/services/analyticsApi';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);

interface LearningVelocityChartProps {
  data: ProgressStatistics;
  showComparison?: boolean;
  showDistribution?: boolean;
  className?: string;
}

export const LearningVelocityChart: React.FC<LearningVelocityChartProps> = ({
  data,
  showComparison = true,
  showDistribution = true,
  className
}) => {
  const velocityData = useMemo(() => {
    const velocity = data.learningVelocity || {
      newWordsPerDay: 0,
      learnedWordsPerDay: 0,
      masteredWordsPerDay: 0,
      reviewVelocity: 0
    };

    return {
      labels: ['New Words', 'Words Learned', 'Words Mastered', 'Reviews'],
      datasets: [{
        label: 'Per Day Average',
        data: [
          velocity.newWordsPerDay,
          velocity.learnedWordsPerDay,
          velocity.masteredWordsPerDay,
          velocity.reviewVelocity
        ],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',
          'rgba(16, 185, 129, 0.8)',
          'rgba(245, 158, 11, 0.8)',
          'rgba(168, 85, 247, 0.8)'
        ],
        borderColor: [
          'rgb(59, 130, 246)',
          'rgb(16, 185, 129)',
          'rgb(245, 158, 11)',
          'rgb(168, 85, 247)'
        ],
        borderWidth: 2,
        borderRadius: 8,
        borderSkipped: false
      }]
    };
  }, [data]);

  const distributionData = useMemo(() => {
    const velocity = data.learningVelocity || {
      newWordsPerDay: 0,
      learnedWordsPerDay: 0,
      masteredWordsPerDay: 0,
      reviewVelocity: 0
    };

    const total = velocity.newWordsPerDay + velocity.learnedWordsPerDay + velocity.masteredWordsPerDay;

    return {
      labels: ['New Words', 'Learning', 'Mastered'],
      datasets: [{
        data: [
          velocity.newWordsPerDay,
          velocity.learnedWordsPerDay,
          velocity.masteredWordsPerDay
        ],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',
          'rgba(16, 185, 129, 0.8)',
          'rgba(245, 158, 11, 0.8)'
        ],
        borderColor: [
          'rgb(59, 130, 246)',
          'rgb(16, 185, 129)',
          'rgb(245, 158, 11)'
        ],
        borderWidth: 2
      }]
    };
  }, [data]);

  const barOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
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
          label: (context: any) => {
            const value = context.parsed.y;
            const label = context.label;
            const unit = label.includes('Review') ? ' reviews' : ' words';
            return `${label}: ${value.toFixed(1)}${unit}/day`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        },
        ticks: {
          font: {
            size: 11
          },
          callback: (value: any) => value.toFixed(1)
        },
        title: {
          display: true,
          text: 'Words per Day',
          font: {
            size: 12,
            weight: '500'
          }
        }
      },
      x: {
        grid: {
          display: false
        },
        ticks: {
          font: {
            size: 11
          }
        }
      }
    },
    animation: {
      duration: 1000,
      easing: 'easeInOutQuart'
    }
  }), []);

  const doughnutOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          usePointStyle: true,
          padding: 20,
          font: {
            size: 11
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
          label: (context: any) => {
            const value = context.parsed;
            const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0);
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
            return `${context.label}: ${value.toFixed(1)} (${percentage}%)`;
          }
        }
      }
    },
    animation: {
      duration: 1000,
      easing: 'easeInOutQuart'
    }
  }), []);

  const efficiencyMetrics = useMemo(() => {
    const velocity = data.learningVelocity || {
      newWordsPerDay: 0,
      learnedWordsPerDay: 0,
      masteredWordsPerDay: 0,
      reviewVelocity: 0
    };

    const totalVelocity = velocity.newWordsPerDay + velocity.learnedWordsPerDay + velocity.masteredWordsPerDay;
    const efficiency = totalVelocity > 0 ? (velocity.masteredWordsPerDay / totalVelocity) * 100 : 0;
    const learningRate = velocity.newWordsPerDay > 0 ? (velocity.learnedWordsPerDay / velocity.newWordsPerDay) * 100 : 0;
    const masteryRate = velocity.learnedWordsPerDay > 0 ? (velocity.masteredWordsPerDay / velocity.learnedWordsPerDay) * 100 : 0;

    return {
      efficiency,
      learningRate,
      masteryRate,
      totalVelocity
    };
  }, [data]);

  const velocityAssessment = useMemo(() => {
    const { totalVelocity, efficiency } = efficiencyMetrics;

    if (totalVelocity === 0) {
      return {
        level: 'No Activity',
        color: 'gray',
        description: 'Start learning to build your velocity',
        recommendation: 'Begin with 5-10 new words per day'
      };
    } else if (totalVelocity < 5) {
      return {
        level: 'Building Momentum',
        color: 'blue',
        description: 'You\'re establishing a learning routine',
        recommendation: 'Try to add 1-2 more words per day'
      };
    } else if (totalVelocity < 15) {
      return {
        level: 'Steady Progress',
        color: 'green',
        description: 'Good consistent learning pace',
        recommendation: 'Maintain this pace and focus on retention'
      };
    } else if (totalVelocity < 25) {
      return {
        level: 'High Velocity',
        color: 'orange',
        description: 'Excellent learning speed',
        recommendation: 'Ensure you\'re maintaining good retention rates'
      };
    } else {
      return {
        level: 'Ultra High Velocity',
        color: 'red',
        description: 'Outstanding learning pace',
        recommendation: 'Monitor for burnout and ensure quality over quantity'
      };
    }
  }, [efficiencyMetrics]);

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Learning Velocity</span>
          <Badge variant={velocityAssessment.color as any}>
            {velocityAssessment.level}
          </Badge>
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          Your daily learning rate across different stages
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          {/* Main Bar Chart */}
          <div className="h-64">
            <Bar data={velocityData} options={barOptions} />
          </div>

          {/* Efficiency Metrics */}
          <div className="grid grid-cols-3 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">
                {efficiencyMetrics.totalVelocity.toFixed(1)}
              </div>
              <div className="text-xs text-muted-foreground">Total Words/Day</div>
              <ProgressComponent
                value={Math.min(100, efficiencyMetrics.totalVelocity * 4)}
                className="mt-2 h-2"
              />
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {efficiencyMetrics.efficiency.toFixed(0)}%
              </div>
              <div className="text-xs text-muted-foreground">Efficiency</div>
              <ProgressComponent
                value={efficiencyMetrics.efficiency}
                className="mt-2 h-2"
              />
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">
                {efficiencyMetrics.masteryRate.toFixed(0)}%
              </div>
              <div className="text-xs text-muted-foreground">Mastery Rate</div>
              <ProgressComponent
                value={efficiencyMetrics.masteryRate}
                className="mt-2 h-2"
              />
            </div>
          </div>

          {/* Distribution Chart */}
          {showDistribution && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div>
                <h4 className="text-sm font-medium mb-3">Learning Stage Distribution</h4>
                <div className="h-40">
                  <Doughnut data={distributionData} options={doughnutOptions} />
                </div>
              </div>
              <div>
                <h4 className="text-sm font-medium mb-3">Velocity Analysis</h4>
                <div className="space-y-3">
                  <div>
                    <div className="flex justify-between text-sm mb-1">
                      <span>New Words</span>
                      <span>{data.learningVelocity?.newWordsPerDay.toFixed(1) || 0}/day</span>
                    </div>
                    <ProgressComponent
                      value={Math.min(100, (data.learningVelocity?.newWordsPerDay || 0) * 5)}
                      className="h-2"
                    />
                  </div>
                  <div>
                    <div className="flex justify-between text-sm mb-1">
                      <span>Learning Progress</span>
                      <span>{data.learningVelocity?.learnedWordsPerDay.toFixed(1) || 0}/day</span>
                    </div>
                    <ProgressComponent
                      value={Math.min(100, (data.learningVelocity?.learnedWordsPerDay || 0) * 5)}
                      className="h-2"
                    />
                  </div>
                  <div>
                    <div className="flex justify-between text-sm mb-1">
                      <span>Mastered Words</span>
                      <span>{data.learningVelocity?.masteredWordsPerDay.toFixed(1) || 0}/day</span>
                    </div>
                    <ProgressComponent
                      value={Math.min(100, (data.learningVelocity?.masteredWordsPerDay || 0) * 5)}
                      className="h-2"
                    />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Assessment */}
          <div className="p-3 bg-muted rounded-lg">
            <div className="text-sm font-medium mb-1">{velocityAssessment.description}</div>
            <div className="text-xs text-muted-foreground">
              💡 {velocityAssessment.recommendation}
            </div>
          </div>

          {/* Comparison with Previous Period */}
          {showComparison && (
            <div className="text-xs text-muted-foreground text-center">
              Track your velocity over time to identify trends and optimize your learning strategy
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default LearningVelocityChart;