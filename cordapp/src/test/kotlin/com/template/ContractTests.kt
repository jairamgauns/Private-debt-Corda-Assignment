/*
package com.template

import net.corda.testing.node.MockServices
import org.junit.Test

class ContractTests {
    private val ledgerServices = MockServices()

    @Test
    fun `dummy test`() {

    }
}*/


package com.template

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.finance.DOLLARS
import net.corda.finance.POUNDS
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.makeTestIdentityService
import org.junit.Test
import java.time.Instant

abstract class ContractTests {
    protected val ledgerServices = MockServices(
            listOf("com.template"),
            identityService = makeTestIdentityService(),
            initialIdentity = TestIdentity(CordaX500Name("TestIdentity", "", "GB")))
    protected val alice = TestIdentity(CordaX500Name("Alice", "", "GB"))
    protected val bob = TestIdentity(CordaX500Name("Bob", "", "GB"))
    protected val charlie = TestIdentity(CordaX500Name("Bob", "", "GB"))

    protected class DummyState : ContractState {
        override val participants: List<AbstractParty> get() = listOf()
    }

    protected class DummyCommand : CommandData

    protected val thousandDollarLoan = LoanState(bob.party,alice.party,1000,1000,5,0,5, Instant.now())
    protected val zeroLoan = LoanState(bob.party, alice.party,0,0,0,0,2, Instant.now())

    protected val requestLoan=RequestLoanState(alice.party,charlie.party,1000,5,5,bob.party,status="REQUEST")

    @Test
    fun `dummy test`() {

    }



}