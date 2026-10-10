# App-adapter

## Описание проекта
App Adapter — это высоконагруженный микросервис-интегратор на Spring Boot, реализующий асинхронное взаимодействие между базой данных Oracle и Apache Kafka по паттерну Outbox. Сервис обеспечивает гарантированную доставку сообщений, многопоточную обработку данных, идемпотентность и предоставляет встроенные инструменты для мониторинга и управления в реальном времени.

## Оглавление
- [Описание проекта](#описание-проекта)
- [Структура проекта](#структура-проекта)
- [Ключевые возможности](#ключевые-возможности)
- [Технологический стек](#технологический-стек)
- [Основной функционал](#основной-функционал)
- [Жизненный цикл сообщения](#Жизненный-цикл-сообщения)
- [API Endpoints](#api-endpoints)
- [Команды для запуска](#команды-для-запуска)

## Структура проекта
<details>
  <summary> Нажмите, чтобы посмотреть структуру папок </summary>

```text
app-adapter/
├── src/
│   ├── main/
│   │   ├── java/foodcards/adapter/
│   │   │   ├── builder/                 # Построители DTO и Entity (EntityBuilder, KafkaBuilder)
│   │   │   ├── config/                  # Конфигурация Spring, Kafka и Async
│   │   │   ├── controller/              # REST контроллеры (AdminRestController)
│   │   │   ├── dao/                     # Слой доступа к данным (AppAdapterDAO)
│   │   │   ├── dto/                     # Data Transfer Objects для Kafka
│   │   │   ├── mapper/                  # MyBatis мапперы (интерфейсы)
│   │   │   ├── models/                  # Entity классы (AppAdapterTransEntity и др.)
│   │   │   ├── service/
│   │   │   │   ├── consumer/            # Логика получения и обработки сообщений из Kafka
│   │   │   │   ├── producer/            # Логика отправки (WorkerTask, TaskGeneratorService)
│   │   │   │   └── WorkerManagerService # Управление жизненным циклом воркеров
│   │   │   ├── utils/                   # Утилиты (TimeParserUtil, Constants)
│   │   │   └── AppAdapterApplication.java # Точка входа
│   │   └── resources/
│   │       ├── application.yml          # Конфигурация приложения
│   │       └── mapper/                  # XML файлы с SQL-запросами MyBatis
│   └── test/                            # Unit тесты
├── build.gradle                         # Скрипт сборки Gradle
├── settings.gradle
├── docker-compose.yml                   # Конфигурация для локального запуска (с Kafka и Oracle)
└── README.md
```
</details>

### Ключевые возможности:
- **Паттерн Outbox: Гарантия доставки сообщений даже при падении приложения или Kafka.**
- **Безопасная многопоточность: Использование @Async, prototype бинов и FOR UPDATE SKIP LOCKED для предотвращения блокировок и дублирования обработки.**
- **Graceful Shutdown: Корректное завершение работы потоков и фиксация транзакций при перезапуске (@PreDestroy + Spring Lifecycle).**
- **Динамическая конфигурация: Чтение списков топиков и параметров потоков напрямую из таблицы APP_ADAPTER_CONFIG без перезапуска приложения.**
- **Встроенный мониторинг: REST API для отслеживания статусов транзакций, количества активных воркеров и их принудительного перезапуска.**
- **Идемпотентность: Защита от повторной обработки одинаковых сообщений через проверку requestId.**

## Технологический стек
### Основные технологии
| Категория | Технология | Версия | Назначение |
|-----------|------------|--------|------------|
| **Framework** | Spring Boot | 3.2.5 | Основной фреймворк |
| **Язык** | Java | 21 | Язык программирования |
| **Build Tool** | Gradle | 8.5 | Сборка проекта |

### Базы данных
| СУБД | Драйвер | Назначение |
|------|---------|------------|
| **Oracle** | ojdbc11 21.9.0.0 | Хранение GRU данных (`GruVistaTab`) |

### Message Broker
| Технология | Версия | Назначение |
|------|---------|------------|
| **Apache Kafka** | 3.7.0 | Асинхронная передача сообщений между сервисами |
| **Spring Kafka** | 3.1 | Интеграция Kafka со Spring Boot |

### ORM и доступ к данным
| Библиотека | Версия | Назначение |
|------|---------|------------|
| **MyBatis** | 3.0.3 | Маппинг Java-объектов в SQL (XML-мапперы) |
| **MyBatis Spring Boot** | 3.0.3 |Интеграция MyBatis со Spring Boot |

### Утилиты и сериализация
| Библиотека | Версия | Назначение |
|------|---------|------------|
| **Lombok** | 1.18.30 | Уменьшение boilerplate кода |
| **Jackson Databind** | 2.15.3 | Сериализация/десериализация JSON |
| **Jackson JSR310** | 2.15.3 | Поддержка Java Time API в JSON |

## Основной функционал
1. Планировщик (WorkerTask): Фоновые потоки, которые с заданным интервалом (например, 5 сек) опрашивают таблицу GRU_VISTA_TAB на наличие записей со статусом WAIT.
2. Блокировка и подготовка: Захват N записей (batch-size) с использованием FOR UPDATE SKIP LOCKED, мгновенная смена статуса на PROGRESS и сборка JSON-сообщения.
3. Сохранение в Outbox: Атомарная запись метаданных отправки в APP_ADAPTER_TRANS и APP_ADAPTER_IO_MSGS со статусом SENT_TO_KAFKA.
4. Отправка в Kafka: Публикация сообщения в исходящий топик (например, gru.food.out).
5. Обработка ответа (Consumer): Прослушивание входящих топиков (gru.food.in), валидация requestId, обновление бизнес-данных в GRU_VISTA_TAB и финальная смена статуса транзакции на PROCESSED.

## Жизненный цикл сообщения
Статусы в транзакционной таблице (APP_ADAPTER_TRANS) позволяют точно отслеживать, на каком этапе находится обработка:

1. WAIT: Запись создана внешней системой, ожидает обработки.
2. PROGRESS: Воркер забрал запись, заблокировал её в БД.
3. SENT_TO_KAFKA: Сообщение успешно сериализовано, сохранено в лог и отправлено в Kafka.
4. PROCESSED: Получен ответ от внешней системы, бизнес-данные обновлены, транзакция закрыта.
5. ERROR → Произошла внутренняя ошибка или внешняя система вернула отказ (детали в GRU_REJECT_TAB).

## API Endpoints
| Метод | Endpoint | Описание | 
|-----------|------------|--------|
| GET | /admin/health | Базовая проверка доступности сервиса (возвращает детальную сводку). |
| GET | /admin/stats | Получить сводную статистику по статусам транзакций и количеству активных воркеров. |
| GET | /admin/workers | Получить список имён (entityType) текущих активных воркеров. |
| POST | /admin/workers/restart | Инициировать безопасный перезапуск всех фоновых воркеров. |
| POST | /admin/workers/restart/{entityType} | Перезапустить конкретный воркер по типу сущности (например, ACCOUNT). |

## Команды для запуска

### Сборка проекта
1. Сборка проекта
```bash
./gradlew clean build
```

2. Запуск Docker Compose
```bash
docker-compose up -d --build
```

2. Запуск через JAR (Standalone)
```bash
java -jar build/libs/app-adapter-1.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1 \
  --spring.datasource.username=GRU \
  --spring.datasource.password=your_password \
  --spring.kafka.bootstrap-servers=localhost:9092
```

### Проверка здоровья сервиса
```bash
curl http://localhost:8080/admin/health
```

Пример ответа
```json
{
  "status": "UP",
  "timestamp": 1728576000000,
  "active_workers": 2,
  "transactions": {
    "WAIT": 0,
    "PROGRESS": 2,
    "SENT_TO_KAFKA": 15,
    "PROCESSED": 120,
    "ERROR": 3
  }
}
```


### Получение статистики
```bash
curl http://localhost:8080/admin/stats
```

Пример ответа
```json
{
  "adapter_transactions": {
    "WAIT": 0,
    "PROGRESS": 0,
    "SENT_TO_KAFKA": 15,
    "PROCESSED": 120,
    "ERROR": 2
  },
  "active_workers": 2,
  "service_status": "RUNNING"
}
```

### Перезапуск воркера
```bash
curl -X POST http://localhost:8080/admin/workers/restart/ACCOUNT
```

Пример ответа
```json
{
  "status": "success",
  "message": "Воркер ACCOUNT перезапущен"
}
```
