package aar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

/** Client de consola: llegeix comandes de l'usuari, les envia al servidor i mostra la resposta. */
public class TaskClient {

    private final String host;
    private final int port;

    public TaskClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void execute() {
        try (ServerConnection connection = new ServerConnection(host, port);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Connected to " + host + ":" + port + ". Type 'exit' to quit.");
            runSession(connection, console);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    // Una comanda -> una resposta. Com que SUBMIT respon a l'instant, el client mai espera que acabi una tasca
    // i l'usuari pot escriure la comanda següent tot i tenir tasques PENDING o RUNNING.
    private void runSession(ServerConnection connection, BufferedReader console) throws IOException {
        while (true) {
            System.out.print("> ");
            String command = console.readLine();
            if (command == null || command.trim().equalsIgnoreCase("exit")) {   // Ctrl-D o "exit"
                return;
            }
            if (command.isBlank()) {
                continue;
            }
            System.out.println(connection.send(command));
        }
    }
}
