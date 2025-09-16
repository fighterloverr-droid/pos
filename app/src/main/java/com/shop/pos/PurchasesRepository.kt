package com.shop.pos

object PurchasesRepository {
    private val purchaseItems = mutableListOf<PurchaseItem>()

    init {
        // Sample data တွေကို ဖယ်ရှားပြီးသားဖြစ်ပါတယ်
    }

    fun getPurchaseItems(): List<PurchaseItem> {
        return purchaseItems
    }

    fun addPurchaseItem(item: PurchaseItem) {
        purchaseItems.add(0, item)
    }

    fun updatePurchaseItem(position: Int, updatedItem: PurchaseItem) {
        if (position >= 0 && position < purchaseItems.size) {
            purchaseItems[position] = updatedItem
        }
    }

    fun deletePurchaseItem(position: Int) {
        if (position >= 0 && position < purchaseItems.size) {
            purchaseItems.removeAt(position)
        }
    }

    fun getTotalPurchases(): Double {
        return purchaseItems.sumOf { it.totalAmount }
    }

    // Function အသစ်: မှတ်တမ်းတစ်ခုကို "ရောက်ပြီ" ဟု သတ်မှတ်ရန်
    fun markAsArrived(position: Int): PurchaseItem? {
        if (position >= 0 && position < purchaseItems.size) {
            val item = purchaseItems[position]
            if (!item.hasArrived) { // ရောက်မထားမှသာ update လုပ်ပါ
                item.hasArrived = true
                return item
            }
        }
        return null
    }
}