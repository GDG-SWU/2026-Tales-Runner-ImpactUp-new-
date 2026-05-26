package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.databinding.ActivityOnboarding1Binding

class Onboarding1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: binding.ivIllustration.setImageResource(R.drawable.onboarding_illustration)

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
        }
    }
}
