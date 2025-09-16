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
    private lateinit var purchasesRepository: PurchasesRepository
    private var purchaseItems = mutableListOf<PurchaseItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val purchaseDao = (requireActivity().application as PosApplication).database.purchaseDao()
        purchasesRepository = PurchasesRepository(purchaseDao)

        purchasesRecyclerView = view.findViewById(R.id.recyclerViewPurchases)
        fabAddPurchase = view.findViewById(R.id.fabAddPurchase)

        purchasesAdapter = PurchasesAdapter(purchaseItems, this)
        purchasesRecyclerView.adapter = purchasesAdapter
        purchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddPurchase.setOnClickListener {
            // Edit မဟုတ်တဲ့အတွက် -1 ကို position အဖြစ် ပို့စရာမလိုပါ
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
            purchasesAdapter.notifyDataSetChanged()
        }
    }

    override fun onMarkAsArrived(position: Int) {
        lifecycleScope.launch {
            val item = purchaseItems[position]
            if (!item.hasArrived) {
                val updatedItem = item.copy(hasArrived = true)
                purchasesRepository.updatePurchaseItem(updatedItem)

                val inventoryDao = (requireActivity().application as PosApplication).database.inventoryDao()
                val inventoryRepository = InventoryRepository(inventoryDao)
                inventoryRepository.addStockFromPurchase(updatedItem.items)

                loadPurchases()
                Toast.makeText(requireContext(), "${item.supplierName} မှ ပစ္စည်းများ လက်ကျန်ထဲရောက်ရှိပါပြီ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- မဖြစ်မနေလိုအပ်သော Function (၂) ခုကို ထပ်ထည့်ပါ ---

    override fun onEditItem(position: Int) {
        val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
        val itemToEdit = purchaseItems[position]
        // Database က id ကို Intent ထဲ ထည့်ပေးလိုက်ပါ
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
                    // TODO: Implement delete in repository and DAO
                    // purchasesRepository.deletePurchaseItem(itemToDelete)
                    loadPurchases()
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