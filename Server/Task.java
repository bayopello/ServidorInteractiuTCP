package aar;

/** Una tasca i el seu estat. Es comparteix entre el fil que l'executa i els fils que atenen els clients. */
public class Task {

    private final int id;
    private final String clientId;   // client que l'ha demanat
    private final TaskType type;
    private final String data;       // paràmetre de la tasca (text, n, segons...)

    // Canvien mentre altres fils els llegeixen: tot l'accés passa per mètodes synchronized.
    private TaskState state = TaskState.PENDING;
    private String result;           // resultat si FINISHED; missatge d'error si ERROR

    public Task(int id, String clientId, TaskType type, String data) {
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public TaskType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public synchronized TaskState getState() {
        return state;
    }

    public synchronized String getResult() {
        return result;
    }

    public synchronized void markRunning() {
        state = TaskState.RUNNING;
    }

    // Resultat i estat s'actualitzen junts: cap lector veu FINISHED sense resultat.
    public synchronized void finish(String result) {
        this.result = result;
        this.state = TaskState.FINISHED;
    }

    public synchronized void fail(String errorMessage) {
        this.result = errorMessage;
        this.state = TaskState.ERROR;
    }
}
