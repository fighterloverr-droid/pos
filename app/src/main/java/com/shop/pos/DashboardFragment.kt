package com.shop.pos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var fragmentView: View? = null
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var purchasesRepository: PurchasesRepository
    private lateinit var salesRepository: SalesRepository
    private lateinit var expensesRepository: ExpensesRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        }
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as PosApplication
        inventoryRepository = InventoryRepository(app.database.inventoryDao())
        purchasesRepository = PurchasesRepository(app.database.purchaseDao())
        salesRepository = SalesRepository(app.database.salesDao())
        expensesRepository = ExpensesRepository(app.database.expensesDao())

        val buttonViewSalesHistory = view.findViewById<Button>(R.id.buttonViewSalesHistory)
        buttonViewSalesHistory.setOnClickListener {
            val intent = Intent(requireContext(), SalesHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardCards()
    }

    private fun updateDashboardCards() {
        lifecycleScope.launch {
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            val totalSales = salesRepository.getTotalSales() ?: 0.0
            val totalExpenses = expensesRepository.getTotalExpenses() ?: 0.0
            val totalPurchases = purchasesRepository.getTotalPurchases() ?: 0.0
            val totalInventoryValue = inventoryRepository.getTotalInventoryValue()

            val grossProfit = totalSales
            val netProfit = grossProfit - totalExpenses
            val operatingCash = totalSales - totalPurchases - totalExpenses

            setupMetricCard(
                cardId = R.id.cardOperatingCash,
                title = "လုပ်ငန်းလည်ပတ်ငွေ",
                value = "${numberFormat.format(operatingCash.toInt())} Ks",
                isNegative = operatingCash < 0
            )
            setupMetricCard(
                cardId = R.id.cardInventoryValue,
                title = "လက်ကျန်ပစ္စည်းတန်ဖိုး",
                value = "${numberFormat.format(totalInventoryValue.toInt())} Ks"
            )
            setupMetricCard(
                cardId = R.id.cardNetProfit,
                title = "အသားတင်အမြတ်ငွေ",
                value = "${numberFormat.format(netProfit.toInt())} Ks",
                isNegative = netProfit < 0
            )
            setupMetricCard(
                cardId = R.id.cardGrossProfit,
                title = "အမြတ်ငွေ (စုစုပေါင်း ရောင်းရငွေ)",
                value = "${numberFormat.format(grossProfit.toInt())} Ks"
            )
        }
    }

    private fun setupMetricCard(@IdRes cardId: Int, title: String, value: String, isNegative: Boolean = false) {
        fragmentView?.let { view ->
            val cardView = view.findViewById<View>(cardId)
            val titleTextView = cardView.findViewById<TextView>(R.id.textViewMetricTitle)
            val valueTextView = cardView.findViewById<TextView>(R.id.textViewMetricValue)
            titleTextView.text = title
            valueTextView.text = value
            if (isNegative) {
                valueTextView.setTextColor(Color.RED)
            } else {
                valueTextView.setTextColor(titleTextView.currentTextColor)
            }
        }
    }
}