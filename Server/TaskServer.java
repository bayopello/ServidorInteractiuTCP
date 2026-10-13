package aar;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Accepta connexions TCP i assigna cada client a un ClientHandler (un fil per client). */
public class TaskServer {

    private final int port;

    // Un fil per client connectat: bloquegen esperant comandes, així que un pool de mida fixa limitaria els clients.
    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    // Una sola instància compartida per tots els handlers (és thread-safe).
    private final CommandProcessor processor = new CommandProcessor(new TaskManager());

    public TaskServer(int port) {
        this.port = port;
    }

    /** Bucle principal del servidor: no retorna mai (només falla si no es pot obrir el port). */
    public void execute() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TaskServer listening on port " + port);
            while (true) {
                acceptClient(serverSocket);
            }
        }
    }

    private void acceptClient(ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();   // bloqueja fins que arriba un client
            clientPool.execute(new ClientHandler(socket, processor));
        } catch (IOException e) {
            System.err.println("Accept failed: " + e.getMessage());   // un error puntual no tomba el servidor
        }
    }
}
