package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class NegotiateLoanFlow(val requestRef: UniqueIdentifier,
                        val interestRate: Long,
                        val paymentSchedule: Long) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call() {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(requestRef))
        val inputStateAndRef = serviceHub.vaultService.queryBy<RequestLoanState>(inputCriteria).states.single()
        val input = inputStateAndRef.state.data

        //Creating an output
        val (borrower) = listOf(input.borrower).map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }

        val output = input.copy(interestRate = this.interestRate, paymentSchedule = this.paymentSchedule,  bidLender = ourIdentity, status = "NEGOTIATE")

        //Creating Command
        val requiredSigners = listOf(ourIdentity.owningKey, input.borrower.owningKey)
        val command = Command(RequestAndNegotiateLoanContract.Commands.ModifyLoan(), requiredSigners)

        //Creating Transaction
        val notary = inputStateAndRef.state.notary
        val txBuilder = TransactionBuilder(notary)
        txBuilder.addInputState(inputStateAndRef)
        txBuilder.addOutputState(output, RequestAndNegotiateLoanContract.ID)
        txBuilder.addCommand(command)

        txBuilder!!.verify(serviceHub)

        // Signing the transaction ourselves.
        val partStx = serviceHub.signInitialTransaction(txBuilder)

        // Gathering the counterparty's signature.
        val counterpartySession = initiateFlow(borrower)

        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullyStx))
    }

}
