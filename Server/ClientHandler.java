package aar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Atén un client: llegeix les seves comandes línia a línia i respon a cadascuna. */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final CommandProcessor processor;
    private final String clientId;   // "/ip:port" del client; és el "propietari" de les tasques que demana

    public ClientHandler(Socket socket, CommandProcessor processor) {
        this.socket = socket;
        this.processor = processor;
        this.clientId = String.valueOf(socket.getRemoteSocketAddress());
    }

    @Override
    public void run() {
        System.out.println("Connected: " + clientId);
        // try-with-resources: en acabar es tanquen els streams i el socket.
        // UTF-8 explícit: evita problemes d'accents entre màquines amb codificacions diferents.
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {   // readLine() torna null quan el client tanca la connexió
                System.out.println(clientId + " > " + line);
                out.println(processor.process(clientId, line));   // una comanda -> una resposta
            }
        } catch (IOException e) {
            // Només afecta aquest client: el servidor i la resta de clients continuen.
            System.err.println("Connection error with " + clientId + ": " + e.getMessage());
        }
        System.out.println("Disconnected: " + clientId);
    }
}
