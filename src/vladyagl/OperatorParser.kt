package vladyagl

class OperatorParser<S : Expression>(val operators: List<OperatorCreator<S>>, val parseToken: (String) -> S) {

    private operator fun Int.plus(other: Boolean): Int {
        return if (other) this.plus(1) else plus(0)
    }

    private fun parse(text: String, level: Int): S {
        if (level >= operators.size) {
            return parseToken(text)
        }
        val type = operators[level]
        val matches = text.mapIndexedNotNull { i, c ->
            if (c == type.symbol[0] && checkCorrectBrackets(text.substring(i)))
                i
            else null
        }
        val position = if (type.leftPriority) matches.lastOrNull()
        else matches.firstOrNull()
        if (position == null) {
            return parse(text, level + 1)
        } else {
            try {
                val first: S = parse(text.substring(0, position), level + !type.leftPriority)
                val second: S = parse(text.substring(position + operators[level].symbol.length), level + type.leftPriority)
                return operators[level].create(first, second)
            } catch (e: Exception) {
                throw ParseException("Error while parsing: " + text, e)
            }
        }
    }

    fun parse(text: String): S {
        return parse(text, 0)
    }
}