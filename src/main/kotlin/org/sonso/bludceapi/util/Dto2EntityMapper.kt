package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.entity.ReceiptPositionEntity

fun ReceiptPosition.toReceiptPositionEntity(receipt: ReceiptEntity) = ReceiptPositionEntity(
    name = this.name,
    price = this.price,
    quantity = this.quantity,
    receipt = receipt
)
