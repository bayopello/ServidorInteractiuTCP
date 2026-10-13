package aar;

import java.util.BitSet;
import java.util.Locale;

/** Tipus de tasca: cadascun sap validar les seves dades i executar-se. */
public enum TaskType {

    /** REVERSE <text>: retorna el text invertit. */
    REVERSE(false) {
        @Override
        String execute(String data) {
            return new StringBuilder(data).reverse().toString();
        }
    },

    /** PRIMES <n>: tots els primers entre 2 i n, separats per espais. */
    PRIMES(true) {
        @Override
        void validate(String data) {
            parseInt(data, 2, MAX_PRIMES_LIMIT, "n");
        }

        @Override
        String execute(String data) {
            return primesUpTo(parseInt(data, 2, MAX_PRIMES_LIMIT, "n"));
        }
    },

    /** DELAY <segons>: espera els segons indicats i retorna "delay completado". */
    DELAY(false) {
        @Override
        void validate(String data) {
            parseInt(data, 0, MAX_DELAY_SECONDS, "seconds");
        }

        @Override
        String execute(String data) throws InterruptedException {
            Thread.sleep(parseInt(data, 0, MAX_DELAY_SECONDS, "seconds") * 1000L);
            return "delay completado";
        }
    };

    // Límits per evitar que una sola petició esgoti la memòria o els fils del servidor.
    private static final int MAX_PRIMES_LIMIT = 100_000_000;
    private static final int MAX_DELAY_SECONDS = 3600;

    // true = gasta CPU i memòria (PRIMES); false = gairebé no gasta recursos (REVERSE; DELAY només dorm).
    private final boolean cpuBound;

    TaskType(boolean cpuBound) {
        this.cpuBound = cpuBound;
    }

    boolean isCpuBound() {
        return cpuBound;
    }

    /** Llança IllegalArgumentException si les dades no són vàlides. Per defecte accepta qualsevol text. */
    void validate(String data) {
    }

    /** Executa la tasca (pot trigar) i retorna el resultat. Només es crida amb dades ja validades. */
    abstract String execute(String data) throws InterruptedException;

    /** Converteix el nom rebut pel protocol (sense distingir majúscules) en un TaskType. */
    static TaskType fromName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown task type '" + name + "' (use REVERSE, PRIMES or DELAY)");
        }
    }

    // Converteix text a enter dins de [min, max]; si no, llança IllegalArgumentException.
    private static int parseInt(String text, int min, int max, String name) {
        String error = name + " must be an integer between " + min + " and " + max;
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(error);
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(error);
        }
        return value;
    }

    // Garbell d'Eratòstenes: marca els compostos; els bits que queden a 0 són els primers.
    private static String primesUpTo(int n) {
        BitSet composite = new BitSet(n + 1);
        for (int i = 2; (long) i * i <= n; i++) {
            if (!composite.get(i)) {
                for (int multiple = i * i; multiple <= n; multiple += i) {
                    composite.set(multiple);
                }
            }
        }
        StringBuilder primes = new StringBuilder();
        for (int p = composite.nextClearBit(2); p <= n; p = composite.nextClearBit(p + 1)) {
            if (primes.length() > 0) {
                primes.append(' ');
            }
            primes.append(p);
        }
        return primes.toString();
    }
}
