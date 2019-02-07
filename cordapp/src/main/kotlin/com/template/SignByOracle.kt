package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.FilteredTransaction
import net.corda.core.utilities.unwrap

// Simple flow which takes a filtered transaction and returns
// a digital signature over the transaction Merkle root.
@InitiatingFlow
class SignByOracle(val oracle: Party, val ftx: FilteredTransaction) : FlowLogic<TransactionSignature>() {
    @Suspendable override fun call(): TransactionSignature {
        logger.info("+++++++++++++SignByOracle+++++++++++++++++++++++++++")
        println("+++++++++++++SignByOracle+++++++++++++++++++++++++++")
       val session = initiateFlow(oracle)
        println("+++++++++++++SignedByOracleHandler+++++++++++++++++++++++++++")
       return session.sendAndReceive<TransactionSignature>(ftx).unwrap { it }
    }
}