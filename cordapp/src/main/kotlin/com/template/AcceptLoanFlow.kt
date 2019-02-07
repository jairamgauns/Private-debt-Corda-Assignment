package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class AcceptLoanFlow(val requestRef: UniqueIdentifier) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call() {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(requestRef))
        val inputStateAndRef = serviceHub.vaultService.queryBy<RequestLoanState>(inputCriteria).states.single()
        val input = inputStateAndRef.state.data

        //Creating an output
        val output = LoanState(
                borrower = input.borrower,
                lender = input.bidLender,
                loanAmount = input.amount,
                interestRate = input.interestRate,
                paymentSchedule = input.paymentSchedule,
                payableLoanAmount = input.amount+ (input.amount.div(input.interestRate)))
        logger.info("+++++++++++++++++AcceptLoanFlow")
        //Creating Command
        val requiredSigners = listOf(ourIdentity.owningKey, input.bidLender.owningKey)
        val command = Command(RequestAndNegotiateLoanContract.Commands.AcceptLoan(), requiredSigners)

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
        val (wellKnownProposer) = listOf(input.bidLender).map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        val counterparty = wellKnownProposer
        val counterpartySession = initiateFlow(counterparty)
        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullyStx))
    }

}
