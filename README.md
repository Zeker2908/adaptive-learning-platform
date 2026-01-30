# 🧠 adaptive-learning-platform — Адаптивная платформа для подготовки к техническим собеседованиям

**MVP-система с персонализированным обучением на основе анализа прогресса пользователя**

## 📝 Описание

Этот репозиторий содержит набор микросервисов на **Spring Boot**, реализующих адаптивную платформу для подготовки к техническим собеседованиям. Система анализирует уровень знаний пользователя и динамически формирует персональную очередь задач, фокусируясь на слабых темах.

### Основные компоненты:

- **Auth Service** — регистрация, аутентификация, управление аккаунтом
- **Task Service** — управление задачами, тегами и сложностью
- **Solution Service** — хранение решений, расчёт прогресса, рекомендации
- **Sandbox Service** — интеграция с Judge0 для безопасного запуска кода
- **Notification Service** — email-уведомления через Kafka
- **API Gateway** — единая точка входа, валидация JWT, маршрутизация
- **Инфраструктура**: PostgreSQL, Redis, Kafka + Zookeeper (через Docker Compose)

---

## 🔧 Технологический стек

- **Язык и фреймворки**: Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0
- **Безопасность**: JWT (EC-256), Spring Security, OAuth2 (Google)
- **Хранилища**:
  - PostgreSQL (Spring Data JPA + Liquibase)
  - Redis — кэш, сессии, токены
- **Месседжинг**: Apache Kafka (Spring Kafka)
- **Инструменты**: Lombok, MapStruct, Caffeine, Thymeleaf
- **Документация**: OpenAPI 3 (Swagger UI), Javadoc
- **Контейнеризация**: Docker, Docker Compose

---

## 🚀 Начало работы

### Требования

- Java 21
- Maven 3.8+
- Docker & Docker Compose
- Переменные окружения:

```bash
export CLIENT_ID=<Google OAuth2 Client ID>
export CLIENT_SECRET=<Google OAuth2 Client Secret>
export ADMIN_EMAIL=<email администратора по умолчанию>
export MAIL_USERNAME=<SMTP username>
export MAIL_PASSWORD=<SMTP password>
export JUDGE0_URL=http://<ваш-judge0-хост>:2358  # или публичный
```

### Генерация ключей JWT

```bash
mkdir -p secrets/keys
cd secrets/keys
openssl ecparam -name prime256v1 -genkey -noout -out ec-private.pem
openssl pkcs8 -topk8 -nocrypt -in ec-private.pem -out private.pem
openssl ec -in private.pem -pubout -out public.pem
```

### Запуск

```bash
# Из корня репозитория
docker compose up --build
```

После запуска:
- API доступно на `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- База данных и сервисы инициализируются автоматически (включая 15 демо-задач)
- В логах authentication-service при первом запуске будет пароль для админа
---

## ✅ Реализованный функционал (MVP)

### Для пользователя:
- ✅ Регистрация и авторизация (email + OAuth2)
- ✅ Входное тестирование (10–15 случайных задач)
- ✅ Запуск кода в песочнице (Python, JavaScript)
- ✅ Адаптивные рекомендации на основе рейтинга по темам
- ✅ Личный кабинет с прогрессом и статистикой активности

### Для администратора:
- ✅ CRUD операции с задачами
- ✅ Назначение тегов и уровня сложности
- ✅ Управление через REST API

### Системные особенности:
- 🔄 **Идемпотентная обработка** Kafka-сообщений
- ⏱️ **Таймаут решений** (зависшие задачи помечаются как `TIMEOUT`)
- 📊 **Прогресс по темам** с нормализацией по количеству тегов
- 🧪 **Тестирование на одном случайном тест-кейсе** (для ускорения)
- 🗂️ **Демо-данные** создаются автоматически при первом запуске

---

## 📂 Структура проекта

```
adaptive-learning-platform/
├── api-gateway/             # Spring Cloud Gateway
├── authentication-service/  # Auth + JWT + OAuth2
├── notification-service/    # Email через Kafka + Thymeleaf
├── task-service/            # Задачи, теги, сложность
├── solution-service/        # Решения, прогресс, рекомендации
├── sandbox-service/         # Интеграция с Judge0
├── common-module/                  # Общие DTO, исключения, утилиты
├── secrets/                 # Ключи (не в репозитории)
├── postman/                 # Коллекция постмана
├── docker-compose.yml       # Запуск всей инфраструктуры
└── README.md
```

## 🧪 Postman Collection

Для удобства тестирования API предоставлена коллекция Postman:

- [📥 Скачать коллекцию](postman/adaptive-learning-platform.postman_collection.json)

Инструкция:
1. Открой Postman.
2. Нажми **Import** → **Upload Files**.
3. Выбери скачанный файл.


---

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку: `git checkout -b feature/YourFeature`
3. Сделайте коммит: `git commit -m "Добавил фичу"`
4. Запушьте: `git push origin feature/YourFeature`
5. Откройте Pull Request

> Пожалуйста, соблюдайте стиль кода и добавляйте тесты.

---

## 📞 Контакты

Maintainer: **Leonid** – [mud.runner@bk.ru](mailto:mud.runner@bk.ru)  
Проект создан в рамках учебного MVP по ТЗ «Адаптивная платформа для подготовки к техническим собеседованиям».

---

## 📄 Лицензия

Distributed under the **Apache License Version 2.0**. See `LICENSE` for more information.