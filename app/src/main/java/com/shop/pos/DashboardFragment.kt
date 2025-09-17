package com.shop.pos

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var fragmentView: View? = null
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var purchasesRepository: PurchasesRepository
    private lateinit var salesRepository: SalesRepository
    private lateinit var expensesRepository: ExpensesRepository
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        barChart = view.findViewById(R.id.barChartSales)

        // Sales History Button နဲ့ သက်ဆိုင်တဲ့ logic ကို ဤနေရာမှ ဖယ်ရှားပြီးဖြစ်သည်
    }

    override fun onResume() {
        super.onResume()
        updateDashboardCards()
        setupSalesChart()
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
            val operatingCash = totalSales - totalExpenses

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

    private fun setupSalesChart() {
        lifecycleScope.launch {
            val salesRecords = salesRepository.getSaleRecords()

            val salesByDate = mutableMapOf<String, Float>()
            val sdf = SimpleDateFormat("dd-MMM", Locale.getDefault())

            val sevenDaysAgo = Calendar.getInstance()
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7)

            salesRecords.filter {
                val recordDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).parse(it.saleDate)
                recordDate != null && recordDate.after(sevenDaysAgo.time)
            }.forEach { record ->
                val dayKey = sdf.format(SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).parse(record.saleDate) ?: Date())
                salesByDate[dayKey] = (salesByDate[dayKey] ?: 0f) + record.totalAmount.toFloat()
            }

            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            var index = 0f

            salesByDate.toSortedMap().forEach { (date, total) ->
                entries.add(BarEntry(index, total))
                labels.add(date)
                index++
            }

            if (entries.isEmpty()) {
                barChart.visibility = View.GONE
                return@launch
            }

            barChart.visibility = View.VISIBLE
            val dataSet = BarDataSet(entries, "Daily Sales")
            dataSet.color = Color.parseColor("#6750A4")

            val barData = BarData(dataSet)
            barData.barWidth = 0.5f

            barChart.data = barData
            barChart.description.isEnabled = false
            barChart.setFitBars(true)

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            barChart.invalidate()
        }
    }
}