package com.template

import com.template.ContractTests
import com.template.RequestAndNegotiateLoanContract.Companion.ID
import net.corda.core.identity.CordaX500Name
import com.template.RequestAndNegotiateLoanContract
//import net.corda.examples.obligation.ObligationContract
//import net.corda.examples.obligation.ObligationContract.Companion.OBLIGATION_CONTRACT_ID
import net.corda.finance.DOLLARS
import net.corda.finance.POUNDS
import net.corda.finance.SWISS_FRANCS
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.ledger
import org.junit.Test


class RequestAndNegotiateLoanContractTest: ContractTests(){

    @Test
    fun `issue loan request must have no inputs`() {
        ledgerServices.ledger {
            transaction {
               // input(ID, DummyState())
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                output(ID, zeroLoan)
                this `fails with` "No inputs should be consumed when issuing a loan request."
            }
            transaction {
                output(ID, thousandDollarLoan)
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                verifies() // As there are no input states.
            }
        }
    }

}