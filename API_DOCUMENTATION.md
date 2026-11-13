# Документация REST API и WebSocket — HelpDeskService

Сервис технической поддержки и чатов (HelpDeskService).

> [!TIP]
> **Android разработчикам:** Полное руководство по интеграции мобильного приложения на **Kotlin + Ktor Client** (с DTO, Multipart-отправкой файлов, Flow WebSocket и Push-уведомлениями) вынесено в отдельный документ: [ANDROID_KTOR_API.md](file:///D:/intellig_progects1/HelpDeskService/ANDROID_KTOR_API.md).

---

## 1. Общие сведения

### Базовые URL (Base URL)
* **Разработка (dev):** `http://localhost:8080` (или `http://localhost:8080/api/...`)
* **Продакшн (prod):** `https://helpdesk.impuls-perm.ru/bc`
  > [!NOTE]
  > В `prod` профиле настроен контекстный путь `server.servlet.context-path: /bc`. Все эндпоинты начинаются с `/bc/api/...`, а WebSocket с `/bc/ws/...`.

### Аутентификация
* **Access Token:** Передаётся в заголовке запроса:
  ```http
  Authorization: Bearer <access_jwt_token>
  ```
* **Refresh Token:** Хранится в защищенной `HttpOnly` cookie с именем `refreshToken`.
* **Роли пользователей:**
  * `ADMIN` — полный доступ ко всей системе, пользователям, логам, группам.
  * `SUPPORT` — доступ к операторской панели, присоединению к чатам, ответам клиентам, просмотру списка пользователей.
  * `USER` / `CLIENT` — авторизованный клиент (из личного кабинета / внешнего приложения).
  * **Анонимный клиент** — посетитель сайта без учетной записи, создающий обращение через форму на сайте.

---

## 2. Сценарии обращения клиента в поддержку

| Сценарий | Авторизация | Создание чата (`POST /api/chats`) | Чтение и WebSocket |
| :--- | :--- | :--- | :--- |
| **Из личного кабинета** | `Authorization: Bearer <jwt>` | Поля `name`, `login`, `email` подтягиваются из профиля автоматически (можно передать только `message`). Чат связывается с аккаунтом `User`, работают Push-уведомления. | Доступ по ID чата, в `/api/chats/user_chats` видны все чаты аккаунта. |
| **Окно на сайте (гость)** | Без заголовка (анонимно) | Заполняются поля формы (`name`, `login` или телефон, `email`, `phone`, `message`). Чат создаётся с `user = null`. | Чтение чата по `id`, WebSocket `/ws/chats/{id}/messages` открыт без токена. |

---

## 3. Аутентификация (`/api/auth`)

### 3.1. Вход по логину и паролю
* **Метод:** `POST /api/auth/login`
* **Доступ:** Публичный
* **Тело запроса:**
```json
{
  "username": "operator1",
  "password": "secret_password"
}
```
* **Ответ `200 OK`:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "type": "Bearer"
}
```
*(В заголовке `Set-Cookie` возвращается `refreshToken`).*

---

### 3.2. Вход из внешнего приложения / биллинга
* **Метод:** `POST /api/auth/login_from_app`
* **Доступ:** Публичный
* **Тело запроса:**
```json
{
  "name": "Иван Иванов",
  "username": "ivan_billing",
  "email": "ivan@example.com",
  "password": "optional_password",
  "token": "YII_CSRF_TOKEN=... или внешний токен валидации"
}
```
* **Ответ `200 OK`:** `TokenResponse` (access токен). Пользователь автоматически создается или обновляется.

---

### 3.3. Вход незарегистрированного пользователя
* **Метод:** `POST /api/auth/unregistered-login` (также доступен alias `POST /api/auth/unregistered_login`)
* **Доступ:** Публичный
* **Тело запроса:**
```json
{
  "name": "Клиент с сайта",
  "phone": "+79991234567",
  "email": "client@example.com"
}
```
* **Ответ `200 OK`:** `TokenResponse` с временным токеном для гостя.

---

### 3.4. Обновление токена доступа (Refresh)
* **Метод:** `POST /api/auth/refresh`
* **Доступ:** Публичный (требует `refreshToken` в cookie)
* **Ответ `200 OK`:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "type": "Bearer"
}
```

