package org.usfirst.frc.team6813.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.Ultrasonic;

public class Sensor {
	
	Ultrasonic ultra;
	
	protected Sensor(int echoPin, int trigPin)
	{
		this.ultra = new Ultrasonic(echoPin, trigPin);
	}
	
	public double getUltrasonicData()
	{
		return this.ultra.getRangeInches();
	}
	
}
