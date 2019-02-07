package com.template.utils

import java.util.*

data class LoanRecord(val borrower: String,
                      val amount: Long,
                      val interestRate: Long,
                      val paymentSchedue: Long,
                      val requestLoanId: String)