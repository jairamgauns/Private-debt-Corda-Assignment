 Private-debt-Corda-Assignment CorDapp

Instructions for setting up:
git clone https://github.com/jairamgauns/Private-debt-Corda-Assignment.git
cd Private-debt-Corda-Assignment
./gradlew deployNodes
./build/nodes/runnodes
At this point you should have 6 console windows in total.The nodes are(Notary,BorrowerOne,LenderOne,Oracle,Regulator,Intermediary)  The nodes take about 20-30 seconds to finish booting up.

Starting the Webapp:
The minimum nodes we need are the 2 below:
Run the task below:
./gradlew runBorrowerOneServer
This will start the UI for the Borrower on IP: http://127.0.0.1:8080

./gradlew runLenderOneServer
This will start UI for the Lender on IP: http://127.0.0.1:8082

We similarly start the endpoints for the other nodes 

API Endpoints

// Print the identity
http://127.0.0.1:8080/me

//print identity of peers
http://127.0.0.1:8080/peers

//borrower Request loan
http://127.0.0.1:8080/request-loan?amount=10000&interestRate=5&paymentSchedule=1&intermediary=Intermediary

//lender request loan list
http://127.0.0.1:8082/request-loan-list

//lender negotiates on the loan
http://127.0.0.1:8082/negotiate-loan?id=<request_loan_id>&interestRate=10&paymentSchedule=2<this is in minutes>

//borrower accepts loan
http://127.0.0.1:8080/accept-loan?id=<request_loan_id>

//borrower or lender views the request
http://127.0.0.1:8080/loan-list-pending

//borrower pays certain amount
http://127.0.0.1:8080/pay?id=<loan_id>&payAmount=10000

//borrower/Lender views paid list
http://127.0.0.1:8080/loan-list-paid
http://127.0.0.1:8082/loan-list-paid
