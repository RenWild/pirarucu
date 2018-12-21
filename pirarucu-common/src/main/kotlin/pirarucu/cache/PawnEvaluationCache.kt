package pirarucu.cache

import pirarucu.board.Bitboard
import pirarucu.board.Board
import pirarucu.board.Square
import pirarucu.eval.EvalConstants
import pirarucu.util.PlatformSpecific

/**
 * 160 Bits per cache entry
 */
class PawnEvaluationCache(sizeMb: Int) {

    private val tableBits = Square.getSquare(sizeMb) + 16
    var tableLimit = Bitboard.getBitboard(tableBits).toInt()
    private val indexShift = 64 - tableBits

    private val keys = LongArray(tableLimit)
    private val values = IntArray(tableLimit)
    private val passed = LongArray(tableLimit)

    fun reset() {
        PlatformSpecific.arrayFill(keys, 0)
    }

    fun findEntry(board: Board): Int {
        val wantedKey = getKey(board)
        val index = getIndex(wantedKey)
        val passedPawnInfo = passed[index]
        val value = values[index]
        val savedKey = keys[index] xor passedPawnInfo xor value.toLong()
        if (savedKey == wantedKey) {
            board.evalInfo.passedPawnBitboard = passedPawnInfo
            return value
        }
        return EvalConstants.SCORE_UNKNOWN
    }

    fun saveEntry(board: Board, value: Int, passedPawnBitboard: Long) {
        if (EvalConstants.PAWN_EVAL_CACHE) {
            val key = getKey(board)
            val index = getIndex(key)
            keys[index] = key xor value.toLong() xor passedPawnBitboard
            values[index] = value
            passed[index] = passedPawnBitboard
        }
    }

    private fun getKey(board: Board): Long {
        return board.pawnZobristKey
    }

    private fun getIndex(key: Long): Int {
        return (key ushr indexShift).toInt()
    }

}