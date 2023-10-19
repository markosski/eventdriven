@startuml

frame "monolith bbom" {
    [payments]
    [inventory]
    [catalog]
    [cart]
    [shipping]
    [users]
    database "Database"

    cart --> inventory
    payments --> shipping
    payments --> inventory
    cart --> payments
    cart --> catalog
    cart --> users
    shipping --> users
    shipping --> inventory
    shipping --> catalog
    inventory --> catalog
}

@enduml