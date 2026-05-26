package com.example.dualtales.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_prefs")

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    val language1 = MutableLiveData<String>()
    val language2 = MutableLiveData<String>()

    val availableLanguages = listOf("영어", "프랑스어", "중국어")

    val isCompleteEnabled = MediatorLiveData<Boolean>(false).apply {
        val update = {
            value = !language1.value.isNullOrEmpty() || !language2.value.isNullOrEmpty()
        }
        addSource(language1) { update() }
        addSource(language2) { update() }
    }

    fun saveLanguagesToDataStore() {
        viewModelScope.launch {
            getApplication<Application>().applicationContext.dataStore.edit { prefs ->
                prefs[LANG1_KEY] = language1.value.orEmpty()
                prefs[LANG2_KEY] = language2.value.orEmpty()
            }
        }
    }

    companion object {
        val LANG1_KEY = stringPreferencesKey("language1")
        val LANG2_KEY = stringPreferencesKey("language2")
    }
}
