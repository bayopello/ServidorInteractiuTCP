package aar;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Registre compartit de tasques + piscines de fils que les executen.
 * Tots els mètodes són thread-safe: cada ClientHandler els crida des del seu propi fil.
 */
public class TaskManager {

    // Dues piscines de fils segons el perfil de la tasca, perquè una tasca lenta no deixi sense fils les d'un altre perfil:
    //  - cpuPool: tasques que gasten CPU i memòria (PRIMES) -> tants fils com nuclis; la resta espera en estat PENDING.
    //  - lightPool: tasques que gairebé no gasten recursos (REVERSE, DELAY que només dorm) -> molts fils,
    //    així una tasca ràpida no espera mai darrere d'un DELAY llarg.
    private static final int CPU_WORKERS = Runtime.getRuntime().availableProcessors();
    private static final int LIGHT_WORKERS = 100;
    // Màxim de tasques en espera (PENDING) per piscina. Passat el límit, SUBMIT es rebutja en lloc de gastar memòria sense fi.
    private static final int MAX_QUEUED_TASKS = 1000;

    private final AtomicInteger nextId = new AtomicInteger(1);               // IDs únics sense locks
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();      // registre compartit entre tots els fils
    private final ExecutorService cpuPool = newBoundedPool(CPU_WORKERS);
    private final ExecutorService lightPool = newBoundedPool(LIGHT_WORKERS);

    /**
     * Crea la tasca (PENDING), la registra i l'encua a la piscina que toca. Torna a l'instant, sense esperar l'execució.
     *
     * @throws IllegalStateException si la cua de tasques en espera és plena
     */
    public Task submit(String clientId, TaskType type, String data) {
        Task task = new Task(nextId.getAndIncrement(), clientId, type, data);
        tasks.put(task.getId(), task);
        ExecutorService pool = type.isCpuBound() ? cpuPool : lightPool;
        try {
            pool.execute(new TaskRunner(task));
        } catch (RejectedExecutionException e) {
            tasks.remove(task.getId());   // no deixem rastre d'una tasca que no s'executarà
            throw new IllegalStateException("server busy: too many pending tasks, try again later");
        }
        return task;
    }

    public Optional<Task> find(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    /** Tasques demanades per un client, ordenades per ID. */
    public List<Task> tasksOf(String clientId) {
        return tasks.values().stream()
                .filter(task -> task.getClientId().equals(clientId))
                .sorted(Comparator.comparingInt(Task::getId))
                .collect(Collectors.toList());
    }

    private static ExecutorService newBoundedPool(int threads) {
        return new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(MAX_QUEUED_TASKS));
    }
}
