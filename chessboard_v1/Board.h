#ifndef Board_h
#define Board_h

class Board{
public: 
  // Constructor
  Board();

  void readPiecePresence(); 
  void printPiecePresence();
  void processDetection(int row, int column);
  void move(int fromRow, int fromColumn, int toRow, int toColumn);
  void printState();
  void reset();
  void checkHumanIntervention(int row, int column, int muxNumber);

//private:
  char state[8][12];
  int presence[8][12];
};

#endif