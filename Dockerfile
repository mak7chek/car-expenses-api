# Етап 1: ЗБІРКА (наш "завод")
# Використовуємо образ, що містить і Java 17, і Gradle
FROM gradle:8.5-jdk17-alpine AS build

# Встановлюємо робочу директорію
WORKDIR /app

# Копіюємо налаштування Gradle
COPY build.gradle.kts settings.gradle.kts ./

# Копіюємо весь наш код
COPY src ./src

# Запускаємо збірку. Gradle автоматично завантажить всі залежності з build.gradle.kts
# '-x test' пропускає тести, щоб прискорити деплой на Render
RUN gradle build -x test

# -----------------------------------------------

# Етап 2: ЗАПУСК (наш "гараж")
# Беремо чистий і маленький образ тільки з Java 17
FROM openjdk:17-slim
WORKDIR /app

# Ось вона, наша команда!
# Копіюємо ТІЛЬКИ зібраний .jar з етапу 'build'
COPY --from=build /app/build/libs/*.jar app.jar

# Запускаємо наш додаток
ENTRYPOINT ["java", "-jar", "app.jar"]