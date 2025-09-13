package com.shop.pos

object PurchasesRepository {
    private val purchaseItems = mutableListOf<PurchaseItem>()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        if (purchaseItems.isEmpty()) {
            purchaseItems.add(PurchaseItem("Mobile Mart Supplier", "12-Sep-2025", 2500000.0))
            purchaseItems.add(PurchaseItem("Case & Accessories Hub", "10-Sep-2025", 850000.0))
            purchaseItems.add(PurchaseItem("Local IT Solutions", "05-Sep-2025", 1200000.0))
        }
    }

    fun getPurchaseItems(): List<PurchaseItem> {
        return purchaseItems
    }

    fun addPurchaseItem(item: PurchaseItem) {
        purchaseItems.add(0, item)
    }

    // Function အသစ်: အဝယ်မှတ်တမ်းကို ပြင်ဆင်ရန်
    fun updatePurchaseItem(position: Int, updatedItem: PurchaseItem) {
        if (position >= 0 && position < purchaseItems.size) {
            purchaseItems[position] = updatedItem
        }
    }

    // Function အသစ်: အဝယ်မှတ်တမ်းကို ဖျက်ရန်
    fun deletePurchaseItem(position: Int) {
        if (position >= 0 && position < purchaseItems.size) {
            purchaseItems.removeAt(position)
        }
    }
}