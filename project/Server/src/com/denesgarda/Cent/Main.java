package com.denesgarda.Cent;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static int ID = 0;
    public static ArrayList<Request> requests = new ArrayList<>();

    public static Server server;

    public static void main(String[] args) throws IOException {
        server = new Server();
        server.start();
    }
}
