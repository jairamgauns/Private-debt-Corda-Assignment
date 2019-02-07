package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.function.Predicate

@InitiatingFlow
@StartableByRPC
class PayLoanFlow(val requestRef: UniqueIdentifier, val payAmount: Long) : FlowLogic<SignedTransaction>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        logger.info("+++++++++++++++++PayLoanFlow")
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(requestRef))
        val inputStateAndRef = serviceHub.vaultService.queryBy<LoanState>(inputCriteria).states.single()
        val input = inputStateAndRef.state.data

        val updateLoanAmountPaid = input.loanAmountPaid + payAmount
        val updateCalLoanAmount = input.payableLoanAmount - payAmount

        //Creating an output
        val output = input.copy(loanAmountPaid = updateLoanAmountPaid, payableLoanAmount = updateCalLoanAmount)

        //Creating Command
        val requiredSigners = listOf(ourIdentity.owningKey, input.lender.owningKey)
        val command = Command(RequestAndNegotiateLoanContract.Commands.PayLoan(), requiredSigners)

        //Creating Transaction
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)

        txBuilder.addInputState(inputStateAndRef)
        txBuilder.addOutputState(output, RequestAndNegotiateLoanContract.ID)
        txBuilder.addCommand(command)

        txBuilder!!.verify(serviceHub)

        // Signing the transaction ourselves.
        val partStx = serviceHub.signInitialTransaction(txBuilder)

        // Gathering the counterparty's signature.
        val (lender) = listOf(input.lender).map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        val counterparty = lender
        val counterpartySession = initiateFlow(counterparty)
        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        val oracle = serviceHub.identityService.partiesFromName("Oracle", true).single()
        val ftx = fullyStx.buildFilteredTransaction(Predicate {
            when (it) {
                is Command<*> ->  it.value is RequestAndNegotiateLoanContract.Commands.PayLoan
                else -> false
            }
        })

        val oracleSignature = subFlow(SignByOracle(oracle, ftx))
        println("+++++++++++++Got signature of oracle+++++++++++++++++++++++++++")
        val stx = fullyStx.withAdditionalSignature(oracleSignature)

        // Finalising the transaction.
        return subFlow(FinalityFlow(stx))
    }
}
