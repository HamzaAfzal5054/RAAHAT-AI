package pk.raahat.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class BackendHealth(val available: Boolean, val mode: String = "offline")

/** Lightweight Android client for the optional local/hosted RAAHAT API. */
class BackendApi(private val baseUrl: String = "http://10.0.2.2:8080") {
    suspend fun health(): BackendHealth = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("$baseUrl/health").openConnection() as HttpURLConnection
            connection.connectTimeout = 900
            connection.readTimeout = 900
            connection.requestMethod = "GET"
            connection.use { responseCode, body ->
                if (responseCode !in 200..299) error("Backend returned $responseCode")
                val json = JSONObject(body)
                BackendHealth(true, json.optString("mode", "live"))
            }
        }.getOrDefault(BackendHealth(false))
    }

    suspend fun submitReport(report: CitizenReport): SeverityAssessment = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("location", report.location)
            put("waterLevel", report.waterLevel)
            put("situations", report.situations.toList())
            put("description", report.description)
            put("photo", report.photo)
        }.toString()
        val connection = URL("$baseUrl/api/v1/reports").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 2_000
        connection.readTimeout = 2_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(payload.toByteArray()) }
        connection.use { responseCode, body ->
            if (responseCode !in 200..299) error("Report rejected: $responseCode")
            val assessment = JSONObject(body).getJSONObject("assessment")
            SeverityAssessment(
                score = assessment.getInt("score"),
                severity = Severity.valueOf(assessment.getString("severity")),
                confidence = assessment.getInt("confidence"),
                reasoning = assessment.getJSONArray("reasoning").let { array -> List(array.length()) { array.getString(it) } }
            )
        }
    }
}

private inline fun <T> HttpURLConnection.use(block: (Int, String) -> T): T = try {
    val code = responseCode
    val stream = if (code in 200..299) inputStream else errorStream
    block(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
} finally {
    disconnect()
}
