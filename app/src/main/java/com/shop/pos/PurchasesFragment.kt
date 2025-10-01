package com.shop.pos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PurchasesFragment : Fragment(), PurchaseItemListener {

    private lateinit var purchasesRecyclerView: RecyclerView
    private lateinit var purchasesAdapter: PurchasesAdapter
    private lateinit var fabAddPurchase: FloatingActionButton

    // Repositories
    private lateinit var purchasesRepository: PurchasesRepository
    private lateinit var inventoryRepository: InventoryRepository

    // Data list
    private var purchaseItems = mutableListOf<PurchaseItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as PosApplication
        purchasesRepository = PurchasesRepository(app.database.purchaseDao())
        inventoryRepository = InventoryRepository(app.database.inventoryDao())

        purchasesRecyclerView = view.findViewById(R.id.recyclerViewPurchases)
        fabAddPurchase = view.findViewById(R.id.fabAddPurchase)

        purchasesAdapter = PurchasesAdapter(purchaseItems, this)
        purchasesRecyclerView.adapter = purchasesAdapter
        purchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddPurchase.setOnClickListener {
            startActivity(Intent(requireContext(), AddPurchaseActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadPurchases()
    }

    private fun loadPurchases() {
        lifecycleScope.launch {
            val itemsFromDb = purchasesRepository.getPurchaseItems()
            purchaseItems.clear()
            purchaseItems.addAll(itemsFromDb)
            // UI update ကို main thread မှာ ပြန်လုပ်ပါ
            activity?.runOnUiThread {
                purchasesAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onMarkAsArrived(position: Int) {
        lifecycleScope.launch {
            val itemToUpdate = purchaseItems[position]
            if (!itemToUpdate.hasArrived) {
                val updatedItem = itemToUpdate.copy(hasArrived = true)
                purchasesRepository.updatePurchaseItem(updatedItem)

                // Null check ထည့်ပြီးမှ stock ပေါင်းပါ
                updatedItem.items?.let {
                    updatedItem.items?.let { inventoryRepository.addStockFromPurchase(it) }
                }

                loadPurchases() // List ကို အသစ်ပြန်ခေါ်ပါ

                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "${itemToUpdate.supplierName} မှ ပစ္စည်းများ လက်ကျန်ထဲရောက်ရှိပါပြီ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onEditItem(position: Int) {
        val itemToEdit = purchaseItems[position]
        val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
        intent.putExtra("EDIT_PURCHASE_ID", itemToEdit.id)
        startActivity(intent)
    }

    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("မှတ်တမ်း ဖျက်ရန်")
            .setMessage("ဒီအဝယ်မှတ်တမ်းကို ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                lifecycleScope.launch {
                    val itemToDelete = purchaseItems[position]
                    // TODO: Add logic to revert stock if purchase was already marked as arrived
                    purchasesRepository.deletePurchaseItem(itemToDelete)
                    loadPurchases() // List ကို အသစ်ပြန်ခေါ်ပါ
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
