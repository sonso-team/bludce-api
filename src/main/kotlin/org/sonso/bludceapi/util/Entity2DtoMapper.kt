package org.sonso.bludceapi.util

import org.sonso.bludceapi.dto.ReceiptPosition
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import org.sonso.bludceapi.dto.User
import org.sonso.bludceapi.dto.response.ReceiptPositionResponse
import org.sonso.bludceapi.dto.response.ReceiptResponse
import org.sonso.bludceapi.dto.ws.InitResponse
import org.sonso.bludceapi.dto.ws.Payload
import org.sonso.bludceapi.entity.ReceiptEntity
import org.sonso.bludceapi.entity.ReceiptPositionEntity
import org.sonso.bludceapi.entity.UserEntity
import java.math.BigDecimal
import java.util.UUID

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

fun ReceiptPositionEntity.toWSResponse() = Payload(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
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

fun ReceiptEntity.toInitPayload(
    uid: UUID,
    initiatorId: String,
    amount: BigDecimal,
    fullAmount: BigDecimal,
    state: List<Payload>
): InitResponse {
    val userAmount = if (receiptType == ReceiptType.EVENLY) {
        BigDecimal(totalAmount.toDouble() / personCount)
    } else null

    val tipsAmount = if (tipsType == TipsType.EVENLY) {
        BigDecimal(tipsValue!!.toDouble() / personCount)
    } else null

    return InitResponse(
        userId = uid,
        initiatorId = UUID.fromString(initiatorId),
        receiptType = receiptType,
        tipsType = tipsType,
        tipsAmount = tipsAmount,
        tipsPercent = tipsPercent,
        userAmount = userAmount,
        amount = amount,
        fullAmount = fullAmount,
        state = state,
    )
}
