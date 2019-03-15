/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final SendableChooser<String> startingPos = new SendableChooser<>();

  private final double speed = 0.65;
  private ControlBoardInterface mControlBoard = GamepadControlBoard.getInstance();
  private NetworkTableEntry alignDifEntry;
	double forward = 0;
  double lr = 0;
  double k = 0;
	String gameData;
	Spark drive1 = new Spark(0);
	Spark drive2 = new Spark(1);
	Spark drive3 = new Spark(2);
	Spark drive4 = new Spark(3);
	VictorSP kick = new VictorSP(4);
	private Timer timer = new Timer();
	char[] gameMap = {'_', 'L', '_', 'R'};
	int pos;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    CameraServer.getInstance().startAutomaticCapture();
		startingPos.setDefaultOption("None", "0");
		startingPos.addOption("Left", "1");
		startingPos.addOption("Middle", "2");
		startingPos.addOption("Right", "3");
    SmartDashboard.putData(startingPos);
    
    NetworkTableInstance tblinst = NetworkTableInstance.getDefault();
    NetworkTable table = tblinst.getTable("SmartDashboard");
    alignDifEntry = table.getEntry("AlignDif");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    gameData = DriverStation.getInstance().getGameSpecificMessage();
		pos = Integer.parseInt(startingPos.getSelected());
		timer.start();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        if(timer.get() <= 2)
        {
          move(0.5, 0.5,1);
        }
        else if (timer.get() > 3) 
        {
          move(0,0,1);
          if (gameData.charAt(0) == 'L' && timer.get() <= 5) 
          {
            kick.set(-0.9);
          }
          else {
            kick.set(0);
          }
        }
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    double throttle = mControlBoard.getThrottle();
		double turn = mControlBoard.getTurn();
    boolean brake = mControlBoard.getBrake();
    boolean a_button = mControlBoard.getAButton();
		double RT = mControlBoard.getRightTrigger();
    double LT = mControlBoard.getLeftTrigger();

		if (Math.abs(throttle) > 0.05) {
				forward = throttle;
		}

		if(Math.abs(turn) > 0.05) {
			lr = turn;
		}
		if(throttle != 0 && lr == 0) {
			move(forward*speed, forward*speed, -1);
		}
		else if(Math.abs(throttle) < 0.1 && lr != 0) {
			move(lr*speed, -lr*speed,1);
		} else {
			move((forward+lr)*speed, (forward-lr)*speed, 1);
		}

    if (RT < 0.05) k = -RT/8;
    if (LT < -0.05) k = LT/4;

    kick.set((RT-LT)*0.3);

  //   if (a_button) {
  //   double alignDif = alignDifEntry.getDouble(0.0);
  //   int threshold = 30; // pixels
  //   float alignSpeed = 0.2f;
  //   if (alignDif > threshold) { // need to go right
  //     move(alignSpeed, -alignSpeed, 1);
  //   } else if (alignDif < -threshold) { // need to go left
  //     move(-alignSpeed, alignSpeed, 1);
  //   } else if (Math.abs(alignDif) <= threshold) {
  //     move(forward*speed, forward*speed, 1);
  //   }
  // }

    forward = lr = k = 0;
    // kick.set(0);
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

  private void move(double a, double b, float m)
	{
		if(Math.abs(a) > Math.abs(m*speed)) a = m*speed*a/Math.abs(a);
		if(Math.abs(b) > Math.abs(m*speed)) b = m*speed*b/Math.abs(b);
		
		drive1.set(a);
    drive2.set(a);
    drive3.set(-b);
		drive4.set(-b);
	}
}