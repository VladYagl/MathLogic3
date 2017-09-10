package vladyagl

import java.util.*

open class Variable(val varName: String) : Term("__Variable[$varName]__") {
    override fun nodeEquals(other: Expression, variableMap: HashMap<String, String>): Boolean {
        return if (other is Variable) {
            return varName == other.varName
//            if (variableMap.containsKey(varName)) {
//                variableMap[varName] == other.varName
//            } else {
//                variableMap[varName] = other.varName
//                true
//            }
        } else {
            false
        }
    }

    override fun getFreeVariables(): Set<String> {
        val set = HashSet<String>()
        set.add(varName)
        return set
    }

    override fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return true
    }

    override fun substitute(other: Expression, varName: String): Term{
        if (varName == this.varName) {
            if (other !is Term) throw SubstituteError(varName, other)
            return other
        } else {
            return this
        }
    }

    override fun toString(): String {
        return varName
    }
}