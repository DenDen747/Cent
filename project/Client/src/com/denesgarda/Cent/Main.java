package com.denesgarda.Cent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class Main {
    public static String breaker = "===============================";

    public static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    public static String serverAddress;
    public static Client client;

    public static String un;
    public static String pw;

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Thank you for using Cent.");
                    client.send("0");
                    client.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Retrieving server address...");
        URLConnection urlConnection = new URL("https://raw.githubusercontent.com/DenDen747/Cent/main/.link").openConnection();
        BufferedReader connectionReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        serverAddress = connectionReader.readLine();
        connectionReader.close();
        System.out.println("Connecting to server...");
        client = new Client(serverAddress);
        client.start();
        System.out.println("Connected.");
        try {
            authentication:
            while (true) {
                printBreaker();
                System.out.println("Authentication\n[1] Login\n[2] Signup\n[~] Exit");
                String authentication = in.readLine();
                if (authentication.equals("1")) {
                    enterUsername:
                    while (true) {
                        System.out.print("Enter username: ");
                        String username = in.readLine();
                        if (username.isBlank()) {
                            invalid();
                        } else {
                            enterPassword:
                            while (true) {
                                System.out.print("Enter password: ");
                                String password = in.readLine();
                                if (password.isBlank()) {
                                    invalid();
                                } else {
                                    String res = query("1|" + username + "|" + password);
                                    if (res.equals("0")) {
                                        System.out.println("Successfully logged in.");
                                        un = username;
                                        pw = password;
                                        break authentication;
                                    } else {
                                        invalid("Incorrect username or password.");
                                        break enterUsername;
                                    }
                                }
                            }
                        }
                    }
                } else if (authentication.equals("2")) {
                    System.out.println("NOTE: The details you are about to enter CANNOT be changed in the future, and your password CANNOT be reset.\n[ENTER] Continue");
                    in.readLine();
                    createUsername:
                    while (true) {
                        System.out.print("Create username (ONLY a-z, A-Z, 0-9, ., _,): ");
                        String username = in.readLine();
                        if (username.matches("^[-a-zA-Z0-9._]+")) {
                            String taken = query("2|" + username);
                            if (taken.equals("-1")) {
                                invalid("Username is taken.");
                            } else {
                                System.out.print("Create password (NO |): ");
                                String password = in.readLine();
                                if (password.contains("|")) {
                                    invalid("Invalid format.");
                                } else {
                                    String ok = query("3|" + username + "|" + password);
                                    if (ok.equals("0")) {
                                        System.out.println("Account created.\n[ENTER] Continue");
                                        in.readLine();
                                    } else {
                                        System.out.println("Failed to create account. Try again later.");
                                    }
                                }
                            }
                        } else {
                            invalid("Invalid format.");
                        }
                        break createUsername;
                    }
                } else if (authentication.equals("~")) {
                    System.exit(0);
                } else {
                    invalid();
                }
            }
            mainMenuInput:
            while (true) {
                printBreaker();
                System.out.println("Cent Main Menu\n[1] Send\n[2] Receive\n[3] Mine\n[~] Exit");
                String mainMenuInput = in.readLine();
                if (mainMenuInput.equals("1")) {

                } else if (mainMenuInput.equals("2")) {

                } else if (mainMenuInput.equals("3")) {
                    printBreaker();
                    System.out.println("Beginning mining.\n[~] Stop");
                    while (true) {
                        if (in.ready()) {
                            String exit = in.readLine();
                            if (exit.equals("~")) {
                                break;
                            }
                        }
                        String req = query("4");
                        String[] reqArgs = req.split("\\|");
                        int ID = Integer.parseInt(reqArgs[1]);
                        int start = Integer.parseInt(reqArgs[2]);
                        if (start != 0) {
                            System.out.println(ID + ": " + start);
                            Random random = new Random();
                            while (true) {
                                int key = random.nextInt(1000);
                                int target = 0;
                                try {
                                    target = start / key;
                                } catch (ArithmeticException ignored) {
                                }
                                String check = query("6|" + ID + "|" + target + "|" + un);
                                String[] checkArgs = check.split("\\|");
                                if (checkArgs[1].equals("0")) {
                                    System.out.println("    -> " + target + "\n    + " + checkArgs[2] + "\n    = " + query("8|" + un));
                                    break;
                                }
                            }
                        }
                    }
                } else if (mainMenuInput.equals("~")) {
                    System.exit(0);
                } else {
                    invalid();
                }
            }
        } catch (SocketException e) {
            client.connectionLost(/*serverAddress, false*/);
        }
    }

    public static void printBreaker() {
        System.out.println(breaker);
    }

    public static void invalid() throws IOException {
        System.out.println("Invalid input.\n[ENTER] Continue");
        in.readLine();
    }

    public static void invalid(String message) throws IOException {
        System.out.println(message + "\n[ENTER] Continue");
        in.readLine();
    }

    public static String query(String query) throws IOException {
        client.send(query);
        return client.in.readLine();
    }
}
