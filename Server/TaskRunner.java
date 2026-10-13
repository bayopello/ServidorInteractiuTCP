package aar;

/** Feina que executa el pool per a una tasca: PENDING -> RUNNING -> FINISHED | ERROR. */
class TaskRunner implements Runnable {

    private final Task task;

    TaskRunner(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.markRunning();
        try {
            task.finish(task.getType().execute(task.getData()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // conserva l'estat d'interrupció del fil
            task.fail("interrupted");
        } catch (Exception | OutOfMemoryError e) {
            task.fail(e.toString());   // la tasca sempre acaba en estat final; mai es queda RUNNING
        }
    }
}
