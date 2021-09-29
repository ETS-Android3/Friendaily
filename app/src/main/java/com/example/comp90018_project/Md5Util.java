package com.example.comp90018_project;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

    private Md5Util() {
        throw new IllegalStateException("Utility class");
    }

    // Static method for hashing strings
    public static String md5(String password) {
        byte[] encodePassword = null;
        try {
            encodePassword = MessageDigest.getInstance("md5").digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new BigInteger(1, encodePassword).toString(16);
    }
}
