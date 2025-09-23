# Memorize Words - Performance Monitoring Setup

This directory contains the monitoring and observability configuration for the Memorize Words application.

## Components

### 1. Prometheus
- **Purpose**: Metrics collection and storage
- **Port**: 9090
- **Configuration**: `prometheus/prometheus.yml`
- **Alert Rules**: `prometheus/alert_rules.yml`

### 2. Grafana
- **Purpose**: Visualization and dashboards
- **Port**: 3000
- **Default Credentials**: admin/admin
- **Dashboards**: `grafana/dashboards/`

### 3. AlertManager
- **Purpose**: Alert management and notification
- **Port**: 9093
- **Configuration**: `alertmanager/alertmanager.yml`

### 4. Node Exporter
- **Purpose**: System metrics collection
- **Port**: 9100

## Quick Start

1. **Start the monitoring stack**:
   ```bash
   docker-compose up -d
   ```

2. **Access the services**:
   - Grafana: http://localhost:3000
   - Prometheus: http://localhost:9090
   - AlertManager: http://localhost:9093

3. **Import dashboards in Grafana**:
   - Navigate to Dashboards → Import
   - Use the JSON files in `grafana/dashboards/`

## Application Configuration

The application is configured to expose metrics at:
- **Metrics Endpoint**: `/actuator/prometheus`
- **Health Endpoint**: `/actuator/health`
- **Info Endpoint**: `/actuator/info`

## Key Metrics Tracked

### Application Metrics
- HTTP request rates and response times
- Service method execution times
- Repository query performance
- Error rates and exceptions

### System Metrics
- JVM memory usage and garbage collection
- Thread counts and CPU usage
- Database connection pool metrics
- Cache hit/miss ratios

### Business Metrics
- Review session performance
- User activity metrics
- Learning progress tracking

## Alerting Configuration

### Critical Alerts
- Application downtime
- Critical memory usage (>90%)
- Database connection pool exhaustion
- High error rates (>20%)

### Warning Alerts
- High response times
- Memory usage warnings (>80%)
- Slow query rates
- Low cache hit rates

## Configuration Files

### Prometheus Configuration
- Scrapes application metrics every 15 seconds
- Includes alert rules for proactive monitoring
- Configured for 15-day data retention

### Grafana Dashboards
- **Application Performance**: Overview of application metrics
- **Alerts & System Health**: System health and alert status
- Pre-configured visualizations for key metrics

### AlertManager
- Email notifications for alerts
- Slack integration for team notifications
- Inhibition rules to prevent alert spam

## Production Deployment

For production deployment:

1. **Update configurations**:
   - Replace localhost with actual hostnames
   - Update SMTP settings for email notifications
   - Configure Slack webhook URL

2. **Security considerations**:
   - Change default Grafana password
   - Use HTTPS for all endpoints
   - Restrict access to monitoring endpoints

3. **Scaling**:
   - Adjust Prometheus retention period as needed
   - Consider multiple Prometheus instances for high availability
   - Set up Grafana authentication and user management

## Maintenance

### Backup and Restore
- Prometheus data: Back up the `prometheus_data` volume
- Grafana dashboards: Export dashboards regularly
- AlertManager configuration: Version control all configs

### Monitoring the Monitoring Stack
- Monitor Prometheus memory usage
- Watch for Grafana performance issues
- Ensure AlertManager is processing alerts correctly

## Troubleshooting

### Common Issues
1. **No metrics showing**: Check application is running and metrics endpoint is accessible
2. **Alerts not firing**: Verify alert rules and AlertManager configuration
3. **Grafana not connecting**: Check Prometheus data source configuration

### Logs
Check container logs for debugging:
```bash
docker-compose logs -f prometheus
docker-compose logs -f grafana
docker-compose logs -f alertmanager
```