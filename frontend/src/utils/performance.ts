/**
 * Performance optimization utilities for the review system
 */

// Debounce function to limit rapid function calls
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null;

  return (...args: Parameters<T>) => {
    if (timeout) {
      clearTimeout(timeout);
    }

    timeout = setTimeout(() => {
      func(...args);
    }, wait);
  };
}

// Throttle function to limit function calls to once per specified time period
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean = false;

  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
}

// Memoize expensive computations
export function memoize<T extends (...args: any[]) => any>(
  func: T,
  keyGenerator?: (...args: Parameters<T>) => string
): T {
  const cache = new Map<string, ReturnType<T>>();

  return ((...args: Parameters<T>): ReturnType<T> => {
    const key = keyGenerator ? keyGenerator(...args) : JSON.stringify(args);

    if (cache.has(key)) {
      return cache.get(key)!;
    }

    const result = func(...args);
    cache.set(key, result);

    // Limit cache size to prevent memory leaks
    if (cache.size > 100) {
      const firstKey = cache.keys().next().value;
      cache.delete(firstKey);
    }

    return result;
  }) as T;
}

// Lazy load components
export function lazyLoad<T extends React.ComponentType<any>>(
  importFunc: () => Promise<{ default: T }>,
  fallback?: React.ComponentType
): React.LazyExoticComponent<T> {
  return React.lazy(() => {
    return new Promise<{ default: T }>((resolve) => {
      // Add a small delay to show loading state
      setTimeout(() => {
        importFunc().then(resolve);
      }, 100);
    });
  });
}

// Intersection Observer for lazy loading
export function useIntersectionObserver(
  elementRef: React.RefObject<Element>,
  options: IntersectionObserverInit = {}
): boolean {
  const [isIntersecting, setIsIntersecting] = React.useState(false);

  React.useEffect(() => {
    const element = elementRef.current;
    if (!element) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        setIsIntersecting(entry.isIntersecting);
      },
      {
        threshold: 0.1,
        rootMargin: '50px',
        ...options,
      }
    );

    observer.observe(element);

    return () => {
      observer.unobserve(element);
    };
  }, [elementRef, options]);

  return isIntersecting;
}

// Performance monitoring
export class PerformanceMonitor {
  private static instance: PerformanceMonitor;
  private metrics: Map<string, number[]> = new Map();
  private isEnabled: boolean = false;

  private constructor() {}

  static getInstance(): PerformanceMonitor {
    if (!PerformanceMonitor.instance) {
      PerformanceMonitor.instance = new PerformanceMonitor();
    }
    return PerformanceMonitor.instance;
  }

  enable(): void {
    this.isEnabled = true;
  }

  disable(): void {
    this.isEnabled = false;
  }

  startMeasure(name: string): void {
    if (!this.isEnabled) return;

    if (typeof performance !== 'undefined' && performance.mark) {
      performance.mark(`${name}-start`);
    }
  }

  endMeasure(name: string): number {
    if (!this.isEnabled) return 0;

    if (typeof performance !== 'undefined' && performance.mark && performance.measure) {
      performance.mark(`${name}-end`);
      performance.measure(name, `${name}-start`, `${name}-end`);

      const measures = performance.getEntriesByName(name);
      if (measures.length > 0) {
        const duration = measures[measures.length - 1].duration;

        // Store metric
        if (!this.metrics.has(name)) {
          this.metrics.set(name, []);
        }
        this.metrics.get(name)!.push(duration);

        // Clean up old marks
        performance.clearMarks(`${name}-start`);
        performance.clearMarks(`${name}-end`);
        performance.clearMeasures(name);

        return duration;
      }
    }

    return 0;
  }

  getAverageMetric(name: string): number {
    const values = this.metrics.get(name) || [];
    if (values.length === 0) return 0;

    const sum = values.reduce((acc, val) => acc + val, 0);
    return sum / values.length;
  }

  getMetrics(): Record<string, { average: number; count: number }> {
    const result: Record<string, { average: number; count: number }> = {};

    for (const [name, values] of this.metrics.entries()) {
      const sum = values.reduce((acc, val) => acc + val, 0);
      result[name] = {
        average: sum / values.length,
        count: values.length,
      };
    }

    return result;
  }

  clearMetrics(): void {
    this.metrics.clear();
  }
}

