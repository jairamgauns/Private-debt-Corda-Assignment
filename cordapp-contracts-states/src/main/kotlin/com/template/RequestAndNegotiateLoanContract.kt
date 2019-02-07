package com.template

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.lang.IllegalArgumentException
import java.security.PublicKey

class RequestAndNegotiateLoanContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        val ID = "com.template.RequestAndNegotiateLoanContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class RequestLoan : Commands
        class ModifyLoan : Commands
        class AcceptLoan : Commands
        class UpdateLoanWithInterest : Commands
        class PayLoan : Commands
        class PaidLoan : Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()

        when(command.value){
            is Commands.RequestLoan -> verifyLoan(tx,setOfSigners)
            is Commands.ModifyLoan ->modifyLoan(tx,setOfSigners)
            is Commands.AcceptLoan ->acceptLoan(tx,setOfSigners)
            is Commands.UpdateLoanWithInterest ->updateLoanWithInterest(tx,setOfSigners)
            is Commands.PayLoan ->payLoan(tx,setOfSigners)
            is Commands.PaidLoan ->paidLoan(tx,setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised Command")
        }
    }

    private fun verifyLoan(tx: LedgerTransaction, signers: Set<PublicKey>) {
        requireThat {
            "No input should be consumed when requesting Loan" using (tx.inputStates.isEmpty())
            "Only one request loan state should be created" using (tx.outputStates.size==1)
            "The output should be of type RequestLoanState" using (tx.outputsOfType<RequestLoanState>().size == 1)

            val output = tx.outputsOfType<RequestLoanState>().single()
            "Loan Request Should be signed by the borrower" using(signers.contains(output.borrower.owningKey))
        }
    }

    private fun modifyLoan(tx: LedgerTransaction, signers: Set<PublicKey>) {
        requireThat {
            "There should be exactly one input" using (tx.inputStates.size == 1)
            "There should be exactly one output" using (tx.outputStates.size == 1)
            "Input should be of the type RequestLoanState" using (tx.inputsOfType<RequestLoanState>().size == 1)
            "Output should be of the type RequestLoanState" using (tx.outputsOfType<RequestLoanState>().size == 1)

            val input = tx.inputsOfType<RequestLoanState>().single()
            val output = tx.outputsOfType<RequestLoanState>().single()

            "The amount requested should not be modified" using (input.amount == output.amount)
            "The borrower should be same" using (input.borrower == output.borrower)
            "The borrower and lender should not be the same" using (input.borrower != input.bidLender)
            "The input status should be REQUEST" using(input.status.equals("REQUEST"))
            "The output status should be NEGOTIATE" using(output.status.equals("NEGOTIATE"))

            "Lender should be the required signer" using (signers.contains(output.bidLender!!.owningKey) )
            "Borrower should be the required signer" using (signers.contains(input.borrower.owningKey) )

        }
    }

    private fun acceptLoan(tx: LedgerTransaction, signers: Set<PublicKey>) {
        requireThat {
            "There should be exactly one input" using (tx.inputStates.size == 1)
            "There should be exactly one output" using (tx.outputStates.size == 1)
            "Input should be of the type RequestLoanState" using (tx.inputsOfType<RequestLoanState>().size == 1)
            "Output should be of the type LoanState" using (tx.outputsOfType<LoanState>().size == 1)

            val input = tx.inputsOfType<RequestLoanState>().single()
            val output = tx.outputsOfType<LoanState>().single()

            "The loan requested remains same" using (input.amount == output.loanAmount)
            "The interest remains the same" using (input.interestRate == output.interestRate)
            "The payable Schedule remains the same" using (input.paymentSchedule == output.paymentSchedule)
            "The borrower is the same" using (input.borrower == output.borrower)
            "The lender remains the same" using (input.bidLender == output.lender)
        }
    }

    private fun payLoan(tx: LedgerTransaction, ofSigners: Set<PublicKey>) {
        requireThat {
            "There should be only one input" using (tx.inputStates.size == 1)
            "There should be only one output" using (tx.outputStates.size == 1)
            "Input should be of the type LoanState" using (tx.inputsOfType<LoanState>().size == 1)
            "Output should be of the type LoanState" using (tx.outputsOfType<LoanState>().size == 1)

            val input = tx.inputsOfType<LoanState>().single()
            val output = tx.outputsOfType<LoanState>().single()

            "The pay amount should be greater than 0." using (input.loanAmountPaid < output.loanAmountPaid)
            "The pay amount is more than required." using (output.payableLoanAmount >=0)
        }

    }

    //Not sure if we need to write a contract as this is internally scheduled
    private fun updateLoanWithInterest(tx: LedgerTransaction, ofSigners: Set<PublicKey>) {

    }

    //Not Yet used
    private fun paidLoan(tx: LedgerTransaction, ofSigners: Set<PublicKey>) {

    }

}