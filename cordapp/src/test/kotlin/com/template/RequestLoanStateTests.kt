package com.template

import groovy.util.GroovyTestCase
import junit.framework.Assert
import net.corda.core.contracts.LinearState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test

class RequestLoanStateTests{


    val borrower = TestIdentity(CordaX500Name("Borrower","","GB")).party
    val intermediary = TestIdentity(CordaX500Name("Intermediary","","GB")).party
    val lender = TestIdentity(CordaX500Name("lender","","GB")).party
    @Test
    fun RequestLoanStateHasCorrectParamsOfCorrectTypeInConstructor() {

        RequestLoanState(borrower,intermediary,1000,10,3,lender)
    }

    @Test
    fun RequestLoanStateHasGetters() {
        var requestLoanState = RequestLoanState(borrower,intermediary,1000,10,3,lender)
        Assert.assertEquals(borrower, requestLoanState.borrower)
        Assert.assertEquals(intermediary, requestLoanState.intermediary)
        Assert.assertEquals(1000, requestLoanState.amount)
    }

    @Test
    fun RequestLoanStateImplementsLinearState() {
        assert(RequestLoanState(borrower,intermediary,1000,10,3,lender) is LinearState)
    }

    @Test
    fun RequestLoanStateHasTwoParticipants() {
        var requestLoanState = RequestLoanState(borrower,intermediary,1000,10,3,lender)
        GroovyTestCase.assertEquals(2, requestLoanState.participants.size)
        assert(requestLoanState.participants.contains(borrower))
        assert(requestLoanState.participants.contains(intermediary))
    }
}