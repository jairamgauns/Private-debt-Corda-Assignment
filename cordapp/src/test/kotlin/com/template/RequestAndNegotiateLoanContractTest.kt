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
    @Throws(Exception::class)
    fun `No input should be consumed when requesting Loan`() {
        ledgerServices.ledger {
            transaction {
                output(ID, requestLoan)
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                verifies() // As there are no input states.
            }

            transaction {
                input(ID, DummyState())
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                output(ID, requestLoan)
                this `fails with` "No input should be consumed when requesting Loan"
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun `Only one request loan state should be created`() {
        ledgerServices.ledger {
            transaction {
                output(ID, requestLoan)
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                verifies() // As there are no input states.
            }

            transaction {
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                output(ID, requestLoan)
                output(ID,zeroLoan)
                this `fails with` "Only one request loan state should be created"
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun `The output should be of type RequestLoanState`() {
        ledgerServices.ledger {
            transaction {
                output(ID, requestLoan)
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                verifies() // As there are no input states.
            }

            transaction {
                command(listOf(alice.publicKey, bob.publicKey), RequestAndNegotiateLoanContract.Commands.RequestLoan())
                output(ID,zeroLoan)
                this `fails with` "The output should be of type RequestLoanState"
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun `Loan Request Should be signed by the borrower`() {
        ledgerServices.ledger {
            transaction {
                output(ID, requestLoan)
                command(alice.publicKey, RequestAndNegotiateLoanContract.Commands.RequestLoan())
                verifies() // As there are no input states.
            }

//            transaction {
//                command(charlie.partyKey, RequestAndNegotiateLoanContract.Commands.RequestLoan())
//                output(ID,requestLoan)
//                this `fails with` "Loan Request Should be signed by the borrower"
//            }
        }
    }

}