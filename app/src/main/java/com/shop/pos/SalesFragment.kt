package com.shop.pos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SalesFragment : Fragment(), SalesItemListener {

    // UI Components
    private lateinit var editTextCustomerAddress: EditText
    private lateinit var editTextDiscount: EditText
    private lateinit var editTextDeliveryFee: EditText
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var salesRecyclerView: RecyclerView
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var buttonAddItem: Button
    private lateinit var textViewSubtotal: TextView
    private lateinit var textViewGrandTotal: TextView
    private lateinit var editTextCustomerName: EditText
    private lateinit var editTextCustomerPhone: EditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonConfirmSale: Button
    private lateinit var switchDelivered: SwitchMaterial
    private lateinit var buttonViewSalesHistory: Button
    private lateinit var buttonScanCode: ImageButton

    // Data & Repositories
    private val salesItems = mutableListOf<SaleItem>()
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var salesRepository: SalesRepository
    private var editingSaleId = -1

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCANNED_CODE")
            if (!scannedCode.isNullOrEmpty()) {
                findAndAddItemByCode(scannedCode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as PosApplication
        inventoryRepository = InventoryRepository(app.database.inventoryDao())
        salesRepository = SalesRepository(app.database.salesDao())

        arguments?.let {
            editingSaleId = it.getInt("EDIT_SALE_ID", -1)
        }

        bindViews(view)

        salesAdapter = SalesAdapter(salesItems, this)
        salesRecyclerView.adapter = salesAdapter
        salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupListeners()

        if (editingSaleId != -1) {
            buttonConfirmSale.text = "Update Sale"
            loadSaleForEditing()
        } else {
            buttonConfirmSale.text = "Confirm Sale"
            updateSummary()
        }
    }

    private fun loadSaleForEditing() {
        lifecycleScope.launch {
            val saleRecord = salesRepository.getSaleById(editingSaleId)
            if (saleRecord != null) {
                // အရေးကြီး: Edit မလုပ်ခင် stock တွေကို ယာယီပြန်ပေါင်းထည့်ပါ
                inventoryRepository.addStockFromCancelledSale(saleRecord.items)

                // UI မှာ data တွေ ပြန်ဖြည့်ပါ
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

    private fun confirmSale() {
        if (salesItems.isEmpty()) {
            Toast.makeText(requireContext(), "ကျေးဇူးပြု၍ ပစ္စည်းများ အရင်ထည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // ပစ္စည်းအရေအတွက် အသစ်များဖြင့် stock ကို နုတ်ပါ
            val isStockAvailable = inventoryRepository.deductStockFromSale(salesItems)
            if (!isStockAvailable) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "ပစ္စည်းလက်ကျန်မလုံလောက်ပါ!", Toast.LENGTH_LONG).show()
                }

                // အရေးကြီး: Stock မလောက်ပါက Edit Mode ဝင်စဉ်က ယာယီပြန်ပေါင်းထားသော stock ကို ပြန်နုတ်ပြီး မူလအခြေအနေအတိုင်းပြန်ထားပါ
                if (editingSaleId != -1) {
                    val originalRecord = salesRepository.getSaleById(editingSaleId)
                    if (originalRecord != null) {
                        inventoryRepository.deductStockFromSale(originalRecord.items)
                    }
                }
                return@launch
            }

            // UI မှ data များကို SaleRecord object အဖြစ် ပြောင်းပါ
            val customerName = editTextCustomerName.text.toString()
            val customerPhone = editTextCustomerPhone.text.toString()
            val customerAddress = editTextCustomerAddress.text.toString()
            val subtotal = salesItems.sumOf { it.quantity * it.price }
            val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
            val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
            val totalAmount = subtotal - discount + deliveryFee
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            val paymentType = view?.findViewById<RadioButton>(selectedPaymentId)?.text.toString() ?: "N/A"
            val isDelivered = switchDelivered.isChecked
            val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            // Mode အလိုက် Add သို့မဟုတ် Update လုပ်ပါ
            if (editingSaleId != -1) {
                // --- Edit Mode ---
                val updatedRecord = SaleRecord(
                    id = editingSaleId, // ID အဟောင်းကို သုံးပါ
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    items = salesItems.toList(),
                    subtotal = subtotal,
                    discount = discount,
                    deliveryFee = deliveryFee,
                    totalAmount = totalAmount,
                    paymentType = paymentType,
                    paymentStatus = paymentType, // You might need different logic here
                    isDelivered = isDelivered,
                    saleDate = currentDate
                )
                salesRepository.updateSaleRecord(updatedRecord)
            } else {
                // --- Add Mode ---
                val newRecord = SaleRecord(
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
                    saleDate = currentDate
                )
                salesRepository.addSaleRecord(newRecord)
            }

            activity?.runOnUiThread {
                Toast.makeText(requireContext(), if(editingSaleId != -1) "ပြင်ဆင်ပြီးပါပြီ" else "အရောင်း အောင်မြင်ပါသည်", Toast.LENGTH_LONG).show()
                if (editingSaleId != -1) {
                    // Edit ပြီးရင် Sales History ကို ပြန်သွားပါ
                    val intent = Intent(requireContext(), SalesHistoryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    // Add ပြီးရင် form ကို ရှင်းလင်းပါ (Voucher ကိုသွားလိုက comment ဖြုတ်ပါ)
                    clearSale()
//                    val intent = Intent(requireContext(), VoucherActivity::class.java)
//                    intent.putExtra("EXTRA_SALE_RECORD", newRecord)
//                    startActivity(intent)
                }
            }
        }
    }

    // --- Other Helper and Listener Functions ---

    private fun bindViews(view: View) {
        salesRecyclerView = view.findViewById(R.id.recyclerViewSalesItems)
        buttonAddItem = view.findViewById(R.id.buttonAddItem)
        textViewSubtotal = view.findViewById(R.id.textViewSubtotal)
        textViewGrandTotal = view.findViewById(R.id.textViewGrandTotal)
        editTextCustomerName = view.findViewById(R.id.editTextCustomerName)
        editTextCustomerPhone = view.findViewById(R.id.editTextCustomerPhone)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        buttonConfirmSale = view.findViewById(R.id.buttonConfirmSale)
        editTextCustomerAddress = view.findViewById(R.id.editTextCustomerAddress)
        editTextDiscount = view.findViewById(R.id.editTextDiscount)
        editTextDeliveryFee = view.findViewById(R.id.editTextDeliveryFee)
        radioGroupPayment = view.findViewById(R.id.radioGroupPayment)
        switchDelivered = view.findViewById(R.id.switchDelivered)
        buttonViewSalesHistory = view.findViewById(R.id.buttonViewSalesHistory)
        buttonScanCode = view.findViewById(R.id.buttonScanCode)
    }

    private fun setupListeners() {
        buttonAddItem.setOnClickListener { showProductSelectionDialog() }
        buttonCancel.setOnClickListener { clearSale() }
        buttonConfirmSale.setOnClickListener { confirmSale() }

        buttonViewSalesHistory.setOnClickListener {
            val intent = Intent(requireContext(), SalesHistoryActivity::class.java)
            startActivity(intent)
        }

        buttonScanCode.setOnClickListener {
            showScannerSelectionDialog()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSummary()
            }
        }
        editTextDiscount.addTextChangedListener(textWatcher)
        editTextDeliveryFee.addTextChangedListener(textWatcher)
    }

    private fun showScannerSelectionDialog() {
        val options = arrayOf("Scan Code (OCR)", "Scan Barcode")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Scanner Type")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // OCR Scanner
                        val intent = Intent(requireContext(), ScannerActivity::class.java)
                        scannerLauncher.launch(intent)
                    }
                    1 -> { // Barcode Scanner
                        val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                        scannerLauncher.launch(intent)
                    }
                }
                dialog.dismiss()
            }
            .show()
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

    private fun clearSale() {
        if (editingSaleId != -1) {
            val intent = Intent(requireContext(), SalesHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
            return
        }

        val itemCount = salesItems.size
        if (itemCount > 0) {
            salesItems.clear()
            salesAdapter.notifyItemRangeRemoved(0, itemCount)
        }
        editTextCustomerName.text.clear()
        editTextCustomerPhone.text.clear()
        editTextCustomerAddress.text.clear()
        editTextDiscount.text.clear()
        editTextDeliveryFee.text.clear()
        radioGroupPayment.check(R.id.radioButtonPaid)
        switchDelivered.isChecked = false
        updateSummary()
    }

    private fun showProductSelectionDialog() {
        lifecycleScope.launch {
            val inventory = inventoryRepository.getForSaleItems()
            if (inventory.isEmpty()) {
                Toast.makeText(requireContext(), "အရောင်းတင်ရန် ပစ္စည်းမရှိပါ", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val itemNames = inventory.map { "${it.name} (လက်ကျန်: ${it.stockQuantity})" }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("ပစ္စည်း ရွေးချယ်ပါ")
                .setItems(itemNames) { dialog, which ->
                    val selectedItem = inventory[which]
                    val existingItemInCart = salesItems.find { it.name == selectedItem.name }

                    if (existingItemInCart != null) {
                        val index = salesItems.indexOf(existingItemInCart)
                        onIncreaseQuantity(index)
                    } else {
                        val newItem = SaleItem(
                            name = selectedItem.name,
                            quantity = 1,
                            price = selectedItem.price,
                            costPrice = selectedItem.costPrice
                        )
                        salesItems.add(newItem)
                        salesAdapter.notifyItemInserted(salesItems.size - 1)
                    }
                    updateSummary()
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun findAndAddItemByCode(code: String) {
        lifecycleScope.launch {
            val itemFromDb = inventoryRepository.findItemByCode(code)

            if (itemFromDb != null) {
                activity?.runOnUiThread {
                    val existingItemInCart = salesItems.find { it.name == itemFromDb.name }
                    if (existingItemInCart != null) {
                        val index = salesItems.indexOf(existingItemInCart)
                        onIncreaseQuantity(index)
                    } else {
                        val newItem = SaleItem(
                            name = itemFromDb.name,
                            quantity = 1,
                            price = itemFromDb.price,
                            costPrice = itemFromDb.costPrice
                        )
                        salesItems.add(newItem)
                        salesAdapter.notifyItemInserted(salesItems.size - 1)
                    }
                    updateSummary()
                }
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "'$code' code နှင့် ပစ္စည်းမရှိပါ", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
            salesAdapter.notifyItemRemoved(position)
        }
        salesAdapter.notifyItemChanged(position)
        updateSummary()
    }
}