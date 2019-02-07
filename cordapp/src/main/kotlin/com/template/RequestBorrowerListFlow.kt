package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.utils.CustomVaultService
import com.template.utils.LoanRecord
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap

object RequestBorrowerListFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val owner: Party) : FlowLogic<SignedTransaction?>() {

        @Suspendable
        override fun call(): SignedTransaction? {
            val session = initiateFlow(owner)
            val list = session.receive<MutableList<LoanRecord>>().unwrap { it }

            return null
        }

    }

    @InitiatedBy(RequestBorrowerListFlow.Initiator::class)
    class SendPropertyListFlow(val session: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val customVaultService = serviceHub.cordaService(CustomVaultService.Service::class.java)
            val propertyList: MutableList<LoanRecord> = customVaultService.getLoanRequests(serviceHub.myInfo.legalIdentities.first().name.toString())
            session.send(propertyList)
        }
    }

}