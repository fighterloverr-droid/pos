package com.shop.pos

object PurchasesRepository {
    private val purchaseItems = mutableListOf<PurchaseItem>()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        if (purchaseItems.isEmpty()) {
            val sampleDetailItems = listOf(
                PurchaseDetailItem("iPhone 14", 2, 3500000.0),
                PurchaseDetailItem("Samsung S23", 1, 3000000.0)
            )
            // hasArrived: false (မရောက်သေး) အခြေအနေနဲ့ နမူနာ data ထည့်ပါ
            purchaseItems.add(PurchaseItem("Mobile Mart Supplier", "12-Sep-2025", sampleDetailItems, 10000000.0, hasArrived = false))
            purchaseItems.add(PurchaseItem("Case & Accessories Hub", "10-Sep-2025", emptyList(), 850000.0, hasArrived = true)) // ဒါကို ရောက်ပြီးသားလို့ စမ်းသပ် သတ်မှတ်ကြည့်ပါ
        }
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