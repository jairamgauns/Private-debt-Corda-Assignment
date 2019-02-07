package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import java.util.function.Predicate

@InitiatedBy(PayLoanFlow::class)
class RespondPayLoanFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val flow = object : SignTransactionFlow(counterpartySession) {

            override fun checkTransaction(stx: SignedTransaction) {
                logger.info("+++++++++++++++++RespondPayLoanFlow+++++++++++++++++++++")
                println("+++++++++++++++++RespondPayLoanFlow+++++++++++++++++++++")

                println("+++++++++++++++++RespondPayLoanFlow+++++++END++++++++++++++")
            }
        }


        return subFlow(flow)
    }
}