---

### 3.5. Выход из системы
* **Метод:** `POST /api/auth/logout`
* **Доступ:** Публичный
* Удаляет refresh токен из базы данных и очищает cookie.

---

## 4. Чаты и сообщения (`/api/chats`)

### 4.1. Создание чата / обращения
* **Метод:** `POST /api/chats?groupId={groupId}`
* **Доступ:** Публичный (может вызываться как с Bearer токеном, так и без него)
* **Параметры строки:**
  * `groupId` (UUID, обязательно) — идентификатор группы/темы обращения.
* **Тело запроса:**
```json
{
  "name": "Алексей",
  "login": "alex_client",
  "email": "alex@example.com",
  "phone": "+79990001122",
  "message": "Здравствуйте! Не работает доступ в интернет."
}
```
> [!TIP]
> Для **авторизованных клиентов** поля `name`, `login`, `email` опциональны — они подтянутся из профиля.
> Для **неавторизованных посетителей** обязательны `message` и хотя бы один контакт (`name` / `login` / `phone`).

* **Ответ `201 Created`:** `ChatDto`
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Алексей",
  "login": "alex_client",
  "email": "alex@example.com",
  "phone": "+79990001122",
  "is_open": true,
  "is_work": false,
  "is_active": true,
  "unread_messages_count": 0,
  "created_date": "2026-11-12T10:00:00Z",
  "group": { "id": "...", "name": "Техподдержка" },
  "connections": [ ... ]
}
```

---

### 4.2. Получение деталей чата и сообщений
* **Метод:** `GET /api/chats/{id}`
* **Доступ:** Публичный (доступно клиенту по сохраненному UUID чата)
* **Параметры строки (Query):**
  * `to` (Instant, опционально) — верхняя граница времени (для пагинации истории).
  * `count` (int, default: 100, max: 1000) — количество сообщений.
* **Ответ `200 OK`:** `ChatDto` с историей подключений и сообщений.

---

### 4.3. Отправка сообщения клиентом
* **Метод:** `POST /api/chats/{id}/message/client`
* **Контент:** `multipart/form-data`
* **Доступ:** Публичный (для гостя или авторизованного пользователя)
* **Части запроса (Parts):**
  * `data` (JSON, `application/json`):
    ```json
    {
      "text": "Прикрепляю скриншот ошибки"
    }
    ```
  * `files` (опционально, массив файлов): вложения (скриншоты, логи).
* **Ответ `200 OK`**

---

### 4.4. Получение чатов пользователя
* **Метод:** `GET /api/chats/user_chats`
* **Доступ:**
  * С Bearer-токеном: параметр `login` опционален (берётся из JWT).
  * Без токена: параметр `?login=...` обязателен.
* **Параметры:** `login` (string, опционально).
* **Ответ `200 OK`:** Список `ChatDto[]`.

---

### 4.5. Закрытие чата клиентом или оператором
* **Метод:** `POST /api/chats/{id}/close`
* **Доступ:** Публичный (может закрыть клиент по завершении решения вопроса).
* **Ответ `200 OK`**

---

### 4.6. Операторские методы работы с чатом
*(Требуют авторизации с ролью `SUPPORT` или `ADMIN`)*

* **Список всех чатов:**
  * `GET /api/chats`
  * **Параметры:**
    * `groupId` (UUID) — фильтр по группе.
    * `isOpen` (boolean) — открытые/закрытые.
    * `isWork` (boolean) — взят ли в работу другим оператором.
    * `isMy` (boolean) — только чаты, где текущий оператор в активном connection.
    * `search` (string) — поиск по имени, логину, тексту.
    * `count` (int, default: 100) — лимит.
* **Присоединиться к чату:**
  * `POST /api/chats/{id}/join` — оператор берет чат в работу (создается `Connection` оператора, отправляется системное уведомление).
* **Выйти из чата:**
  * `POST /api/chats/{id}/exit` — оператор освобождает чат.
* **Отправить сообщение от оператора:**
  * `POST /api/chats/{id}/message/support` (`multipart/form-data`)
  * `data`: `{ "text": "Ваш вопрос решён!" }`
  * `files`: вложения.
  *(Автоматически отправляет Push-уведомление клиенту, если он авторизован).*
* **Удалить чат:**
  * `DELETE /api/chats/{id}` (`ADMIN`).

---

## 5. Сообщения (`/api/messages`)

### Пометить сообщение как прочитанное
* **Метод:** `POST /api/messages/{messageId}/read`
* **Доступ:** Публичный / Клиентский / Операторский
* **Ответ `200 OK`**

---

## 6. Вложения и файлы (`/api/files`)

### Просмотр / Скачивание файла
* **Метод:** `GET /api/files/{filePath}`
* **Доступ:** Публичный
* **Ответ `200 OK`:** Бинарный поток файла с соответствующим `Content-Type` (image/png, application/pdf и т.д.).

---

## 7. Группы обращений (`/api/groups`)

* `GET /api/groups` — Получить все активные группы обращений.
* `GET /api/groups/{id}` — Получить группу по UUID.
* `GET /api/groups/oper?oper_id={id}` — Получить группу по ID оператора.
* `POST /api/groups` (`ADMIN`) — Создать группу (`GroupCreateRequest`: `name`, `operId`).
* `PUT /api/groups/{id}` (`ADMIN`) — Обновить группу (`GroupEditRequest`).
* `DELETE /api/groups/{id}` (`ADMIN`) — Удалить группу.

---

## 8. Пользователи (`/api/users`)

*(Требуется авторизация `ADMIN` или `SUPPORT`)*

* `GET /api/users` (`ADMIN`, `SUPPORT`) — Список пользователей.
* `POST /api/users` (`ADMIN`) — Создание пользователя:
  ```json
  {
    "name": "Оператор 2",
    "username": "support_user2",
    "email": "sup2@impuls.ru",
    "password": "Password123!",
    "role_id": "uuid-роли"
  }
  ```
* `PUT /api/users/{id}` (`ADMIN`) — Редактирование пользователя.
* `PATCH /api/users/{id}/password` (`ADMIN`) — Смена пароля:
  ```json
  {
    "new_password": "NewStrongPassword123"
  }
  ```
* `PUT /api/users/sync` (`ADMIN`, `SUPPORT`) — Пакетная синхронизация пользователей из биллинга.

---

## 9. Роли (`/api/roles`)

* `GET /api/roles` — Список системных ролей (`ADMIN`, `SUPPORT`, `CLIENT`).

---

## 10. Пуш-уведомления Firebase / Huawei HMS (`/api/fcm`)

* **Регистрация устройства:**
  * `POST /api/fcm/register-token`
  * **Доступ:** Авторизованный пользователь (`CLIENT` / `USER`).
  * **Тело запроса:**
    ```json
    {
      "token": "fcm_or_hms_device_token_string...",
      "platform": "FCM", 
      "device_id": "android_uuid_or_imei",
      "device_name": "Samsung Galaxy S24"
    }
    ```
    *(Платформа: `"FCM"` или `"HMS"`).*
* **Отправка уведомления (тест/админ):**
  * `POST /api/fcm/send-notification?token=...&title=...&body=...` (`ADMIN`).
* **Отправка по топику:**
  * `POST /api/fcm/send-topic-notification?topic=...&title=...&body=...` (`ADMIN`).

---

## 11. Системные логи (`/api/logs`)

* `GET /api/logs?to=...&count=100` (`ADMIN`) — Получить системные логи аудита действий.

---

## 12. WebSocket API

### 12.1. Точки подключения (Handshake)

1. **Глобальный канал оператора:**
   * **URL:** `ws://<host>[:<port>]/ws` (в prod: `wss://<host>/bc/ws`)
   * **Аутентификация:** Обязательна. Передаётся токен:
     * Через query параметр: `?token=<jwt_access_token>`
     * Или через заголовок `Authorization: Bearer <jwt_access_token>`.
   * **Назначение:** Получение событий по всем чатам, пользователям, группам, логам в режиме реального времени.

