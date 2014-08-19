package com.emilstrom.picwall.helper;

import java.util.Random;

public class GameMath
{
	static Random random = new Random();

	public static boolean getChance(float percentage)
	{
		return random.nextFloat() <= percentage;
	}
	
	public static double getRndDouble()
	{
		return random.nextDouble();
	}
	public static double getRndDouble(double max)
	{
		return random.nextDouble() * max;
	}
	public static double getRndDouble(double min, double max)
	{
		return getRndDouble(max - min) + min;
	}
	
	public static int getRndInt()
	{
		return random.nextInt();
	}
	public static int getRndInt(int max)
	{
		return random.nextInt(max + 1);
	}
	public static int getRndInt(int min, int max)
	{
		return random.nextInt(max + 1 - min) + min;
	}

	public static double getDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
	
	public static double getDirection(double x1, double y1, double x2, double y2)
	{
		return Math.atan2(y2 - y1, x2 - x1) * 180 / Math.PI;
	}

	public static double lengthDirX(float dir, float len)
	{
		return cosd(dir) * len;
	}
	public static double lengthDirY(float dir, float len)
	{
		return sind(dir) * len;
	}
	
	public static double sind(double degrees)
	{
		return Math.sin(Math.toRadians(degrees));
	}
	public static double cosd(double degrees)
	{
		return Math.cos(Math.toRadians(degrees));
	}
	public static double tand(double degrees)
	{
		return Math.tan(Math.toRadians(degrees));
	}
	
	public static double mod(double v, double max)
	{
		while (v < 0)
			v+=max;
		return v % max;
	}
	public static double mod(double v, double min, double max)
	{
		return mod(v - min, max - min) + min;
	}
}
