package com.astro.util;

public class CommonUtil {

	public static int generateOTP() {
		int randomPin = (int) (Math.random() * 9000) + 1000;
		return randomPin;
	}
}
