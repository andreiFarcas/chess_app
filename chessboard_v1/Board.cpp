#include "Board.h"
#include <Arduino.h>

char state[8][12];

Board::Board() {
  // Matrix that represents each piece on their initial position
  char initialState[8][12] = {
    {'.', '.', 'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R', '.', '.'},
    {'.', '.', 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P', '.', '.'},
    {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'},
    {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'},
    {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'},
    {'.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'},
    {'.', '.', 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p', '.', '.'},
    {'.', '.', 'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r', '.', '.'}
  };

  // Copy initialState into our state
  for(int i = 0; i < 8; i++){
    for(int j = 0; j < 12; j++){
      state[i][j] = initialState[i][j];
    }
  }
}

void Board::move(int fromRow, int fromColumn, int toRow, int toColumn){
  state[toRow][toColumn] = state[fromRow][fromColumn];
  state[fromRow][fromColumn] = '.';
}

void Board::printState(){
  for (int row = 0; row < 8; row++) {
    for (int col = 0; col < 12; col++) {
        Serial.print(state[row][col]);
        Serial.print(" ");
    }
    Serial.println();  // New line at the end of each row
  }  
}
