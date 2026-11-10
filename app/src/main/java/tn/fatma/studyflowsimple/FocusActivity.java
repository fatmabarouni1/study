package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Locale;

public class FocusActivity extends Activity {
    private static final int DURATION_REQUEST = 3;
    private EditText minutesInput;
    private TextView timerText, statusText;
    private Button startButton, pauseButton;
    private CountDownTimer timer;
    private long remaining = 25 * 60000L;
    private long endTime;
    private boolean running = false;
    private boolean started = false;
    private int sessionMinutes = 25;
    private long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        setResult(RESULT_CANCELED); // Retour avant la fin : aucune seance terminee.
        minutesInput = findViewById(R.id.minutesInput);
        timerText = findViewById(R.id.timerText);
        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);

        // Une rotation conserve la duree et etat du minuteur.
        if (savedInstanceState != null) {
            remaining = savedInstanceState.getLong("remaining");
            endTime = savedInstanceState.getLong("endTime");
            running = savedInstanceState.getBoolean("running");
            started = savedInstanceState.getBoolean("started");
            sessionMinutes = savedInstanceState.getInt("sessionMinutes", 25);
            sessionId = savedInstanceState.getLong("sessionId");
            minutesInput.setText(savedInstanceState.getString("minutes", "25"));
        }
        findViewById(R.id.chooseDurationButton).setOnClickListener(v -> {
            int currentMinutes;
            try { currentMinutes = Integer.parseInt(minutesInput.getText().toString()); }
            catch (NumberFormatException exception) { currentMinutes = 25; }
            Intent intent = new Intent(this, DurationActivity.class);
            intent.putExtra("selectedMinutes", currentMinutes);
            startActivityForResult(intent, DURATION_REQUEST);
        });
        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> pauseTimer());
        findViewById(R.id.resetButton).setOnClickListener(v -> resetTimer());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        updateScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DURATION_REQUEST && resultCode == RESULT_OK && data != null && !started) {
            int minutes = data.getIntExtra("selectedMinutes", 25);
            if (minutes == 15 || minutes == 25 || minutes == 50) {
                minutesInput.setText(String.valueOf(minutes));
                remaining = minutes * 60000L;
                updateScreen();
            }
        }
    }

    private void startTimer() {
        if (running) return;
        if (!started) {
            int minutes;
            try { minutes = Integer.parseInt(minutesInput.getText().toString()); }
            catch (NumberFormatException exception) { minutes = 0; }
            if (minutes < 1 || minutes > 180) {
                minutesInput.setError(getString(R.string.duration_error));
                return;
            }
            sessionMinutes = minutes;
            sessionId = System.currentTimeMillis();
            remaining = minutes * 60000L;
            started = true;
        }
        running = true;
        endTime = SystemClock.elapsedRealtime() + remaining;
        runCountdown();
    }

    private void runCountdown() {
        remaining = Math.max(0, endTime - SystemClock.elapsedRealtime());
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remaining = Math.max(0, endTime - SystemClock.elapsedRealtime());
                updateScreen();
            }
            @Override
            public void onFinish() {
                completeSession();
            }
        };
        timer.start();
        updateScreen();
    }

    private void pauseTimer() {
        if (!running) return;
        remaining = Math.max(0, endTime - SystemClock.elapsedRealtime());
        if (timer != null) timer.cancel();
        running = false;
        if (remaining == 0) completeSession();
        else updateScreen();
    }

    private void completeSession() {
        if (!started) return;
        remaining = 0;
        running = false;
        started = false;
        SharedPreferences preferences = getSharedPreferences("StudyFlow", MODE_PRIVATE);

        // Le meme identifiant ne doit pas etre compte deux fois apres une rotation.
        if (preferences.getLong("lastSessionId", -1) != sessionId) {
            int sessions = preferences.getInt("focusSessions", 0);
            int minutes = preferences.getInt("focusMinutes", 0);
            preferences.edit()
                    .putInt("focusSessions", sessions + 1)
                    .putInt("focusMinutes", minutes + sessionMinutes)
                    .putInt("lastFocusMinutes", sessionMinutes)
                    .putLong("lastSessionId", sessionId)
                    .apply();
        }
        updateScreen();
        // Renvoyer la duree terminee a MainActivity.
        Intent result = new Intent();
        result.putExtra("completedMinutes", sessionMinutes);
        setResult(RESULT_OK, result);
        finish(); // MainActivity recoit le resultat dans onActivityResult().
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        running = false;
        started = false;
        remaining = 25 * 60000L;
        minutesInput.setText("25");
        updateScreen();
    }

    private void updateScreen() {
        long seconds = (remaining + 999) / 1000;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
        minutesInput.setEnabled(!started);
        findViewById(R.id.chooseDurationButton).setEnabled(!started);
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        startButton.setText(started ? R.string.resume : R.string.start);
        int message = running ? R.string.timer_running : remaining == 0 ? R.string.timer_done
                : started ? R.string.timer_paused : R.string.timer_ready;
        statusText.setText(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (running) runCountdown();
    }

    @Override
    protected void onStop() {
        if (timer != null) timer.cancel();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("remaining", remaining);
        outState.putLong("endTime", endTime);
        outState.putBoolean("running", running);
        outState.putBoolean("started", started);
        outState.putInt("sessionMinutes", sessionMinutes);
        outState.putLong("sessionId", sessionId);
        outState.putString("minutes", minutesInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
