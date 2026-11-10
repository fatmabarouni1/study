package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Ce code identifie la demande envoyee a FocusActivity.
    private static final int FOCUS_REQUEST = 1;
    private TextView focusResultText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("StudyFlow", MODE_PRIVATE);
        if (!preferences.getBoolean("connected", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        focusResultText = findViewById(R.id.focusResultText);
        if (savedInstanceState != null) {
            focusResultText.setText(savedInstanceState.getString("focusResult", getString(R.string.no_focus_result)));
        }
        TextView welcomeText = findViewById(R.id.welcomeText);
        String email = getIntent().getStringExtra("email");
        if (email == null) email = preferences.getString("email", "");
        welcomeText.setText(getString(R.string.welcome, email));

        // Un Intent explicite ouvre chaque ecran.
        findViewById(R.id.todoButton).setOnClickListener(v ->
                startActivity(new Intent(this, TodoActivity.class)));
        findViewById(R.id.focusButton).setOnClickListener(v ->
                startActivityForResult(new Intent(this, FocusActivity.class), FOCUS_REQUEST));
        findViewById(R.id.summaryButton).setOnClickListener(v ->
                startActivity(new Intent(this, SummaryActivity.class)));
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            preferences.edit().putBoolean("connected", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }


    // Appelee quand FocusActivity se ferme et renvoie son resultat.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FOCUS_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                int minutes = data.getIntExtra("completedMinutes", 0);
                focusResultText.setText(getString(R.string.focus_result, minutes));
                startActivity(new Intent(this, SummaryActivity.class));
            } else {
                focusResultText.setText(R.string.focus_canceled);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (focusResultText != null) {
            outState.putString("focusResult", focusResultText.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
}
