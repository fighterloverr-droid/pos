package com.shop.pos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditSaleActivity : AppCompatActivity(), SalesItemListener {

    private var editingSaleId = -1
    private var originalSaleRecord: SaleRecord? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var editTextCustomerName: EditText
    private lateinit var editTextCustomerPhone: EditText
    private lateinit var editTextCustomerAddress: EditText
    private lateinit var recyclerViewSalesItems: RecyclerView
    private lateinit var buttonAddItem: Button
    private lateinit var editTextDiscount: EditText
    private lateinit var editTextDeliveryFee: EditText
    private lateinit var textViewSubtotal: TextView
    private lateinit var textViewGrandTotal: TextView
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var switchDelivered: SwitchMaterial
    private lateinit var buttonCancel: Button
    private lateinit var buttonConfirmSale: Button
    private lateinit var buttonScanCode: ImageButton

    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var salesRepository: SalesRepository

    private val salesItems = mutableListOf<SaleItem>()
    private lateinit var salesAdapter: SalesAdapter

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCANNED_CODE")
            if (!scannedCode.isNullOrEmpty()) {
                findAndAddItemByCode(scannedCode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sale)

        val app = application as PosApplication
        inventoryRepository = InventoryRepository(app.database.inventoryDao())
        salesRepository = SalesRepository(app.database.salesDao())

        bindViews()
        setupListeners()

        salesAdapter = SalesAdapter(salesItems, this)
        recyclerViewSalesItems.adapter = salesAdapter
        recyclerViewSalesItems.layoutManager = LinearLayoutManager(this)

        editingSaleId = intent.getIntExtra("EDIT_SALE_ID", -1)
        if (editingSaleId != -1) {
            toolbar.title = "အရောင်း ပြင်ဆင်ရန်"
            loadSaleForEditing()
        } else {
            Toast.makeText(this, "Error: Sale ID not found.", Toast.LENGTH_SHORT).show()
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelEdit()
            }
        })
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        editTextCustomerName = findViewById(R.id.editTextCustomerName)
        editTextCustomerPhone = findViewById(R.id.editTextCustomerPhone)
        editTextCustomerAddress = findViewById(R.id.editTextCustomerAddress)
        recyclerViewSalesItems = findViewById(R.id.recyclerViewSalesItems)
        buttonAddItem = findViewById(R.id.buttonAddItem)
        editTextDiscount = findViewById(R.id.editTextDiscount)
        editTextDeliveryFee = findViewById(R.id.editTextDeliveryFee)
        textViewSubtotal = findViewById(R.id.textViewSubtotal)
        textViewGrandTotal = findViewById(R.id.textViewGrandTotal)
        radioGroupPayment = findViewById(R.id.radioGroupPayment)
        switchDelivered = findViewById(R.id.switchDelivered)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonConfirmSale = findViewById(R.id.buttonConfirmSale)
        buttonScanCode = findViewById(R.id.buttonScanCode)
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        buttonCancel.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        buttonAddItem.setOnClickListener { showProductSelectionDialog() }
        buttonScanCode.setOnClickListener { showScannerSelectionDialog() }
        buttonConfirmSale.setOnClickListener { confirmSale() }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateSummary() }
        }
        editTextDiscount.addTextChangedListener(textWatcher)
        editTextDeliveryFee.addTextChangedListener(textWatcher)
    }

    private fun loadSaleForEditing() {
        lifecycleScope.launch {
            originalSaleRecord = salesRepository.getSaleById(editingSaleId)
            if (originalSaleRecord != null) {
                val saleRecord = originalSaleRecord!!
                inventoryRepository.addStockFromCancelledSale(saleRecord.items)

                runOnUiThread {
                    editTextCustomerName.setText(saleRecord.customerName)
                    editTextCustomerPhone.setText(saleRecord.customerPhone)
                    editTextCustomerAddress.setText(saleRecord.customerAddress)
                    editTextDiscount.setText(if (saleRecord.discount > 0) saleRecord.discount.toInt().toString() else "")
                    editTextDeliveryFee.setText(if (saleRecord.deliveryFee > 0) saleRecord.deliveryFee.toInt().toString() else "")

                    when (saleRecord.paymentType) {
                        "COD" -> radioGroupPayment.check(R.id.radioButtonCod)
                        "အကြွေး" -> radioGroupPayment.check(R.id.radioButtonCredit)
                        else -> radioGroupPayment.check(R.id.radioButtonPaid)
                    }

                    switchDelivered.isChecked = saleRecord.isDelivered

                    salesItems.clear()
                    salesItems.addAll(saleRecord.items)
                    salesAdapter.notifyDataSetChanged()
                    updateSummary()
                }
            }
        }
    }

    private fun confirmSale() {
        lifecycleScope.launch {
            val isStockAvailable = inventoryRepository.deductStockFromSale(salesItems)
            if (!isStockAvailable) {
                runOnUiThread { Toast.makeText(this@EditSaleActivity, "ပစ္စည်းလက်ကျန်မလုံလောက်ပါ!", Toast.LENGTH_LONG).show() }
                if (originalSaleRecord != null) {
                    inventoryRepository.addStockFromCancelledSale(originalSaleRecord!!.items)
                }
                return@launch
            }

            val customerName = editTextCustomerName.text.toString()
            val customerPhone = editTextCustomerPhone.text.toString()
            val customerAddress = editTextCustomerAddress.text.toString()
            val subtotal = salesItems.sumOf { it.quantity * it.price }
            val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
            val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
            val totalAmount = subtotal - discount + deliveryFee
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            val paymentType = findViewById<RadioButton>(selectedPaymentId)?.text.toString() ?: "N/A"
            val isDelivered = switchDelivered.isChecked
            val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val updatedRecord = SaleRecord(
                id = editingSaleId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                items = salesItems.toList(),
                subtotal = subtotal,
                discount = discount,
                deliveryFee = deliveryFee,
                totalAmount = totalAmount,
                paymentType = paymentType,
                paymentStatus = paymentType,
                isDelivered = isDelivered,
                saleDate = originalSaleRecord?.saleDate ?: currentDate
            )
            salesRepository.updateSaleRecord(updatedRecord)

            runOnUiThread {
                Toast.makeText(this@EditSaleActivity, "ပြင်ဆင်ပြီးပါပြီ", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun cancelEdit() {
        lifecycleScope.launch {
            if (originalSaleRecord != null) {
                // If user cancels, revert stock changes. Re-deduct original items.
                inventoryRepository.deductStockFromSale(originalSaleRecord!!.items)
            }
            finish()
        }
    }

    private fun updateSummary() {
        val subtotal = salesItems.sumOf { it.quantity * it.price }
        val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = subtotal - discount + deliveryFee
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        textViewSubtotal.text = "စုစုပေါင်း: ${numberFormat.format(subtotal.toInt())} Ks"
        textViewGrandTotal.text = "ကျသင့်ငွေ: ${numberFormat.format(grandTotal.toInt())} Ks"
    }

    private fun findAndAddItemByCode(code: String) {
        // ... (this function is the same as before)
    }

    private fun showScannerSelectionDialog() {
        // ... (this function is the same as before)
    }

    private fun showProductSelectionDialog() {
        // ... (this function is the same as before)
    }

    override fun onDeleteItem(position: Int) {
        salesItems.removeAt(position)
        salesAdapter.notifyItemRemoved(position)
        updateSummary()
    }

    override fun onIncreaseQuantity(position: Int) {
        val item = salesItems[position]
        salesItems[position] = item.copy(quantity = item.quantity + 1)
        salesAdapter.notifyItemChanged(position)
        updateSummary()
    }

    override fun onDecreaseQuantity(position: Int) {
        val item = salesItems[position]
        if (item.quantity > 1) {
            salesItems[position] = item.copy(quantity = item.quantity - 1)
        } else {
            salesItems.removeAt(position)
        }
        salesAdapter.notifyDataSetChanged()
        updateSummary()
    }
}