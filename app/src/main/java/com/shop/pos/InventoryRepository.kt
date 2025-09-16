package com.shop.pos

object InventoryRepository {
    private val inventoryItems = mutableListOf<InventoryItem>()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        if (inventoryItems.isEmpty()) {
            inventoryItems.add(InventoryItem("Coca-Cola", 50, 1000.0, 700.0, 120))
            inventoryItems.add(InventoryItem("Potato Chips", 30, 1500.0, 1100.0, 85))
            inventoryItems.add(InventoryItem("Chocolate Bar", 100, 800.0, 550.0, 250))
            inventoryItems.add(InventoryItem("Mineral Water", 80, 500.0, 300.0, 300))
        }
    }

    fun getInventoryItems(): List<InventoryItem> {
        return inventoryItems
    }

    fun addInventoryItem(item: InventoryItem) {
        inventoryItems.add(item)
    }

    fun updateInventoryItem(position: Int, updatedItem: InventoryItem) {
        if (position >= 0 && position < inventoryItems.size) {
            inventoryItems[position] = updatedItem
        }
    }

    fun deleteInventoryItem(position: Int) {
        if (position >= 0 && position < inventoryItems.size) {
            inventoryItems.removeAt(position)
        }
    }

    fun addStockFromPurchase(purchaseDetailItems: List<PurchaseDetailItem>) {
        purchaseDetailItems.forEach { purchasedItem ->
            val existingItem = inventoryItems.find { it.name.equals(purchasedItem.name, ignoreCase = true) }

            if (existingItem != null) {
                val index = inventoryItems.indexOf(existingItem)
                val updatedQuantity = existingItem.stockQuantity + purchasedItem.quantity
                // ဝယ်ဈေးကိုလည်း နောက်ဆုံးဝယ်ဈေးနဲ့ update လုပ်နိုင် (optional)
                inventoryItems[index] = existingItem.copy(
                    stockQuantity = updatedQuantity,
                    costPrice = purchasedItem.purchasePrice
                )
            } else {
                // ပစ္စည်းအသစ်ကို inventory ထဲ ထည့်တဲ့အခါ costPrice ပါ ထည့်ပေးပါ
                val newItem = InventoryItem(
                    name = purchasedItem.name,
                    stockQuantity = purchasedItem.quantity,
                    price = purchasedItem.purchasePrice, // ရောင်းဈေးကို ဝယ်ဈေးအတိုင်း လောလောဆယ် သတ်မှတ်ပါ
                    costPrice = purchasedItem.purchasePrice
                )
                inventoryItems.add(newItem)
            }
        }
    }

    // deductStockFromSale function ကို အဆင့်မြှင့်တင်ပါ
    fun deductStockFromSale(saleItems: List<SaleItem>): Boolean {
        for (saleItem in saleItems) {
            val inventoryItem = inventoryItems.find { it.name.equals(saleItem.name, ignoreCase = true) }
            if (inventoryItem == null || inventoryItem.stockQuantity < saleItem.quantity) {
                return false
            }
        }

        for (saleItem in saleItems) {
            val inventoryItem = inventoryItems.find { it.name.equals(saleItem.name, ignoreCase = true) }!!
            val index = inventoryItems.indexOf(inventoryItem)

            // stock နုတ်တဲ့အပြင် soldQuantity ကိုပါ တိုးပေးပါ
            val updatedStock = inventoryItem.stockQuantity - saleItem.quantity
            val updatedSoldCount = inventoryItem.soldQuantity + saleItem.quantity

            inventoryItems[index] = inventoryItem.copy(
                stockQuantity = updatedStock,
                soldQuantity = updatedSoldCount
            )
        }

        return true
    }

    fun getTotalInventoryValue(): Double {
        var totalValue = 0.0
        // စုစုပေါင်းတန်ဖိုးကို ရောင်းဈေးအစား အရင်းဈေးနဲ့ တွက်ပါ
        inventoryItems.forEach { item ->
            totalValue += item.stockQuantity * item.costPrice
        }
        return totalValue
    }
}