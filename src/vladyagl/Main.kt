package vladyagl

import java.io.*
import java.util.*
import kotlin.collections.HashSet


fun checkCorrectBrackets(text: String): Boolean {
    var balance = 0
    text.forEach {
        if (it == '(') balance++
        if (it == ')') balance--
        if (balance < 0) return false
    }
    return balance == 0
}

fun String.parse(): Expression? {
    if (this.isBlank()) return null
    return ExpressionParser.parse(this)
}

fun processFile(file: File, writer: Writer, parseHeader: Boolean = true, showStatus: Boolean = false) {
    val suppositions = ArrayList<Expression>()
    val proof = ArrayList<Expression>()
    var expression: Expression? = null
    try {
        BufferedReader(FileReader(file)).use { reader ->
            if (parseHeader) {
                val headerLine = reader.readLine().split("|-")
                val header = headerLine.first()
                expression = headerLine.last().parse()
                var last = 0
                (header + ',').mapIndexedNotNullTo(suppositions) { i, c ->
                    if (c == ',' && checkCorrectBrackets(header.substring(i))) {
                        val tmp = last
                        last = i + 1
                        header.substring(tmp, i).parse()
                    } else null
                }
            }
            var line: String? = reader.readLine()
            while (line != null) {
                if (!line.isEmpty() && line.trim().first() != '#') {
                    proof.add(line.parse()!!)
//                    println(proof.size)
                }
                line = reader.readLine()
            }
        }
    } catch (e: IOException) {
        print("Cant process file " + e.toString())
        return
    }

    val alpha = suppositions.lastOrNull()
    if (parseHeader) {
        writer.appendln(suppositions.dropLast(1).map(Expression::toString).joinToString(separator = ",")
                + "|-(" + (alpha?.let { alpha.toString() + ")->(" } ?: "") + expression.toString() + ")")
    } else {
        expression = proof.last()
    }

    val checker = ProofChecker()

    val statusThread: Thread
    statusThread = if (showStatus) getStatusThread(checker) else Thread()

    statusThread.start()

    var count = 0

    if (checker.check(suppositions, proof, expression!!) {
        writer.appendln(it.toString())
        count++
    }) {
        println("Вывод корректен")
    }
    statusThread.interrupt()
    statusThread.join()
}

fun getStatusThread(checker: ProofChecker): Thread {
    return Thread(Runnable {
        val startDate = Date()
        while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                println("total time: " + (Date() - startDate).toDouble() / 1000)
                return@Runnable
            }
            println("${(checker.getStatus() * 100).toInt()}% time: ${(Date() - startDate).toDouble() / 1000}secs")
        }
        println("total time: " + (Date() - startDate).toDouble() / 1000)
    })
}

operator fun  Date.minus(startDate: Date): Long {
    return this.time - startDate.time
}

fun main(args: Array<String>) {
    val files = args.filter { it[0] != '-' }.map(::File)
    val outputFiles = HashSet<File>()
    val parseHeader = !args.contains("-no-header")
    val showStatus = args.contains("-show-status")

    files.forEach { file ->
        println("\n\t# Processing file: $file")
        val outputFile = File(file.parent, file.nameWithoutExtension + ".out")
        outputFiles.add(outputFile)
        BufferedWriter(FileWriter(outputFile)).use { writer ->
            processFile(file, writer, parseHeader, showStatus)
        }
    }

    if (args.contains("-check")) {
        println("_____________________________________________________________________________")
        println("_____________________________________________________________________________")

        outputFiles.forEach { file ->
            println("\n\t# Checking answer: $file")
            BufferedWriter(StringWriter()).use { writer ->
                processFile(file, writer, parseHeader, showStatus)
            }
        }
    }
}