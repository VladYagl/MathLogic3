package vladyagl

object ExpressionParser {
    private val logicOperators = listOf(
            OperatorCreator<Expression>(symbol = "->", leftPriority = false, factory = ::Implication),
            OperatorCreator<Expression>(symbol = "|", factory = ::Disjunction),
            OperatorCreator<Expression>(symbol = "&", factory = ::Conjunction)
    )

    private val terms = listOf(
            OperatorCreator<Term>(symbol = "+", factory = ::Addition),
            OperatorCreator<Term>(symbol = "*", factory = ::Multiplication)
    )

    private fun parseTermUnary(text: String): Term {
        val token = text.trim()
        when {
            token.takeLast(1) == "'" -> return Stroke(termParser.parse(token.dropLast(1)))
            token.first() == '(' -> return termParser.parse(token.substring(1, token.length - 1))
            token.takeLast(1) == ")" -> {
                val name = token.substringBefore('(')
                val arguments = token.dropLast(1).substringAfter('(').split(",")
                return Term(name, null, *arguments.map { termParser.parse(it) }.toTypedArray())
            }
            token == "0" -> return Zero()
            else -> return Variable(token)
        }
    }

    private fun parsePredicate(text: String): Predicate {
        if (text.contains('=')) {
            return Equality(termParser.parse(text.substringBefore('=')), termParser.parse(text.substringAfter('=')))
        } else {
            if (text.contains('(')) {
                val name = text.substringBefore('(')
                val arguments = text.substringAfter('(').dropLast(1).split(",")
                return Predicate(name, null, *arguments.map { termParser.parse(it) }.toTypedArray())
            } else {
                if (text.trim().contains("[^a-zA-Z0-9_]".toRegex())) {
                    return termParser.parse(text)
                } else {
                    return Predicate(text)
                }
            }
        }
    }

    private fun parseLogicUnary(text: String): Expression {
        val token = text.trim()
        if (token.first() == '(' && token.last() == ')' && checkCorrectBrackets(token.drop(1).dropLast(1)))
            return logicParser.parse(token.substring(1, token.length - 1))
        when (token.first()) {
            '%' -> return Replaceable(token.drop(1))
            '!' -> return Negation(parseLogicUnary(token.drop(1)))
            //'(' -> return logicParser.parse(token.substring(1, token.length - 1))
            '?', '@' -> {
                val position = token.drop(1).indexOfFirst { !it.isLowerCase() && !it.isDigit() }
                val variable = Variable(token.drop(1).take(position))
                val args = parseLogicUnary(token.drop(position + 1))
                return if (token.first() == '?') Existential(variable, args) else Universal(variable, args)
            }
            else -> return parsePredicate(token)
        }
    }

    private val logicParser = OperatorParser(logicOperators, { parseLogicUnary(it) })
    private val termParser = OperatorParser(terms, { parseTermUnary(it) })

    fun parse(text: String): Expression {
        //text = text.replace(" |\t|\r".toRegex(), "")
        return logicParser.parse(text)
    }
}
