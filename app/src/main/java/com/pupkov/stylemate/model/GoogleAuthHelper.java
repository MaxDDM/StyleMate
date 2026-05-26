package com.pupkov.stylemate.model;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.annotation.NonNull;

import java.util.concurrent.Executors;

public class GoogleAuthHelper {

    private static final String WEB_CLIENT_ID = "855745772938-4ufgpdssmk6ohoe4b1jurl1e68b9vnfd.apps.googleusercontent.com";
    private final Activity activity;
    private final CredentialManager credentialManager;

    public GoogleAuthHelper(Activity activity) {
        this.activity = activity;
        this.credentialManager = CredentialManager.create(activity);
    }

    public void signIn(AuthCallback callback) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        activity.runOnUiThread(() -> callback.onError(e.getMessage()));
                    }
                }
        );
    }

    private void handleSignIn(GetCredentialResponse response, AuthCallback callback) {
        Credential credential = response.getCredential();

        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credential.getType())) {

            GoogleIdTokenCredential googleCredential =
                    GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

            String idToken = googleCredential.getIdToken();
            String email = googleCredential.getId();
            String displayName = googleCredential.getDisplayName();

            activity.runOnUiThread(() -> callback.onSuccess(idToken, email, displayName));
        } else {
            activity.runOnUiThread(() -> callback.onError("Неизвестный тип Credential"));
        }
    }

    public interface AuthCallback {
        void onSuccess(String idToken, String email, String displayName);
        void onError(String message);
    }

    public void signOut(SignOutCallback callback) {
        ClearCredentialStateRequest request = new ClearCredentialStateRequest();

        credentialManager.clearCredentialStateAsync(
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        activity.runOnUiThread(callback::onSuccess);
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        activity.runOnUiThread(() -> callback.onError(e.getMessage()));
                    }
                }
        );
    }

    public interface SignOutCallback {
        void onSuccess();
        void onError(String message);
    }
}
