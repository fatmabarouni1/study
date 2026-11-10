package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
    private EditText emailInput, passwordInput;
    private TextView errorText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("StudyFlow", MODE_PRIVATE);

        // Une session deja ouverte permet de passer directement a accueil.
        if (preferences.getBoolean("connected", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.errorText);
        findViewById(R.id.loginButton).setOnClickListener(v -> login());
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Compte public de demonstration, pas une authentification de production.
        if (!email.equals("demo@studyflow.test") || !password.equals("123456")) {
            errorText.setText(R.string.login_error);
            return;
        }

        preferences.edit().putBoolean("connected", true).putString("email", email).apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
