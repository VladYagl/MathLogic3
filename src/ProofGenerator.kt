import vladyagl.*

class ProofGenerator(a: Int, b: Int) : ProofChecker() {

    override val axioms = super.axioms + "a=a".parse()!!

    private fun makeAxiomSchema(number: Int, a: Expression = Zero(), b: Expression = Zero(), c: Expression = Zero()): Expression {
        return axiomSchemas[number].substitute(a, "A").substitute(b, "B").substitute(c, "C")
    }

    private val onlyZeros = "0=0->0=0->0=0".parse()!!

    private fun Expression.substitute(a: Term = Zero(), b: Term = Zero(), c: Term = Zero()): Expression {
        return this.substitute(a, "a").substitute(b, "b").substitute(c, "c")
    }

    private fun String.substitute(a: Term = Zero(), b: Term = Zero(), c: Term = Zero()): Expression {
        return ExpressionParser.parse(this).substitute(a, b, c)
    }

    private fun String.substitute(a: Int, b: Int = 0, c: Int = 0): Expression {
        return ExpressionParser.parse(this).substitute(makeNumber(a), makeNumber(b), makeNumber(c))
    }

    private fun prepareExpression(expression: Expression): List<Expression> {
        val list = ArrayList<Expression>()
        list += expression
        list += makeAxiomSchema(0, expression, onlyZeros, expression)
        list += Implication(onlyZeros, expression)
        expression.getFreeVariables().forEach {
            val last = list.last() as Implication
            list += Implication(last.left, Universal(Variable(it), last.right))
        }
        list += list.last().args[1]
        return list
    }

    private fun prepareAxioms(): List<Expression> {
        return listOf(onlyZeros) + axioms.map { prepareExpression(it) }.reduce { list, elements -> list + elements }
    }

    private fun useAxiom(number: Int, a: Term = Zero(), b: Term = Zero(), c: Term = Zero()): List<Expression> {
        val axiom = axioms[number].getFreeVariables().fold(axioms[number]) { last: Expression, name: String ->
            Universal(Variable(name), last)
        }

        fun removeUniversal(axiom: Expression): List<Expression> {
            return if (axiom is Quantifier) {
                val newAxiom = axiom.expression.substitute(
                        when (axiom.variable.varName) {
                            "a" -> a
                            "b" -> b
                            "c" -> c
                            else -> throw UnsupportedOperationException()
                        }, axiom.variable.varName)
                listOf(Implication(axiom, newAxiom)) + newAxiom + removeUniversal(newAxiom)
            } else {
                emptyList()
            }
        }
        return removeUniversal(axiom)
    }

    private fun useAxiom(number: Int, a: Int, b: Int = 0, c: Int = 0): List<Expression> {
        return useAxiom(number, makeNumber(a), makeNumber(b), makeNumber(c))
    }

    private fun makeNumber(number: Int): Term = when (number) {
        0 -> Zero()
        else -> Stroke(makeNumber(number - 1))
    }

    private fun proofLess(a: Int, b: Int): List<Expression> {
        return prepareAxioms() + onlyZeros + useAxiom(5, makeNumber(a)) +
                (0..b - a - 1).map {
                    val sum = "a + b".substitute(a, it) as Term
                    val sum_ = "a + b'".substitute(a, it) as Term
                    val expression = useAxiom(1, sum_, Stroke(sum), sum_)
                    val exp2 = useAxiom(1, Stroke(sum), sum_, makeNumber(a + it + 1))
                    useAxiom(0, sum, makeNumber(a + it)) +
                            "(a + b)'=c'".substitute(a, it, a + it) +
                            useAxiom(4, a, it) +
                            useAxiom(8, sum_) +
                            expression + expression.last().args[1] + expression.last().args[1].args[1] +
                            exp2 + exp2.last().args[1] + exp2.last().args[1].args[1]
                }.reduce { list, elements -> list.plus(elements) } +
                "a+c=b -> ?p(a+p=b)".substitute(a, b, b - a) +
                "?p(a+p=b)".substitute(a, b, b - a)
    }

    init {
        println(proofLess(a, b).joinToString(separator = "\n"))
        val result = check(proof = proofLess(a, b)) {}
        println("\n" + if (result) "OK" else "ERROR")
    }
}