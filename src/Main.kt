
import vladyagl.ProofChecker
import vladyagl.getStatusThread
import java.io.File

val proofFile = File("proof.txt")

fun main(args: Array<String>) {
    val proof = ProofGenerator(args[0].toInt(), args[1].toInt()).proof

    proofFile.writer().use { writer ->
        writer.append(proof.joinToString(separator = "\n"))
    }

    if (args.contains("-check")) {
        println("\nPerforming self check...")
        val checker = ProofChecker()

        val statusThread = if (args.contains("-show-status")) getStatusThread(checker) else Thread()
        statusThread.start()

        val result = checker.check(proof = proof) {}
        println("\n" + if (result) "OK" else "ERROR")

        statusThread.interrupt()
        statusThread.join()
    }
}
