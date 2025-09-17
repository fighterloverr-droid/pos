package com.shop.pos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class SalesHistoryActivity : AppCompatActivity(), SaleHistoryItemListener {

    private lateinit var salesHistoryRecyclerView: RecyclerView
    private lateinit var salesHistoryAdapter: SalesHistoryAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var salesRepository: SalesRepository
    private lateinit var inventoryRepository: InventoryRepository

    private var salesRecords = mutableListOf<SaleRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)

        val app = application as PosApplication
        salesRepository = SalesRepository(app.database.salesDao())
        inventoryRepository = InventoryRepository(app.database.inventoryDao())

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
        salesHistoryAdapter = SalesHistoryAdapter(salesRecords, this)
        salesHistoryRecyclerView.adapter = salesHistoryAdapter
        salesHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSalesHistory() {
        lifecycleScope.launch {
            val recordsFromDb = salesRepository.getSaleRecords()
            salesRecords.clear()
            salesRecords.addAll(recordsFromDb)
            runOnUiThread {
                salesHistoryAdapter.updateList(salesRecords)
            }
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
                val filteredList = if (newText.isNullOrEmpty()) {
                    salesRecords
                } else {
                    salesRecords.filter {
                        it.customerName.contains(newText, true) ||
                                it.saleDate.contains(newText, true)
                    }
                }
                salesHistoryAdapter.updateList(filteredList)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMarkAsPaid(position: Int) {
        lifecycleScope.launch {
            val recordToUpdate = salesHistoryAdapter.getRecordAt(position)
            recordToUpdate.paymentStatus = "ငွေရပြီး"
            salesRepository.updateSaleRecord(recordToUpdate)
            loadSalesHistory()
            runOnUiThread{
                Toast.makeText(this@SalesHistoryActivity, "Status updated to Paid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCancelSale(position: Int) {
        val recordToCancel = salesHistoryAdapter.getRecordAt(position)

        AlertDialog.Builder(this)
            .setTitle("အရောင်း Cancel လုပ်ရန်")
            .setMessage("ဒီအရောင်းမှတ်တမ်းကို cancel လုပ်မှာ သေချာလား? ပစ္စည်းလက်ကျန်များ မူလအတိုင်း ပြန်ဖြစ်သွားပါမည်။")
            .setPositiveButton("အတည်ပြုမည်") { dialog, _ ->
                lifecycleScope.launch {
                    inventoryRepository.addStockFromCancelledSale(recordToCancel.items)
                    salesRepository.deleteSaleRecord(recordToCancel)
                    loadSalesHistory()

                    runOnUiThread {
                        Toast.makeText(this@SalesHistoryActivity, "အရောင်းကို Cancel လုပ်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}