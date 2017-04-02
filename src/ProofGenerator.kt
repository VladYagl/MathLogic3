import vladyagl.*
import java.io.File

class ProofGenerator(val a: Int, val b: Int) : ProofChecker() {

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
        expression.getFreeVariables().reversed().forEach {
            val last = list.last() as Implication
            list += Implication(last.left, Universal(Variable(it), last.right))
        }
        list += list.last().right
        return list
    }

    private fun prepareAxioms(): List<Expression> {
        return listOf(onlyZeros) + axioms.map {
            if (it != "a=a".parse()!!) {
                prepareExpression(it)
            } else {
                val axiom = useAxiom(1, "a+0".parse()!! as Term, Variable("a"), Variable("a"))
                axiom +
                        axiom.last().right +
                        prepareExpression(it)
            }
        }.reduce { list, elements -> list + elements }
    }

    private fun useAxiom(number: Int, a: Term = Zero(), b: Term = Zero(), c: Term = Zero()): List<Expression> {
        val axiom = axioms[number].getFreeVariables().reversed().fold(axioms[number]) { last: Expression, name: String ->
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
                    val proof1 = useAxiom(1, sum_, Stroke(sum), sum_)
                    val proof2 = useAxiom(1, Stroke(sum), sum_, makeNumber(a + it + 1))
                    useAxiom(0, sum, makeNumber(a + it)) +
                            "(a + b)'=c'".substitute(a, it, a + it) +
                            useAxiom(4, a, it) +
                            useAxiom(8, sum_) +
                            proof1 + proof1.last().right + proof1.last().right.right +
                            proof2 + proof2.last().right + proof2.last().right.right
                }.reduce { list, elements -> list.plus(elements) } +
                "a+c=b -> ?p(a+p=b)".substitute(a, b, b - a) +
                "?p(a+p=b)".substitute(a, b, b - a)
    }

    private val comFile = File("src/com.txt").readLines()
    private val lemmaFile = File("src/lemma.txt").readLines()
    private val lemma2File = File("src/lemma2.txt").readLines()
    private val morganFile = File("src/morgan.txt").readLines()

    fun Expression.doReplace(): Expression {
        if (this is Quantifier) {
            return Quantifier(name, symbol!!, variable, expression.doReplace())
        }
        if (this is Variable) {
            return this
        }
        if (this.name == "B" || this.name == "C") {
            var expression = this.args[0].doReplace() as Term
            val count = if (this.name == "B") b else a - b - 1
            (0..count - 1).forEach {
                expression = Stroke(expression)
            }
            return expression
        } else {
            if (this is Term)
                return Term(name, symbol, *args.map { it.doReplace() as Term }.toTypedArray())
            return Expression(name, symbol, *args.map { it.doReplace() }.toTypedArray())
        }
    }


    private fun proofMoreOrEqual(a: Int, b: Int): List<Expression> {
        val result = ArrayList<Expression>()
        result.addAll(prepareAxioms())
        result.addAll(comFile.map { it.parse() as Expression })
        result.addAll(lemmaFile.map { it.parse() as Expression })

        result.addAll(lemma2File.map { ((it.parse() as Expression).doReplace().toString().parse() as Expression) })
        result.addAll(morganFile.map { ((it.parse() as Expression).doReplace().toString().parse() as Expression) })

        return result
    }

    fun doSomeStuff() {
        File("src/lemma.txt").writer().use {
            it.append(lemmaFile.map { it.parse() as Expression }.joinToString(separator = "\n"))
        }

    }

    val proof
        get() = if (a < b) {
            proofLess(a, b)
        } else {
            proofMoreOrEqual(a, b)
        }
}