package com.pocketfm.analytics.internal

import org.json.JSONObject

internal object JsonUtil {
    fun metadataToJson(metadata: Map<String, String>): String {
        return JSONObject(metadata).toString()
    }

    fun metadataFromJson(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()
        val obj = JSONObject(json)
        val result = LinkedHashMap<String, String>()
        obj.keys().forEach { key -> result[key] = obj.optString(key, "") }
        return result
    }
}
