package com.template.webserver

import com.template.*
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val rpcOps = rpc.proxy
    private val myIdentity = rpcOps.nodeInfo().legalIdentities.first()

    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @RequestMapping(value = "/me", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    private fun me(): String {
        return myIdentity.toSimpleName()
    }

    @RequestMapping(value = "/peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun peers() = mapOf("peers" to rpcOps.networkMapSnapshot()
            .filter { nodeInfo -> nodeInfo.legalIdentities.first() != myIdentity }
            .map { it.legalIdentities.first().name.organisation })


    @GetMapping(value = "/request-loan", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun requestLoanState(@RequestParam(value = "amount") amount: Long,
                        @RequestParam(value = "interestRate") interestRate: Long,
                        @RequestParam(value = "paymentSchedule") paymentSchedule: Long,
                        @RequestParam(value = "intermediary") intermediary: String): ResponseEntity<String> {

        // 1. Get party objects for the counterparty.
        val intermediaryIdentity = rpcOps.partiesFromName(intermediary, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for $intermediary.")

        // 2. Start the RequestLoanFlow flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    RequestLoanFlow::class.java,
                    amount,
                    interestRate,
                    paymentSchedule,
                    intermediaryIdentity
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            //HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
            HttpStatus.CREATED to "Created Request Loan Record"

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 4. Return the result.
        return ResponseEntity.status(status).body(message)
    }

    @GetMapping(value = "/request-loan-list", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun requestLoanStateList(): List<RequestLoanStateSimpleObj> {
        val statesAndRefs = rpcOps.vaultQuery(RequestLoanState::class.java).states
        return statesAndRefs
                .map { stateAndRef -> stateAndRef.state.data }
                .map { state ->
                    // We map the anonymous lender and borrower to well-known identities if possible.
                    val intermediary = rpcOps.wellKnownPartyFromAnonymous(state.intermediary) ?: state.intermediary
                    val possiblyWellKnownLender = rpcOps.wellKnownPartyFromAnonymous(state.bidLender) ?: state.bidLender
                    val possiblyWellKnownBorrower = rpcOps.wellKnownPartyFromAnonymous(state.borrower) ?: state.borrower

                    RequestLoanState(possiblyWellKnownBorrower,
                            intermediary,
                            state.amount,
                            state.interestRate,
                            state.paymentSchedule,
                            possiblyWellKnownLender,
                            state.status,
                            state.linearId).toSimpleObj()
                }
    }

    @RequestMapping(value = "/negotiate-loan", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun negotiateLoan(@RequestParam(value = "id") id: String,
                      @RequestParam(value = "interestRate") interestRate: String,
                      @RequestParam(value = "paymentSchedule") paymentSchedule: String
                      ): ResponseEntity<String> {
        val linearId = UniqueIdentifier.fromString(id)

        // negotiate loan flow

        val(status,message) = try {
            val flowHandle = rpcOps.startFlowDynamic(NegotiateLoanFlow::class.java,
                    linearId,
                    interestRate.toLong(),
                    paymentSchedule.toLong())
            val result = flowHandle.use {  it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Negotiated Flow"
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to e.message
        }

        return ResponseEntity.status(status).body(message)
    }

    @GetMapping(value = "/accept-loan", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun acceptLoanState(@RequestParam(value = "id") id: String) : ResponseEntity<String>{

        val linearId = UniqueIdentifier.fromString(id)

        //start accept loan flow

        val(status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(AcceptLoanFlow::class.java,
                    linearId)
         val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Accepted"
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to e.message
        }

        return ResponseEntity.status(status).body(message)
    }

    @RequestMapping(value = "/pay", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun payLoan(@RequestParam(value = "id") id: String,
                @RequestParam(value = "payAmount") payAmount: String): ResponseEntity<String>{
        val linearId = UniqueIdentifier.fromString(id)
        val(status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(PayLoanFlow::class.java,
                    linearId,
                    payAmount.toLong())
            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Paid Amount: $payAmount"
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to e.message
        }

        return ResponseEntity.status(status).body(message)
    }

    @RequestMapping(value = "/loan-list-pending", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun loanStateList(): List<LoanStateSimpleObj> {
        val stateAndRefs = rpcOps.vaultQuery(LoanState::class.java).states
        return stateAndRefs
                .map { stateAndRef -> stateAndRef.state.data}
                .map { loanState ->
                    /*
                    val possiblyWellKnownLender = rpcOps.wellKnownPartyFromAnonymous(loanState.lender) ?: loanState.lender
                    val possiblyWellKnownBorrower = rpcOps.wellKnownPartyFromAnonymous(loanState.borrower) ?: loanState.borrower
                    LoanState(
                            possiblyWellKnownBorrower,
                            possiblyWellKnownLender,
                            loanState.loanAmount,
                            loanState.payableLoanAmount,
                            loanState.interestRate,
                            loanState.loanAmountPaid,
                            loanState.paymentSchedule,
                            loanState.requestTime,
                            loanState.linearId
                    ).toSimpleObj()*/
                    loanState.toSimpleObj()
                }
    }

    @GetMapping(value="/loan-list-paid", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun paidLoan(): List<LoanStateSimpleObj> {
        val consumedCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)
        val stateAndRef = rpcOps.vaultQueryByCriteria<LoanState>(consumedCriteria, LoanState::class.java).states

        return stateAndRef
                .map { stateAndRef -> stateAndRef.state.data }
                .map { loanState -> loanState.toSimpleObj() }
    }

}