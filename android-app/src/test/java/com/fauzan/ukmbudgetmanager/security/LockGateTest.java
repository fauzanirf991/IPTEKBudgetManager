package com.fauzan.ukmbudgetmanager.security;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LockGateTest {
    private long now; private LockState state; private FakeAuthenticator auth; private LockController controller;
    private LockController.Screen lastScreen; private String lastMessage;

    private static final class FakeAuthenticator implements Authenticator {
        Availability availability = Availability.READY; Callback pending; int prompts;
        @Override public Availability availability() { return availability; }
        @Override public void authenticate(Callback callback) { prompts++; pending = callback; }
    }
    @Before public void setup() {
        now = 0; state = new LockState(() -> now, 60_000); auth = new FakeAuthenticator(); controller = new LockController(state, auth);
    }
    private void unlock() { controller.unlock((screen, message) -> { lastScreen = screen; lastMessage = message; }); }

    @Test public void startsLockedAndOpensOnlyAfterSuccessfulPrompt() {
        assertEquals(LockController.Screen.LOCKED, controller.evaluate());
        unlock(); assertEquals(1, auth.prompts); assertFalse(state.isUnlocked());
        auth.pending.onSuccess();
        assertTrue(state.isUnlocked()); assertEquals(LockController.Screen.OPEN, lastScreen); assertEquals(LockController.Screen.OPEN, controller.evaluate());
    }
    @Test public void cancelledPromptStaysLockedWithoutErrorText() {
        unlock(); auth.pending.onFailure("Dibatalkan", true);
        assertFalse(state.isUnlocked()); assertEquals(LockController.Screen.LOCKED, lastScreen); assertEquals("", lastMessage);
    }
    @Test public void failedPromptShowsMessageAndAllowsRetry() {
        unlock(); auth.pending.onFailure("Terlalu banyak percobaan", false);
        assertEquals("Terlalu banyak percobaan", lastMessage); unlock(); assertEquals(2, auth.prompts);
    }
    @Test public void secondPromptIsIgnoredWhileOneIsShowing() { unlock(); unlock(); assertEquals(1, auth.prompts); }
    @Test public void deviceWithoutScreenLockIsBlockedAndNeverPrompts() {
        auth.availability = Authenticator.Availability.NO_SCREEN_LOCK;
        assertEquals(LockController.Screen.NEEDS_SCREEN_LOCK, controller.evaluate());
        unlock(); assertEquals(0, auth.prompts); assertEquals(LockController.Screen.NEEDS_SCREEN_LOCK, lastScreen); assertFalse(state.isUnlocked());
    }
    @Test public void removingTheScreenLockWhileOpenLocksTheApp() {
        unlock(); auth.pending.onSuccess(); assertTrue(state.isUnlocked());
        auth.availability = Authenticator.Availability.NO_SCREEN_LOCK;
        assertEquals(LockController.Screen.NEEDS_SCREEN_LOCK, controller.evaluate()); assertFalse(state.isUnlocked());
    }
    @Test public void unavailableAuthenticatorBlocksAccess() {
        auth.availability = Authenticator.Availability.UNAVAILABLE;
        assertEquals(LockController.Screen.UNAVAILABLE, controller.evaluate()); assertFalse(state.isUnlocked());
    }
    @Test public void relocksOnlyAfterGracePeriodInBackground() {
        state.unlock();
        state.onBackground(); now = 59_000; state.onForeground(); assertTrue(state.isUnlocked());
        state.onBackground(); now = 59_000 + 60_001; state.onForeground(); assertFalse(state.isUnlocked());
    }
    @Test public void lockNowAndListenersFireOnlyOnChange() {
        AtomicInteger changes = new AtomicInteger(); state.addListener(changes::incrementAndGet);
        state.unlock(); state.unlock(); assertEquals(1, changes.get());
        state.lockNow(); state.lockNow(); assertEquals(2, changes.get()); assertFalse(state.isUnlocked());
    }
    @Test public void backgroundWhileLockedDoesNothing() { state.onBackground(); now = 1_000_000; state.onForeground(); assertFalse(state.isUnlocked()); }
}
