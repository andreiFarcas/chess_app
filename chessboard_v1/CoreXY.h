#include <AccelStepper.h>
#include <Arduino.h>

#ifndef CoreXY_h
#define CoreXY_h

class CoreXY {
  
public:
    // Constructor
    CoreXY();

    // Function that sets the initial parameters for both motors
    void initialize();

    // More functions related to CoreXY to be added here
    void controlMovement();
    void setIsMoving(bool value);
    void setMotorsTarget(int x, int y);
    bool getIsMoving();
    void returnToInitialPosition();
    void moveTo(int x, int y);
    
private:
    AccelStepper leftMotor;  
    AccelStepper rightMotor; 
};

#endif
