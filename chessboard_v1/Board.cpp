#include "Board.h"
#include <Arduino.h>

char state[8][12];
int presence[8][12];
int liftedPiece[2];

const int S0 = 50;
const int S1 = 51; 
const int S2 = 52;
const int S3 = 53;

const int S0_1 = 40;
const int S1_1 = 49; 
const int S2_1 = 48;
const int S3_1 = 42;

const int mux_o1 = 5;
const int mux_o2 = 6;
const int mux_o3 = 7;
const int mux_o4 = 37;
const int mux_o5 = 38;
const int mux_o6 = 39;


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

  // Make sure MUX pins are correctly defined
  pinMode(S0, OUTPUT); 
  pinMode(S1, OUTPUT); 
  pinMode(S2, OUTPUT); 
  pinMode(S3, OUTPUT); 
  pinMode(S0_1, OUTPUT); 
  pinMode(S1_1, OUTPUT); 
  pinMode(S2_1, OUTPUT); 
  pinMode(S3_1, OUTPUT); 

  pinMode(mux_o1, INPUT);
  pinMode(mux_o2, INPUT);
  pinMode(mux_o3, INPUT);
  pinMode(mux_o4, INPUT);
  pinMode(mux_o5, INPUT);
  pinMode(mux_o6, INPUT);
}

void Board::readPiecePresence(){
  // Read sequentially 6 squares at a time
  for(int i = 0; i < 16; i++){
    // Converts the value of i to binary and sends each bit to mux on pins S0, S1, S2 and S3
    digitalWrite(S0, (i >> 0) & 1); // Shifts i by 0 positions to the right and extracts the last digit 
    digitalWrite(S1, (i >> 1) & 1);
    digitalWrite(S2, (i >> 2) & 1);
    digitalWrite(S3, (i >> 3) & 1);
    digitalWrite(S0_1, (i >> 0) & 1);
    digitalWrite(S1_1, (i >> 1) & 1);
    digitalWrite(S2_1, (i >> 2) & 1);
    digitalWrite(S3_1, (i >> 3) & 1);

    // Read the value of each mux output for a certain square on the board and update the array
    int row = (i/8 + 1) * 7 + i/8 - i;

    if(presence[row][1 - i/8] != !digitalRead(mux_o1)){
      /*
      if(presence[row][1 - i/8] == 1){
        // We removed a piece
        liftedPiece[0] = row;
        liftedPiece[1] = 1 - i/8;
      }

      if(presence[row][1 - i/8] == 0){
        // We dropped the piece down
        move(liftedPiece[0], liftedPiece[1], row, 1 - i/8);
        // TO DO: SEND THE MOVE TO APPLICATION 
      }
      */
      presence[row][1 - i/8] = !digitalRead(mux_o1);
      printPiecePresence();
    } 
    if (presence[row][3 - i/8] != !digitalRead(mux_o2)) {
      presence[row][3 - i/8] = !digitalRead(mux_o2);
      printPiecePresence();
    }
    if (presence[row][5 - i/8] != !digitalRead(mux_o3)) {
      presence[row][5 - i/8] = !digitalRead(mux_o3);
      printPiecePresence();
    }

    if (presence[row][7 - i/8] != !digitalRead(mux_o4)) {
      presence[row][7 - i/8] = !digitalRead(mux_o4);
      printPiecePresence();
    }

    if (presence[row][9 - i/8] != !digitalRead(mux_o5)) {
      presence[row][9 -i/8] = !digitalRead(mux_o5);
      printPiecePresence();
    } 

    if (presence[row][11 - i/8] != !digitalRead(mux_o6)) {
      presence[row][11 - i/8] = !digitalRead(mux_o6);
      printPiecePresence();
    }
    
    delay(10);
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

void Board::printPiecePresence(){
  for (int row = 0; row < 8; row++) {
    for (int col = 0; col < 12; col++) {
        Serial.print(presence[row][col]);
        Serial.print(" ");
    }
    Serial.println();  // New line at the end of each row
  }
  Serial.println("------------------------"); 
}
