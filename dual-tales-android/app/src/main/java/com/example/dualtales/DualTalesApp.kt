package com.example.dualtales

import android.app.Application
import com.example.dualtales.network.TokenManager
import com.example.dualtales.network.UserManager

class DualTalesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
        UserManager.init(this)
    }
}
