package pirarucu.tuning

import pirarucu.board.Board
import pirarucu.board.factory.BoardFactory
import pirarucu.eval.Evaluator
import java.util.concurrent.Callable

class ErrorCalculator : Callable<Double> {
    private var constantCalculated: Boolean = false
    private var constant: Double = 0.toDouble()

    private val fens = HashMap<String, Double>()
    private var board: Board = Board()

    fun addFenWithScore(fen: String, score: Double) {
        fens[fen] = score
    }

    override fun call(): Double? {
        if (!constantCalculated) {
            constant = calculateConstant()
            constantCalculated = true
        }
        return calculateError(constant)
    }

    private fun calculateConstant(): Double {
        var bestError = calculateError(ORIGINAL_CONSTANT)
        var bestConstant = ORIGINAL_CONSTANT
        println(String.format("Original constant %f | %.10f", bestConstant, bestError))
        var variation = INITIAL_VARIATION
        var bottomError = 0.0
        var topError = 1.0
        while (Math.abs(bottomError - topError) > MINIMUM_ERROR) {
            val bottomnConstant = bestConstant - variation
            bottomError = calculateError(bottomnConstant)

            val topConstant = bestConstant + variation
            topError = calculateError(topConstant)

            if (bottomError < topError) {
                if (bestError > bottomError) {
                    bestConstant = bottomnConstant
                    bestError = bottomError
                }
            } else if (topError < bottomError) {
                if (bestError > topError) {
                    bestConstant = topConstant
                    bestError = topError
                }
            }
            println(String.format("Constant %f | %.10f", bestConstant, bestError))

            variation /= 2.0
        }
        println(String.format("Final best constant %f | %.10f", bestConstant, bestError))
        constant = bestConstant
        return bestConstant
    }

    private fun calculateError(currentConstant: Double): Double {
        var error = 0.0
        for ((key, value) in fens) {
            BoardFactory.setBoard(key, board)
            try {
                val entryError = Math.pow(value - calculateSigmoid(Evaluator.evaluate(board),
                    currentConstant), 2.0)
                error += entryError
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return error / fens.size
    }

    companion object {

        const val MINIMUM_ERROR = 0.00000000001
        const val ORIGINAL_CONSTANT = 1.0
        private const val INITIAL_VARIATION = 1.0

        fun calculateSigmoid(score: Int, constant: Double): Double {
            return 1 / (1 + Math.pow(10.0, -constant * score / 400))
        }
    }
}
