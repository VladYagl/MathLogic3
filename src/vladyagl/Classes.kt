@file:Suppress("CanBeParameter")

package vladyagl

//BASE

class ParseException(message: String = "WTF", cause: Throwable? = null) : Exception(message, cause)

open class Predicate(name: String, symbol: String? = null, vararg termArgs: Term) : Expression(name, symbol) {
    override val args = termArgs

    override fun substitute(other: Expression, varName: String): Predicate {
        return Predicate(name, symbol, *args.map { it.substitute(other, varName) }.toTypedArray())
    }
}

open class Term(name: String, symbol: String? = null, vararg termArgs: Term) : Predicate(name, symbol, *termArgs) {

    override fun substitute(other: Expression, varName: String): Term {
        return Term(name, symbol, *args.map { it.substitute(other, varName) }.toTypedArray())
    }
}

// LOGIC

class Conjunction(override val left: Expression, override val right: Expression) : Expression("__Conjunction__", "&", left, right)

class Disjunction(override val left: Expression, override val right: Expression) : Expression("__Disjunction__", "|", left, right)

class Implication(override val left: Expression, override val right: Expression) : Expression("__Implication__", "->", left, right) {

    override fun substitute(other: Expression, varName: String): Implication {
        return Implication(left = left.substitute(other, varName), right = right.substitute(other, varName))
    }
}

class Negation(override val expression: Expression) : Expression("__Negation__", "!", expression)

// QUANTIFIERS

class Universal(variable: Variable, expression: Expression) : Quantifier("__Universal__", "@", variable, expression)

class Existential(variable: Variable, expression: Expression) : Quantifier("__Existential__", "?", variable, expression)

// PREDICATES

class Equality(override val left: Term, override val right: Term) : Predicate("__Equality__", "=", left, right)

// TERM

class Addition(override val left: Term, override val right: Term) : Term("__Sum__", "+", left, right)

class Multiplication(override val left: Term, override val right: Term) : Term("__Multiply__", "*", left, right)

class Stroke(val term: Term) : Term("__Stroke__", "'", term)

class Zero : Term("__Zero__", "0")