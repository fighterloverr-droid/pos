package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InventoryFragment : Fragment(), InventoryItemListener {

    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var fabAddItem: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventoryRecyclerView = view.findViewById(R.id.recyclerViewInventory)
        fabAddItem = view.findViewById(R.id.fabAddItem)

        inventoryAdapter = InventoryAdapter(InventoryRepository.getInventoryItems(), this)

        inventoryRecyclerView.adapter = inventoryAdapter
        inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        inventoryAdapter.notifyDataSetChanged()
    }

    private fun showAddItemDialog(position: Int = -1) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)
        // EditText အသစ်ကို ချိတ်ဆက်ပါ
        val editTextCostPrice = dialogView.findViewById<EditText>(R.id.editTextCostPrice)

        val isEditing = position != -1
        val dialogTitle = if(isEditing) "ပစ္စည်း အချက်အလက် ပြင်ဆင်ရန်" else "ပစ္စည်းအသစ် ထည့်သွင်းပါ"

        if (isEditing) {
            val item = InventoryRepository.getInventoryItems()[position]
            editTextItemName.setText(item.name)
            editTextQuantity.setText(item.stockQuantity.toString())
            editTextPrice.setText(item.price.toString())
            // data အဟောင်းကို EditText အသစ်မှာ ဖြည့်ပါ
            editTextCostPrice.setText(item.costPrice.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("သိမ်းမည်") { dialog, _ ->
                val name = editTextItemName.text.toString()
                val quantityStr = editTextQuantity.text.toString()
                val priceStr = editTextPrice.text.toString()
                // EditText အသစ်က data ကို ရယူပါ
                val costPriceStr = editTextCostPrice.text.toString()

                if (name.isNotEmpty() && quantityStr.isNotEmpty() && priceStr.isNotEmpty() && costPriceStr.isNotEmpty()) {
                    val price = priceStr.toDouble()
                    val costPrice = costPriceStr.toDouble()

                    val item = InventoryItem(
                        name = name,
                        stockQuantity = quantityStr.toInt(),
                        price = price,
                        costPrice = costPrice
                    )
                    if(isEditing) {
                        val oldItem = InventoryRepository.getInventoryItems()[position]
                        val updatedItemWithSoldCount = item.copy(soldQuantity = oldItem.soldQuantity)
                        InventoryRepository.updateInventoryItem(position, updatedItemWithSoldCount)
                        inventoryAdapter.notifyItemChanged(position)
                    } else {
                        InventoryRepository.addInventoryItem(item)
                        inventoryAdapter.notifyItemInserted(InventoryRepository.getInventoryItems().size - 1)
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "အချက်အလက် အပြည့်အစုံ ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun onEditItem(position: Int) {
        showAddItemDialog(position)
    }

    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("ပစ္စည်း ဖျက်ရန်")
            .setMessage("ဒီပစ္စည်းကို စာရင်းထဲက ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                InventoryRepository.deleteInventoryItem(position)
                inventoryAdapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}