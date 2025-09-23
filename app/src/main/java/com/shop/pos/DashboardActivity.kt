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
                    // getString() ကို အသုံးပြုပြီး strings.xml ထဲက စာသားကို ခေါ်သုံးပါ
                    titleTextView.text = getString(R.string.title_dashboard)
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.navigation_sales -> {
                    titleTextView.text = getString(R.string.title_sales)
                    loadFragment(SalesFragment())
                    true
                }
                R.id.navigation_inventory -> {
                    titleTextView.text = getString(R.string.title_inventory)
                    loadFragment(InventoryFragment())
                    true
                }
                R.id.navigation_purchases -> {
                    titleTextView.text = getString(R.string.title_purchases)
                    loadFragment(PurchasesFragment())
                    true
                }
                R.id.navigation_expenses -> {
                    titleTextView.text = getString(R.string.title_expenses)
                    loadFragment(ExpensesFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            if (!handleIntentExtras(intent)) {
                bottomNav.selectedItemId = R.id.navigation_dashboard
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentExtras(intent)
    }

    private fun handleIntentExtras(intent: Intent): Boolean {
        if (intent.hasExtra("TARGET_FRAGMENT")) {
            when (intent.getStringExtra("TARGET_FRAGMENT")) {
                "SALES" -> {
                    val saleId = intent.getIntExtra("EDIT_SALE_ID", -1)
                    val bundle = Bundle().apply {
                        putInt("EDIT_SALE_ID", saleId)
                    }
                    val salesFragment = SalesFragment().apply {
                        arguments = bundle
                    }
                    loadFragment(salesFragment)
                    bottomNav.selectedItemId = R.id.navigation_sales
                    return true
                }
            }
        }
        return false
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

