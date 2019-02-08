package com.template

import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.FilteredTransaction
import java.lang.IllegalArgumentException
import java.math.BigInteger

@CordaService
class Oracle(val services: ServiceHub) : SingletonSerializeAsToken() {
    private val primes = generateSequence(1) { it + 1 }.filter { BigInteger.valueOf(it.toLong()).isProbablePrime(16) }
    val myKey = services.myInfo.legalIdentities.first().owningKey

    fun sign(ftx: FilteredTransaction): TransactionSignature {

        println(primes)
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