2. **Канал сообщений конкретного чата (для клиента и оператора):**
   * **URL:** `ws://<host>[:<port>]/ws/chats/{chatId}/messages` (в prod: `wss://<host>/bc/ws/chats/{chatId}/messages`)
   * **Аутентификация:** **Не требуется** (доступен как авторизованному клиенту, так и анонимному гостю с сайта).
   * **Назначение:** Получение новых сообщений в конкретном открытом чате.

3. **Канал подключений конкретного чата:**
   * **URL:** `ws://<host>[:<port>]/ws/chats/{chatId}/connections`
   * **Аутентификация:** Не требуется.
   * **Назначение:** Уведомления о присоединении оператора («К чату подключился...»), выходе, закрытии.

---

### 12.2. Формат сообщений WebSocket

Все сообщения от сервера приходят в едином JSON-формате:

```json
{
  "path": "/chats/3fa85f64-5717-4562-b3fc-2c963f66afa6/messages",
  "event": "UPDATED",
  "data": { ... }
}
```

#### Поля события:
* `path` (string) — маршрут/категория события:
  * `/chats` — изменение в списке чатов (создание, смена статуса).
  * `/chats/{chatId}/messages` — новое сообщение или обновление сообщений в чате.
  * `/chats/{chatId}/connections` — новое подключение/смена оператора в чате.
  * `/users` — создание или изменение пользователя.
  * `/groups` — создание или изменение группы.
  * `/logs` — системный лог (доставляется только сессиям с ролью `ADMIN`).
