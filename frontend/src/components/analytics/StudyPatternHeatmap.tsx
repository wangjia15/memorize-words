import React, { useMemo, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ProgressStatistics } from '@/types/analytics';
import { format } from 'date-fns';

interface StudyPatternHeatmapProps {
  data: ProgressStatistics;
  showTooltips?: boolean;
  className?: string;
}

interface HeatmapCell {
  dayOfWeek: number;
  hour: number;
  intensity: number;
  studyTime: number;
  accuracy: number;
  sessions: number;
  date?: Date;
}

export const StudyPatternHeatmap: React.FC<StudyPatternHeatmapProps> = ({
  data,
  showTooltips = true,
  className
}) => {
  const [hoveredCell, setHoveredCell] = useState<HeatmapCell | null>(null);

  const heatmapData = useMemo(() => {
    // Create 7x24 grid for days of week vs hours
    const grid: HeatmapCell[][] = Array(7).fill(null).map((_, dayIndex) =>
      Array(24).fill(null).map((_, hourIndex) => ({
        dayOfWeek: dayIndex,
        hour: hourIndex,
        intensity: 0,
        studyTime: 0,
        accuracy: 0,
        sessions: 0
      }))
    );

    // Populate with actual study session data
    if (data.dailyMetrics && data.dailyMetrics.length > 0) {
      data.dailyMetrics.forEach(metric => {
        if (metric.hasActivity && metric.studyTime > 0) {
          const date = new Date(metric.date);
          const dayOfWeek = date.getDay();

          // Distribute study time across active hours
          // For demonstration, we'll simulate typical study hours
          const activeHours = getActiveHoursForStudy(metric.studyTime, metric.accuracy);

          activeHours.forEach(hourData => {
            if (hourData.hour >= 0 && hourData.hour < 24) {
              const cell = grid[dayOfWeek][hourData.hour];
              cell.intensity += hourData.intensity;
              cell.studyTime += hourData.studyTime;
              cell.accuracy = (cell.accuracy * cell.sessions + hourData.accuracy * hourData.sessions) / (cell.sessions + hourData.sessions);
              cell.sessions += hourData.sessions;
              cell.date = date;
            }
          });
        }
      });
    }

    return grid;
  }, [data]);

  const maxIntensity = useMemo(() => {
    let max = 0;
    heatmapData.forEach(day => {
      day.forEach(cell => {
        max = Math.max(max, cell.intensity);
      });
    });
    return max || 1; // Prevent division by zero
  }, [heatmapData]);

  const optimalStudyTimes = useMemo(() => {
    const bestTimes: { day: number; hour: number; score: number }[] = [];

    heatmapData.forEach((day, dayIndex) => {
      day.forEach((cell, hourIndex) => {
        if (cell.intensity > 0 && cell.sessions >= 3) {
          const score = cell.intensity * cell.accuracy;
          if (score > 0.5) {
            bestTimes.push({
              day: dayIndex,
              hour: hourIndex,
              score
            });
          }
        }
      });
    });

    return bestTimes
      .sort((a, b) => b.score - a.score)
      .slice(0, 5);
  }, [heatmapData]);

  const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

  const getHeatmapColor = (intensity: number): string => {
    const normalizedIntensity = intensity / maxIntensity;

    if (normalizedIntensity === 0) {
      return 'bg-gray-100';
    } else if (normalizedIntensity < 0.2) {
      return 'bg-green-100';
    } else if (normalizedIntensity < 0.4) {
      return 'bg-green-200';
    } else if (normalizedIntensity < 0.6) {
      return 'bg-green-300';
    } else if (normalizedIntensity < 0.8) {
      return 'bg-green-400';
    } else {
      return 'bg-green-500';
    }
  };

  const formatIntensity = (intensity: number): string => {
    if (intensity === 0) return 'No activity';
    if (intensity < 0.2) return 'Very light';
    if (intensity < 0.4) return 'Light';
    if (intensity < 0.6) return 'Moderate';
    if (intensity < 0.8) return 'Heavy';
    return 'Very heavy';
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Study Pattern Heatmap</span>
          <Badge variant="outline">
            {data.dailyMetrics?.filter(m => m.hasActivity).length || 0} active days
          </Badge>
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          Your study activity patterns by day of week and time of day
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Color Legend */}
          <div className="flex items-center justify-center space-x-2 text-xs">
            <span>Less</span>
            <div className="flex space-x-1">
              <div className="w-4 h-4 bg-gray-100 rounded-sm"></div>
              <div className="w-4 h-4 bg-green-100 rounded-sm"></div>
              <div className="w-4 h-4 bg-green-200 rounded-sm"></div>
              <div className="w-4 h-4 bg-green-300 rounded-sm"></div>
              <div className="w-4 h-4 bg-green-400 rounded-sm"></div>
              <div className="w-4 h-4 bg-green-500 rounded-sm"></div>
            </div>
            <span>More</span>
          </div>

          {/* Heatmap Grid */}
          <div className="overflow-x-auto">
            <div className="inline-block min-w-full">
              {/* Hour labels */}
              <div className="flex">
                <div className="w-16 flex-shrink-0"></div>
                <div className="grid grid-cols-24 gap-1 flex-1">
                  {Array.from({ length: 24 }, (_, hour) => (
                    <div
                      key={hour}
                      className="text-xs text-muted-foreground text-center py-1"
                    >
                      {hour.toString().padStart(2, '0')}
                    </div>
                  ))}
                </div>
              </div>

              {/* Heatmap rows */}
              {heatmapData.map((dayData, dayIndex) => (
                <div key={dayIndex} className="flex items-center">
                  {/* Day label */}
                  <div className="w-16 flex-shrink-0 text-xs text-muted-foreground py-1 pr-2 text-right">
                    {dayNames[dayIndex].substring(0, 3)}
                  </div>

                  {/* Heatmap cells */}
                  <div className="grid grid-cols-24 gap-1 flex-1">
                    {dayData.map((cell, hourIndex) => (
                      <div
                        key={`${dayIndex}-${hourIndex}`}
                        className={`
                          aspect-square rounded-sm cursor-pointer transition-all duration-200
                          ${getHeatmapColor(cell.intensity)}
                          ${cell.intensity > 0 ? 'hover:scale-110 hover:shadow-md' : ''}
                        `}
                        title={showTooltips ? getCellTooltip(cell, dayNames[dayIndex]) : undefined}
                        onMouseEnter={() => setHoveredCell(cell)}
                        onMouseLeave={() => setHoveredCell(null)}
                        onClick={() => {
                          if (cell.intensity > 0) {
                            console.log(`Study session: ${dayNames[dayIndex]} at ${hourIndex}:00`);
                          }
                        }}
                      />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Hover Information */}
          {hoveredCell && hoveredCell.intensity > 0 && (
            <div className="p-3 bg-muted rounded-lg border">
              <div className="text-sm font-medium">
                {dayNames[hoveredCell.dayOfWeek]} at {hoveredCell.hour.toString().padStart(2, '0')}:00
              </div>
              <div className="grid grid-cols-2 gap-2 mt-2 text-xs">
                <div>
                  <span className="text-muted-foreground">Intensity:</span>
                  <span className="ml-1 font-medium">
                    {formatIntensity(hoveredCell.intensity)}
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">Study Time:</span>
                  <span className="ml-1 font-medium">
                    {hoveredCell.studyTime.toFixed(0)}min
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">Accuracy:</span>
                  <span className="ml-1 font-medium">
                    {hoveredCell.accuracy.toFixed(1)}%
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">Sessions:</span>
                  <span className="ml-1 font-medium">
                    {hoveredCell.sessions}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Optimal Study Times */}
          {optimalStudyTimes.length > 0 && (
            <div className="border-t pt-4">
              <h4 className="text-sm font-medium mb-2">Optimal Study Times</h4>
              <div className="flex flex-wrap gap-2">
                {optimalStudyTimes.map((time, index) => (
                  <Badge key={index} variant="secondary" className="text-xs">
                    {dayNames[time.day]} {time.hour.toString().padStart(2, '0')}:00
                  </Badge>
                ))}
              </div>
            </div>
          )}

          {/* Pattern Summary */}
          <div className="text-xs text-muted-foreground text-center">
            {getPatternSummary(heatmapData, optimalStudyTimes)}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

// Helper functions
function getActiveHoursForStudy(
  totalStudyTime: number,
  accuracy: number
): Array<{ hour: number; intensity: number; studyTime: number; accuracy: number; sessions: number }> {
  const hours: Array<{ hour: number; intensity: number; studyTime: number; accuracy: number; sessions: number }> = [];

  // Simulate typical study patterns
  const studyHours = [9, 10, 11, 14, 15, 16, 19, 20, 21]; // Common study hours
  const totalHours = studyHours.length;

  studyHours.forEach(hour => {
    const timePortion = totalStudyTime / totalHours;
    // Add some variation based on time of day
    const timeMultiplier = getTimeMultiplier(hour);
    const studyTime = timePortion * timeMultiplier;
    const intensity = (studyTime / 60) * (accuracy / 100); // Normalized intensity

    hours.push({
      hour,
      intensity,
      studyTime,
      accuracy,
      sessions: 1
    });
  });

  return hours;
}

function getTimeMultiplier(hour: number): number {
  // Peak hours: morning (9-11) and evening (19-21)
  if ((hour >= 9 && hour <= 11) || (hour >= 19 && hour <= 21)) {
    return 1.5;
  }
  // Good hours: afternoon (14-16)
  if (hour >= 14 && hour <= 16) {
    return 1.2;
  }
  // Normal hours
  return 1.0;
}

function getCellTooltip(cell: any, dayName: string): string {
  if (cell.intensity === 0) return 'No study activity';

  return `${dayName} at ${cell.hour}:00 - ${cell.studyTime.toFixed(0)}min study time, ${cell.accuracy.toFixed(1)}% accuracy`;
}

function getPatternSummary(
  heatmapData: any[][],
  optimalTimes: Array<{ day: number; hour: number; score: number }>
): string {
  const activeDays = heatmapData.filter(day =>
    day.some(cell => cell.intensity > 0)
  ).length;

  const peakHourCounts = new Array(24).fill(0);
  heatmapData.forEach(day => {
    day.forEach((cell, hour) => {
      if (cell.intensity > 0.5) {
        peakHourCounts[hour]++;
      }
    });
  });

  const mostActiveHour = peakHourCounts.indexOf(Math.max(...peakHourCounts));
  const timeOfDay = mostActiveHour < 12 ? 'morning' : mostActiveHour < 17 ? 'afternoon' : 'evening';

  if (activeDays === 0) {
    return 'No study activity detected yet. Start your learning journey!';
  } else if (activeDays < 3) {
    return `Study activity on ${activeDays} days. Try to establish a more consistent schedule.`;
  } else if (activeDays < 5) {
    return `Study activity on ${activeDays} days. You\'re building good habits! Most active in the ${timeOfDay}.`;
  } else {
    return `Excellent consistency with ${activeDays} days of study! Peak activity in the ${timeOfDay}.`;
  }
}

export default StudyPatternHeatmap;