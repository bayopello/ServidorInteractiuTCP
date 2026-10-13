package aar;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/*
 * Tradueix cada línia rebuda del client en una operació sobre les tasques i en construeix la resposta.
 *
 * PROTOCOL (TCP, text UTF-8) -- tot el que necessita saber el client:
 *   - El client envia UNA línia per comanda; el servidor respon SEMPRE amb UNA línia.
 *     Per tant el client només ha de fer: enviar línia -> llegir línia -> mostrar-la.
 *   - El servidor no envia res en connectar (parla primer el client).
 *   - Les comandes no distingeixen majúscules de minúscules.
 *   - Un error es respon amb "ERROR: <motiu>" i la connexió continua oberta.
 *   - Per desconnectar, el client només ha de tancar el socket.
 *
 *   SUBMIT <REVERSE|PRIMES|DELAY> <dades>  ->  Task accepted. ID: 17
 *   STATUS <taskId>                        ->  Task 17: RUNNING   (PENDING | RUNNING | FINISHED | ERROR)
 *   RESULT <taskId>                        ->  el resultat tal qual (només si la tasca està FINISHED)
 *   LIST                                   ->  Task 17 (PRIMES): RUNNING | Task 18 (REVERSE): FINISHED   (o "No tasks")
 */
public class CommandProcessor {

    private final TaskManager taskManager;

    public CommandProcessor(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /** Executa una línia rebuda i retorna la línia de resposta. Mai llança excepcions. */
    public String process(String clientId, String line) {
        try {
            String[] parts = line.trim().split("\\s+", 2);   // [comanda, resta de la línia]
            String arguments = (parts.length > 1) ? parts[1] : "";
            switch (parts[0].toUpperCase(Locale.ROOT)) {
                case "SUBMIT": return submit(clientId, arguments);
                case "STATUS": return status(arguments);
                case "RESULT": return result(arguments);
                case "LIST":   return list(clientId);
                case "":       throw new IllegalArgumentException("empty command");
                default:       throw new IllegalArgumentException(
                        "unknown command '" + parts[0] + "' (use SUBMIT, STATUS, RESULT or LIST)");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "ERROR: " + e.getMessage();   // petició incorrecta o servidor ocupat
        } catch (RuntimeException e) {
            e.printStackTrace();                 // error inesperat: queda al log, però el client segueix connectat
            return "ERROR: internal server error";
        }
    }

    private String submit(String clientId, String arguments) {
        String[] parts = arguments.split("\\s+", 2);   // [tipus, dades]
        if (parts.length < 2) {
            throw new IllegalArgumentException("usage: SUBMIT <REVERSE|PRIMES|DELAY> <data>");
        }
        TaskType type = TaskType.fromName(parts[0]);
        String data = parts[1];
        type.validate(data);
        Task task = taskManager.submit(clientId, type, data);
        return "Task accepted. ID: " + task.getId();   // respon a l'instant, sense esperar que s'executi
    }

    private String status(String arguments) {
        Task task = findTask(arguments, "STATUS");
        return "Task " + task.getId() + ": " + task.getState();
    }

    private String result(String arguments) {
        Task task = findTask(arguments, "RESULT");
        TaskState state = task.getState();   // es llegeix una sola vegada: l'estat pot canviar entre lectures
        switch (state) {
            case FINISHED:
                return task.getResult();
            case ERROR:
                return "ERROR: task " + task.getId() + " failed: " + task.getResult();
            default:
                throw new IllegalArgumentException("task " + task.getId() + " is not finished yet (" + state + ")");
        }
    }

    // Només mostra les tasques d'aquest client (connexió); STATUS i RESULT, en canvi, accepten qualsevol ID.
    private String list(String clientId) {
        List<Task> tasks = taskManager.tasksOf(clientId);
        if (tasks.isEmpty()) {
            return "No tasks";
        }
        return tasks.stream()
                .map(task -> "Task " + task.getId() + " (" + task.getType() + "): " + task.getState())
                .collect(Collectors.joining(" | "));
    }

    private Task findTask(String arguments, String command) {
        int id = parseTaskId(arguments, command);
        return taskManager.find(id)
                .orElseThrow(() -> new IllegalArgumentException("task " + id + " does not exist"));
    }

    private int parseTaskId(String text, String command) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("usage: " + command + " <taskId>");
        }
    }
}
