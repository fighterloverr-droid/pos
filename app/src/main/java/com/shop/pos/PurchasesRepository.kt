package com.shop.pos

object PurchasesRepository {
    private val purchaseItems = mutableListOf<PurchaseItem>()

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