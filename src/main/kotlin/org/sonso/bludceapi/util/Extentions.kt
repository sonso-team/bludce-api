package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.request.ReceiptRequest
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
    name = name,
    quantity = quantity,
    price = price,
    receiptId = this.receipt?.id
)

fun ReceiptPosition.toReceiptPositionEntity() = ReceiptPositionEntity(
    name = this.name,
    price = this.price,
    quantity = this.quantity
)

fun ReceiptPosition.toReceiptPositionEntityForSave(receipt: ReceiptEntity) = ReceiptPositionEntity(
    name = this.name,
    price = this.price,
    quantity = this.quantity,
    receipt = receipt
)

fun ReceiptEntity.toReceiptResponse() = ReceiptResponse(
    id = this.id,
    receiptType = this.receiptType,
    tipsType = this.tipsType,
    tipsPercent = this.tipsPercent,
    personCount = this.personCount,
    initiator = this.initiator.toUser(),
    positions = this.positions.map { it.toReceiptPosition() }
)

fun ReceiptRequest.toReceiptEntity(initiator: UserEntity, positions: List<ReceiptPositionEntity>) =
    ReceiptEntity().apply {
        receiptType = this@toReceiptEntity.receiptType
        tipsType = this@toReceiptEntity.tipsType
        tipsPercent = this@toReceiptEntity.tipsPercent
        personCount = this@toReceiptEntity.personCount
        this.initiator = initiator
        this.positions = positions.toMutableList()
    }
