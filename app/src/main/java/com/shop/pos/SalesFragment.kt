package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class SalesFragment : Fragment(), SalesItemListener {

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

        salesRecyclerView = view.findViewById(R.id.recyclerViewSalesItems)
        buttonAddItem = view.findViewById(R.id.buttonAddItem)
        textViewSubtotal = view.findViewById(R.id.textViewSubtotal)
        textViewGrandTotal = view.findViewById(R.id.textViewGrandTotal)
        editTextCustomerName = view.findViewById(R.id.editTextCustomerName)
        editTextCustomerPhone = view.findViewById(R.id.editTextCustomerPhone)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        buttonConfirmSale = view.findViewById(R.id.buttonConfirmSale)

        salesAdapter = SalesAdapter(salesItems, this)
        salesRecyclerView.adapter = salesAdapter
        salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        buttonAddItem.setOnClickListener {
            showProductSelectionDialog()
        }

        buttonCancel.setOnClickListener {
            clearSale()
        }

        buttonConfirmSale.setOnClickListener {
            confirmSale()
        }

        updateSummary()
    }

    // Manual data ထည့်တဲ့ dialog အစား Inventory က ပစ္စည်းတွေ ရွေးနိုင်တဲ့ dialog အသစ်
    private fun showProductSelectionDialog() {
        val inventory = InventoryRepository.getInventoryItems()
        if (inventory.isEmpty()) {
            Toast.makeText(requireContext(), "Inventory ထဲတွင် ပစ္စည်းမရှိပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val itemNames = inventory.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("ပစ္စည်း ရွေးချယ်ပါ")
            .setItems(itemNames) { dialog, which ->
                val selectedItem = inventory[which]

                // Cart ထဲမှာ ဒီပစ္စည်း ရှိပြီးသားလား စစ်ဆေးပါ
                val existingItem = salesItems.find { it.name == selectedItem.name }

                if (existingItem != null) {
                    // ရှိပြီးသားဆို အရေအတွက်ပဲ တိုးပါ
                    val index = salesItems.indexOf(existingItem)
                    onIncreaseQuantity(index)
                } else {
                    // မရှိသေးရင် အသစ်ထည့်ပါ
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

    private fun updateSummary() {
        var subtotal = 0.0
        salesItems.forEach { subtotal += it.quantity * it.price }

        val grandTotal = subtotal
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        textViewSubtotal.text = "စုစုပေါင်း: ${numberFormat.format(subtotal.toInt())} Ks"
        textViewGrandTotal.text = "ကျသင့်ငွေ: ${numberFormat.format(grandTotal.toInt())} Ks"
    }

    private fun clearSale() {
        val itemCount = salesItems.size
        salesItems.clear()
        salesAdapter.notifyItemRangeRemoved(0, itemCount)
        editTextCustomerName.text.clear()
        editTextCustomerPhone.text.clear()
        updateSummary()
        Toast.makeText(requireContext(), "စာရင်းကို ရှင်းလင်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
    }

    private fun confirmSale() {
        if (salesItems.isEmpty()) {
            Toast.makeText(requireContext(), "ကျေးဇူးပြု၍ ပစ္စည်းများ အရင်ထည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmountText = textViewGrandTotal.text.toString()
        val totalItems = salesItems.size

        AlertDialog.Builder(requireContext())
            .setTitle("အရောင်း အတည်ပြုချက်")
            .setMessage("စုစုပေါင်း ပစ္စည်း ${totalItems} မျိုး၊ $totalAmountText ဖြင့် ရောင်းချမှာ သေချာလား?")
            .setPositiveButton("အတည်ပြုမည်") { dialog, _ ->
                Toast.makeText(requireContext(), "အရောင်း အောင်မြင်ပါသည်", Toast.LENGTH_LONG).show()
                clearSale()
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    // --- Interface Functions ---

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