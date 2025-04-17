package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.response.ReceiptPositionResponse
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.entity.ReceiptPositionEntity
import org.sonso.bludceapi.entity.UserEntity

fun UserEntity.toUser() = User(
    id = this.id.toString(),
    phoneNumber = this.phoneNumber,
    email = this.email,
)

fun ReceiptPositionEntity.toReceiptPosition() = ReceiptPosition(
    name = name,
    quantity = quantity,
    price = price
)

fun ReceiptPositionEntity.toReceiptPositionResponse() = ReceiptPositionResponse(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    receiptId = this.receipt?.id
)

fun ReceiptEntity.toReceiptResponse() = ReceiptResponse(
    id = this.id,
    receiptType = this.receiptType,
    tipsType = this.tipsType,
    tipsPercent = this.tipsPercent,
    personCount = this.personCount,
    initiatorName = this.initiator.name,
    totalAmount = this.totalAmount,
    tipsAmount = this.tipsAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    positions = this.positions.map { it.toReceiptPosition() }
)
