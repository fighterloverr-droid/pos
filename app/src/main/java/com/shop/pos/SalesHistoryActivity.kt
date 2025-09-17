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
    private lateinit var salesRepository: SalesRepository

    private var salesRecords = mutableListOf<SaleRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)

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
        salesHistoryAdapter = SalesHistoryAdapter(salesRecords)
        salesHistoryRecyclerView.adapter = salesHistoryAdapter
        salesHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSalesHistory() {
        lifecycleScope.launch {
            val recordsFromDb = salesRepository.getSaleRecords()
            salesRecords.clear()
            salesRecords.addAll(recordsFromDb)
            salesHistoryAdapter.updateList(salesRecords)
        }
    }

    // ... (onCreateOptionsMenu is the same as the last working version)
}