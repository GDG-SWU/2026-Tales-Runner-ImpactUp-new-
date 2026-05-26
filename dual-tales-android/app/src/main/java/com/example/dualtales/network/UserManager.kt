package com.example.dualtales.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.userDataStore by preferencesDataStore(name = "user_info")

object UserManager {
    private val NICKNAME_KEY = stringPreferencesKey("nickname")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val USER_ID_KEY = longPreferencesKey("user_id")
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun saveUser(id: Long, email: String, nickname: String) = runBlocking {
        appContext.userDataStore.edit {
            it[USER_ID_KEY] = id
            it[EMAIL_KEY] = email
            it[NICKNAME_KEY] = nickname
        }
    }

    fun getNickname(): String = runBlocking {
        appContext.userDataStore.data.first()[NICKNAME_KEY] ?: ""
    }

    fun getEmail(): String = runBlocking {
        appContext.userDataStore.data.first()[EMAIL_KEY] ?: ""
    }

    fun getUserId(): Long = runBlocking {
        appContext.userDataStore.data.first()[USER_ID_KEY] ?: 0L
    }

    fun clear() = runBlocking {
        appContext.userDataStore.edit { it.clear() }
    }
}
