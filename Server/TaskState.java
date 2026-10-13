package aar;

/** Cicle de vida d'una tasca. */
public enum TaskState {
    PENDING,    // a la cua, esperant un fil lliure
    RUNNING,    // un worker l'està executant
    FINISHED,   // acabada; el resultat ja és disponible
    ERROR       // ha fallat durant l'execució
}
