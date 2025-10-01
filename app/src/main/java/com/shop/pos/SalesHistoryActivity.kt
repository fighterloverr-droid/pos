package com.shop.pos

import android.app.DatePickerDialog
import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SalesHistoryActivity : AppCompatActivity(), SaleHistoryItemListener {

    private lateinit var salesHistoryRecyclerView: RecyclerView
    private lateinit var salesHistoryAdapter: SalesHistoryAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var salesRepository: SalesRepository
    private lateinit var inventoryRepository: InventoryRepository

    // Database ကနေ ဆွဲထုတ်ထားတဲ့ မူရင်း list အပြည့်အစုံ
    private var originalSalesRecords = listOf<SaleRecord>()
    // RecyclerView မှာ ပြသဖို့အတွက် Header တွေနဲ့ ပေါင်းစပ်ထားတဲ့ list
    private var groupedListItems = mutableListOf<SalesHistoryListItem>()

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
        salesHistoryAdapter = SalesHistoryAdapter(groupedListItems, this)
        salesHistoryRecyclerView.adapter = salesHistoryAdapter
        salesHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSalesHistory() {
        lifecycleScope.launch {
            originalSalesRecords = salesRepository.getSaleRecords()
            groupAndDisplaySales(originalSalesRecords)
        }
    }

    private fun groupAndDisplaySales(sales: List<SaleRecord>) {
        groupedListItems.clear()
        val groupedByDate = sales.groupBy { it.saleDate }

        groupedByDate.keys.sortedDescending().forEach { date ->
            val records = groupedByDate[date]!!
            groupedListItems.add(SalesHistoryListItem.DateHeader(formatDateHeader(date)))
            records.forEach { record ->
                groupedListItems.add(SalesHistoryListItem.SaleItem(record))
            }
        }

        runOnUiThread {
            salesHistoryAdapter.updateList(groupedListItems)
        }
    }

    private fun formatDateHeader(dateString: String): String {
        val inputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString

        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val dateCal = Calendar.getInstance().apply { time = date }

        return when {
            todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) && todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR) -> "Today, $dateString"
            yesterdayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) && yesterdayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR) -> "Yesterday, $dateString"
            else -> dateString
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sales_history_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    originalSalesRecords
                } else {
                    originalSalesRecords.filter {
                        it.customerName.contains(newText, ignoreCase = true) || it.saleDate.contains(newText, ignoreCase = true)
                    }
                }
                groupAndDisplaySales(filteredList)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter_by_date -> {
                showDatePickerDialogForFilter()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDatePickerDialogForFilter() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val myFormat = "dd-MMM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                val selectedDateString = sdf.format(selectedCalendar.time)

                val filteredList = originalSalesRecords.filter { it.saleDate == selectedDateString }
                groupAndDisplaySales(filteredList)
            },
            year, month, day
        )

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Filter ရှင်းရန်") { _, _ ->
            groupAndDisplaySales(originalSalesRecords)
        }

        datePickerDialog.show()
    }

    override fun onSaleRecordClick(saleRecord: SaleRecord) {
        val options = mutableListOf<String>()

        if (saleRecord.paymentStatus == "COD") {
            options.add("Mark as Paid")
        }
        val deliveryOption = if (saleRecord.isDelivered) "Mark as Not Delivered" else "Mark as Delivered"
        options.add(deliveryOption)

        if (options.isEmpty()){
            Toast.makeText(this, "No actions available for this item.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "Mark as Paid" -> markAsPaid(saleRecord)
                    "Mark as Delivered" -> toggleDeliveryStatus(saleRecord, true)
                    "Mark as Not Delivered" -> toggleDeliveryStatus(saleRecord, false)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun markAsPaid(saleRecord: SaleRecord) {
        lifecycleScope.launch {
            val recordToUpdate = saleRecord.copy(paymentStatus = "ငွေရပြီး")
            salesRepository.updateSaleRecord(recordToUpdate)
            loadSalesHistory()
            runOnUiThread{
                Toast.makeText(this@SalesHistoryActivity, "Status updated to Paid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleDeliveryStatus(saleRecord: SaleRecord, delivered: Boolean) {
        lifecycleScope.launch {
            val recordToUpdate = saleRecord.copy(isDelivered = delivered)
            salesRepository.updateSaleRecord(recordToUpdate)
            loadSalesHistory()
            runOnUiThread {
                val status = if(delivered) "Delivered" else "Not Delivered"
                Toast.makeText(this@SalesHistoryActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditSale(saleRecord: SaleRecord) {
        val intent = Intent(this, EditSaleActivity::class.java).apply {
            putExtra("EDIT_SALE_ID", saleRecord.id)
        }
        startActivity(intent)
    }

    override fun onCancelSale(saleRecord: SaleRecord) {
        AlertDialog.Builder(this)
            .setTitle("အရောင်း Cancel လုပ်ရန်")
            .setMessage("ဒီအရောင်းမှတ်တမ်းကို cancel လုပ်မှာ သေချာလား? ပစ္စည်းလက်ကျန်များ မူလအတိုင်း ပြန်ဖြစ်သွားပါမည်။")
            .setPositiveButton("အတည်ပြုမည်") { dialog, _ ->
                lifecycleScope.launch {
                    saleRecord.items?.let {
                        saleRecord.items?.let { inventoryRepository.addStockFromCancelledSale(it) }
                    }
                    salesRepository.deleteSaleRecord(saleRecord)
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

