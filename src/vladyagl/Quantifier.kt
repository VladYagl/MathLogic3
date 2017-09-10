package vladyagl

import java.util.*

open class Quantifier(name: String, symbol: String, val variable: Variable, override val expression: Expression) : Expression(name, symbol, expression) {

    override fun getFreeVariables(): Set<String> {
        return expression.getFreeVariables() - variable.varName
    }

    override fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return !(varName != variable.varName &&
                expression.getFreeVariables().contains(varName) &&
                other.getFreeVariables().contains(variable.varName))
    }

    override fun substitute(other: Expression, varName: String): Expression {
        if (varName != variable.varName) {
            return Quantifier(name, symbol!!, variable, expression.substitute(other, varName)).toString().parse()!!
        } else {
            return this
        }
    }

    override fun nodeEquals(other: Expression, variableMap: HashMap<String, String>): Boolean {
        return if (other is Quantifier) {
            val saveMatch = variableMap[variable.varName]
            variableMap[variable.varName] = other.variable.varName
            val result = expression.treeEquals(other.expression, variableMap)
            if (saveMatch == null) {
                variableMap.remove(variable.varName)
            } else {
                variableMap[variable.varName] = saveMatch
            }
            result
        } else {
            false
        }
    }

    override fun nodeToString(level: Int): String {
        return "$name{$variable}"
    }

    override fun toString(): String {
        return "$symbol${variable.varName}($expression)"
    }
}