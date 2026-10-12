package com.fauzan.ukmbudgetmanager.security;

import android.app.KeyguardManager;
import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/**
 * Device screen-lock authentication (biometric or PIN/pattern/password).
 * BIOMETRIC_WEAK | DEVICE_CREDENTIAL is used because DEVICE_CREDENTIAL alone, and BIOMETRIC_STRONG | DEVICE_CREDENTIAL,
 * are unsupported on API 28-29 while minSdk is 26. Construct in {@code onCreate}.
 */
public final class ScreenLock implements Authenticator {
    private static final int ALLOWED = BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
    private final Context context; private final BiometricPrompt prompt; private final BiometricPrompt.PromptInfo info;
    private Callback pending;

    public ScreenLock(FragmentActivity activity) {
        context = activity.getApplicationContext();
        prompt = new BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) { finish(null, false); }
            @Override public void onAuthenticationError(int code, CharSequence message) {
                boolean canceled = code == BiometricPrompt.ERROR_USER_CANCELED || code == BiometricPrompt.ERROR_CANCELED || code == BiometricPrompt.ERROR_NEGATIVE_BUTTON;
                finish(message.toString(), canceled);
            }
        });
        info = new BiometricPrompt.PromptInfo.Builder().setTitle("Buka IPTEK Budget")
                .setSubtitle("Gunakan kunci layar perangkat Anda").setAllowedAuthenticators(ALLOWED).setConfirmationRequired(false).build();
    }
    private void finish(String error, boolean canceled) {
        Callback callback = pending; pending = null;
        if (callback == null) return;
        if (error == null) callback.onSuccess(); else callback.onFailure(error, canceled);
    }
    @Override public Availability availability() {
        KeyguardManager keyguard = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguard == null || !keyguard.isDeviceSecure()) return Availability.NO_SCREEN_LOCK;
        return BiometricManager.from(context).canAuthenticate(ALLOWED) == BiometricManager.BIOMETRIC_SUCCESS ? Availability.READY : Availability.UNAVAILABLE;
    }
    @Override public void authenticate(Callback callback) { pending = callback; prompt.authenticate(info); }
}
