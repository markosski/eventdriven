package eventdriven.transactions.domain.event

import eventdriven.transactions.domain.model.transaction.Decision

case class DecisionedAuth(
                         preAuth: PreDecisionedAuth,
                         decision: Decision.Value,
                         decisionTimeInMillis: Long,
                         ruleVersion: String
                    )
