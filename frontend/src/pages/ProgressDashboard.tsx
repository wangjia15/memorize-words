import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ProgressChart } from '@/components/analytics/ProgressChart';
import { AccuracyTrendChart } from '@/components/analytics/AccuracyTrendChart';
import { StudyPatternHeatmap } from '@/components/analytics/StudyPatternHeatmap';
import { LearningVelocityChart } from '@/components/analytics/LearningVelocityChart';
import { AchievementsPanel } from '@/components/analytics/AchievementsPanel';
import { PredictionsCard } from '@/components/analytics/PredictionsCard';
import { cachedAnalyticsApi, analyticsUtils } from '@/services/analyticsApi';
import {
  ProgressStatistics,
  Timeframe,
  MetricType,
  UserPreferences,
  DashboardWidget,
  StatCardProps
} from '@/types/analytics';
import {
  Flame,
  Target,
  Clock,
  BookOpen,
  TrendingUp,
  TrendingDown,
  Minus,
  Calendar,
  Download,
  RefreshCw,
  Settings,
  BarChart3,
  PieChart,
  Activity
} from 'lucide-react';
import { format, subDays, subMonths, subWeeks, startOfWeek, endOfWeek } from 'date-fns';

interface StatCardExtendedProps extends StatCardProps {
  subtitle?: string;
  change?: number;
  changeType?: 'increase' | 'decrease' | 'neutral';
}

const StatCard: React.FC<StatCardExtendedProps> = ({
  title,
  value,
  unit,
  subtitle,
  change,
  changeType = 'neutral',
  icon: Icon,
  className
}) => {
  const getChangeIcon = () => {
    switch (changeType) {
      case 'increase':
        return <TrendingUp className="w-4 h-4" />;
      case 'decrease':
        return <TrendingDown className="w-4 h-4" />;
      default:
        return <Minus className="w-4 h-4" />;
    }
  };

  const getChangeColor = () => {
    switch (changeType) {
      case 'increase':
        return 'text-green-600';
      case 'decrease':
        return 'text-red-600';
      default:
        return 'text-gray-600';
    }
  };

  return (
    <Card className={className}>
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">{title}</p>
            <div className="flex items-baseline gap-2 mt-1">
              <p className="text-2xl font-bold">{value}</p>
              {unit && <p className="text-sm text-muted-foreground">{unit}</p>}
            </div>
            {subtitle && (
              <p className="text-xs text-muted-foreground mt-1">{subtitle}</p>
            )}
            {change !== undefined && (
              <div className={`flex items-center gap-1 mt-2 text-xs ${getChangeColor()}`}>
                {getChangeIcon()}
                <span>{Math.abs(change)}%</span>
              </div>
            )}
          </div>
          {Icon && <Icon className="w-8 h-8 text-muted-foreground" />}
        </div>
      </CardContent>
    </Card>
  );
};

