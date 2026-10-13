package aar;

import java.io.IOException;

/** Punt d'entrada: arrenca el servidor al port 4444 (o al que es passi com a primer argument). */
public class MainTaskServer {

    private static final int DEFAULT_PORT = 4444;

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try {
            new TaskServer(port).execute();
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
