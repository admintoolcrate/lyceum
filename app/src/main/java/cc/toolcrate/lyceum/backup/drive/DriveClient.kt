package cc.toolcrate.lyceum.backup.drive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class DriveException(val code: Int, message: String) : IOException(message)

/**
 * A minimal Google Drive v3 client for the app data folder: a hidden, per-app folder in the user's own
 * Drive. With the drive.appdata scope Lyceum sees nothing else in their Drive, and nothing else sees this
 * folder. This is the only code in Lyceum that uses the network.
 */
class DriveClient(private val accessToken: String) {

    suspend fun findBackup(): String? = withContext(Dispatchers.IO) { findBackupBlocking() }

    suspend fun download(id: String): String = withContext(Dispatchers.IO) { request("GET", "$API/files/$id?alt=media") }

    /** Writes [content] as the one backup file, replacing the previous one. */
    suspend fun upload(content: String) = withContext(Dispatchers.IO) {
        val id = findBackupBlocking()
        if (id == null) create(content) else update(id, content)
    }

    suspend fun deleteBackup(): Boolean = withContext(Dispatchers.IO) {
        val id = findBackupBlocking() ?: return@withContext false
        request("DELETE", "$API/files/$id")
        true
    }

    private fun findBackupBlocking(): String? {
        val q = URLEncoder.encode("name = '$FILE_NAME'", "UTF-8")
        val body = request("GET", "$API/files?spaces=appDataFolder&fields=files(id)&q=$q")
        return Json.parseToJsonElement(body).jsonObject["files"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.content
    }

    private fun create(content: String) {
        // Random, so no journal text can ever contain it (the content is sealed anyway).
        val boundary = "lyceum-" + UUID.randomUUID().toString().replace("-", "")
        val metadata = JsonObject(mapOf("name" to JsonPrimitive(FILE_NAME), "parents" to JsonArray(listOf(JsonPrimitive("appDataFolder")))))
        val body = "--$boundary\r\n" +
            "Content-Type: application/json; charset=UTF-8\r\n\r\n$metadata\r\n" +
            "--$boundary\r\n" +
            "Content-Type: application/json; charset=UTF-8\r\n\r\n$content\r\n" +
            "--$boundary--"
        request("POST", "$UPLOAD/files?uploadType=multipart", body, "multipart/related; boundary=$boundary")
    }

    private fun update(id: String, content: String) {
        // HttpURLConnection can't send PATCH; Google APIs accept this override instead.
        request(
            "POST", "$UPLOAD/files/$id?uploadType=media", content, "application/json; charset=UTF-8",
            mapOf("X-HTTP-Method-Override" to "PATCH"),
        )
    }

    private fun request(
        method: String,
        url: String,
        body: String? = null,
        contentType: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", contentType)
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw DriveException(code, "Google Drive returned $code")
            return text
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        const val SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val API = "https://www.googleapis.com/drive/v3"
        private const val UPLOAD = "https://www.googleapis.com/upload/drive/v3"
        const val FILE_NAME = "lyceum-backup.json"
    }
}
