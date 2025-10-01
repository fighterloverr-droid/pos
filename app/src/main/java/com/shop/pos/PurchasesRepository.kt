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

    suspend fun deletePurchaseItem(item: PurchaseItem) { // <-- Function အသစ်
        purchaseDao.delete(item)
    }

    suspend fun getPurchaseById(id: Int): PurchaseItem? { // <-- Function အသစ်
        return purchaseDao.getPurchaseById(id)
    }

    suspend fun getTotalPurchases(): Double? {
        return purchaseDao.getTotalPurchases()
    }
}