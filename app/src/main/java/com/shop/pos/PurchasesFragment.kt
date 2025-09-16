package com.shop.pos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PurchasesFragment : Fragment(), PurchaseItemListener {

    private lateinit var purchasesRecyclerView: RecyclerView
    private lateinit var purchasesAdapter: PurchasesAdapter
    private lateinit var fabAddPurchase: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        purchasesRecyclerView = view.findViewById(R.id.recyclerViewPurchases)
        fabAddPurchase = view.findViewById(R.id.fabAddPurchase)

        purchasesAdapter = PurchasesAdapter(PurchasesRepository.getPurchaseItems(), this)
        purchasesRecyclerView.adapter = purchasesAdapter
        purchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddPurchase.setOnClickListener {
            val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        purchasesAdapter.notifyDataSetChanged()
    }

    override fun onMarkAsArrived(position: Int) {
        val purchaseItem = PurchasesRepository.markAsArrived(position)
        if (purchaseItem != null) {
            InventoryRepository.addStockFromPurchase(purchaseItem.items)
            purchasesAdapter.notifyItemChanged(position)
            Toast.makeText(requireContext(), "${purchaseItem.supplierName} မှ ပစ္စည်းများ လက်ကျန်ထဲရောက်ရှိပါပြီ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEditItem(position: Int) {
        val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
        intent.putExtra("EDIT_PURCHASE_POSITION", position)
        startActivity(intent)
    }

    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("မှတ်တမ်း ဖျက်ရန်")
            .setMessage("ဒီအဝယ်မှတ်တမ်းကို ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                PurchasesRepository.deletePurchaseItem(position)
                purchasesAdapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}