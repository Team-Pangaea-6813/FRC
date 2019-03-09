package frc.robot;

import edu.wpi.first.wpilibj.Joystick;

public class GamepadControlBoard implements ControlBoardInterface 
{
	private final Joystick mGamepad;
	private static ControlBoardInterface mInstance = null;

	public static ControlBoardInterface getInstance()
	{
		if (mInstance == null) {
			mInstance = new GamepadControlBoard();
		}
		return mInstance;
    }
    
	protected GamepadControlBoard()
	{
		mGamepad = new Joystick(0);
	}

	@Override
	public double getTurn()
	{
		return mGamepad.getRawAxis(4);
	}

	@Override
	public double getThrottle()
	{
		return -mGamepad.getRawAxis(1);
	}

	@Override
	public boolean getBrake()
	{	
		return mGamepad.getRawButton(3);
	}
	
	@Override
	public double getRightTrigger() 
	{
		return mGamepad.getRawAxis(3);
	}
	
	@Override
	public double getLeftTrigger()
	{
		return mGamepad.getRawAxis(2);
	}
}