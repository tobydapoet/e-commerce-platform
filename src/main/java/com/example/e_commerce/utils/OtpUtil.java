package com.example.e_commerce.utils;

import java.security.SecureRandom;

public final class OtpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_MIN = 100000;
    private static final int OTP_MAX = 900000;

    private OtpUtil() {
    }

    public static String generateOtp() {
        int otp = OTP_MIN + RANDOM.nextInt(OTP_MAX);
        return String.valueOf(otp);
    }
}
