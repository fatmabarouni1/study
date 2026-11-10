package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import java.util.HashSet;

public class SummaryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        SharedPreferences preferences = getSharedPreferences("StudyFlow", MODE_PRIVATE);

        // Lire les trois informations sauvegardees a la fin des seances.
        int sessions = preferences.getInt("focusSessions", 0);
        int minutes = preferences.getInt("focusMinutes", 0);
        int lastMinutes = preferences.getInt("lastFocusMinutes", 0);
        int done = preferences.getStringSet("completed", new HashSet<>()).size();

        TextView sessionsText = findViewById(R.id.sessionsText);
        TextView minutesText = findViewById(R.id.minutesText);
        TextView lastSessionText = findViewById(R.id.lastSessionText);
        TextView completedText = findViewById(R.id.completedText);
        sessionsText.setText(getString(R.string.summary_sessions, sessions));
        minutesText.setText(getString(R.string.summary_minutes, minutes));
        completedText.setText(getString(R.string.summary_tasks, done));
        if (sessions == 0) lastSessionText.setText(R.string.summary_empty);
        else lastSessionText.setText(getString(R.string.summary_last, lastMinutes));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
