package com.shop.pos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(private val inventoryDao: InventoryDao) {

    suspend fun getInventoryItems(): List<InventoryItem> {
        return withContext(Dispatchers.IO) {
            inventoryDao.getAllItems()
        }
    }

    suspend fun addInventoryItem(item: InventoryItem) {
        withContext(Dispatchers.IO) {
            inventoryDao.insert(item)
        }
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        withContext(Dispatchers.IO) {
            inventoryDao.update(item)
        }
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        withContext(Dispatchers.IO) {
            inventoryDao.delete(item)
        }
    }

    suspend fun findItemByName(name: String): InventoryItem? {
        return withContext(Dispatchers.IO) {
            inventoryDao.findItemByName(name)
        }
    }

    suspend fun addStockFromPurchase(purchaseDetailItems: List<PurchaseDetailItem>) {
        withContext(Dispatchers.IO) {
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
                        code = null,
                        imageUri = null,
                        // ++ Error ပြင်ဆင်ပြီး ++
                        stockQuantity = purchasedItem.quantity,
                        price = purchasedItem.purchasePrice, // Default price same as cost
                        costPrice = purchasedItem.purchasePrice
                    )
                    inventoryDao.insert(newItem)
                }
            }
        }
    }

    suspend fun deductStockFromSale(saleItems: List<SaleItem>): Boolean {
        return withContext(Dispatchers.IO) {
            for (saleItem in saleItems) {
                val inventoryItem = inventoryDao.findItemByName(saleItem.name)
                if (inventoryItem == null || inventoryItem.stockQuantity < saleItem.quantity) {
                    return@withContext false
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
            return@withContext true
        }
    }

    suspend fun addStockFromCancelledSale(saleItems: List<SaleItem>) {
        withContext(Dispatchers.IO) {
            for (saleItem in saleItems) {
                val inventoryItem = inventoryDao.findItemByName(saleItem.name)
                if (inventoryItem != null) {
                    val updatedStock = inventoryItem.stockQuantity + saleItem.quantity
                    val updatedSoldCount = inventoryItem.soldQuantity - saleItem.quantity
                    val updatedItem = inventoryItem.copy(
                        stockQuantity = updatedStock,
                        soldQuantity = if (updatedSoldCount < 0) 0 else updatedSoldCount
                    )
                    inventoryDao.update(updatedItem)
                }
            }
        }
    }

    suspend fun findItemByCode(code: String): InventoryItem? {
        return withContext(Dispatchers.IO) {
            inventoryDao.findItemByCode(code)
        }
    }

    suspend fun getForSaleItems(): List<InventoryItem> {
        return withContext(Dispatchers.IO) {
            inventoryDao.getItemsForSale()
        }
    }

    suspend fun getTotalInventoryValue(): Double {
        return withContext(Dispatchers.IO) {
            var totalValue = 0.0
            inventoryDao.getAllItems().forEach { item ->
                totalValue += item.stockQuantity * item.costPrice
            }
            totalValue
        }
    }
}