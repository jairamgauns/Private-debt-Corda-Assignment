package com.template.webserver

import com.template.LoanState
import com.template.RequestLoanState
import net.corda.core.identity.Party

fun Party.toSimpleName(): String {
    return "${name.organisation} (${name.locality}, ${name.country})"
}

data class RequestLoanStateSimpleObj(
        val borrower: String,
        val intermediary: String,
        val amount: String,
        val interestRate: String,
        val paymentSchedule: String,
        val bidLender: String,
        val status: String,
        val linearId: String)

fun RequestLoanState.toSimpleObj(): RequestLoanStateSimpleObj {
    return RequestLoanStateSimpleObj(
            borrower.nameOrNull()!!.organisation,
            intermediary.nameOrNull()!!.organisation,
            amount.toString(),
            interestRate.toString(),
            paymentSchedule.toString(),
            bidLender.nameOrNull()!!.organisation,
            status,
            linearId.id.toString())
}

data class LoanStateSimpleObj(
        val borrower: String,
        val lender: String,
        val loanAmount: String,
        val payableLoanAmount: String,
        val interestRate: String,
        val loanAmountPaid: String,
        val paymentSchedule: String,
        val requestTime: String,
        val status: String,
        val linearId: String)

fun LoanState.toSimpleObj(): LoanStateSimpleObj {
    return LoanStateSimpleObj(
            borrower.nameOrNull()!!.organisation,
            lender.nameOrNull()!!.organisation,
            loanAmount.toString(),
            payableLoanAmount.toString(),
            interestRate.toString(),
            loanAmountPaid.toString(),
            paymentSchedule.toString(),
            requestTime.toString(),
            status,
            linearId.id.toString()
    )
}
