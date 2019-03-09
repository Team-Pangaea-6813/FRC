package org.usfirst.frc.team6813.robot;



import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;



/**

 * The VM is configured to automatically run this class, and to call the

 * functions corresponding to each mode, as described in the IterativeRobot

 * documentation. If you change the name of this class or the package after

 * creating this project, you must also update the manifest file in the resource

 * directory.

 */

public class Robot extends IterativeRobot {

	final String defaultAuto = "Default";

	final String customAuto = "My Auto";
	
	private final double speed = 0.8;

	String autoSelected;

	SendableChooser<String> chooser = new SendableChooser<>();
	SendableChooser<Integer> startingPos = new SendableChooser<>();
	private ControlBoardInterface mControlBoard = GamepadControlBoard.getInstance();
	private boolean dampenedDrive = false;
	double forward = 0;
	double lr = 0;
	String gameData;
	Spark drive1 = new Spark(0);
	Spark drive2 = new Spark(1);
	Spark drive3 = new Spark(2);
	Spark drive4 = new Spark(3);
	Spark kick = new Spark(4);
	private Timer timer = new Timer();
	char[] gameMap = {'_', 'L', '_', 'R'};
	int pos;
	/**

	 * This function is run when the robot is first started up and should be

	 * used for any initialization code.

	 */

	@Override

	public void robotInit() {

		chooser.addDefault("Default Auto", defaultAuto);

		chooser.addObject("My Auto", customAuto);

		SmartDashboard.putData("Auto choices", chooser);
		
		CameraServer.getInstance().startAutomaticCapture();
		startingPos.addDefault("None", 0);
		startingPos.addObject("Left", 1);
		startingPos.addObject("Middle", 2);
		startingPos.addObject("Right", 3);
		SmartDashboard.putData(startingPos);
	}

	/**

	 * This autonomous (along with the chooser code above) shows how to select
;
	 * between different autonomous modes using the dashboard. The sendable

	 * chooser code works with the Java SmartDashboard. If you prefer the

	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro

	 *

	 * You can add additional auto modes by adding additional comparisons to the

	 * switch structure below with additional strings. If using the

	 * SendableChooser makes sure to add them to the chooser code above as well.


	 *
	 *
	 */

	@Override

	public void autonomousInit() {
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		pos = startingPos.getSelected();
		timer.start(); //start the autonomous timer
		
		
		
		
	}



	/**

	 * This function is called periodically during autonomous

	 */

	@Override
	public void autonomousPeriodic() {
		
		if(timer.get() <= 2)
		{
			move(0.5, 0.5,1);
		}
		else if (timer.get() > 3) {
			move(0,0,1);
			if (gameData.charAt(0) == 'L' && timer.get() <= 5) {
				kick.set(-0.9);
			}
			else {
				kick.set(0);
			}
		}
	}

	/**
	 * This function is called periodically during operator control

	 */

	
	
	@Override
	
	public void teleopPeriodic() 
	{
		double throttle = mControlBoard.getThrottle();
		double turn = mControlBoard.getTurn();
		boolean brake = mControlBoard.getBrake();
		double RT = mControlBoard.getRightTrigger();
		double LT = mControlBoard.getLeftTrigger();
		if (Math.abs(throttle) > 0.1) {
				forward =throttle;
		}

		if(Math.abs(turn) > 0.1) {
			lr = turn;
		}
		if(throttle != 0 && lr == 0) {
			move(forward*speed, forward*speed,1);
		}
		else if(Math.abs(throttle) < 0.1 && lr != 0) {
			move(lr*speed, -lr*speed,1);
		} else {
			move((forward+lr)*speed, (forward-lr)*speed,1);
		}

		forward = lr = 0;
	}



	/**

	 * This function is called periodically during test mode

	 */

	@Override

	public void testPeriodic() {

	}

	private void move(double a, double b, float m)
	{
		if(Math.abs(a) > m*speed) a = m*speed*a/Math.abs(a);
		if(Math.abs(b) > m*speed) b = m*speed*b/Math.abs(b);
		
		drive1.set(a);
		drive2.set(-b);
		drive3.set(a);
		drive4.set(-b);
	}
}
