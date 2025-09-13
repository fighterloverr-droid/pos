package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SalesFragment : Fragment() {

    private lateinit var salesRecyclerView: RecyclerView
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var buttonAddItem: Button

    // List ကို ပြောင်းလဲနိုင်အောင် MutableList အဖြစ် ကြေညာပါ
    private val salesItems = mutableListOf<SaleItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // နမူနာ Data တွေကို List ထဲ အရင်ထည့်ပါ
        if (salesItems.isEmpty()) {
            loadSampleData()
        }

        // UI element တွေကို ရှာပြီး ချိတ်ဆက်ပါ
        salesRecyclerView = view.findViewById(R.id.recyclerViewSalesItems)
        buttonAddItem = view.findViewById(R.id.buttonAddItem)

        // Adapter ကို salesItems list နဲ့ တည်ဆောက်ပါ
        // requireContext() က Fragment ရဲ့ context ကို ရယူပေးပါတယ်
        salesAdapter = SalesAdapter(salesItems)

        salesRecyclerView.adapter = salesAdapter
        salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // "Add Item" ခလုတ်ကို နှိပ်လိုက်ရင် Dialog ပေါ်လာအောင် Listener တပ်ပါ
        buttonAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    // နမူနာ data ထည့်သွင်းရန် function
    private fun loadSampleData() {
        salesItems.add(SaleItem("Coca-Cola", 2, 1000.0))
        salesItems.add(SaleItem("Potato Chips", 1, 1500.0))
        salesItems.add(SaleItem("Chocolate Bar", 3, 800.0))
        salesItems.add(SaleItem("Mineral Water", 1, 500.0))
    }

    // ပစ္စည်းအသစ်ထည့်သွင်းဖို့ Dialog Box ပြပေးမယ့် function
    private fun showAddItemDialog() {
        // dialog_add_item.xml layout ကို ပြသရန် ပြင်ဆင်ပါ
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("ပစ္စည်းအသစ် ထည့်သွင်းပါ")
            .setView(dialogView)
            .setPositiveButton("ထည့်မည်") { dialog, _ ->
                val name = editTextItemName.text.toString()
                val quantityStr = editTextQuantity.text.toString()
                val priceStr = editTextPrice.text.toString()

                // Data တွေ အကုန်ပြည့်စုံမှ List ထဲ ထည့်ပါ
                if (name.isNotEmpty() && quantityStr.isNotEmpty() && priceStr.isNotEmpty()) {
                    val newItem = SaleItem(name, quantityStr.toInt(), priceStr.toDouble())
                    salesItems.add(newItem)

                    // Adapter ကို Data အသစ်ဝင်လာကြောင်း အသိပေးပြီး list ကို update လုပ်ပါ
                    salesAdapter.notifyItemInserted(salesItems.size - 1)

                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "အချက်အလက် အပြည့်အစုံ ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }

        builder.create().show()
    }
}