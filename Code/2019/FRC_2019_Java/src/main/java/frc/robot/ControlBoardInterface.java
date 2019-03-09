package frc.robot;

public interface ControlBoardInterface {

	double getTurn();

	double getThrottle();
	
	double getRightTrigger();
	double getLeftTrigger();

	boolean getBrake();

}