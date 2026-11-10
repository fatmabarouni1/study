package tn.fatma.studyflowsimple;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TodoActivity extends Activity {
    private static final int ADD_TASK_REQUEST = 2;
    private final ArrayList<String> tasks = new ArrayList<>();
    private Set<String> completed;
    private ArrayAdapter<String> adapter;
    private ListView taskList;
    private TextView countText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        taskList = findViewById(R.id.taskList);
        countText = findViewById(R.id.countText);
        preferences = getSharedPreferences("StudyFlow", MODE_PRIVATE);

        // Les titres sont sauvegardes dans une chaine, un titre par ligne.
        String savedTasks = preferences.getString("tasks", "");
        if (!savedTasks.isEmpty()) tasks.addAll(Arrays.asList(savedTasks.split("\n")));
        completed = new HashSet<>(preferences.getStringSet("completed", new HashSet<>()));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, tasks);
        taskList.setAdapter(adapter);
        taskList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        taskList.setEmptyView(findViewById(R.id.emptyText));

        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            intent.putStringArrayListExtra("existingTasks", new ArrayList<>(tasks));
            startActivityForResult(intent, ADD_TASK_REQUEST);
        });
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            String title = tasks.get(position);
            if (taskList.isItemChecked(position)) completed.add(title);
            else completed.remove(title);
            saveTasks();
            showTasks();
        });
        taskList.setOnItemLongClickListener((parent, view, position, id) -> {
            String title = tasks.get(position);
            new AlertDialog.Builder(this).setTitle(R.string.delete_title).setMessage(title)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        tasks.remove(title);
                        completed.remove(title);
                        saveTasks();
                        showTasks();
                    }).show();
            return true;
        });
        showTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("taskTitle");
            if (title != null && !title.trim().isEmpty() && !tasks.contains(title)) {
                tasks.add(title);
                saveTasks();
                showTasks();
            }
        }
        // RESULT_CANCELED : aucune modification de la liste.
    }

    private void saveTasks() {
        preferences.edit().putString("tasks", TextUtils.join("\n", tasks))
                .putStringSet("completed", new HashSet<>(completed)).apply();
    }

    private void showTasks() {
        adapter.notifyDataSetChanged();
        taskList.clearChoices();
        for (int i = 0; i < tasks.size(); i++) {
            taskList.setItemChecked(i, completed.contains(tasks.get(i)));
        }
        countText.setText(getString(R.string.task_count, tasks.size(), completed.size()));
    }
}
