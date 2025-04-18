package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.response.ReceiptPositionResponse
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.entity.ReceiptPositionEntity
import org.sonso.bludceapi.entity.UserEntity

fun UserEntity.toUser() = User(
    id = id.toString(),
    name = name,
    phoneNumber = phoneNumber,
    email = email,
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
    receiptId = receipt?.id
)

fun ReceiptEntity.toReceiptResponse() = ReceiptResponse(
    id = id,
    receiptType = receiptType,
    tipsType = tipsType,
    tipsValue = tipsValue,
    tipsPercent = tipsPercent,
    personCount = personCount,
    initiatorName = initiator.name,
    totalAmount = totalAmount,
    tipsAmount = tipsAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    positions = positions.map { it.toReceiptPosition() }
)
