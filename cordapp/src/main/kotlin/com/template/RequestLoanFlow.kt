package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class RequestLoanFlow(val amount: Long,
                      val interestRate: Long,
                      val paymentSchedule: Long,
                      val intermediary: Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {

        val output = RequestLoanState(ourIdentity, intermediary, amount,interestRate,paymentSchedule,intermediary)

        val commandType = RequestAndNegotiateLoanContract.Commands.RequestLoan()
        val requiredSigners = listOf(ourIdentity.owningKey, intermediary.owningKey)
        val command = Command(commandType,requiredSigners)

        // Building the transaction.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(output,RequestAndNegotiateLoanContract.ID)
                .addCommand(command)

        txBuilder!!.verify(serviceHub)

        // Signing the transaction ourselves.
        val partStx = serviceHub.signInitialTransaction(txBuilder)

        // Gathering the counterparty's signature.
        val counterpartySession = initiateFlow(intermediary)
        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        val ftx = subFlow(FinalityFlow(fullyStx))

        // Get a list of all identities from the network map cache.
        var allParties = serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }

        // Filter out the notary identities and remove our identity.
        var broadcastLenderOrRegList = allParties.filter {
            ( it.name.organisation.contains("Lender", ignoreCase = true))
            // ||      (it.name.organisation.contains("Regulator", ignoreCase = true)) )
        }

        if(amount > 100000){
            val regulator = serviceHub.identityService.partiesFromName("Regulator", true).single()
            broadcastLenderOrRegList = broadcastLenderOrRegList.plus(regulator)
        }
        // Broadcast this transaction to all parties on this business network.
        subFlow(BroadcastLoanRequest(ftx, broadcastLenderOrRegList))

        return(ftx)

    }

}
