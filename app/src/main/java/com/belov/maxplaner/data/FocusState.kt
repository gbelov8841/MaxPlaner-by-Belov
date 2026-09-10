package com.belov.maxplaner.data

const val FOCUS_DURATION_MILLIS = 25 * 60 * 1000L

enum class FocusMode { Idle, Running, Paused, Completed }

/** Elapsed time survives process death; boot ID detects when to use the saved wall deadline. */
data class FocusClock(val wallMillis: Long, val elapsedMillis: Long, val bootId: Int)

data class FocusSession(
    val mode: FocusMode = FocusMode.Idle,
    val pausedMillis: Long = FOCUS_DURATION_MILLIS,
    val deadlineWallMillis: Long = 0L,
    val deadlineElapsedMillis: Long = 0L,
    val bootId: Int = -1
) {
    fun remainingMillis(clock: FocusClock): Long = when (mode) {
        FocusMode.Idle -> FOCUS_DURATION_MILLIS
        FocusMode.Completed -> 0L
        FocusMode.Paused -> pausedMillis
        FocusMode.Running -> if (bootId >= 0 && bootId == clock.bootId) {
            deadlineElapsedMillis - clock.elapsedMillis
        } else {
            deadlineWallMillis - clock.wallMillis
        }
    }.coerceIn(0L, FOCUS_DURATION_MILLIS)

    fun refresh(clock: FocusClock): FocusSession {
        if (mode != FocusMode.Running) return this
        val remaining = remainingMillis(clock)
        if (remaining == 0L) return copy(mode = FocusMode.Completed)
        // After reboot, anchor the recovered remainder to the new monotonic clock.
        return if (clock.bootId >= 0 && bootId != clock.bootId) copy(
            deadlineElapsedMillis = clock.elapsedMillis + remaining,
            deadlineWallMillis = clock.wallMillis + remaining,
            bootId = clock.bootId
        ) else this
    }

    fun start(clock: FocusClock): FocusSession {
        if (mode == FocusMode.Running) return this
        val remaining = if (mode == FocusMode.Paused) pausedMillis else FOCUS_DURATION_MILLIS
        return copy(
            mode = FocusMode.Running,
            deadlineWallMillis = clock.wallMillis + remaining,
            deadlineElapsedMillis = clock.elapsedMillis + remaining,
            bootId = clock.bootId
        )
    }

    fun pause(clock: FocusClock): FocusSession {
        val current = refresh(clock)
        return if (current.mode == FocusMode.Running) current.copy(mode = FocusMode.Paused, pausedMillis = current.remainingMillis(clock)) else current
    }
}

/** Session and credited total form one persisted transaction, preventing repeat awards. */
data class FocusState(val session: FocusSession = FocusSession(), val totalMinutes: Int = 0) {
    fun refresh(clock: FocusClock): FocusState {
        val next = session.refresh(clock)
        val award = if (session.mode == FocusMode.Running && next.mode == FocusMode.Completed) 25 else 0
        return copy(session = next, totalMinutes = totalMinutes + award)
    }

    fun start(clock: FocusClock): FocusState {
        val current = refresh(clock)
        return current.copy(session = current.session.start(clock))
    }

    fun pause(clock: FocusClock): FocusState {
        val current = refresh(clock)
        return current.copy(session = current.session.pause(clock))
    }
}
