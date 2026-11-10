package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import java.util.ArrayList;

public class AddTaskActivity extends Activity {
    private EditText taskInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        setResult(RESULT_CANCELED);
        taskInput = findViewById(R.id.taskInput);
        findViewById(R.id.validateTaskButton).setOnClickListener(v -> returnTask());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void returnTask() {
        String title = taskInput.getText().toString().trim().replace('\n', ' ');
        if (title.isEmpty()) {
            taskInput.setError(getString(R.string.task_error));
            return;
        }
        ArrayList<String> existingTasks = getIntent().getStringArrayListExtra("existingTasks");
        if (existingTasks != null && existingTasks.contains(title)) {
            taskInput.setError(getString(R.string.task_duplicate));
            return;
        }
        // On renvoie le titre. TodoActivity se charge de la sauvegarde.
        Intent result = new Intent();
        result.putExtra("taskTitle", title);
        setResult(RESULT_OK, result);
        finish();
    }
}