// Responsive design utilities
export const responsive = {
  breakpoints: {
    xs: 0,
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    '2xl': 1536,
  },

  useBreakpoint: () => {
    const [breakpoint, setBreakpoint] = React.useState<string>('md');

    React.useEffect(() => {
      const updateBreakpoint = () => {
        const width = window.innerWidth;
        if (width < responsive.breakpoints.sm) {
          setBreakpoint('xs');
        } else if (width < responsive.breakpoints.md) {
          setBreakpoint('sm');
        } else if (width < responsive.breakpoints.lg) {
          setBreakpoint('md');
        } else if (width < responsive.breakpoints.xl) {
          setBreakpoint('lg');
        } else if (width < responsive.breakpoints['2xl']) {
          setBreakpoint('xl');
        } else {
          setBreakpoint('2xl');
        }
      };

      updateBreakpoint();
      window.addEventListener('resize', updateBreakpoint);

      return () => {
        window.removeEventListener('resize', updateBreakpoint);
      };
    }, []);

    return breakpoint;
  },

  isMobile: () => {
    return typeof window !== 'undefined' && window.innerWidth < responsive.breakpoints.md;
  },

  isTablet: () => {
    return typeof window !== 'undefined' &&
           window.innerWidth >= responsive.breakpoints.md &&
           window.innerWidth < responsive.breakpoints.lg;
  },

  isDesktop: () => {
    return typeof window !== 'undefined' && window.innerWidth >= responsive.breakpoints.lg;
  },
};

// Animation performance utilities
export const animation = {
  // Optimize animation performance by using transform and opacity
  getOptimizedStyle: (props: {
    x?: number;
    y?: number;
    scale?: number;
    opacity?: number;
    rotate?: number;
  }) => {
    const transform = [];

    if (props.x !== undefined) transform.push(`translateX(${props.x}px)`);
    if (props.y !== undefined) transform.push(`translateY(${props.y}px)`);
    if (props.scale !== undefined) transform.push(`scale(${props.scale})`);
    if (props.rotate !== undefined) transform.push(`rotate(${props.rotate}deg)`);

    return {
      transform: transform.length > 0 ? transform.join(' ') : 'none',
      opacity: props.opacity ?? 1,
      willChange: 'transform, opacity',
    };
  },

  // Prefers reduced motion
  useReducedMotion: () => {
    const [prefersReducedMotion, setPrefersReducedMotion] = React.useState(false);

    React.useEffect(() => {
      const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
      setPrefersReducedMotion(mediaQuery.matches);

      const handler = (e: MediaQueryListEvent) => {
        setPrefersReducedMotion(e.matches);
      };

      mediaQuery.addEventListener('change', handler);

      return () => {
        mediaQuery.removeEventListener('change', handler);
      };
    }, []);

    return prefersReducedMotion;
  },
};

// Memory management utilities
export const memory = {
  // Clean up event listeners
  useCleanup: (cleanup: () => void) => {
    React.useEffect(() => {
      return cleanup;
    }, [cleanup]);
  },

  // Limit number of re-renders
  useRenderLimit: (limit: number = 100) => {
    const renderCount = React.useRef(0);

    React.useEffect(() => {
      renderCount.current++;

      if (renderCount.current > limit) {
        console.warn(`Component has rendered ${renderCount.current} times, which may indicate a performance issue`);
      }
    });
  },

  // Memoize expensive calculations
  useMemo: <T>(factory: () => T, deps: React.DependencyList) => {
    return React.useMemo(factory, deps);
  },

  // Callback memoization
  useCallback: <T extends (...args: any[]) => any>(
    callback: T,
    deps: React.DependencyList
  ) => {
    return React.useCallback(callback, deps);
  },
};

// Network utilities
export const network = {
  // Check if online
  useOnlineStatus: () => {
    const [isOnline, setIsOnline] = React.useState(
      typeof navigator !== 'undefined' ? navigator.onLine : true
    );

    React.useEffect(() => {
      const handleOnline = () => setIsOnline(true);
      const handleOffline = () => setIsOnline(false);

      window.addEventListener('online', handleOnline);
      window.addEventListener('offline', handleOffline);

      return () => {
        window.removeEventListener('online', handleOnline);
        window.removeEventListener('offline', handleOffline);
      };
    }, []);

    return isOnline;
  },

  // Retry failed requests with exponential backoff
  retryWithBackoff: async <T>(
    fn: () => Promise<T>,
    maxRetries: number = 3,
    baseDelay: number = 1000
  ): Promise<T> => {
    let lastError: Error;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        return await fn();
      } catch (error) {
        lastError = error as Error;

        if (attempt === maxRetries) {
          break;
        }

        const delay = baseDelay * Math.pow(2, attempt - 1) + Math.random() * 1000;
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }

    throw lastError!;
  },
};

// Export default instance
export const performanceMonitor = PerformanceMonitor.getInstance();