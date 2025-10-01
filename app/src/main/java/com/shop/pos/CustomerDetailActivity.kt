package com.shop.pos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailActivity : AppCompatActivity(), SaleHistoryItemListener {

    private lateinit var salesHistoryAdapter: SalesHistoryAdapter
    private var groupedListItems = mutableListOf<SalesHistoryListItem>()
    private var customer: Customer? = null

    private lateinit var salesRepository: SalesRepository
    private lateinit var inventoryRepository: InventoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_detail)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val app = application as PosApplication
        salesRepository = SalesRepository(app.database.salesDao())
        inventoryRepository = InventoryRepository(app.database.inventoryDao())

        customer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_CUSTOMER", Customer::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_CUSTOMER")
        }

        if (customer == null) {
            Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar.title = customer?.name
        setupCustomerInfo()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadCustomerSalesHistory()
    }

    private fun setupCustomerInfo() {
        findViewById<TextView>(R.id.textViewCustomerNameDetail).text = customer?.name
        findViewById<TextView>(R.id.textViewCustomerPhoneDetail).text = customer?.phone
        findViewById<TextView>(R.id.textViewCustomerAddressDetail).text = customer?.address
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewCustomerSales)
        salesHistoryAdapter = SalesHistoryAdapter(groupedListItems, this)
        recyclerView.adapter = salesHistoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCustomerSalesHistory() {
        lifecycleScope.launch {
            val salesForCustomer = customer?.name?.let { salesRepository.getSalesForCustomer(it) } ?: emptyList()
            groupAndDisplaySales(salesForCustomer)
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
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dateCal = Calendar.getInstance().apply { time = date }
        return when {
            todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) && todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR) -> "Today, $dateString"
            yesterdayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) && yesterdayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR) -> "Yesterday, $dateString"
            else -> dateString
        }
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
            loadCustomerSalesHistory()
            runOnUiThread{
                Toast.makeText(this@CustomerDetailActivity, "Status updated to Paid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleDeliveryStatus(saleRecord: SaleRecord, delivered: Boolean) {
        lifecycleScope.launch {
            val recordToUpdate = saleRecord.copy(isDelivered = delivered)
            salesRepository.updateSaleRecord(recordToUpdate)
            loadCustomerSalesHistory()
            runOnUiThread {
                val status = if(delivered) "Delivered" else "Not Delivered"
                Toast.makeText(this@CustomerDetailActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
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
                        inventoryRepository.addStockFromCancelledSale(it)
                    }
                    salesRepository.deleteSaleRecord(saleRecord)
                    loadCustomerSalesHistory()
                    runOnUiThread {
                        Toast.makeText(this@CustomerDetailActivity, "အရောင်းကို Cancel လုပ်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
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
