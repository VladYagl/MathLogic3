package vladyagl

open class ProofException(message: String = "") : Exception(message)

class QuantifierRuleVariableException(val varName: String) : ProofException()

class FreeVariableException(val varName: String, val expression: Expression) : ProofException()

class SubstituteException(val varName: String, val term : Term, val expression: Expression) : ProofException()

class SubstituteError(val varName : String, val expression : Expression) : RuntimeException()