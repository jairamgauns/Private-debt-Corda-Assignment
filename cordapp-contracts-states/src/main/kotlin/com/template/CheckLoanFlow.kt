package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant

@InitiatingFlow
@SchedulableFlow
class CheckLoanFlow(private val stateRef: StateRef) : FlowLogic<Unit>(){

    //override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(FlowException::class)
    override fun call() {

        logger.info("CheckLoanFlow")

        // Get the actual state from the ref.
        val input = serviceHub.toStateAndRef<LoanState>(stateRef)
        println("input")
        val inputState = input.state.data
        println("inputstate")
        //Creating Transaction
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)

        println("inputState.payableLoanAmount: "+ inputState.payableLoanAmount)
        if (inputState.payableLoanAmount > 0) {
            logger.info("++++++++++++++++++Pending Loan")

            val interestCal = inputState.payableLoanAmount.div(inputState.interestRate)
            val updateCalLoanAmount = inputState.payableLoanAmount + interestCal
            logger.info("updateCalLoanAmount"+ updateCalLoanAmount)

            //Creating an output
            val output = inputState.copy(payableLoanAmount = updateCalLoanAmount, requestTime = Instant.now(), status = "PENDING")
            logger.info("output.calLoanAmount"+ output.payableLoanAmount)

            //Creating Command
            val requiredSigners = listOf(ourIdentity.owningKey )
            val command = Command(RequestAndNegotiateLoanContract.Commands.UpdateLoanWithInterest(), requiredSigners)

            txBuilder.addInputState(input)
            txBuilder.addOutputState(output, RequestAndNegotiateLoanContract.ID)
            txBuilder.addCommand(command)
        } else if (inputState.payableLoanAmount == 0L) {
            println("total paid")
            logger.info("++++++++++++++++++Loan Paid")
            //Creating Command
            val requiredSigners = listOf(ourIdentity.owningKey )
            val command = Command(RequestAndNegotiateLoanContract.Commands.PaidLoan(), requiredSigners)
            //Creating an output
         //   val output = inputState.copy(status = "PAID")

            txBuilder.addInputState(input)
         //   txBuilder.addOutputState(output, RequestAndNegotiateLoanContract.ID)
            txBuilder.addCommand(command)
        }

        txBuilder!!.verify(serviceHub)

        // Signing the transaction ourselves.
        val fullyStx = serviceHub.signInitialTransaction(txBuilder)

//        val counterpartySession = initiateFlow(counterparty)
//        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullyStx))
    }

}
