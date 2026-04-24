package com.pocketfm.analytics.internal

import android.content.Context
import java.util.UUID

internal class IdentityManager(context: Context) {
    private val prefs = context.getSharedPreferences("pfm_analytics_identity", Context.MODE_PRIVATE)

    fun anonymousId(): String {
        return prefs.getString(KEY_ANON_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_ANON_ID, it).apply()
        }
    }

    fun userId(): String? = prefs.getString(KEY_USER_ID, null)

    fun identify(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun clearUser() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    companion object {
        private const val KEY_ANON_ID = "anonymous_id"
        private const val KEY_USER_ID = "user_id"
    }
}
