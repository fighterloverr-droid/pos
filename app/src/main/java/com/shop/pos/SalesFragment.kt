package com.shop.pos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesFragment : Fragment(), SalesItemListener {

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

    private val salesItems = mutableListOf<SaleItem>()
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var salesRepository: SalesRepository
    private lateinit var customerRepository: CustomerRepository

    private var allCustomers = listOf<Customer>()
    private var selectedCustomer: Customer? = null

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCANNED_CODE")
            if (!scannedCode.isNullOrEmpty()) {
                findAndAddItemByCode(scannedCode)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- ကျွမ်းကျင်သူ အကြံပေးထားသည့်အတိုင်း ပြင်ဆင်ခြင်း ---
        val app = requireActivity().application as PosApplication
        inventoryRepository = InventoryRepository(app.database.inventoryDao())
        salesRepository = SalesRepository(app.database.salesDao())
        customerRepository = CustomerRepository(app.database.customerDao())
        // ----------------------------------------------------

        bindViews(view)

        salesAdapter = SalesAdapter(salesItems, this)
        salesRecyclerView.adapter = salesAdapter
        salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupListeners()
        updateSummary()

        loadAllCustomers()
    }

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
            startActivity(Intent(requireContext(), SalesHistoryActivity::class.java))
        }
        buttonScanCode.setOnClickListener { showScannerSelectionDialog() }
        editTextCustomerName.setOnClickListener { showCustomerSelectionDialog() }


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

    private fun loadAllCustomers() {
        lifecycleScope.launch {
            allCustomers = customerRepository.getAllCustomers()
        }
    }

    private fun showCustomerSelectionDialog() {
        val customerNames = allCustomers.map { "${it.name} (${it.phone})" }.toMutableList()
        customerNames.add(0, "+ Customer အသစ်ထည့်ရန်")

        AlertDialog.Builder(requireContext())
            .setTitle("Customer ရွေးချယ်ပါ")
            .setItems(customerNames.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    showAddNewCustomerDialog()
                } else {
                    selectedCustomer = allCustomers[which - 1]
                    editTextCustomerName.setText(selectedCustomer?.name)
                    editTextCustomerPhone.setText(selectedCustomer?.phone)
                    editTextCustomerAddress.setText(selectedCustomer?.address)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddNewCustomerDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_customer, null)
        val etName = dialogView.findViewById<EditText>(R.id.editTextCustomerNameDialog)
        val etPhone = dialogView.findViewById<EditText>(R.id.editTextCustomerPhoneDialog)
        val etAddress = dialogView.findViewById<EditText>(R.id.editTextCustomerAddressDialog)

        AlertDialog.Builder(requireContext())
            .setTitle("Customer အသစ်ထည့်ရန်")
            .setView(dialogView)
            .setPositiveButton("သိမ်းမည်") { dialog, _ ->
                val name = etName.text.toString()
                val phone = etPhone.text.toString()
                val address = etAddress.text.toString()

                if (name.isNotEmpty()) {
                    val newCustomer = Customer(name = name, phone = phone, address = address)
                    lifecycleScope.launch {
                        customerRepository.insertCustomer(newCustomer)
                        loadAllCustomers()
                    }
                    editTextCustomerName.setText(name)
                    editTextCustomerPhone.setText(phone)
                    editTextCustomerAddress.setText(address)
                    selectedCustomer = newCustomer
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Customer အမည်ထည့်ပါ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("မလုပ်တော့ပါ", null)
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
        selectedCustomer = null
        updateSummary()
    }

    private fun showScannerSelectionDialog() {
        val options = arrayOf("Scan Code (OCR)", "Scan Barcode")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Scanner Type")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> scannerLauncher.launch(Intent(requireContext(), ScannerActivity::class.java))
                    1 -> scannerLauncher.launch(Intent(requireContext(), BarcodeScannerActivity::class.java))
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun findAndAddItemByCode(code: String) {
        lifecycleScope.launch {
            val itemFromDb = inventoryRepository.findItemByCode(code)
            activity?.runOnUiThread {
                if (itemFromDb != null) {
                    val existingItemInCart = salesItems.find { it.name == itemFromDb.name }
                    if (existingItemInCart != null) {
                        val index = salesItems.indexOf(existingItemInCart)
                        onIncreaseQuantity(index)
                    } else {
                        val newItem = SaleItem(
                            name = itemFromDb.name,
                            quantity = 1,
                            price = itemFromDb.price,
                            costPrice = itemFromDb.costPrice,
                            imageUri = itemFromDb.imageUri
                        )
                        salesItems.add(newItem)
                        salesAdapter.notifyItemInserted(salesItems.size - 1)
                    }
                    updateSummary()
                } else {
                    Toast.makeText(requireContext(), "Code '$code' not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProductSelectionDialog() {
        lifecycleScope.launch {
            val inventory = inventoryRepository.getForSaleItems()
            if (inventory.isEmpty()) {
                activity?.runOnUiThread { Toast.makeText(requireContext(), "အရောင်းတင်ရန် ပစ္စည်းမရှိပါ", Toast.LENGTH_SHORT).show() }
                return@launch
            }
            val itemNames = inventory.map { "${it.name} (လက်ကျန်: ${it.stockQuantity})" }.toTypedArray()

            activity?.runOnUiThread {
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
                                costPrice = selectedItem.costPrice,
                                imageUri = selectedItem.imageUri
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
    }

    private fun confirmSale() {
        lifecycleScope.launch {
            if (salesItems.isEmpty()) {
                activity?.runOnUiThread { Toast.makeText(requireContext(), "ကျေးဇူးပြု၍ ပစ္စည်းများ အရင်ထည့်ပါ", Toast.LENGTH_SHORT).show() }
                return@launch
            }

            val isStockAvailable = inventoryRepository.deductStockFromSale(salesItems)
            if (!isStockAvailable) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "ပစ္စည်းလက်ကျန်မလုံလောက်ပါ!", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            val customerName = editTextCustomerName.text.toString()
            val customerPhone = editTextCustomerPhone.text.toString()
            val customerAddress = editTextCustomerAddress.text.toString()

            if (selectedCustomer == null && customerName.isNotEmpty()) {
                val newCustomer = Customer(name = customerName, phone = customerPhone, address = customerAddress)
                customerRepository.insertCustomer(newCustomer)
                loadAllCustomers()
            }

            val subtotal = salesItems.sumOf { it.quantity * it.price }
            val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
            val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
            val totalAmount = subtotal - discount + deliveryFee
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
            val paymentType = view?.findViewById<RadioButton>(selectedPaymentId)?.text.toString() ?: "N/A"
            val isDelivered = switchDelivered.isChecked
            val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val saleRecord = SaleRecord(
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

            salesRepository.addSaleRecord(saleRecord)

            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "အရောင်း အောင်မြင်ပါသည်", Toast.LENGTH_LONG).show()
                val intent = Intent(requireContext(), VoucherActivity::class.java)
                intent.putExtra("EXTRA_SALE_RECORD", saleRecord)
                startActivity(intent)
                clearSale()
            }
        }
    }

    override fun onDeleteItem(position: Int) {
        salesItems.removeAt(position)
        salesAdapter.notifyItemRemoved(position)
        updateSummary()
    }

    // --- UPDATED FUNCTIONS START ---

    override fun onIncreaseQuantity(position: Int) {
        val item = salesItems[position]
        val newQuantity = item.quantity + 1

        lifecycleScope.launch {
            val inventoryItem = inventoryRepository.findItemByName(item.name)
            if (inventoryItem != null) {
                var newPrice = item.price
                // Check for wholesale price
                if (inventoryItem.wholesaleQuantity != null && inventoryItem.wholesalePrice != null) {
                    if (newQuantity >= inventoryItem.wholesaleQuantity) {
                        newPrice = inventoryItem.wholesalePrice
                    }
                }
                salesItems[position] = item.copy(quantity = newQuantity, price = newPrice)

                activity?.runOnUiThread {
                    salesAdapter.notifyItemChanged(position)
                    updateSummary()
                }
            }
        }
    }

    override fun onDecreaseQuantity(position: Int) {
        val item = salesItems[position]

        if (item.quantity > 1) {
            val newQuantity = item.quantity - 1
            lifecycleScope.launch {
                val inventoryItem = inventoryRepository.findItemByName(item.name)
                if (inventoryItem != null) {
                    var newPrice = item.price
                    // Check if quantity drops below wholesale threshold
                    if (inventoryItem.wholesaleQuantity != null && newQuantity < inventoryItem.wholesaleQuantity) {
                        newPrice = inventoryItem.price // Revert to retail price
                    } else if (inventoryItem.wholesaleQuantity != null && inventoryItem.wholesalePrice != null && newQuantity >= inventoryItem.wholesaleQuantity) {
                        newPrice = inventoryItem.wholesalePrice // Keep wholesale price
                    }
                    salesItems[position] = item.copy(quantity = newQuantity, price = newPrice)

                    activity?.runOnUiThread {
                        salesAdapter.notifyItemChanged(position)
                        updateSummary()
                    }
                }
            }
        } else {
            // If quantity is 1, just delete the item
            onDeleteItem(position)
        }
    }

    // --- UPDATED FUNCTIONS END ---
}