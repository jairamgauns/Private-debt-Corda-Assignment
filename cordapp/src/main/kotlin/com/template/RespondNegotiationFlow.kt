package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(NegotiateLoanFlow::class)
class RespondNegotiationFlow(val counterpartySession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        subFlow(object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        })
    }
}