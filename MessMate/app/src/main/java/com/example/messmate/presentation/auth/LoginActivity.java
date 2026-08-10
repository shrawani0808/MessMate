package com.example.messmate.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messmate.R;
import com.example.messmate.presentation.member.MemberMainActivity;
import com.example.messmate.presentation.owner.OwnerMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // -----------------------------
    // UI
    // -----------------------------

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    private MaterialButton btnLogin;

    private TextView txtRegister;

    // -----------------------------
    // Firebase
    // -----------------------------

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // -----------------------------
    // Session
    // -----------------------------

    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize SessionManager first
        sessionManager = new SessionManager(this);

        // Check whether user is already logged in
        if (sessionManager.isLoggedIn()) {

            String role = sessionManager.getRole();

            if (role != null) {

                role = role.trim().toLowerCase();

                // =========================
                // OWNER SESSION
                // =========================

                if (role.equals("owner")) {

                    openOwnerDashboard();
                    return;
                }

                // =========================
                // MEMBER SESSION
                // =========================

                if (role.equals("member") || role.equals("user")) {

                    openMemberDashboard();
                    return;
                }
            }

            // If session is invalid
            sessionManager.logout();
        }

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginmain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize views
        initializeViews();

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences manager
        sessionManager = new SessionManager(this);

        // Login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Register button/text
        txtRegister.setOnClickListener(v -> openRegisterPage());
    }


    private void openOwnerDashboard() {

        Intent intent = new Intent(
                LoginActivity.this,
                OwnerMainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private void openMemberDashboard() {

        Intent intent = new Intent(
                LoginActivity.this,
                MemberMainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        btnLogin = findViewById(R.id.btnLogin);

        txtRegister = findViewById(R.id.txtRegister);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    private void loginUser() {

        clearErrors();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString()
                        .trim();


        // -----------------------------
        // Validate email
        // -----------------------------

        if (TextUtils.isEmpty(email)) {

            emailLayout.setError("Enter your email");

            etEmail.requestFocus();

            return;
        }


        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailLayout.setError(
                    "Enter a valid email"
            );

            etEmail.requestFocus();

            return;
        }


        // -----------------------------
        // Validate password
        // -----------------------------

        if (TextUtils.isEmpty(password)) {

            passwordLayout.setError(
                    "Enter your password"
            );

            etPassword.requestFocus();

            return;
        }


        setLoading(true);


        // =====================================================
        // FIREBASE AUTHENTICATION
        // =====================================================

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )

                .addOnSuccessListener(authResult -> {

                    FirebaseUser currentUser =
                            firebaseAuth.getCurrentUser();


                    if (currentUser == null) {

                        setLoading(false);

                        showToast(
                                "Login failed"
                        );

                        return;
                    }


                    String uid =
                            currentUser.getUid();


                    // =================================================
                    // FIRST: CHECK COMMON USERS COLLECTION
                    // =================================================

                    firestore
                            .collection("users")
                            .document(uid)
                            .get()

                            .addOnSuccessListener(
                                    documentSnapshot -> {

                                        if (documentSnapshot.exists()) {

                                            String role =
                                                    documentSnapshot
                                                            .getString("role");

                                            if (role == null) {

                                                setLoading(false);

                                                showToast(
                                                        "User role not found"
                                                );

                                                return;
                                            }


                                            handleRole(
                                                    uid,
                                                    role
                                            );

                                        } else {

                                            // =================================================
                                            // OLD OWNER COLLECTION FALLBACK
                                            // =================================================

                                            checkOldOwnerCollection(
                                                    uid
                                            );
                                        }
                                    }
                            )

                            .addOnFailureListener(e -> {

                                setLoading(false);

                                showToast(
                                        "Firestore Error: "
                                                + e.getMessage()
                                );
                            });

                })

                .addOnFailureListener(e -> {

                    setLoading(false);

                    handleLoginError(e);
                });
    }


    // =========================================================
    // CHECK OLD OWNER COLLECTION
    // =========================================================

    private void checkOldOwnerCollection(String uid) {

        firestore
                .collection("owners")
                .document(uid)
                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String role =
                                documentSnapshot
                                        .getString("role");


                        if (role == null) {

                            // Old owner document exists but
                            // role field is missing.

                            role = "owner";
                        }


                        handleRole(
                                uid,
                                role
                        );

                    } else {

                        setLoading(false);

                        firebaseAuth.signOut();

                        showToast(
                                "User profile not found"
                        );
                    }
                })

                .addOnFailureListener(e -> {

                    setLoading(false);

                    showToast(
                            "Failed to load user profile: "
                                    + e.getMessage()
                    );
                });
    }


    // =========================================================
    // HANDLE ROLE
    // =========================================================

    private void handleRole(String uid, String role) {

        if (role == null) {

            setLoading(false);
            showToast("Invalid user role");
            return;
        }

        role = role.trim().toLowerCase();

        // Get email directly from Firebase
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            setLoading(false);
            showToast("User session not found");
            return;
        }

        String userEmail = user.getEmail();

        if (userEmail == null) {
            userEmail = "";
        }


        // =========================
        // OWNER
        // =========================

        if (role.equals("owner")) {

            sessionManager.saveLoginSession(
                    uid,
                    userEmail,
                    "owner"
            );

            setLoading(false);

            Intent intent = new Intent(
                    LoginActivity.this,
                    OwnerMainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        }


        // =========================
        // MEMBER
        // =========================

        else if (
                role.equals("member") ||
                        role.equals("user")) {

            sessionManager.saveLoginSession(
                    uid,
                    userEmail,
                    "member"
            );

            setLoading(false);

            Intent intent = new Intent(
                    LoginActivity.this,
                    MemberMainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        }


        // =========================
        // INVALID ROLE
        // =========================

        else {

            setLoading(false);

            firebaseAuth.signOut();

            showToast(
                    "Invalid user role: " + role
            );
        }
    }


    // =========================================================
    // OPEN REGISTRATION
    // =========================================================

    private void openRegisterPage() {

        Intent intent =
                new Intent(
                        LoginActivity.this,
                        RegisterActivity.class
                );

        startActivity(intent);
    }


    // =========================================================
    // LOADING
    // =========================================================

    private void setLoading(boolean loading) {

        if (loading) {

            btnLogin.setEnabled(false);

            btnLogin.setText(
                    "Signing in..."
            );

        } else {

            btnLogin.setEnabled(true);

            btnLogin.setText(
                    "Login"
            );
        }
    }


    // =========================================================
    // CLEAR ERRORS
    // =========================================================

    private void clearErrors() {

        emailLayout.setError(null);

        passwordLayout.setError(null);
    }


    // =========================================================
    // LOGIN ERROR
    // =========================================================

    private void handleLoginError(
            Exception e) {

        String message =
                e.getMessage();


        if (message == null) {

            showToast(
                    "Login failed"
            );

            return;
        }


        if (message.contains(
                "The password is invalid")) {

            passwordLayout.setError(
                    "Incorrect password"
            );

        } else if (message.contains(
                "no user record")) {

            emailLayout.setError(
                    "No account found with this email"
            );

        } else if (message.contains(
                "badly formatted")) {

            emailLayout.setError(
                    "Invalid email address"
            );

        } else if (message.contains(
                "network")) {

            showToast(
                    "Check your internet connection"
            );

        } else {

            showToast(
                    "Login failed: "
                            + message
            );
        }
    }


    // =========================================================
    // TOAST
    // =========================================================

    private void showToast(String message) {

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}