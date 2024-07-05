package com.example.chessgame.data

data class ChessBoardState (
    val piecesState: List<List<String>> = listOf(
        listOf("bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"),
        listOf("bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"),
        listOf("", "", "", "", "", "", "", ""),
        listOf("", "", "", "", "", "", "", ""),
        listOf("", "", "", "", "", "", "", ""),
        listOf("", "", "", "", "", "", "", ""),
        listOf("wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"),
        listOf("wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR")
    ),

    // The last square that was clicked
    val clickedSquare: Pair<Int, Int> = Pair(-1, -1),

    // The possible moves for the clicked square
    val possibleMoves: List<Pair<Int, Int>> = listOf(),

    // true = white moves, false = black moves
    val whiteTurn: Boolean = true,

    // Boolean keeping track of checks
    val wKingInCheck: Boolean = false,
    val bKingInCheck: Boolean = false,

    // For Fen generation
    val moveCounter: Int = 1,

    // Stockfish options
    val playVsStockfish: Boolean = false,
    val difficultyStockfish: Int = 1,

    // Game Recording functionalities
    val recordingGame: Boolean = false,
    val moves: MutableList<List<Int>> = mutableListOf(),
    val currentMove: Int = 0,
    val watchRecording: Boolean = false,
    val captures: List<Pair<Int, String>> = mutableListOf(),
)