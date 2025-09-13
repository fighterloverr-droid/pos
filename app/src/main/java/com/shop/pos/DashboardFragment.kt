package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    // Fragment ရဲ့ view ကို ကိုင်ထားဖို့ variable တစ်ခု ကြေညာပါ
    private var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragmentView ထဲကို view ကို တစ်ခါပဲ တည်ဆောက်ပြီး ထည့်ပါ
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        }
        return fragmentView
    }

    // onResume() က ဒီ Fragment ကို မြင်ရတိုင်း အလုပ်လုပ်ပါတယ်
    override fun onResume() {
        super.onResume()
        // Dashboard ကို ပြန်ရောက်တိုင်း data တွေ update ဖြစ်အောင် ဒီမှာခေါ်ပါ
        updateDashboardCards()
    }

    private fun updateDashboardCards() {
        // --- လက်ကျန်ပစ္စည်းတန်ဖိုး ကတ် ---
        val totalValue = InventoryRepository.getTotalInventoryValue()
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        setupMetricCard(
            cardId = R.id.cardInventoryValue,
            title = "လက်ကျန်ပစ္စည်းတန်ဖိုး",
            value = "${numberFormat.format(totalValue.toInt())} Ks"
        )

        // --- တခြားကတ်များ (လောလောဆယ် data မရှိသေးပါ) ---
        setupMetricCard(
            cardId = R.id.cardOperatingCash,
            title = "လုပ်ငန်းလည်ပတ်ငွေ",
            value = "0 Ks" // TODO: နောက်ပိုင်းမှာ ငွေစာရင်းနဲ့ ချိတ်ဆက်ပါမည်
        )

        setupMetricCard(
            cardId = R.id.cardNetProfit,
            title = "အသားတင်အမြတ်ငွေ",
            value = "0 Ks" // TODO: နောက်ပိုင်းမှာ အရောင်း/အရှုံးနဲ့ ချိတ်ဆက်ပါမည်
        )

        setupMetricCard(
            cardId = R.id.cardGrossProfit,
            title = "အမြတ်ငွေ",
            value = "0 Ks" // TODO: နောက်ပိုင်းမှာ အရောင်းနဲ့ ချိတ်ဆက်ပါမည်
        )
    }

    private fun setupMetricCard(@IdRes cardId: Int, title: String, value: String) {
        // fragmentView က null မဟုတ်မှ အလုပ်လုပ်ပါ
        fragmentView?.let { view ->
            val cardView = view.findViewById<View>(cardId)
            val titleTextView = cardView.findViewById<TextView>(R.id.textViewMetricTitle)
            val valueTextView = cardView.findViewById<TextView>(R.id.textViewMetricValue)

            titleTextView.text = title
            valueTextView.text = value
        }
    }
}