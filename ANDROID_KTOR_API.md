# Интеграция HelpDeskService в Android (Ktor Client)

Руководство и примеры кода для разработки Android-приложения с использованием **Ktor Client**, **Kotlinx Serialization**, **Kotlin Coroutines / Flow** и **WebSockets**.

---

## 1. Зависимости (Gradle kts)

В `build.gradle.kts` модуля приложения (`app`):

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization) // id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    val ktorVersion = "2.3.12" // или актуальная 3.x

    // Ktor Core & OkHttp Engine
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

    // Content Negotiation & JSON Serialization
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // WebSockets
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    // Auth & Logging
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Firebase Messaging (для Push-уведомлений)
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
}
```

---

## 2. Разрешения и Сеть (AndroidManifest.xml)

В `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- Android 13+ для Push-уведомлений -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:usesCleartextTraffic="true" <!-- Только для локальной отладки по HTTP -->
        ...>
    </application>
</manifest>
```

> [!IMPORTANT]
> **Сетевые адреса:**
> - **Эмулятор Android (локально):** `http://10.0.2.2:9092`
> - **Реальное устройство (WiFi):** `http://192.168.x.x:9092`
> - **Боевой сервер (Prod):** `https://helpdesk.impuls-perm.ru/bc` (обратите внимание на контекстный путь `/bc`!)
> - **WebSocket Prod:** `wss://helpdesk.impuls-perm.ru/bc/ws/chats/{chatId}/messages`

---

## 3. Модели данных (DTO с `@Serializable`)

```kotlin
package ru.impuls.helpdesk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Авторизация ---

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("type") val tokenType: String = "Bearer"
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserBillingRequest(
    val name: String,
    val username: String,
    val email: String? = null,
    val password: String? = null,
    val token: String? = null
)

@Serializable
data class UnregisteredUserRequest(
    val name: String,
    val email: String,
    val phone: String
)

// --- Чаты и сообщения ---

@Serializable
data class ChatCreateRequest(
    val name: String? = null,
    val login: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val message: String
)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    @SerialName("oper_id") val operId: Long? = null
)

@Serializable
data class FileDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("original_name") val originalName: String? = null,
    @SerialName("mime_type") val mimeType: String? = null
)

@Serializable
data class MessageDto(
    val id: String,
    val text: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    val date: String? = null,
    @SerialName("isSupport") val isSupport: Boolean = false,
    val files: List<FileDto> = emptyList()
)

@Serializable
data class ConnectionDto(
    val id: String,
    val start: String? = null,
    val end: String? = null,
    val messages: List<MessageDto> = emptyList()
)

@Serializable
data class ChatDto(
    val id: String,
    val name: String? = null,
    val login: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("is_open") val isOpen: Boolean = true,
    @SerialName("is_work") val isWork: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("unread_messages_count") val unreadMessagesCount: Long = 0,
    @SerialName("created_date") val createdDate: String? = null,
    val group: GroupDto? = null,
    val connections: List<ConnectionDto> = emptyList()
)

// --- Регистрация Push-токена ---

@Serializable
data class TokenRegistrationRequest(
    val token: String,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
    val platform: String = "FCM" // "FCM" или "HMS"
)

// --- WebSocket Event Envelope ---

@Serializable
data class WsEvent<T>(
    val path: String,
    val event: String, // CREATED, UPDATED, DELETED, CONNECTED
    val data: T? = null
)
```

---

## 4. Конфигурация Ktor Client

Создайте синглтон или модуль Dependency Injection (Hilt / Koin) для `HttpClient`:

```kotlin
package ru.impuls.helpdesk.data.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NetworkClient(
    private val tokenStorage: TokenStorage
) {
    companion object {
        // Для Prod: "https://helpdesk.impuls-perm.ru/bc"
        // Для эмулятора Dev: "http://10.0.2.2:9092"
        const val BASE_URL = "https://helpdesk.impuls-perm.ru/bc"
        const val WS_BASE_URL = "wss://helpdesk.impuls-perm.ru/bc"
    }

    val httpClient: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
            }
        }

        // WebSockets
        install(WebSockets) {
            pingInterval = 20_000 // пинг каждые 20 сек
        }

        // JSON Сериализация
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        // Логирование запросов
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }

        // Базовый префикс и заголовки по умолчанию
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

        // Авторизация Bearer JWT с авто-подстановкой
        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenStorage.getAccessToken()
                    if (token != null) BearerTokens(token, "") else null
                }
                refreshTokens {
                    // При необходимости обновления через POST /api/auth/refresh
                    null
                }
                sendWithoutRequest { request ->
                    // Не отправлять токен на публичные эндпоинты входа
                    !request.url.encodedPath.contains("/api/auth/")
                }
            }
        }
    }
}

interface TokenStorage {
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun clear()
}
```

---

## 5. Сценарии работы API (Репозиторий)

### 5.1. Авторизация незарегистрированного гостя
Гость вводит Имя, Email и Телефон и получает временный JWT-токен:

```kotlin
suspend fun loginUnregistered(name: String, email: String, phone: String): TokenResponse {
    return httpClient.post("api/auth/unregistered-login") {
        setBody(UnregisteredUserRequest(name = name, email = email, phone = phone))
    }.body()
}
```

### 5.2. Авторизация из внешнего приложения / биллинга
```kotlin
suspend fun loginFromApp(
    name: String,
    username: String,
    email: String?,
    externalToken: String?
): TokenResponse {
    return httpClient.post("api/auth/login_from_app") {
        setBody(UserBillingRequest(
            name = name,
            username = username,
            email = email,
            token = externalToken
        ))
    }.body()
}
```

