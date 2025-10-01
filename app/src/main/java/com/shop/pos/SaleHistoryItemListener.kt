package com.shop.pos

interface SaleHistoryItemListener {
    fun onSaleRecordClick(saleRecord: SaleRecord)
    fun onEditSale(saleRecord: SaleRecord)
    fun onCancelSale(saleRecord: SaleRecord)
}