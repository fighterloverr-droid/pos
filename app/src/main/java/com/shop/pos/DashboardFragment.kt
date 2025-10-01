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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class DashboardFragment : Fragment() {

    private var fragmentView: View? = null
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var purchasesRepository: PurchasesRepository
    private lateinit var salesRepository: SalesRepository
    private lateinit var expensesRepository: ExpensesRepository
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart

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
        pieChart = view.findViewById(R.id.pieChartExpenses)
        lineChart = view.findViewById(R.id.lineChartSales)

        val buttonViewSalesHistory = view.findViewById<Button>(R.id.buttonViewSalesHistory)
        buttonViewSalesHistory?.setOnClickListener {
            val intent = Intent(requireContext(), SalesHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardCards()
        setupSalesBarChart()
        setupExpensesPieChart()
        setupSalesLineChart()
    }

    private fun updateDashboardCards() {
        lifecycleScope.launch {
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            val totalSales = salesRepository.getTotalSales() ?: 0.0
            val totalExpenses = expensesRepository.getTotalExpenses() ?: 0.0
            val totalInventoryValue = inventoryRepository.getTotalInventoryValue()
            val totalCOGS = salesRepository.getTotalCostOfGoodsSold() ?: 0.0

            val grossProfit = totalSales - totalCOGS
            val netProfit = grossProfit - totalExpenses
            val operatingCash = totalInventoryValue + netProfit

            setupMetricCard(
                cardId = R.id.cardOperatingCash,
                title = "လုပ်ငန်းလည်ပတ်ငွေ",
                value = "${numberFormat.format(operatingCash.toInt())} Ks",
                isNegative = operatingCash < 0
            )

            setupMetricCard(
                cardId = R.id.cardInventoryValue,
                title = "လက်ကျန်ပစ္စည်းတန်ဖိုး (အရင်း)",
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
                title = "အမြတ်ငွေ (Gross Profit)",
                value = "${numberFormat.format(grossProfit.toInt())} Ks"
            )
        }
    }

    private fun setupMetricCard(
        @IdRes cardId: Int,
        title: String,
        value: String,
        isNegative: Boolean = false
    ) {
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

    private fun setupSalesBarChart() {
        lifecycleScope.launch {
            val salesRecords = salesRepository.getSaleRecords()

            val salesByDate = TreeMap<String, Float>()
            val displayFormat = SimpleDateFormat("dd-MMM", Locale.getDefault())
            val parseFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

            val sevenDaysAgo = Calendar.getInstance()
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7)

            salesRecords.forEach { record ->
                try {
                    val recordDate = parseFormat.parse(record.saleDate)
                    if (recordDate != null && recordDate.after(sevenDaysAgo.time)) {
                        val dayKey = displayFormat.format(recordDate)
                        salesByDate[dayKey] = (salesByDate[dayKey] ?: 0f) + record.totalAmount.toFloat()
                    }
                } catch (e: Exception) {
                    // Ignore malformed dates
                }
            }

            val entries = ArrayList<BarEntry>()
            val labels = ArrayList(salesByDate.keys)

            labels.forEachIndexed { index, _ ->
                entries.add(BarEntry(index.toFloat(), salesByDate[labels[index]] ?: 0f))
            }


            if (entries.isEmpty()) {
                barChart.visibility = View.GONE
                return@launch
            }

            barChart.visibility = View.VISIBLE
            val dataSet = BarDataSet(entries, "Daily Sales")
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary_teal)

            val barData = BarData(dataSet)
            barData.barWidth = 0.5f

            barChart.data = barData
            barChart.description.isEnabled = false
            barChart.setFitBars(true)

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            barChart.invalidate()
        }
    }


    private fun setupExpensesPieChart() {
        lifecycleScope.launch {
            val expenses = expensesRepository.getExpenseItems()

            val currentMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val currentMonthStr = currentMonthFormat.format(Date())

            val monthlyExpenses = expenses.filter {
                try {
                    val recordDate =
                        SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).parse(it.date)
                    recordDate != null && currentMonthFormat.format(recordDate) == currentMonthStr
                } catch (e: Exception) {
                    false
                }
            }

            if (monthlyExpenses.isEmpty()) {
                pieChart.clear()
                pieChart.invalidate()
                pieChart.visibility = View.GONE
                return@launch
            }

            pieChart.visibility = View.VISIBLE

            val expensesByCategory = monthlyExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

            val entries = ArrayList<PieEntry>()
            expensesByCategory.forEach { (category, total) ->
                entries.add(PieEntry(total, category))
            }

            val dataSet = PieDataSet(entries, "Expenses")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()
            dataSet.sliceSpace = 2f
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.BLACK

            val pieData = PieData(dataSet)
            pieChart.data = pieData
            pieChart.description.isEnabled = false
            pieChart.isDrawHoleEnabled = true
            pieChart.setEntryLabelColor(Color.BLACK)
            pieChart.animateY(1000)
            pieChart.invalidate()
        }
    }

    private fun setupSalesLineChart() {
        lifecycleScope.launch {
            val salesRecords = salesRepository.getSaleRecords()

            val currentMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val currentMonthStr = currentMonthFormat.format(Date())

            val monthlySales = salesRecords.filter {
                try {
                    val recordDate =
                        SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).parse(it.saleDate)
                    recordDate != null && currentMonthFormat.format(recordDate) == currentMonthStr
                } catch (e: Exception) {
                    false
                }
            }

            if (monthlySales.isEmpty()) {
                lineChart.visibility = View.GONE
                return@launch
            }

            lineChart.visibility = View.VISIBLE

            val salesByDay = TreeMap<Int, Float>()
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())

            monthlySales.forEach { record ->
                val recordDate =
                    SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).parse(record.saleDate)
                if (recordDate != null) {
                    val dayOfMonth = dayFormat.format(recordDate).toInt()
                    salesByDay[dayOfMonth] =
                        (salesByDay[dayOfMonth] ?: 0f) + record.totalAmount.toFloat()
                }
            }

            val entries = ArrayList<Entry>()
            salesByDay.forEach { (day, total) ->
                entries.add(Entry(day.toFloat(), total))
            }

            val dataSet = LineDataSet(entries, "Monthly Sales")
            context?.let {
                dataSet.color = ContextCompat.getColor(it, R.color.primary_teal)
                dataSet.valueTextColor = ContextCompat.getColor(it, R.color.black)
                dataSet.fillDrawable = ContextCompat.getDrawable(it, R.drawable.chart_fade_purple)
            }
            dataSet.setCircleColor(dataSet.color)
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(false)
            dataSet.valueTextSize = 10f
            dataSet.setDrawFilled(true)

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            lineChart.description.isEnabled = false
            lineChart.legend.isEnabled = false

            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            lineChart.axisRight.isEnabled = false
            lineChart.axisLeft.setDrawGridLines(true)

            lineChart.animateX(1000)
            lineChart.invalidate()
        }
    }
}
