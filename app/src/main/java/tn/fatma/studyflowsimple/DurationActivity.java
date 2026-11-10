package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

public class DurationActivity extends Activity {
    private RadioGroup durationGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration);
        setResult(RESULT_CANCELED);
        durationGroup = findViewById(R.id.durationGroup);
        if (savedInstanceState == null) {
            int minutes = getIntent().getIntExtra("selectedMinutes", 25);
            if (minutes == 15) durationGroup.check(R.id.duration15);
            else if (minutes == 50) durationGroup.check(R.id.duration50);
            else durationGroup.check(R.id.duration25);
        }
        findViewById(R.id.validateDurationButton).setOnClickListener(v -> returnDuration());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void returnDuration() {
        int selected = durationGroup.getCheckedRadioButtonId();
        int minutes = 25;
        if (selected == R.id.duration15) minutes = 15;
        else if (selected == R.id.duration50) minutes = 50;
        Intent result = new Intent();
        result.putExtra("selectedMinutes", minutes);
        setResult(RESULT_OK, result);
        finish();
    }
}
