package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.lang.IllegalArgumentException

data class RequestLoanState(val borrower: AbstractParty,
                            val intermediary: AbstractParty,
                            val amount: Long,
                            val interestRate: Long,
                            val paymentSchedule: Long,
                            val bidLender: AbstractParty,
                            val status: String = "REQUEST",
                            override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState{

    override val participants: List<AbstractParty>
        //get() = if(bidLender != null) listOf(borrower, intermediary, bidLender) else  listOf(borrower, intermediary)
        get() = listOf(borrower, intermediary)

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(LoanSchema1)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LoanSchema1 -> LoanSchema1.LoanData(
                    this.borrower.toString(),
                    this.intermediary.toString(),
                    this.amount,
                    this.interestRate,
                    this.paymentSchedule,
                    this.bidLender.toString(),
                    this.status,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("The schema : $schema does not exist.")
        }
    }}
