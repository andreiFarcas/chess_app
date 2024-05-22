#include "CoreXY.h"
#include "Board.h"

#ifndef AndroidApp_h
#define AndroidApp_h

class AppInterface {
public:
  CoreXY& coreXY;
  Board& board;

  //  Constructor
  AppInterface(CoreXY& coreXY, Board& board);

  // More functions related to communication to be added here
  void readData();
  void processData(String inputString);

private:
  int tranzistorPIN;
};

#endif