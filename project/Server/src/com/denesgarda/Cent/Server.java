package com.denesgarda.Cent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public ArrayList<Socket> connections = new ArrayList<>();
    public ArrayList<Thread> receivers = new ArrayList<>();

    private final Thread acceptor;
    private boolean pause = false;

    public Server() throws IOException {
        ServerSocket serverSocket = new ServerSocket(4030);
        acceptor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        connections.add(socket);
                        Thread receiver = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        while (true) {
                                            String line = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                                            if (line == null) {
                                                connections.remove(socket);
                                            } else {
                                                // Manage input
                                                String[] args = line.split("\\|");
                                                if (args[0].equals("0")) {
                                                    socket.close();
                                                    connections.remove(socket);
                                                } else if (args[0].equals("1")) {
                                                    String username = args[1];
                                                    String password = args[2];
                                                    try {
                                                        boolean access = Util.validate(username, password);
                                                        if (access) {
                                                            send(socket, "0");
                                                        } else {
                                                            send(socket, "-1");
                                                        }
                                                    } catch (Exception e) {
                                                        send(socket, "-1");
                                                    }
                                                } else if (args[0].equals("2")) {
                                                    File accounts = new File("data" + File.separator + "accounts");
                                                    boolean exists = false;
                                                    for (File account : accounts.listFiles()) {
                                                        Properties acc = new Properties();
                                                        acc.load(new FileReader(account));
                                                        String username = acc.getProperty("username");
                                                        if (username.equals(args[1])) {
                                                            exists = true;
                                                        }
                                                    }
                                                    if (exists) {
                                                        send(socket, "-1");
                                                    } else {
                                                        send(socket, "0");
                                                    }
                                                } else if (args[0].equals("3")) {
                                                    String username = args[1];
                                                    String password = args[2];
                                                    if (username.matches("^[-a-zA-Z0-9._]+") || !password.contains("|")) {
                                                        int id = 0;
                                                        File accounts = new File("data" + File.separator + "accounts");
                                                        for (File account : accounts.listFiles()) {
                                                            if (account.getName().equals(id + ".properties")) {
                                                                id++;
                                                            } else {
                                                                break;
                                                            }
                                                        }
                                                        File account = new File("data" + File.separator + "accounts" + File.separator + id + ".properties");
                                                        boolean successful = account.createNewFile();
                                                        if (successful) {
                                                            BufferedWriter writer = new BufferedWriter(new FileWriter(account));
                                                            writer.write("username=" + username);
                                                            writer.newLine();
                                                            writer.write("password=" + password);
                                                            writer.newLine();
                                                            writer.write("balance=0");
                                                            writer.newLine();
                                                            writer.flush();
                                                            writer.close();
                                                            send(socket, "0");
                                                        } else {
                                                            send(socket, "-1");
                                                        }
                                                    } else {
                                                        send(socket, "-1");
                                                    }
                                                } else if (args[0].equals("4")) {
                                                    Request request = Request.generate(Main.ID);
                                                    Main.requests.add(request);
                                                    send(socket, "5|" + request.ID + "|" + request.start);
                                                    Main.ID++;
                                                } else if (args[0].equals("6")) {
                                                    int ID = Integer.parseInt(args[1]);
                                                    int target = Integer.parseInt(args[2]);
                                                    Request request = null;
                                                    boolean found = false;
                                                    for (Request filter : Main.requests) {
                                                        if (filter.ID == ID) {
                                                            request = filter;
                                                            found = true;
                                                        }
                                                    }
                                                    if (found) {
                                                        if (target == request.target) {
                                                            long millis = request.end();
                                                            double profit = (double) millis / 10000;
                                                            addBalance(args[3], profit);
                                                            send(socket, "7|0|" + profit);
                                                            Main.requests.remove(request);
                                                        } else {
                                                            send(socket, "7|-1|0");
                                                        }
                                                    } else {
                                                        send(socket, "7|-1|0");
                                                    }
                                                } else if (args[0].equals("8")) {
                                                    String username = args[1];
                                                    send(socket, String.valueOf(getBalance(username)));
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        connections.remove(socket);
                                    }
                                }
                            }
                        });
                        receivers.add(receiver);
                        receiver.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void start() {
        acceptor.start();
    }

    public void stop() {
        acceptor.interrupt();
    }

    public void broadcast(String message) throws IOException {
        for (Socket socket : connections) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(message);
            out.newLine();
            out.flush();
        }
    }

    public void send(Socket socket, String message) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write(message);
        out.newLine();
        out.flush();
    }

    static class Util {
        public static boolean validate(String username, String password) throws IOException {
            File accounts = new File("data" + File.separator + "accounts");
            for (File account : accounts.listFiles()) {
                Properties parsed = new Properties();
                parsed.load(new FileReader(account));
                if (parsed.get("username").equals(username) && parsed.get("password").equals(password)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static double getBalance(String username) throws IOException {
        File accounts = new File("data" + File.separator + "accounts");
        for (File account : accounts.listFiles()) {
            Properties parsed = new Properties();
            parsed.load(new FileReader(account));
            if (parsed.get("username").equals(username)) {
                return Double.parseDouble((String) parsed.get("balance"));
            }
        }
        return 0;
    }

    public static double addBalance(String username, double amount) throws IOException {
        File accounts = new File("data" + File.separator + "accounts");
        for (File account : accounts.listFiles()) {
            Properties parsed = new Properties();
            parsed.load(new FileReader(account));
            if (parsed.get("username").equals(username)) {
                double balance = Double.parseDouble((String) parsed.get("balance"));
                balance += amount;
                parsed.put("balance", String.valueOf(balance));
                parsed.store(new FileWriter("data" + File.separator + "accounts" + File.separator + account.getName()), null);
            }
        }
        return 0;
    }
}