### 5.3. Получение списка чатов пользователя
```kotlin
suspend fun getUserChats(login: String? = null): List<ChatDto> {
    return httpClient.get("api/chats/user_chats") {
        login?.let { parameter("login", it) }
    }.body()
}
```

### 5.4. Создание нового обращения (Чата)
Для незарегистрированного гостя рекомендуется использовать дефолтную группу `00000000-0000-0000-0000-000000000009`:

```kotlin
suspend fun createChat(
    groupId: String = "00000000-0000-0000-0000-000000000009",
    message: String,
    name: String? = null,
    phone: String? = null,
    email: String? = null
): ChatDto {
    return httpClient.post("api/chats") {
        parameter("groupId", groupId)
        setBody(ChatCreateRequest(
            name = name,
            phone = phone,
            email = email,
            message = message
        ))
    }.body()
}
```

### 5.5. Получение полной переписки чата
```kotlin
suspend fun getChatDetails(chatId: String, count: Int = 100): ChatDto {
    return httpClient.get("api/chats/$chatId") {
        parameter("count", count)
    }.body()
}
```

---

### 5.6. Отправка сообщения клиентом с вложениями (Multipart)

> [!IMPORTANT]
> Бэкенд принимает сообщение через `@RequestPart`:
> - Часть `data` — JSON-объект `{"text": "..."}` с типом `application/json`.
> - Часть `files` — бинарные файлы с заголовком `Content-Disposition: form-data; name="files"; filename="..."`.

В Ktor Client это реализуется через `submitFormWithBinaryData`:

```kotlin
import io.ktor.client.request.forms.*
import io.ktor.http.*

suspend fun sendMessage(
    chatId: String,
    text: String,
    attachedFiles: List<Pair<String, ByteArray>> = emptyList() // Pair(fileName, fileBytes)
) {
    httpClient.submitFormWithBinaryData(
        url = "api/chats/$chatId/message/client",
        formData = formData {
            // 1. JSON часть "data"
            append(
                key = "data",
                value = """{"text": ${Json.encodeToString(text)}}""",
                headers = Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                }
            )

            // 2. Вложения "files"
            attachedFiles.forEach { (fileName, fileBytes) ->
                append(
                    key = "files",
                    value = fileBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, """form-data; name="files"; filename="$fileName"""")
                    }
                )
            }
        }
    )
}
```

### 5.7. Отметка сообщения как прочитанного
```kotlin
suspend fun markMessageAsRead(messageId: String) {
    httpClient.post("api/messages/$messageId/read")
}
```

### 5.8. Закрытие обращения
```kotlin
suspend fun closeChat(chatId: String) {
    httpClient.post("api/chats/$chatId/close")
}
```

### 5.9. Формирование ссылки на скачивание/просмотр файла
Для загрузки файлов используйте базовый URL:
```kotlin
fun getFileUrl(storedFilePath: String): String {
    return "${NetworkClient.BASE_URL}/api/files/$storedFilePath"
}
```
*(В Coil / Glide достаточно передать этот URL. Если эндпоинт публичный, токен в заголовке не требуется).*

---

## 6. WebSocket в реальном времени

Клиент подключается к каналу сообщений конкретного чата. **Токен авторизации для этого канала не требуется**, что обеспечивает быструю работу как для гостей, так и для авторизованных клиентов:

```kotlin
package ru.impuls.helpdesk.data.network

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import ru.impuls.helpdesk.data.model.MessageDto
import ru.impuls.helpdesk.data.model.WsEvent

class ChatWebSocketClient(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    /**
     * Подключается к WebSocket чата и возвращает бесконечный Flow сообщений с авто-переподключением.
     */
    fun observeChatMessages(chatId: String): Flow<MessageDto> = flow {
        val wsUrl = "${NetworkClient.WS_BASE_URL}/ws/chats/$chatId/messages"

        while (true) {
            try {
                httpClient.webSocket(urlString = wsUrl) {
                    println("WebSocket подключен к чату: $chatId")

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val event = json.decodeFromString<WsEvent<MessageDto>>(text)
                                if (event.data != null) {
                                    emit(event.data)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket отключен (${e.message}), повторное подключение через 5 секунд...")
            }
            delay(5000) // Задержка перед авто-реконнектом
        }
    }
}
```

---

## 7. Регистрация Push-уведомлений (FCM / HMS)

При получении FCM-токена в `FirebaseMessagingService`:

```kotlin
package ru.impuls.helpdesk.service

import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.impuls.helpdesk.data.model.TokenRegistrationRequest
import ru.impuls.helpdesk.data.network.NetworkClient

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Обработка входящего push-уведомления и показ Notification
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Поддержка"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = TokenRegistrationRequest(
                    token = token,
                    deviceId = getDeviceId(),
                    deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    platform = "FCM"
                )
                // Отправка POST api/fcm/register-token
                // httpClient.post("api/fcm/register-token") { setBody(request) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    private fun showNotification(title: String, body: String) {
        // Стандартный Android NotificationCompat.Builder
    }
}
```

---

## 8. Чек-лист готовности Android клиента

| Пункт | Требование | Статус |
| :--- | :--- | :--- |
| **URL окружения** | `https://helpdesk.impuls-perm.ru/bc` с обязательным префиксом `/bc` в Prod | Обязательно |
| **Шифрование** | В Prod только `https://` и `wss://` | Обязательно |
| **Длинные FCM токены** | Не обрезать токен (в базе данных бэкенда выделена колонка `TEXT`) | Поддерживается |
| **Отправка файлов** | Использовать `multipart/form-data` с частью `data` (`application/json`) и частями `files` | Проверено |
| **WebSocket** | Автореконнект при переключении WiFi/Мобильная сеть | Реализовано в `Flow` |
| **Гостевой доступ** | Создание чата с группой `00000000-0000-0000-0000-000000000009` | Поддерживается |
