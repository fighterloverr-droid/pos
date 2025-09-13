package com.shop.pos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
class SalesFragment : Fragment(), SalesItemListener {

    // UI elements အသစ်တွေကို ကြေညာပါ
    private lateinit var editTextCustomerAddress: EditText
    private lateinit var editTextDiscount: EditText
    private lateinit var editTextDeliveryFee: EditText
    private lateinit var radioGroupPayment: RadioGroup

    // ရှိပြီးသား UI elements
    private lateinit var salesRecyclerView: RecyclerView
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var buttonAddItem: Button
    private lateinit var textViewSubtotal: TextView
    private lateinit var textViewGrandTotal: TextView
    private lateinit var editTextCustomerName: EditText
    private lateinit var editTextCustomerPhone: EditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonConfirmSale: Button

    private val salesItems = mutableListOf<SaleItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI Element တွေအားလုံးကို ချိတ်ဆက်ပါ
        bindViews(view)

        salesAdapter = SalesAdapter(salesItems, this)
        salesRecyclerView.adapter = salesAdapter
        salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupListeners()

        updateSummary()
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
        // UI element အသစ်တွေကို ချိတ်ဆက်ပါ
        editTextCustomerAddress = view.findViewById(R.id.editTextCustomerAddress)
        editTextDiscount = view.findViewById(R.id.editTextDiscount)
        editTextDeliveryFee = view.findViewById(R.id.editTextDeliveryFee)
        radioGroupPayment = view.findViewById(R.id.radioGroupPayment)
    }

    private fun setupListeners() {
        buttonAddItem.setOnClickListener { showProductSelectionDialog() }
        buttonCancel.setOnClickListener { clearSale() }
        buttonConfirmSale.setOnClickListener { confirmSale() }

        // --- TextWatcher Logic အသစ် ---
        // Discount (သို့) Delivery Fee EditText ထဲမှာ စာရိုက်လိုက်တိုင်း summary ကို update လုပ်ရန်
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

    private fun updateSummary() {
        val subtotal = salesItems.sumOf { it.quantity * it.price }

        // EditText ထဲက စာသားကို Double အဖြစ်ပြောင်းပါ၊ ဘာမှမရှိရင် 0.0 လို့ သတ်မှတ်ပါ
        val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0

        val grandTotal = subtotal - discount + deliveryFee

        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        textViewSubtotal.text = "စုစုပေါင်း: ${numberFormat.format(subtotal.toInt())} Ks"
        textViewGrandTotal.text = "ကျသင့်ငွေ: ${numberFormat.format(grandTotal.toInt())} Ks"
    }

    private fun confirmSale() {
        if (salesItems.isEmpty()) {
            Toast.makeText(requireContext(), "ကျေးဇူးပြု၍ ပစ္စည်းများ အရင်ထည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        // ... (stock checking logic is the same)
        val isStockAvailable = InventoryRepository.deductStockFromSale(salesItems)
        if (!isStockAvailable) {
            Toast.makeText(requireContext(), "ပစ္စည်းလက်ကျန်မလုံလောက်ပါ!", Toast.LENGTH_LONG).show()
            return
        }

        // --- Data အပြည့်အစုံကို စုစည်းပါ (The rest of the logic is the same)
        val customerName = editTextCustomerName.text.toString()
        val customerPhone = editTextCustomerPhone.text.toString()
        val customerAddress = editTextCustomerAddress.text.toString()

        val subtotal = salesItems.sumOf { it.quantity * it.price }
        val discount = editTextDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val deliveryFee = editTextDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
        val totalAmount = subtotal - discount + deliveryFee

        val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
        val paymentType = view?.findViewById<RadioButton>(selectedPaymentId)?.text.toString()

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
            saleDate = currentDate
        )

        SalesRepository.addSaleRecord(saleRecord)

        // --- Toast message အစား VoucherActivity ကို ဖွင့်ပါ ---
        val intent = Intent(requireContext(), VoucherActivity::class.java)
        intent.putExtra("EXTRA_SALE_RECORD", saleRecord)
        startActivity(intent)

        clearSale()
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
        updateSummary()
    }

    // --- ကျန်တဲ့ function တွေက အရင်အတိုင်းပါပဲ ---
    private fun showProductSelectionDialog() {
        val inventory = InventoryRepository.getInventoryItems()
        if (inventory.isEmpty()) {
            Toast.makeText(requireContext(), "Inventory ထဲတွင် ပစ္စည်းမရှိပါ", Toast.LENGTH_SHORT).show()
            return
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
                    val newItem = SaleItem(name = selectedItem.name, quantity = 1, price = selectedItem.price)
                    salesItems.add(newItem)
                    salesAdapter.notifyItemInserted(salesItems.size - 1)
                }

                updateSummary()
                dialog.dismiss()
            }
            .create()
            .show()
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
            salesAdapter.notifyItemChanged(position)
        } else {
            salesItems.removeAt(position)
            salesAdapter.notifyItemRemoved(position)
        }
        updateSummary()
    }
}