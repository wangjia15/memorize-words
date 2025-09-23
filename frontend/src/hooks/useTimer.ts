import { useState, useEffect, useRef, useCallback } from 'react';

interface UseTimerOptions {
  autoStart?: boolean;
  interval?: number;
  onUpdate?: (time: number) => void;
}

export const useTimer = (options: UseTimerOptions = {}) => {
  const { autoStart = false, interval = 100, onUpdate } = options;

  const [time, setTime] = useState(0);
  const [isRunning, setIsRunning] = useState(autoStart);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const startTimeRef = useRef<number>(0);
  const accumulatedTimeRef = useRef<number>(0);

  // Start the timer
  const start = useCallback(() => {
    if (isRunning) return;

    startTimeRef.current = Date.now() - accumulatedTimeRef.current;
    setIsRunning(true);

    intervalRef.current = setInterval(() => {
      const currentTime = Date.now();
      const elapsedTime = currentTime - startTimeRef.current;
      setTime(elapsedTime);
      onUpdate?.(elapsedTime);
    }, interval);
  }, [isRunning, interval, onUpdate]);

  // Stop the timer
  const stop = useCallback(() => {
    if (!isRunning) return;

    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    accumulatedTimeRef.current = time;
    setIsRunning(false);
  }, [isRunning, time]);

  // Reset the timer
  const reset = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    setTime(0);
    setIsRunning(false);
    accumulatedTimeRef.current = 0;
    startTimeRef.current = 0;
  }, []);

  // Pause the timer (same as stop but keeps accumulated time)
  const pause = useCallback(() => {
    stop();
  }, [stop]);

  // Resume the timer
  const resume = useCallback(() => {
    start();
  }, [start]);

  // Get formatted time
  const getFormattedTime = useCallback((format: 'seconds' | 'minutes' | 'hours' = 'seconds') => {
    const totalSeconds = Math.floor(time / 1000);

    switch (format) {
      case 'hours':
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

      case 'minutes':
        const mins = Math.floor(totalSeconds / 60);
        const secs = totalSeconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;

      case 'seconds':
      default:
        return totalSeconds.toString();
    }
  }, [time]);

  // Clean up on unmount
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  return {
    time,
    isRunning,
    start,
    stop,
    reset,
    pause,
    resume,
    getFormattedTime,
    // Additional computed properties
    seconds: Math.floor(time / 1000),
    milliseconds: time % 1000,
    minutes: Math.floor(time / 60000),
    hours: Math.floor(time / 3600000)
  };
};

export default useTimer;