package com.shop.pos

// Repository ကို class အဖြစ်ပြောင်းပြီး DAO ကို constructor ကနေ လက်ခံပါ
class InventoryRepository(private val inventoryDao: InventoryDao) {

    suspend fun getInventoryItems(): List<InventoryItem> {
        return inventoryDao.getAllItems()
    }

    suspend fun addInventoryItem(item: InventoryItem) {
        inventoryDao.insert(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        inventoryDao.update(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        inventoryDao.delete(item)
    }

    suspend fun addStockFromPurchase(purchaseDetailItems: List<PurchaseDetailItem>) {
        purchaseDetailItems.forEach { purchasedItem ->
            val existingItem = inventoryDao.findItemByName(purchasedItem.name)

            if (existingItem != null) {
                val updatedQuantity = existingItem.stockQuantity + purchasedItem.quantity
                val updatedItem = existingItem.copy(
                    stockQuantity = updatedQuantity,
                    costPrice = purchasedItem.purchasePrice
                )
                inventoryDao.update(updatedItem)
            } else {
                val newItem = InventoryItem(
                    name = purchasedItem.name,
                    stockQuantity = purchasedItem.quantity,
                    price = purchasedItem.purchasePrice,
                    costPrice = purchasedItem.purchasePrice
                )
                inventoryDao.insert(newItem)
            }
        }
    }

    suspend fun deductStockFromSale(saleItems: List<SaleItem>): Boolean {
        for (saleItem in saleItems) {
            val inventoryItem = inventoryDao.findItemByName(saleItem.name)
            if (inventoryItem == null || inventoryItem.stockQuantity < saleItem.quantity) {
                return false
            }
        }

        for (saleItem in saleItems) {
            val inventoryItem = inventoryDao.findItemByName(saleItem.name)!!
            val updatedStock = inventoryItem.stockQuantity - saleItem.quantity
            val updatedSoldCount = inventoryItem.soldQuantity + saleItem.quantity
            val updatedItem = inventoryItem.copy(
                stockQuantity = updatedStock,
                soldQuantity = updatedSoldCount
            )
            inventoryDao.update(updatedItem)
        }
        return true
    }

    suspend fun getTotalInventoryValue(): Double {
        var totalValue = 0.0
        inventoryDao.getAllItems().forEach { item ->
            totalValue += item.stockQuantity * item.costPrice
        }
        return totalValue
    }
}