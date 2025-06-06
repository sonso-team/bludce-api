# Блюдце API
БЛЮДЦЕ - API для кейса на хакатоне в Питере 2025

## Стек технологий:
* Kotlin
* Spring Boot
* Web-Socket
* PostgreSQL
* OpenFeign
* OpenAPI (Swagger)
* Docker
* Github Actions

## Инструкция по запуску
Для запуска приложения на своем компьютере вам необходимо иметь:
- Git
- Docker
- Java 21 или выше

***ИНСТРУКЦИЯ ПО ЗАПУСКУ ПРЕДНАЗНАЧЕНА ДЛЯ ЗАПУСКА НА ОС WINDOWS***

Для начала откройте ***Терминал Windows*** *(Win + R, введите cmd и нажмите Выполнить)*

Запуск приложения проходит в несколько этапов:
### 1. Клонирование репозитория
Для запуска приложения вам необходимо склонировать репозиторий. Для этого необходимо ввести команду:
  ```shell
  git clone https://github.com/sonso-team/bludce-api.git
  cd bludce-api
  ```
После выполнения этих команд вы окажитесь в директории проекта и можете перейти к следующему этапу

### 2. Упаковка Jar-файлов
Для запуска приложения внутри Docker контейнеров необходимо собрать Jar архивы. Для этого выполните команду:
  ```shell
  gradlew clean build
  ```
Приложение установит все необходимые зависимости и пройдет через все этапы сборки проекта *(включая прохождение контроля качества кода)*

### 3. Запуск Docker контейнеров
Финальным этапом служит запуск докер контейнеров. Для этого необходимо выполнить команду:
  ```shell
  docker compose up -d --build
  ```
Данная команда запустит сборку образов и запуск контейнеров Docker в фоновом режиме

***ВЫ ГОТОВЫ К РАБОТЕ***
