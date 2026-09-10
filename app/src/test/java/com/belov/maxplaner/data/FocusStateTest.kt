package com.belov.maxplaner.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusStateTest {
    private val clock = FocusClock(1_800_000_000_000L, 100_000L, 7)
    private fun later(millis: Long) = clock.copy(wallMillis = clock.wallMillis + millis, elapsedMillis = clock.elapsedMillis + millis)

    @Test fun openingScreenDoesNotStartSession() {
        val state = FocusState(totalMinutes = 75).refresh(later(10_000_000))
        assertEquals(FocusMode.Idle, state.session.mode)
        assertEquals(75, state.totalMinutes)
    }

    @Test fun duplicateStartDoesNotRestartCountdown() {
        val state = FocusState().start(clock)
        assertEquals(state, state.start(later(30_000)))
    }

    @Test fun pausePreservesFractionsAndResumeExcludesPausedTime() {
        val paused = FocusState().start(clock).pause(later(12_345))
        assertEquals(FOCUS_DURATION_MILLIS - 12_345, paused.session.remainingMillis(later(300_000)))
        val resumed = paused.start(later(300_000))
        assertEquals(FOCUS_DURATION_MILLIS - 22_345, resumed.session.remainingMillis(later(310_000)))
    }

    @Test fun delayedRefreshCreditsExactlyOneSession() {
        val completed = FocusState(totalMinutes = 50).start(clock).refresh(later(FOCUS_DURATION_MILLIS * 10))
        assertEquals(FocusMode.Completed, completed.session.mode)
        assertEquals(75, completed.totalMinutes)
        assertEquals(completed, completed.refresh(later(FOCUS_DURATION_MILLIS * 20)))
        // Reconstruct persisted state as after process recreation; no second award.
        val restored = FocusState(completed.session.copy(), completed.totalMinutes)
        assertEquals(75, restored.refresh(later(FOCUS_DURATION_MILLIS * 20)).totalMinutes)
    }

    @Test fun pauseAtDeadlineFinishesInsteadOfLosingCredit() {
        val state = FocusState().start(clock).pause(later(FOCUS_DURATION_MILLIS))
        assertEquals(FocusMode.Completed, state.session.mode)
        assertEquals(25, state.totalMinutes)
    }

    @Test fun nextSessionStartsImmediatelyAfterCompletion() {
        val atFinish = later(FOCUS_DURATION_MILLIS)
        val state = FocusState().start(clock).start(atFinish)
        assertEquals(25, state.totalMinutes)
        assertEquals(FocusMode.Running, state.session.mode)
        assertEquals(FOCUS_DURATION_MILLIS, state.session.remainingMillis(atFinish))
    }

    @Test fun changingWallClockDuringSameBootDoesNotChangeTimer() {
        val session = FocusState().start(clock).session
        val adjusted = later(60_000).copy(wallMillis = clock.wallMillis - 86_400_000)
        assertEquals(FOCUS_DURATION_MILLIS - 60_000, session.remainingMillis(adjusted))
    }

    @Test fun deviceRebootFallsBackToPersistedWallDeadline() {
        val restored = FocusState().start(clock).copy()
        val reboot = later(60_000).copy(elapsedMillis = 500L, bootId = 8)
        assertEquals(FOCUS_DURATION_MILLIS - 60_000, restored.session.remainingMillis(reboot))
        assertEquals(25, restored.refresh(reboot.copy(wallMillis = clock.wallMillis + FOCUS_DURATION_MILLIS)).totalMinutes)
        val reanchored = restored.refresh(reboot)
        assertEquals(FOCUS_DURATION_MILLIS - 60_000, reanchored.session.remainingMillis(reboot.copy(wallMillis = 0L)))
    }

    @Test fun processRecreationRetainsRunningDeadline() {
        val running = FocusState(totalMinutes = 100).start(clock)
        val restored = FocusState(running.session.copy(), running.totalMinutes)
        assertEquals(FOCUS_DURATION_MILLIS - 120_000, restored.session.remainingMillis(later(120_000)))
    }
}
