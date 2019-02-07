package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastLoanRequest(val stx: SignedTransaction, val observers: List<Party>) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        logger.info("+++++++++++++++++Broadcast+++++++++++++++++")

        // Create a session for each remaining party.
        var sessions = observers.map { initiateFlow(it) }

        // Send the transaction to all the remaining parties.
        sessions.forEach{subFlow(SendTransactionFlow(it,stx))}

    }

}