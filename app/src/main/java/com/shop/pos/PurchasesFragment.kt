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

class PurchasesFragment : Fragment(), PurchaseWorkflowListener {

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

    // "ပစ္စည်းရောက်ရှိ" ခလုတ်ကို နှိပ်လိုက်ရင် အလုပ်လုပ်မယ့် function
    override fun onMarkAsArrived(position: Int) {
        val purchaseItem = PurchasesRepository.markAsArrived(position)
        if (purchaseItem != null) {
            InventoryRepository.addStockFromPurchase(purchaseItem.items)
            purchasesAdapter.notifyItemChanged(position)
            Toast.makeText(requireContext(), "${purchaseItem.supplierName} မှ ပစ္စည်းများ လက်ကျန်ထဲရောက်ရှိပါပြီ", Toast.LENGTH_SHORT).show()
        }
    }
}