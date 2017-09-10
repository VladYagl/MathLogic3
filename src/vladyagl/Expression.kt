package vladyagl

import java.util.*

@Suppress("EqualsOrHashCode")
open class Expression(val name: String, val symbol: String? = null, vararg argsTemp: Expression) {
    val TAB = "   "

    //Returns first not equal node
    operator fun minus(other: Expression): Expression? {
        return if (name == other.name) {
            args.mapIndexed { i, expression ->
                expression - other.args[i]
            }.firstOrNull { it != null }
        } else {
            return this
        }
    }

    open val args = argsTemp

    open fun getFreeVariables(): Set<String> {
        return if (args.isEmpty()) HashSet()
        else args.map(Expression::getFreeVariables).reduce { set, other -> set.union(other) }
    }

    open fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return args.isEmpty() || args.map { it.isFreeToSubstitute(other, varName) }.reduce(Boolean::and)
    }

    //Guaranties same toString but not same types
    open fun substitute(other: Expression, varName: String): Expression {
        return Expression(name, symbol, *args.map { it.substitute(other, varName) }.toTypedArray())
    }

    fun treeEquals(other: Expression, variableMap: HashMap<String, String> = HashMap()): Boolean {
        return nodeEquals(other, variableMap) && (args.isEmpty() || args.mapIndexed { i, expression ->
            expression.treeEquals(other.args[i])
        }.reduce(Boolean::and))
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Expression) {
            treeEquals(other)
        } else {
            false
        }
    }

    open fun nodeEquals(other: Expression, variableMap: HashMap<String, String> = HashMap()): Boolean {
        return other is Expression && name == other.name
    }

    @Deprecated("")
    fun toStringOld(): String {
        return toStringTree(0)
    }

    private fun toStringTree(level: Int): String {
        val tabs = TAB.repeat(level)
        return if (args.isNotEmpty()) args.joinToString(
                separator = ",\n",
                prefix = tabs + nodeToString(level) + "(\n",
                postfix = "\n$tabs)") { it.toStringTree(level + 1) }
        else tabs + nodeToString(level)
    }

    open protected fun nodeToString(level: Int): String {
        return name
    }

    private fun toStingNull() : String {
        return name + if (args.isNotEmpty()) args.map(Expression::toString).joinToString(separator = ", ", prefix = "(", postfix = ")") else ""
    }

    private fun toStringNotNull(symbol: String): String {
        return if (args.isEmpty()) {
            symbol
        } else if (args.size == 1) {
            if (name == "__Stroke__") {
                if (args.first().name == "__Stroke__") {
                    args.first().toString() + symbol
                } else {
                    "(" + args.first() + ")" + symbol
                }
            } else {
                symbol + "(" + args.first() + ")"
            }
        } else if (args.size == 2) {
            "(" + args.first() + ")" + symbol + "(" + args.last() + ")"
        } else {
            throw UnsupportedOperationException()
        }
    }

    override fun toString(): String {
        return if (symbol == null) {
            toStingNull()
        } else {
            toStringNotNull(symbol)
        }
    }

    annotation class Special(val why: String)

    open val left
        get() = if (args.size == 2) args[1] else throw UnsupportedOperationException()

    open val right
        get() = if (args.size == 2) args[0] else throw UnsupportedOperationException()

    open val expression
        get() = if (args.size == 1) args[0] else throw UnsupportedOperationException()
}