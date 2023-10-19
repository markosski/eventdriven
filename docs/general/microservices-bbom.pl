@startuml

frame "microservices bbom" {
    rectangle {
            [payments]
        }

        rectangle {
            [inventory]
        }

        rectangle {
            [catalog]
        }

        rectangle {
            [cart]
        }

        rectangle {
            [shipping]
        }

        rectangle {
            [users]
        }

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