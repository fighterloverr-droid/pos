package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMetricCards(view)
    }

    private fun setupMetricCards(view: View) {
        setupMetricCard(
            view = view,
            cardId = R.id.cardOperatingCash,
            title = "လုပ်ငန်းလည်ပတ်ငွေ",
            value = "၁,၅၀၀,၀၀၀ Ks"
        )

        setupMetricCard(
            view = view,
            cardId = R.id.cardInventoryValue,
            title = "လက်ကျန်ပစ္စည်းတန်ဖိုး",
            value = "၈,၂၅၀,၀၀၀ Ks"
        )

        setupMetricCard(
            view = view,
            cardId = R.id.cardNetProfit,
            title = "အသားတင်အမြတ်ငွေ",
            value = "၃၅၀,၀၀၀ Ks"
        )

        setupMetricCard(
            view = view,
            cardId = R.id.cardGrossProfit,
            title = "အမြတ်ငွေ",
            value = "၇၈၀,၀၀၀ Ks"
        )
    }

    private fun setupMetricCard(view: View, @IdRes cardId: Int, title: String, value: String) {
        val cardView = view.findViewById<View>(cardId)
        val titleTextView = cardView.findViewById<TextView>(R.id.textViewMetricTitle)
        val valueTextView = cardView.findViewById<TextView>(R.id.textViewMetricValue)

        titleTextView.text = title
        valueTextView.text = value
    }
}