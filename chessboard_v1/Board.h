#ifndef Board_h
#define Board_h

class Board{
public: 
  // Constructor
  Board();

  void readBoardState(); // TO DO
  void move(int fromRow, int fromColumn, int toRow, int toColumn);
  void printState();
//private:
  char state[8][12];
};

#endif