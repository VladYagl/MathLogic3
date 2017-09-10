package vladyagl

class OperatorCreator<S : Expression>(
        val symbol: String,
        val leftPriority: Boolean = true,
        val factory: (S, S) -> S) {
    fun create(vararg args: S): S {
        return factory(args[0], args[1])
    }
}