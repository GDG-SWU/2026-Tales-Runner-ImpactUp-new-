package com.example.dualtales

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.databinding.ActivityMainBinding
import com.example.dualtales.ui.fragments.BookshelfFragment
import com.example.dualtales.ui.fragments.SearchFragment
import com.example.dualtales.ui.screens.CreateFragment
import com.example.dualtales.ui.screens.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private enum class Tab { BOOKSHELF, CREATE, SEARCH, PROFILE }
    private var selectedTab = Tab.BOOKSHELF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabListeners()

        if (savedInstanceState == null) {
            handleTabExtra(intent)
        } else {
            selectedTab = Tab.entries[savedInstanceState.getInt("selected_tab", 0)]
            updateTabAppearance()
        }
    }

    // ReadingActivity 등에서 FLAG_ACTIVITY_CLEAR_TOP | SINGLE_TOP으로 돌아올 때 호출
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTabExtra(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selected_tab", selectedTab.ordinal)
    }

    private fun handleTabExtra(intent: Intent) {
        val tab = when (intent.getStringExtra("tab")) {
            "bookshelf" -> Tab.BOOKSHELF
            "search"    -> Tab.SEARCH
            "profile"   -> Tab.PROFILE
            else        -> Tab.BOOKSHELF
        }
        selectTab(tab)
    }

    private fun setupTabListeners() {
        binding.tabBookshelf.setOnClickListener { selectTab(Tab.BOOKSHELF) }
        binding.tabCreate.setOnClickListener { selectTab(Tab.CREATE) }
        binding.tabSearch.setOnClickListener { selectTab(Tab.SEARCH) }
        binding.tabProfile.setOnClickListener { selectTab(Tab.PROFILE) }
    }

    private fun selectTab(tab: Tab) {
        selectedTab = tab
        updateTabAppearance()

        when (tab) {
            Tab.BOOKSHELF -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookshelfFragment())
                .commit()
            Tab.CREATE -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateFragment())
                .commit()
            Tab.SEARCH -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment())
                .commit()
            Tab.PROFILE -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }
    }

    private fun updateTabAppearance() {
        updateSingleTab(
            binding.tabInnerBookshelf, binding.ivTabBookshelf, binding.tvTabBookshelf,
            selectedTab == Tab.BOOKSHELF
        )
        updateSingleTab(
            binding.tabInnerCreate, binding.ivTabCreate, binding.tvTabCreate,
            selectedTab == Tab.CREATE
        )
        updateSingleTab(
            binding.tabInnerSearch, binding.ivTabSearch, binding.tvTabSearch,
            selectedTab == Tab.SEARCH
        )
        updateSingleTab(
            binding.tabInnerProfile, binding.ivTabProfile, binding.tvTabProfile,
            selectedTab == Tab.PROFILE
        )
    }

    private fun updateSingleTab(
        innerContainer: LinearLayout,
        icon: ImageView,
        label: TextView,
        isSelected: Boolean
    ) {
        if (isSelected) {
            icon.setColorFilter(Color.parseColor("#C383E1"))
            label.setTextColor(Color.parseColor("#C383E1"))
        } else {
            icon.setColorFilter(Color.parseColor("#9E9E9E"))
            label.setTextColor(Color.parseColor("#9E9E9E"))
        }
    }
}
