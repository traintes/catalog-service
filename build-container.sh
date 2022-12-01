./gradlew clean bootJar
docker build -t catalog-service .
grype catalog-service
