package com.shop.pos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bottomNav = findViewById(R.id.bottom_navigation)
        titleTextView = findViewById(R.id.textViewDashboardTitle)
        val buttonSettings = findViewById<ImageButton>(R.id.buttonSettings)

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard -> {
                    titleTextView.text = "Dashboard"
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.navigation_sales -> {
                    titleTextView.text = "Sales"
                    loadFragment(SalesFragment())
                    true
                }
                R.id.navigation_inventory -> {
                    titleTextView.text = "Inventory"
                    loadFragment(InventoryFragment())
                    true
                }
                R.id.navigation_purchases -> {
                    titleTextView.text = "Purchases"
                    loadFragment(PurchasesFragment())
                    true
                }
                // Navigation case အသစ် ထပ်ထည့်ပါ
                R.id.navigation_expenses -> {
                    titleTextView.text = "Expenses"
                    loadFragment(ExpensesFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navigation_dashboard
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}