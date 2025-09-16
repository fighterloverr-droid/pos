package com.shop.pos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class SalesHistoryActivity : AppCompatActivity() {

    private lateinit var salesHistoryRecyclerView: RecyclerView
    private lateinit var salesHistoryAdapter: SalesHistoryAdapter
    private lateinit var toolbar: MaterialToolbar
    // Repository ကို ကြေညာပါ
    private lateinit var salesRepository: SalesRepository

    // Data list ကို class level မှာ ကြေညာပါ
    private var salesRecords = mutableListOf<SaleRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)

        // Repository ကို ရယူပါ
        val dao = (application as PosApplication).database.salesDao()
        salesRepository = SalesRepository(dao)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadSalesHistory()
    }

    private fun setupRecyclerView() {
        salesHistoryRecyclerView = findViewById(R.id.recyclerViewSalesHistory)

        // Adapter ကို data list အလွတ်နဲ့ အရင်တည်ဆောက်ပါ
        salesHistoryAdapter = SalesHistoryAdapter(salesRecords)
        salesHistoryRecyclerView.adapter = salesHistoryAdapter
        salesHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Data တွေကို database ကနေ background မှာ load လုပ်ပါ
    private fun loadSalesHistory() {
        lifecycleScope.launch {
            val recordsFromDb = salesRepository.getSaleRecords()
            salesRecords.clear()
            salesRecords.addAll(recordsFromDb)
            salesHistoryAdapter.notifyDataSetChanged()
        }
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
                // Adapter ရဲ့ filter ကို ခေါ်သုံးမယ့်အစား၊ local list ကို filter လုပ်ပါ
                val filteredList = if (newText.isNullOrEmpty()) {
                    salesRecords
                } else {
                    salesRecords.filter {
                        it.customerName.contains(newText, true) ||
                                it.saleDate.contains(newText, true)
                    }
                }
                // Adapter ကို list အသစ်နဲ့ update လုပ်ပါ
                salesHistoryAdapter.updateList(filteredList)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    // ...onOptionsItemSelected function is pending for next step
}