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
)
