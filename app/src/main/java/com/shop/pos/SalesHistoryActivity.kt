package com.shop.pos

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SalesHistoryActivity : AppCompatActivity() {

    private lateinit var salesHistoryRecyclerView: RecyclerView
    private lateinit var salesHistoryAdapter: SalesHistoryAdapter
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        salesHistoryRecyclerView = findViewById(R.id.recyclerViewSalesHistory)

        salesHistoryAdapter = SalesHistoryAdapter(SalesRepository.getSaleRecords())
        salesHistoryRecyclerView.adapter = salesHistoryAdapter
        salesHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sales_history_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                salesHistoryAdapter.filter.filter(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    // Menu item (calendar icon) ကို နှိပ်လိုက်ရင် အလုပ်လုပ်မယ့် function အသစ်
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter_by_date -> {
                showDatePickerDialogForFilter()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Filter လုပ်ရန် Date Picker Dialog ကို ပြသပေးမယ့် function အသစ်
    private fun showDatePickerDialogForFilter() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // User ရွေးလိုက်တဲ့ ရက်စွဲကို ပုံစံ format ချပါ
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val myFormat = "dd-MMM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                val selectedDateString = sdf.format(selectedCalendar.time)

                // Adapter ရဲ့ filter ကို ထိုရက်စွဲနဲ့ ခေါ်သုံးပါ
                salesHistoryAdapter.filter.filter(selectedDateString)
            },
            year,
            month,
            day
        )

        // "Filter ရှင်းရန်" button ထပ်ထည့်ပါ
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Filter ရှင်းရန်") { _, _ ->
            // Filter ကို ရှင်းလင်းရန် "" (empty string) နဲ့ filter ကို ခေါ်ပါ
            salesHistoryAdapter.filter.filter("")
        }

        datePickerDialog.show()
    }
}