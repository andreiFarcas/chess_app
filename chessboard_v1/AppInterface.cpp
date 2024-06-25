#include <HardwareSerial.h>
#include <Arduino.h>
#include "globals.h"
#include "AppInterface.h"
#include "CoreXY.h"

AppInterface::AppInterface(CoreXY& coreXY, Board& board) : coreXY(coreXY), board(board) { // CoreXY passed by refference to allow call of MoveTo function
  tranzistorPIN = 8;
}

void AppInterface::readData(){
  // Reads input data
  static String inputString = ""; // Holds incoming data (static because we call this multiple times and don't want to reset it every time)

  // Read incoming data until a newline is received
  while (Serial3.available()) {
    char inChar = (char)Serial3.read();

    if (inChar == '\n') {
      processData(inputString);
      inputString = ""; // Reset the input string
    } else {
      inputString += inChar;
    }
  }
}

void AppInterface::processData(String inputString) {
  int offsetX = 5;  // offset on x position from "home" position to first square of the grave
  int offsetY = 296; // max position on y coordinate

  int toMove[4];

  if(inputString[0] == 'c'){ // Command used to calibrate the initial position via bluetooth using coordinates in mm
    int commaIndex = inputString.indexOf(',');
    if (commaIndex != -1) { // Ensure there's a comma
      int x = inputString.substring(1, commaIndex).toInt(); // Convert first part to int (excluding first character which should be 'c')
      int y = inputString.substring(commaIndex + 1).toInt(); // Convert second part to int
      coreXY.moveTo(x, y);
    }
  } else if (inputString == "s") { // Command to return to starting position
    coreXY.returnToInitialPosition();
    // Also reset the state of the board
    board.reset();
    // Resets turn flag
    turn = 1;
  } else {
    //   ----------------------  HERE WE PROCESS ALL DATA (MOVES) RECIEVED FROM THE ANDROID APPLICATION  -----------------------------------
    
    Serial.println(inputString);
    // Data is in the format "fromRow fromColumn toRow toColumn" (with no spaces)
    for (int i = 0; i < 4; i++) {
      toMove[i] = inputString[i] - '0'; // Convert character to integer
    }

    // Check if we capture a piece, then first move the captured piece on the side
    if(board.state[toMove[2]][toMove[3]+2] != '.'){
      // We have a piece on destination square so we have to move it away
      coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]); // move to destination and grab the piece 
      digitalWrite(tranzistorPIN, HIGH); 

      // Vertical movement to make sure we will travel on the edges
      coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2] + 20); // move to destination and grab the piece 

      // Choose a place in the graveyard 
      bool placeFound = false;
      for(int i = 0; i < 8; i++){
        for(int j = 0; j < 2; j++){
          if((board.state[i][j] == '.') && (!placeFound)){
            // Empty grave found, we move there 
            placeFound = true;
            coreXY.moveTo(40*i + 20, offsetY-40*toMove[2] + 20); // Just horizontal movement (with added +20 on x  to keep the piece on the edge)
            coreXY.moveTo(40*i + 20, offsetY-40*j);
            coreXY.moveTo(40*i, offsetY-40*j);
            digitalWrite(tranzistorPIN, LOW); 

            board.move(toMove[2], toMove[3]+2, i, j); // updates the board state
          }
        }
      }
    }    

    // Pick the piece Stockfish moved
    coreXY.moveTo(offsetX+80+40*toMove[1], offsetY-40*toMove[0]); // Moves to pick position
    digitalWrite(tranzistorPIN, HIGH);

    // Check if the moved piece is a knight and compute special path
    if(board.state[toMove[0]][toMove[1]+2] == 'n' || board.state[toMove[0]][toMove[1]+2] == 'N'){
      // We compute a different path to make sure we don't collide with other pieces
      if(toMove[0] - toMove[2] == -1){
        coreXY.moveTo(offsetX+80+40*toMove[1], offsetY-40*toMove[0] - 20);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[0] - 20);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]);
      }else if (toMove[0] - toMove[2] == 1) {
        coreXY.moveTo(offsetX+80+40*toMove[1], offsetY-40*toMove[0] - 20);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[0] - 20);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]);
      }else if (toMove[1] - toMove[3] == 1){
        coreXY.moveTo(offsetX+80+40*toMove[1] - 20, offsetY-40*toMove[0]);
        coreXY.moveTo(offsetX+80+40*toMove[1] - 20, offsetY-40*toMove[2]);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]);
      }else if (toMove[1] - toMove[3] == -1) {
        coreXY.moveTo(offsetX+80+40*toMove[1] + 20, offsetY-40*toMove[0]);
        coreXY.moveTo(offsetX+80+40*toMove[1] + 20, offsetY-40*toMove[2]);
        coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]);
      }
    }else{
          // Piece is not a knight so we make simple, linear move
          coreXY.moveTo(offsetX+80+40*toMove[3], offsetY-40*toMove[2]);
    }

    board.move(toMove[0], toMove[1]+2, toMove[2], toMove[3]+2);
    board.printState();
    digitalWrite(tranzistorPIN, LOW); // Drop the piece
    
    // Flags its human's turn
    turn = 1;
  }
}