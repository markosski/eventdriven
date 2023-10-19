@startuml

frame "microservices EDA" {
    rectangle {
        [payments]
        database DB as pdb
    }

    rectangle {
        [inventory]
        database DB as idb
    }

    rectangle {
        [catalog]
        database DB as cdb
    }

    rectangle {
        [cart]
        database DB as cartdb
    }

    rectangle {
        [shipping]
        database DB as sdb
    }

    rectangle {
        [users]
        database DB as udb
    }

    rectangle {
        [notification]
    }

    cart --> inventory : http
    payments ..> shipping : kafka
    cart --> payments : http
    cart --> catalog : http
    cart --> users : http
    shipping ..> notification : kafka
}

@enduml