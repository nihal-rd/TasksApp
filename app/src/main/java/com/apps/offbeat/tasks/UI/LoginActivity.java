package com.apps.offbeat.tasks.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.apps.offbeat.tasks.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private static final int RC_SIGNIN = 1234;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();

    SignInButton signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = findViewById(R.id.sign_in);
        progressDialog = new ProgressDialog(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_login_key))
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    void signIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGNIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGNIN) {
            Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
                firebaseLoginWithGoogle(googleSignInAccount);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
            }

        }
    }

    void firebaseLoginWithGoogle(GoogleSignInAccount googleSignInAccount) {
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final DocumentReference personalTask = mFirebaseFirestore.collection("Tasks")
                                    .document("UserTasks")
                                    .collection(mFirebaseAuth.getCurrentUser().getUid())
                                    .document(getString(R.string.default_task));
                            personalTask.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (!task.getResult().exists()){
                                        Log.d(TAG, "onComplete: Writting to database");
                                        Map<String, Object> map = new HashMap<>();
                                        map.put(getString(R.string.task_title_key), getString(R.string.default_task));
                                        map.put(getString(R.string.exist_key), true);
                                       map.put(getString(R.string.task_completed_items_key), 0);
                                      map.put(getString(R.string.task_incomplete_items_key), 0);
                                        map.put(getString(R.string.task_color), Color.parseColor("#f44336"));

                                        DocumentReference personalTaskLogin = mFirebaseFirestore.collection("Tasks")
                                                .document("UserTasks")
                                                .collection(mFirebaseAuth.getCurrentUser().getUid())
                                                .document(getString(R.string.default_task));
                                        personalTaskLogin.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: TASK LIST CREATED");
                                                }
                                                else{
                                                    task.getException().printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    Log.d(TAG, "onComplete: LOGIN SUCCESS");
                                    updateUI();
                                }
                            });

                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    void updateUI() {
        progressDialog.dismiss();
        if (mFirebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }
}
