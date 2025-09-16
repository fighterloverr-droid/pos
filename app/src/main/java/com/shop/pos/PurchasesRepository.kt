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

    // Function အသစ်: စုစုပေါင်းအဝယ်တန်ဖိုးကို တွက်ချက်ရန်
    suspend fun getTotalPurchases(): Double? {
        return purchaseDao.getTotalPurchases()
    }
}