package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.ReceiveTransactionFlow
import net.corda.core.node.StatesToRecord

@InitiatedBy(BroadcastLoanRequest::class)
class RecordLoanRequestAsObserver(val otherSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        // Receive and record the new state in our vault EVEN THOUGH we are not a participant as we are
        // using 'ALL_VISIBLE'.
        val flow = ReceiveTransactionFlow(
                otherSideSession = otherSession,
                checkSufficientSignatures = true,
                statesToRecord = StatesToRecord.ALL_VISIBLE
        )

        subFlow(flow)
    }

}