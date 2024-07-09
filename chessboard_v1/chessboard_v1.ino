#include <AccelStepper.h>

#include "AppInterface.h"
#include "CoreXY.h"
#include "Board.h"
#include "globals.h"

// Initialize turn flag which is used to control the turn based behaviour
int turn = 1;

// Declare coreXY which manages the motors and electromagnet position
CoreXY coreXY = CoreXY();

// Declare Board which keeps track of the board state
Board board = Board();

// Declare AppInterface which provides communication functionality
AppInterface interface = AppInterface(coreXY, board);

const int tranzistorPIN = 8;

void setup()
{
  pinMode(tranzistorPIN, OUTPUT);
  digitalWrite(tranzistorPIN, LOW);

  Serial.begin(9600); // Begin serial communication with Arduino IDE (Serial Monitor)
  Serial3.begin(9600); // Begin serial communication with HC-05 using Serial3 (RX3 and TX3)

  coreXY.initialize();

  turn = 0; // First move made by human
}

void loop()
{ 
  if(turn == 2){
    Serial.println("turn == 2");
    board.readPiecePresence(); // Wait for move from human and send it to app
    interface.readData(); // In case we recieve s we know that we must leave the game mode and reset the board, if we recieve r, we keep recording
  } else if(turn == 1){
    Serial.println("turn 1");
    // Human moves
    board.readPiecePresence(); // Waits and reads the move made by human player, sends it to app interface which send it to bluetooth 
  } else{
    Serial.println("turn 0");
    // Stockfish moves
    interface.readData(); // Reads data from serial communication via app and makes the right action based on recieved info
  }

  delay(100);
}



// ----------   ONLY USED DURING DEVELOPMENT   ----------
void readInput(){
  // Reads input data
  static String inputString = ""; // Holds incoming data
  static boolean inputComplete = false; // Whether we've received all data

  // Read incoming data until a newline is received
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    if (inChar == '\n') {
      inputComplete = true;
    } else {
      inputString += inChar;
    }
  }

  if (inputComplete) {
    if (inputString == "s") { // Command to return to starting position
      coreXY.returnToInitialPosition();
    } else {
      if(inputString == "e"){
        digitalWrite(tranzistorPIN, !digitalRead(tranzistorPIN));
      } else 
        processInput(inputString);
    }
    inputString = ""; // Clear the string for new input
    inputComplete = false;
  }
}

void processInput(String data) {
  // Data is in the format "x,y"
  int commaIndex = data.indexOf(',');
  if (commaIndex != -1) { // Ensure there's a comma
    int x = data.substring(0, commaIndex).toInt(); // Convert first part to int
    int y = data.substring(commaIndex + 1).toInt(); // Convert second part to int

    coreXY.moveTo(x, y);
  }
}

