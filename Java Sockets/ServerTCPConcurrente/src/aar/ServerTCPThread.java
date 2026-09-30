package aar;

import java.net.*;
import java.io.*;

public class ServerTCPThread extends Thread {
    private Socket socket = null;

    public ServerTCPThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Create streams failed.");
            System.exit(1);
        }

        try {
            out.println("Hello.");
            String inputLine = in.readLine();
            System.out.println("Recibo de cliente: " + inputLine);
            out.println("Bye.");
        } catch (IOException e) {
            System.err.println("Read failed.");
            System.exit(1);
        }

        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Close failed.");
            System.exit(1);
        }
    }
}
