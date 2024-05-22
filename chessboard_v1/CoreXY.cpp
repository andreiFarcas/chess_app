#include <AccelStepper.h>
#include <Arduino.h>
#include "CoreXY.h"

const int stepsPerRevolution = 2038; // Number of steps per complete rotation
bool isMoving = false; // Sets movement status for the motors

CoreXY::CoreXY() {
  // Pins entered in sequence IN1-IN3-IN2-IN4 for proper step sequence
  leftMotor = AccelStepper(AccelStepper::FULL4WIRE, 44, 46, 45, 47);
  rightMotor = AccelStepper(AccelStepper::FULL4WIRE, 30, 33, 29, 32);
}

void CoreXY::initialize() {
  // Setup motor performances
  leftMotor.setMaxSpeed(625);
  leftMotor.setAcceleration(900);
  rightMotor.setMaxSpeed(625);
  rightMotor.setAcceleration(900);

  // Set initial position as origin
  leftMotor.setCurrentPosition(0);
  rightMotor.setCurrentPosition(0);
}

void CoreXY::moveTo(int x, int y){
    if (!isMoving) { // Check again in case state changed
      setMotorsTarget(x, y);
      isMoving = true;
      while(isMoving){
        controlMovement();
      }
  }
}

void CoreXY::controlMovement() {
  if (isMoving) {
    leftMotor.run();
    rightMotor.run();
    if (leftMotor.distanceToGo() == 0 && rightMotor.distanceToGo() == 0) {
      leftMotor.stop();
      rightMotor.stop();
      isMoving = false;
    }
  }
}

void CoreXY::setMotorsTarget(int x, int y) {
  float q1 = 1.0/32.0 * (x + y);
  float q2 = 1.0/32.0 * (x - y);
  Serial.print("x: ");
  Serial.print(x);
  Serial.print("; y: ");
  Serial.print(y);
  Serial.print("; q1: ");
  Serial.print(q1);
  Serial.print("; q2: ");
  Serial.println(q2);

  leftMotor.moveTo(q1 * stepsPerRevolution);
  rightMotor.moveTo(q2 * stepsPerRevolution);
}

void CoreXY::returnToInitialPosition() {
  Serial.println("Returning to initial position.");
  
  leftMotor.moveTo(0); // Move to initial position (0)
  rightMotor.moveTo(0); // Move to initial position (0)

  isMoving = true;

  while (leftMotor.distanceToGo() != 0 || rightMotor.distanceToGo() != 0) {
    leftMotor.run();
    rightMotor.run();
  }

  Serial.println("Returned to initial position.");
  isMoving = false; // Reset movement status
}

void CoreXY::setIsMoving(bool value) {
  isMoving = value;
}

bool CoreXY::getIsMoving(){
  return isMoving;
}
