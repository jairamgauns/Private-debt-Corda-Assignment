package com.template.utils

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.sql.PreparedStatement
import java.sql.ResultSet

object CustomVaultService {
    @CordaService
    class Service(val services: AppServiceHub) : SingletonSerializeAsToken() {
        fun getLoanRequests(owner: String): MutableList<LoanRecord> {
            val loanRecordList: MutableList<LoanRecord> = mutableListOf()
            val query = """
                        SELECT
                            state.borrower_name,
                            state.amount,
                            state.interest_rate,
                            state.payment_schedule,
                            state.request_loan_id
                        FROM
                            RequestLoanState as state
                        WHERE
                            state.intermediary_name = ?
                        """
            val session = services.jdbcSession()
            val pstmt: PreparedStatement = session.prepareStatement(query)
            pstmt.setObject(1, owner)
            val rs: ResultSet = pstmt.executeQuery()
            while (rs.next()) {
                val borrowerName = rs.getString("borrower_name")
                val amount = rs.getLong("amount")
                val interestRate = rs.getLong("interest_rate")
                val paymentSchedule = rs.getLong("payment_schedule")
                val requestLoanId = rs.getString("request_loan_id")

                val loanRecord = LoanRecord(borrowerName,amount,interestRate,paymentSchedule,requestLoanId)
                loanRecordList.add(loanRecord)
            }
            return loanRecordList
        }
    }
}