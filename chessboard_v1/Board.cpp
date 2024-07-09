#include "globals.h"
#include "HardwareSerial.h"
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

  // Read initial position for piece placement
  int initialPresence[8][12] = {
    {0,0,1,1,1,1,1,1,1,1,0,0},
    {0,0,1,1,1,1,1,1,1,1,0,0},
    {0,0,0,0,0,0,0,0,0,0,0,0},
    {0,0,0,0,0,0,0,0,0,0,0,0},
    {0,0,0,0,0,0,0,0,0,0,0,0},
    {0,0,0,0,0,0,0,0,0,0,0,0},
    {0,0,1,1,1,1,1,1,1,1,0,0},
    {0,0,1,1,1,1,1,1,1,1,0,0},
  };

  // Copy initialState into our state
  for(int i = 0; i < 8; i++){
    for(int j = 0; j < 12; j++){
      presence[i][j] = initialPresence[i][j];
    }
  }

}

void Board::move(int fromRow, int fromColumn, int toRow, int toColumn){
  state[toRow][toColumn] = state[fromRow][fromColumn];
  state[fromRow][fromColumn] = '.';

  presence[toRow][toColumn] = 1;
  presence[fromRow][fromColumn] = 0;

  printState();
}

// Called when human intervention detected and checks wether a piece was placed on the square or removed and act accordingly
void Board::processDetection(int row, int column){
  // Human intervention detected -> check wether a piece was placed on the square or removed
    if(presence[row][column] == 1){
      // Intervention was to remove a piece, meomorize its original place
      liftedPiece[0] = row;
      liftedPiece[1] = column;
    } else if(presence[row][column] == 0){
      // Intevrention was to place down a previously lifted piece (assumes lifted piece exists in the memory)
      char piece = state[liftedPiece[0]][liftedPiece[1]];
  
      // Check if we have moved a black piece, we are in process of making a capture so we do not send that to bluetooth yet
      if ((piece == 'B' || piece == 'K' || piece == 'R' || piece == 'P' || piece == 'Q' || piece == 'N') && (turn != 2)) {
        move(liftedPiece[0], liftedPiece[1], row, column); // Updates the board state
      }else{
        move(liftedPiece[0], liftedPiece[1], row, column); // Updates the board state
  
        // Sends the data to bluetooth 
        String dataToSend = String(liftedPiece[0]) + " " + String(liftedPiece[1]) + " " + String(row) + " " + String(column);
    
        // Send the string to the HC-05 via Serial3
        Serial3.println(dataToSend);
  
        // Signals its Stockfish turn
        if(turn == 1)
          turn = 0;
  
        // Print the data to Serial for debugging
        Serial.println("Sent to Bluetooth: " + dataToSend);
    }    
  }
}

// Reads all sensors and detects any intervention on the pieces
void Board::readPiecePresence(){
  Serial.println("readpiecepresence() called");
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

    // For each mux we check the multiplexor outputs to detect changes in pieces presence
    checkHumanIntervention(row, 1 - i/8, mux_o1);
    checkHumanIntervention(row, 3 - i/8, mux_o2);
    checkHumanIntervention(row, 5 - i/8, mux_o3);
    checkHumanIntervention(row, 7 - i/8, mux_o4);
    checkHumanIntervention(row, 9 - i/8, mux_o5);
    checkHumanIntervention(row, 11 - i/8, mux_o6);
        
    delay(10);
  }
}

void Board::checkHumanIntervention(int row, int column, int muxNumber){
  // For each square we check human intervention
  if (presence[row][column] != !digitalRead(muxNumber)) {
      // Human intervention detected
      // Added filtering to try and eliminate false readings and disturbances
      bool falseReading = false;
      for(int k = 0; k < 5; k++){
        if(presence[row][column] == !digitalRead(muxNumber))
          falseReading = true;
        delay(50);
      }

      if(!falseReading){
        // Good reading so we continue the process
        processDetection(row, column);
        presence[row][column] = !digitalRead(muxNumber); // Update the presence matrix
        printPiecePresence(); 
      }
    }
}

void Board::printState(){
  for (int row = 0; row < 8; row++) {
    for (int col = 0; col < 12; col++) {
        Serial.print(state[row][col]);
        Serial.print(" ");
    }
    Serial.println();  // New line at the end of each row
  }  
  Serial.println("------------------------"); 
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

void Board::reset(){
  *this = Board(); // Rebuilds the object to which "this" pointer points to
}
