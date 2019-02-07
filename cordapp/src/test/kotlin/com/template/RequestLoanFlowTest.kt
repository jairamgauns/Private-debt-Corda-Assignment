package com.template

import com.google.common.collect.ImmutableList
import groovy.util.GroovyTestCase.assertEquals
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test

class RequestLoanFlowTest {

    private lateinit var network: MockNetwork
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(ImmutableList.of("com.template"))
        nodeA = network.createPartyNode(CordaX500Name("Borrower", "", "GB"))
        nodeB = network.createPartyNode(CordaX500Name("Intermediary", "", "GB"))
        // val nodeA = TestIdentity(CordaX500Name("Borrower","","GB")).party
        // val nodeB = TestIdentity(CordaX500Name("Intermediary","","GB")).party
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Flow records transaction in both parties vault`() {
        val flow = RequestLoanFlow(1000, 10, 1, intermediary = nodeB.info.legalIdentities[0])
        val future = nodeA.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()
        for (node in listOf(nodeA, nodeB)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
            //node.services.validatedTransactions.getTransaction(signedTx.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun transactionConstructedByFlowHasOneRequestLoanCommand() {
        val flow = RequestLoanFlow(1000, 10, 1, intermediary = nodeB.info.legalIdentities[0])
        val future = nodeA.startFlow(flow)

        network.runNetwork()

        val signedTransaction = future.get()

        assertEquals(1, signedTransaction.tx.commands.size.toLong())
        val (value) = signedTransaction.tx.commands[0]

        assert(value is RequestAndNegotiateLoanContract.Commands.RequestLoan)

    }


    @Test
    @Throws(Exception::class)
    fun transactionConstructedByFlowUsesTheCorrectNotary() {
        val intermediary: Party

        //val flow = TokenIssueFlow(nodeB.info.legalIdentities[0], 99)
        val flow = RequestLoanFlow(1000, 10, 1, intermediary = nodeA.info.legalIdentities[0])
        val future = nodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()

        assertEquals(1, signedTransaction.tx.outputStates.size.toLong())
        val output = signedTransaction.tx.outputs[0]
        //val output = signedTransactio

        assertEquals(network.notaryNodes[0].info.legalIdentities[0], output.notary)

    }
}