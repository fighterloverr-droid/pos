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
            purchaseItems.add(PurchaseItem("Mobile Mart Supplier", "12-Sep-2025", sampleDetailItems, 10000000.0))
            purchaseItems.add(PurchaseItem("Case & Accessories Hub", "10-Sep-2025", emptyList(), 850000.0))
            purchaseItems.add(PurchaseItem("Local IT Solutions", "05-Sep-2025", emptyList(), 1200000.0))
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
}