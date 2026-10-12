package com.fauzan.ukmbudgetmanager.security;

/** Process-wide unlocked flag with a re-lock timeout once the app has been in the background. Pure Java. */
public final class LockState {
    public interface Clock { long now(); }
    public static final long DEFAULT_GRACE_MS = 60_000;
    /** Shared by every activity instance, so rotating the screen does not lock the app. */
    public static final LockState INSTANCE = new LockState(android.os.SystemClock::elapsedRealtime, DEFAULT_GRACE_MS);

    private final Clock clock; private final long graceMs;
    private final java.util.List<Runnable> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile boolean unlocked; private long backgroundedAt = -1;

    public LockState(Clock clock, long graceMs) { this.clock = clock; this.graceMs = graceMs; }
    public boolean isUnlocked() { return unlocked; }
    public void addListener(Runnable listener) { listeners.add(listener); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }

    public synchronized void unlock() { backgroundedAt = -1; set(true); }
    public synchronized void lockNow() { backgroundedAt = -1; set(false); }
    /** Call when the app leaves the foreground. */
    public synchronized void onBackground() { if (unlocked && backgroundedAt < 0) backgroundedAt = clock.now(); }
    /** Call when the app returns; locks if it was away longer than the grace period. */
    public synchronized void onForeground() {
        if (unlocked && backgroundedAt >= 0 && clock.now() - backgroundedAt > graceMs) set(false);
        backgroundedAt = -1;
    }
    private void set(boolean value) {
        boolean changed = unlocked != value; unlocked = value;
        if (changed) for (Runnable l : listeners) l.run();
    }
}
