package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class InventoryFragment : Fragment(), InventoryItemListener {

    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var inventoryRepository: InventoryRepository

    private var inventoryItems = mutableListOf<InventoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = (requireActivity().application as PosApplication).database.inventoryDao()
        inventoryRepository = InventoryRepository(dao)

        inventoryRecyclerView = view.findViewById(R.id.recyclerViewInventory)
        fabAddItem = view.findViewById(R.id.fabAddItem)

        inventoryAdapter = InventoryAdapter(inventoryItems, this)
        inventoryRecyclerView.adapter = inventoryAdapter
        inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadInventoryItems()
    }

    private fun loadInventoryItems() {
        lifecycleScope.launch {
            val itemsFromDb = inventoryRepository.getInventoryItems()
            inventoryItems.clear()
            inventoryItems.addAll(itemsFromDb)
            activity?.runOnUiThread {
                inventoryAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddItemDialog(position: Int = -1) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)

        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextItemCode = dialogView.findViewById<EditText>(R.id.editTextItemCode)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val editTextCostPrice = dialogView.findViewById<EditText>(R.id.editTextCostPrice)
        val switchForSale = dialogView.findViewById<SwitchMaterial>(R.id.switchForSale)

        val isEditing = position != -1
        val dialogTitle =
            if (isEditing) "ပစ္စည်း အချက်အလက် ပြင်ဆင်ရန်" else "ပစ္စည်းအသစ် ထည့်သွင်းပါ"

        if (isEditing) {
            val item = inventoryItems[position]
            editTextItemName.setText(item.name)
            editTextItemCode.setText(item.code)
            editTextQuantity.setText(item.stockQuantity.toString())
            editTextPrice.setText(item.price.toString())
            editTextCostPrice.setText(item.costPrice.toString())
            switchForSale.isChecked = item.isForSale
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("သိမ်းမည်") { dialog, _ ->
                val name = editTextItemName.text.toString()
                val code = editTextItemCode.text.toString()
                val quantityStr = editTextQuantity.text.toString()
                val priceStr = editTextPrice.text.toString()
                val costPriceStr = editTextCostPrice.text.toString()
                val isForSale = switchForSale.isChecked

                // code အကွက်ကလွဲပြီး ကျန်တဲ့အကွက်တွေ ပြည့်စုံရင် save ခွင့်ပြုပါ
                if (name.isNotEmpty() && quantityStr.isNotEmpty() && priceStr.isNotEmpty() && costPriceStr.isNotEmpty()) {
                    val price = priceStr.toDouble()
                    val costPrice = costPriceStr.toDouble()

                    lifecycleScope.launch {
                        if (isEditing) {
                            val oldItem = inventoryItems[position]
                            val updatedItem = oldItem.copy(
                                name = name,
                                code = code, // code က အလွတ်ဖြစ်ချင်ဖြစ်နေပါမယ်
                                stockQuantity = quantityStr.toInt(),
                                price = price,
                                costPrice = costPrice,
                                isForSale = isForSale
                            )
                            inventoryRepository.updateInventoryItem(updatedItem)
                        } else {
                            val newItem = InventoryItem(
                                name = name,
                                code = code, // code က အလွတ်ဖြစ်ချင်ဖြစ်နေပါမယ်
                                stockQuantity = quantityStr.toInt(),
                                price = price,
                                costPrice = costPrice,
                                isForSale = isForSale
                            )
                            inventoryRepository.addInventoryItem(newItem)
                        }
                        loadInventoryItems()
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "အချက်အလက် အပြည့်အစုံ ဖြည့်ပါ",
                        Toast.LENGTH_SHORT
                    ).show()
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
                lifecycleScope.launch {
                    val itemToDelete = inventoryItems[position]
                    inventoryRepository.deleteInventoryItem(itemToDelete)
                    loadInventoryItems()
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