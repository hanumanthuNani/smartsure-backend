@echo off
echo [1/4] Creating network: smartsure_network...
docker network inspect smartsure_network >nul 2>&1 || docker network create smartsure_network

:: I removed the parentheses and used "and" to avoid batch errors
echo [2/4] Starting Core Dependencies - RabbitMQ and Zipkin...
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 --network smartsure_network rabbitmq:3-management
docker run -d --name zipkin -p 9411:9411 --network smartsure_network openzipkin/zipkin

echo [3/4] Starting Monitoring Stack...
docker-compose -f docker-compose-monitoring.yml up -d

echo [4/4] Verifying Containers...
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo Setup Complete!
pause