export const ProgressDashboard: React.FC = () => {
  const [timeframe, setTimeframe] = useState<Timeframe>(Timeframe.DAILY);
  const [selectedMetric, setSelectedMetric] = useState<MetricType>(MetricType.ACCURACY);
  const [progressData, setProgressData] = useState<ProgressStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);

  useEffect(() => {
    loadProgressData();
    loadPreferences();

    if (autoRefresh) {
      const interval = setInterval(loadProgressData, 30000); // Refresh every 30 seconds
      return () => clearInterval(interval);
    }
  }, [timeframe, autoRefresh]);

  const loadProgressData = async () => {
    setLoading(true);
    try {
      const endDate = new Date();
      let startDate: Date;

      switch (timeframe) {
        case Timeframe.DAILY:
          startDate = subDays(endDate, 30);
          break;
        case Timeframe.WEEKLY:
          startDate = subWeeks(endDate, 12);
          break;
        case Timeframe.MONTHLY:
          startDate = subMonths(endDate, 12);
          break;
        default:
          startDate = subDays(endDate, 30);
      }

      const data = await cachedAnalyticsApi.statistics.getProgressStatistics(startDate, endDate);
      setProgressData(data);
      setLastUpdated(new Date());
    } catch (error) {
      console.error('Failed to load progress data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadPreferences = async () => {
    try {
      // Mock preferences for now - in real app, this would come from API
      setPreferences({
        dashboardLayout: [],
        chartTheme: 'light',
        defaultTimeframe: Timeframe.DAILY,
        defaultMetrics: [MetricType.ACCURACY, MetricType.REVIEWS, MetricType.STUDY_TIME],
        notifications: {
          achievements: true,
          streakReminders: true,
          weeklyReports: true
        }
      });
    } catch (error) {
      console.error('Failed to load preferences:', error);
    }
  };

  const handleExport = async (format: 'csv' | 'json' | 'pdf') => {
    try {
      const blob = await analyticsApi.exportProgressReport(format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `progress-report-${new Date().toISOString().split('T')[0]}.${format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to export progress report:', error);
    }
  };

  const getDateRangeLabel = () => {
    const endDate = new Date();
    let startDate: Date;

    switch (timeframe) {
      case Timeframe.DAILY:
        startDate = subDays(endDate, 30);
        return format(startDate, 'MMM dd') + ' - ' + format(endDate, 'MMM dd, yyyy');
      case Timeframe.WEEKLY:
        startDate = subWeeks(endDate, 12);
        return 'Last 12 weeks';
      case Timeframe.MONTHLY:
        startDate = subMonths(endDate, 12);
        return 'Last 12 months';
      default:
        return 'Custom range';
    }
  };

  if (loading || !progressData) {
    return (
      <div className="container mx-auto py-8">
        <div className="flex justify-center items-center min-h-[400px]">
          <div className="text-center">
            <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-4" />
            <p className="text-muted-foreground">Loading analytics data...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold">Learning Analytics</h1>
          <p className="text-muted-foreground mt-1">
            Track your progress and optimize your learning journey
          </p>
          {lastUpdated && (
            <p className="text-xs text-muted-foreground mt-2">
              Last updated: {format(lastUpdated, 'MMM dd, yyyy HH:mm')}
            </p>
          )}
        </div>

        <div className="flex flex-wrap gap-2">
          <Select value={timeframe} onValueChange={(value: Timeframe) => setTimeframe(value)}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={Timeframe.DAILY}>Daily</SelectItem>
              <SelectItem value={Timeframe.WEEKLY}>Weekly</SelectItem>
              <SelectItem value={Timeframe.MONTHLY}>Monthly</SelectItem>
            </SelectContent>
          </Select>

          <Select value={selectedMetric} onValueChange={(value: MetricType) => setSelectedMetric(value)}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={MetricType.ACCURACY}>Accuracy</SelectItem>
              <SelectItem value={MetricType.REVIEWS}>Reviews</SelectItem>
              <SelectItem value={MetricType.STUDY_TIME}>Study Time</SelectItem>
              <SelectItem value={MetricType.WORDS_ADDED}>Words Added</SelectItem>
              <SelectItem value={MetricType.WORDS_LEARNED}>Words Learned</SelectItem>
              <SelectItem value={MetricType.WORDS_MASTERED}>Words Mastered</SelectItem>
            </SelectContent>
          </Select>

          <Button
            variant="outline"
            size="sm"
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={autoRefresh ? 'bg-primary text-primary-foreground' : ''}
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${autoRefresh ? 'animate-spin' : ''}`} />
            Auto Refresh
          </Button>

          <Button variant="outline" size="sm" onClick={loadProgressData}>
            <RefreshCw className="w-4 h-4 mr-2" />
            Refresh
          </Button>

          <Select onValueChange={(value: 'csv' | 'json' | 'pdf') => handleExport(value)}>
            <SelectTrigger className="w-32">
              <Download className="w-4 h-4 mr-2" />
              Export
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="csv">CSV</SelectItem>
              <SelectItem value="json">JSON</SelectItem>
              <SelectItem value="pdf">PDF</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Date Range */}
      <div className="flex items-center gap-2">
        <Calendar className="w-4 h-4 text-muted-foreground" />
        <span className="text-sm text-muted-foreground">{getDateRangeLabel()}</span>
      </div>

      {/* Key Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Current Streak"
          value={progressData.streakDays}
          unit="days"
          change={progressData.streakDays > 0 ? 5 : 0}
          changeType={progressData.streakDays > 0 ? 'increase' : 'neutral'}
          icon={Flame}
          subtitle="Keep it going!"
        />
        <StatCard
          title="Average Accuracy"
          value={progressData.averageAccuracy.toFixed(1)}
          unit="%"
          change={progressData.performanceTrends?.accuracyTrend || 0}
          changeType={progressData.performanceTrends?.trendDirection === 'improving' ? 'increase' : 'neutral'}
          icon={Target}
          subtitle="Excellent progress!"
        />
        <StatCard
          title="Total Study Time"
          value={analyticsUtils.formatDuration(progressData.totalStudyTime)}
          subtitle={progressData.averageSessionLength ? `Avg: ${analyticsUtils.formatDuration(progressData.averageSessionLength)} per session` : ''}
          icon={Clock}
        />
        <StatCard
          title="Words Mastered"
          value={progressData.wordsMastered}
          change={progressData.learningVelocity?.masteredWordsPerDay ? 10 : 0}
          changeType="increase"
          icon={BookOpen}
          subtitle="Great achievement!"
        />
      </div>

      {/* Main Progress Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Progress Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <ProgressChart
            data={progressData}
            timeframe={timeframe}
            metric={selectedMetric}
            chartType="line"
            showTrendLine={true}
          />
        </CardContent>
      </Card>

      {/* Secondary Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <AccuracyTrendChart data={progressData} />
        <LearningVelocityChart data={progressData} />
        <StudyPatternHeatmap data={progressData} />
        <AchievementsPanel achievements={progressData.achievements || []} />
      </div>

      {/* Predictions and Insights */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <PredictionsCard predictions={progressData.predictions} />
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="w-5 h-5" />
              Personalized Insights
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <h4 className="text-sm font-medium mb-1">Study Pattern</h4>
                <p className="text-sm text-muted-foreground">
                  {progressData.optimalStudyTimes && progressData.optimalStudyTimes.length > 0
                    ? `Your most productive study times are typically during the week`
                    : 'Study consistently to establish optimal patterns'
                  }
                </p>
              </div>
              <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                <h4 className="text-sm font-medium mb-1">Performance Trend</h4>
                <p className="text-sm text-muted-foreground">
                  {progressData.performanceTrends?.trendDirection === 'improving'
                    ? 'Your learning performance is showing positive improvement'
                    : 'Maintain consistent practice to see better results'
                  }
                </p>
              </div>
              <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg">
                <h4 className="text-sm font-medium mb-1">Recommendation</h4>
                <p className="text-sm text-muted-foreground">
                  {progressData.learningVelocity?.newWordsPerDay && progressData.learningVelocity.newWordsPerDay < 5
                    ? 'Consider adding 1-2 more words per day to accelerate your learning'
                    : 'Your current pace is well-balanced for sustainable progress'
                  }
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Footer */}
      <div className="text-center text-sm text-muted-foreground py-4 border-t">
        <p>
          Data updates automatically • {progressData.dailyMetrics?.length || 0} days analyzed •
          <Button variant="link" size="sm" className="p-0 h-auto ml-1">
            <Settings className="w-3 h-3 mr-1" />
            Customize Dashboard
          </Button>
        </p>
      </div>
    </div>
  );
};

export default ProgressDashboard;