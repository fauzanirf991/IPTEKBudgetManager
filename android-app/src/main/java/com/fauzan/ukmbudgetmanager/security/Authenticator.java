package com.fauzan.ukmbudgetmanager.security;

/** Proves the person holding the phone knows its screen lock. Faked in unit tests. */
public interface Authenticator {
    enum Availability { READY, NO_SCREEN_LOCK, UNAVAILABLE }
    interface Callback {
        void onSuccess();
        /** @param canceled true when the user dismissed the prompt rather than failing it */
        void onFailure(String message, boolean canceled);
    }
    Availability availability();
    void authenticate(Callback callback);
}
