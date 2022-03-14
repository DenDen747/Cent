package com.denesgarda.Cent;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Client {
    public Socket connection;
    public BufferedReader in;
    public BufferedWriter out;
    private final Thread receiver;
    private int ID;
    private BigInteger last;

    public Client(String serverAddress) throws IOException {
        connection = new Socket(serverAddress, 4030);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        Client client = this;
        receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    while (true) {
                        if (in.ready()) {
                            String line = in.readLine();
                            if (line == null) {
                                connectionLost(/*serverAddress, false*);
                            }
                            // Manage input
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost(/*serverAddress, false*);
                }*/
            }
        });
    }

    public void start() {
        receiver.start();
    }

    public void stop() throws IOException {
        connection.close();
        receiver.interrupt();
    }

    public void send(String message) throws IOException {
        out.write(message);
        out.newLine();
        out.flush();
    }

    public void connectionLost(/*String serverAddress, boolean re*/) {
        /*try {
            if (re) {
                System.out.println("Failed to reconnect. Retrying...");
            } else {
                System.out.println("Connection lost. Attempting to reconnect in a few seconds...");
            }
            Thread.sleep(5000);
            connection = new Socket(serverAddress, 4030);
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            System.out.println("Successfully reconnected!");
        } catch (InterruptedException | IOException e) {
            connectionLost(serverAddress, true);
        }*/
        System.out.println("Connection lost.\n[ENTER] Exit");
        try {
            Main.in.readLine();
        } catch (Exception ignored) {}
        System.exit(-1);
    }

    public byte[] deserializeArray(final String data) {
        try (final ByteArrayInputStream bias = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             final ObjectInputStream ois = new ObjectInputStream(bias)) {
            return (byte[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
