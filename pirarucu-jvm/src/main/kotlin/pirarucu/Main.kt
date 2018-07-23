package pirarucu

import pirarucu.benchmark.BenchmarkApplication
import pirarucu.uci.InputHandler
import pirarucu.uci.UciInput

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "bench" -> {
                val benchDepth = if (args.size > 1) {
                    Integer.parseInt(args[1])
                } else {
                    BenchmarkApplication.DEFAULT_BENCHMARK_DEPTH
                }
                BenchmarkApplication.runBenchmark(benchDepth)
            }
            else -> {
                println("Unknown argument")
            }
        }
    } else {
        val uciInput = UciInput(InputHandler())
        while (true) {
            try {
                val line = readLine()
                line ?: return
                uciInput.process(line)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}