package com.example.messmate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmate.R;
import com.example.messmate.admin.ui.AdminMainActivity;
import com.example.messmate.common.database.DatabaseHelper;
import com.example.messmate.common.utils.SessionManager;
import com.example.messmate.owner.ui.OwnerMainActivity;
import com.example.messmate.user.ui.UserMainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        // Auto-login check
        if (session.isLoggedIn()) {
            navigateToDashboard(session.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);
        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (dbHelper.checkUser(email, password)) {
                String role = dbHelper.getUserRole(email);
                session.createSession(email, role);
                navigateToDashboard(role);
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Change RegisterActivity if your class name differs
            startActivity(intent);
        });

    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role.toUpperCase()) {
            case "ADMIN":
                intent = new Intent(this, AdminMainActivity.class);
                break;
            case "OWNER":
                intent = new Intent(this, OwnerMainActivity.class);
                break;
            case "USER":
            default:
                intent = new Intent(this, UserMainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}