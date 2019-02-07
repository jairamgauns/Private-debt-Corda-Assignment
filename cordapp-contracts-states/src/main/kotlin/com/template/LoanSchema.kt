package com.template

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object LoanSchema

object LoanSchema1 : MappedSchema(
        schemaFamily = LoanSchema.javaClass,
        version = 1,
        mappedTypes = listOf(LoanData::class.java)) {
    @Entity
    @Table(name = "RequestLoanState")
    class LoanData(
        @Column(name = "borrower_name")
        var borrower: String,

        @Column(name = "intermediary_name")
        var intermediary: String,

        @Column(name = "amount")
        var amount: Long,

        @Column(name = "interest_rate")
        var interestRate: Long,

        @Column(name = "payment_schedule")
        var paymentSchedule: Long,

        @Column(name = "lender")
        var bidLender: String,

        @Column(name = "status")
        var status: String,

        @Column(name = "request_loan_id")
        var linearId: UUID
    ) : PersistentState() {
        constructor(): this("","",0L,0L,0L,"","",UUID.randomUUID())
    }
}