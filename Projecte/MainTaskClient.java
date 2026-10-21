package aar;

/** Punt d'entrada: java aar.MainTaskClient [host] [port] (per defecte 127.0.0.1 i 4444). */
public class MainTaskClient {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 4444;

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : DEFAULT_HOST;
        int port;
        try {
            port = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        } catch (NumberFormatException e) {
            System.err.println("Usage: java aar.MainTaskClient [host] [port]");
            return;
        }
        new TaskClient(host, port).execute();
    }
}
