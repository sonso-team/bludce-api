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
import org.sonso.bludceapi.config.properties.ReceiptType
import org.sonso.bludceapi.config.properties.TipsType
import java.util.UUID

@Entity
@Table(name = "receipt")
class ReceiptEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    var id: UUID = UUID.randomUUID()

    @Column(name = "receipt_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var receiptType: ReceiptType = ReceiptType.PROPORTIONALLY

    @Column(name = "tips_type")
    @Enumerated(EnumType.STRING)
    var tipsType: TipsType? = null

    @Column(name = "tips_percent")
    var tipsPercent: Int? = null

    @Column(name = "person_count")
    var personCount: Int? = null

    @ManyToOne
    @JoinColumn(name = "initiator_id", referencedColumnName = "id", nullable = false)
    lateinit var initiator: UserEntity

    @OneToMany(mappedBy = "receipt", cascade = [CascadeType.ALL], orphanRemoval = true)
    var positions: MutableList<ReceiptPositionEntity> = mutableListOf()
}
