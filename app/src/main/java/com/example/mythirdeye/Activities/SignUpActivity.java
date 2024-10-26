package com.example.mythirdeye.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.thethirdeye.R;
import com.example.thethirdeye.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 1000;
    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.apiKey)) // Use your actual API key
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailETSignUp.getText().toString();
                String password = binding.passwordETSignUp.getText().toString();
                if (email.isEmpty()) {
                    binding.emailETSignUp.setError("Enter your email");
                    return; // Early return if email is invalid
                }

                if (password.isEmpty()) {
                    binding.passwordETSignUp.setError("Enter your password");
                    return; // Early return if password is invalid
                }

                binding.progressCircular.setVisibility(View.VISIBLE);
                finishSignUp(email, password);
            }
        });
        binding.signInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
            }
        });

    }

    private void finishSignUp(String email, String password) {
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

            @Override
            public void onSuccess(AuthResult authResult) {
                //
                binding.progressCircular.setVisibility(View.INVISIBLE);
                goToMainPage();

            }
        })
                .addOnFailureListener(e->{
                    binding.progressCircular.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Sign out failed: "+e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void goToMainPage() {
        startActivity(new Intent(SignUpActivity.this,MainActivity.class));
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Handle sign-in failure
                Log.w("SignInActivity", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);
                        goToMainPage();

                    } else {
                        Log.w("SignInActivity", "Sign in with Google credential failed", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();
            // Navigate to the main app screen or show user info in the UI
        } else {
            // Show sign-in option or handle failure
        }
    }

    private void signOut(FirebaseUser user) {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser()!=null)
        {
            updateUI(auth.getCurrentUser());
            goToMainPage();
        }
    }

}