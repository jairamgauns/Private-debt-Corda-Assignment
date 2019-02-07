package com.template

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.AbstractParty
import java.time.Duration
import java.time.Instant

data class LoanState(
        val borrower: AbstractParty,
        val lender: AbstractParty,
        val loanAmount: Long,
        val payableLoanAmount: Long = 0,
        val interestRate: Long = 10,
        val loanAmountPaid: Long = 0,
        val paymentSchedule: Long = 5,
        val requestTime: Instant = Instant.now(),
        val status: String = "ACCEPTED",
        override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, SchedulableState {
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(CheckLoanFlow::class.java, thisStateRef),requestTime+Duration.ofMinutes(paymentSchedule))
    }

    override val participants: List<AbstractParty>
        get() = listOf(borrower,lender)
}