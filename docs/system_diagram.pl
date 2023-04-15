@startuml

frame {
    frame "Payment Service" {
        [Payment Service] as payments
        database "DB" as paymentsDb
    }

    frame "Account Service" {
        [Account Service] as accounts
        database "DB" as accountsDb
    }

    frame "Transaction Service" {
        [Transaction Service] as transactions
        database "Transactions \nEvent Store" as es
        database "Account \nCache DB" as accountsCacheDb
    }

    frame "Other Services" {
        [Other Service] as other
    }

    payments ..> transactions : "Event \nPaymentProcessed"
    accounts ..> transactions : "Event \nAccountCreditLimitUpdated"
    transactions <--> es
    transactions <--> accountsCacheDb
    transactions ..> other : "Event \nTransactionDecisioned"
    payments <--> transactions : "GET \nCheckAccountBalance"

    note as paymentsNote
        - system of record for payments
        - checks current balance to apply payment
        - sends payment submitted event
    end note

    note as accountsNote
        - system of record for account data
        - sends event for any change of account data
    end note

    note as transactionsNote
        - decisions card authorization
        - maintains relevant account info (e.g. credit limit)
        - uses Event Sourcing to maintain account balance
    end note

    paymentsNote .. payments
    accountsNote .. accounts
    transactionsNote .. transactions
}

cloud "Banking \nWebsite" as internet
cloud "POS / \nPayment Networks" as networks
actor customer

customer <--> networks
customer <--> internet
internet <--> accounts : "PUT \nUpdateAccountInfo"
internet <--> payments : "POST \nSubmitPayment"
internet <--> transactions : "GET \nCheckAccountBalance"
networks <--> transactions : "POST \nProcessTransaction"

@enduml