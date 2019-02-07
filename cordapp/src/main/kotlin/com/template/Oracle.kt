package com.template

import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.FilteredTransaction
import java.lang.IllegalArgumentException

@CordaService
class Oracle(val services: ServiceHub) : SingletonSerializeAsToken() {
    fun sign(ftx: FilteredTransaction): TransactionSignature {
        val myKey = services.myInfo.legalIdentities.first().owningKey


        // Check the partial Merkle tree is valid.
        ftx.verify()

        fun isCommandCorrect(elem: Any) = when {
            elem is Command<*> && elem.value is RequestAndNegotiateLoanContract.Commands.PayLoan -> {
                // TODO need to know what to check here
                true
            }
            else -> false
        }

        // Is it a Merkle tree we are willing to sign over?
        val isValidMerkleTree = ftx.checkWithFun(::isCommandCorrect)

        if (isValidMerkleTree) {
            return services.createSignature(ftx, myKey)
        } else {
            throw IllegalArgumentException("Oracle signature requested over invalid transaction.")
        }
    }
}