package com.shop.pos

class PurchasesRepository(private val purchaseDao: PurchaseDao) {

    suspend fun getPurchaseItems(): List<PurchaseItem> {
        return purchaseDao.getAllPurchases()
    }

    suspend fun addPurchaseItem(item: PurchaseItem) {
        purchaseDao.insert(item)
    }

    suspend fun updatePurchaseItem(item: PurchaseItem) {
        purchaseDao.update(item)
    }

    // ... delete function can be added later if needed
}