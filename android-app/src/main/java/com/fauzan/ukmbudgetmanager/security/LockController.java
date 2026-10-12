package com.fauzan.ukmbudgetmanager.security;

/** Decides which screen to show and runs the unlock prompt. No Android UI types, so it is unit-testable. */
public final class LockController {
    public enum Screen { OPEN, LOCKED, NEEDS_SCREEN_LOCK, UNAVAILABLE }
    public interface Result { void done(Screen screen, String message); }

    private final LockState state; private final Authenticator authenticator; private boolean prompting;
    public LockController(LockState state, Authenticator authenticator) { this.state = state; this.authenticator = authenticator; }

    /** Re-checks the device lock. If the user removed their screen lock the app locks again. */
    public Screen evaluate() {
        Authenticator.Availability availability = authenticator.availability();
        if (availability != Authenticator.Availability.READY) {
            state.lockNow();
            return availability == Authenticator.Availability.NO_SCREEN_LOCK ? Screen.NEEDS_SCREEN_LOCK : Screen.UNAVAILABLE;
        }
        return state.isUnlocked() ? Screen.OPEN : Screen.LOCKED;
    }
    public boolean isPrompting() { return prompting; }

    /** Shows the system prompt when locked. Does nothing if already open, blocked, or a prompt is showing. */
    public void unlock(Result result) {
        Screen screen = evaluate();
        if (screen != Screen.LOCKED) { result.done(screen, ""); return; }
        if (prompting) return;
        prompting = true;
        authenticator.authenticate(new Authenticator.Callback() {
            @Override public void onSuccess() { prompting = false; state.unlock(); result.done(Screen.OPEN, ""); }
            @Override public void onFailure(String message, boolean canceled) { prompting = false; state.lockNow(); result.done(Screen.LOCKED, canceled ? "" : message); }
        });
    }
}
