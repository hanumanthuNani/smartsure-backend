@echo off
echo [1/4] Stopping Monitoring Stack...
docker-compose -f docker-compose-monitoring.yml down

echo [2/4] Removing RabbitMQ and Zipkin...
:: Stopping and removing the individual containers
docker stop rabbitmq zipkin >nul 2>&1
docker rm rabbitmq zipkin >nul 2>&1

echo [3/4] Deleting Network: smartsure_network...
docker network rm smartsure_network >nul 2>&1

echo [4/4] Cleanup Complete.
echo.
echo All monitoring and core services stopped and removed.
:: Shows remaining containers (should be empty if nothing else is running)
docker ps -a
pause