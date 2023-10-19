@startuml

frame "monolith" {
    [payments]
    [inventory]
    [catalog]
    [cart]
    [shipping]
    [users]
    [notification]
    database "Database"

    cart --> inventory : inprocess
    payments --> shipping : inprocess
    cart --> payments
    cart --> catalog
    cart --> users
    shipping --> notification
}

@enduml