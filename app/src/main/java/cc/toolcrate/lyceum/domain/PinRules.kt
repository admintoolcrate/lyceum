package cc.toolcrate.lyceum.domain

/** The PIN's shape, and how long to wait after wrong guesses. */
object PinRules {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 8

    fun isValid(pin: String) = pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }

    /**
     * How long to wait after [failures] wrong PINs in a row: nothing for the first [free], then 30 s,
     * doubling with each further miss, capped at an hour. (The same rule Stagira uses.)
     */
    fun backoffMillis(failures: Int, free: Int = 5): Long {
        val extra = failures - free
        if (extra <= 0) return 0
        return minOf(30_000L shl minOf(extra - 1, 7), 3_600_000L)
    }

    /** Milliseconds still to wait at [now], given the failure count and when the last one happened. */
    fun waitRemaining(failures: Int, lastFailureAt: Long, now: Long): Long =
        (lastFailureAt + backoffMillis(failures) - now).coerceAtLeast(0)
}
