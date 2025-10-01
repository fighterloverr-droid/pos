package com.shop.pos

sealed class SalesHistoryListItem {
    data class SaleItem(val saleRecord: SaleRecord) : SalesHistoryListItem()
    data class DateHeader(val date: String) : SalesHistoryListItem()
}