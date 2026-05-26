package com.example.dualtales.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth")

object TokenManager {
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun saveToken(token: String) = runBlocking {
        appContext.dataStore.edit { it[TOKEN_KEY] = token }
    }

    fun getToken(): String? = runBlocking {
        appContext.dataStore.data.first()[TOKEN_KEY]
    }

    fun clear() = runBlocking {
        appContext.dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
