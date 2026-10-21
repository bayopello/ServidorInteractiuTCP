package aar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Connexió TCP amb el Task Server: envia una comanda i retorna la resposta (sempre una línia). */
public class ServerConnection implements AutoCloseable {

    // Evita que el client es quedi penjat molt de temps si la IP no respon (útil al laboratori).
    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final Socket socket = new Socket();
    private final BufferedReader in;
    private final PrintWriter out;

    public ServerConnection(String host, int port) throws IOException {
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
        // UTF-8 explícit, igual que el servidor: així els accents no es trenquen entre màquines.
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    /**
     * Envia una comanda i espera la resposta d'una línia.
     *
     * @throws IOException si el servidor ha tancat la connexió o falla la xarxa
     */
    public String send(String command) throws IOException {
        out.println(command);
        if (out.checkError()) {   // PrintWriter no llança IOException: s'ha de consultar
            throw new IOException("could not send the command");
        }
        String response = in.readLine();   // null = el servidor ha tancat la connexió
        if (response == null) {
            throw new IOException("server closed the connection");
        }
        return response;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