* `event` (string) — тип события:
  * `CREATED` — объект создан (новый чат, новое сообщение, новый пользователь).
  * `UPDATED` — объект изменён (чат взят в работу, статус обновлён, сообщение прочитано).
  * `DELETED` — объект удалён.
  * `CONNECTED` — новое подключение к чату.
* `data` (object) — тело события (соответствующий DTO: `ChatEvent`, `MessageDto`, `ConnectionDto`, `UserEvent` и т.д.).

---

### 12.3. Пример события о новом сообщении (`/chats/{id}/messages`)

```json
{
  "path": "/chats/b1a77461-1234-4b56-8a7e-123456789abc/messages",
  "event": "UPDATED",
  "data": {
    "id": "e4f828a1-5678-4a9b-9cde-9876543210fe",
    "text": "Здравствуйте! Чем я могу вам помочь?",
    "is_support": true,
    "is_read": false,
    "created_date": "2026-11-12T14:30:00Z",
    "files": []
  }
}
```

---

### 12.4. Пример клиентского JS-подключения

```javascript
const chatId = "b1a77461-1234-4b56-8a7e-123456789abc";
const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
const basePath = window.location.pathname.startsWith("/bc") ? "/bc" : "";

// Подключение к сообщениям чата (работает без авторизации)
const socket = new WebSocket(`${protocol}//${window.location.host}${basePath}/ws/chats/${chatId}/messages`);

socket.onopen = () => {
    console.log("WebSocket подключен к чату", chatId);
};

socket.onmessage = (event) => {
    const wsEvent = JSON.parse(event.data);
    console.log("Получено событие:", wsEvent.event, wsEvent.path);
    if (wsEvent.data && wsEvent.data.text) {
        displayMessage(wsEvent.data);
    }
};

socket.onclose = () => {
    console.log("WebSocket отключен");
};
```
