package org.sonso.bludceapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.sonso.bludceapi.dto.ReceiptType
import org.sonso.bludceapi.dto.TipsType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "receipt")
class ReceiptEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    var id: UUID = UUID.randomUUID()

    @Column(name = "receipt_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var receiptType: ReceiptType = ReceiptType.EVENLY

    @Column(name = "tips_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var tipsType: TipsType = TipsType.NONE

    @Column(name = "tips_percent")
    var tipsPercent: Int? = null

    @Column(name = "person_count", nullable = false)
    var personCount: Int = 1

    @Column(name = "tips_value")
    var tipsValue: BigDecimal? = null

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "tips_amount", nullable = false)
    var tipsAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @ManyToOne
    @JoinColumn(name = "initiator_id", referencedColumnName = "id", nullable = false)
    lateinit var initiator: UserEntity

    @OneToMany(mappedBy = "receipt", cascade = [CascadeType.ALL], orphanRemoval = true)
    var positions: List<ReceiptPositionEntity> = listOf()
}
