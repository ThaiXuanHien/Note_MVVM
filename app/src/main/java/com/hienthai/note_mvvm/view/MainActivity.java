package com.hienthai.note_mvvm.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hienthai.note_mvvm.R;
import com.hienthai.note_mvvm.adapter.NoteAdapter;
import com.hienthai.note_mvvm.model.entity.Note;
import com.hienthai.note_mvvm.viewmodel.NoteViewModel;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_NOTE = 1;
    private static final int REQUEST_EDIT_NOTE = 2;
    private NoteViewModel noteViewModel;
    private RecyclerView rcvNotes;
    private NoteAdapter noteAdapter;
    private FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        anhXa();

        noteAdapter = new NoteAdapter();
        rcvNotes.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.submitList(notes);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note Deleted!", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(rcvNotes);

        noteAdapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(MainActivity.this, SaveNoteActivity.class);
            intent.putExtra(SaveNoteActivity.EXTRA_ID, note.getId());
            intent.putExtra(SaveNoteActivity.EXTRA_TITLE, note.getTitle());
            intent.putExtra(SaveNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
            intent.putExtra(SaveNoteActivity.EXTRA_PRIORITY, note.getPriority());
            startActivityForResult(intent, REQUEST_EDIT_NOTE);
        });

        button_add.setOnClickListener(v -> {
            Intent intent = new Intent(this, SaveNoteActivity.class);
            startActivityForResult(intent, REQUEST_ADD_NOTE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_NOTE && resultCode == RESULT_OK) {
            String title = data.getStringExtra(SaveNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(SaveNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(SaveNoteActivity.EXTRA_PRIORITY, 1);
            Note note = new Note(title, description, priority);
            noteViewModel.insert(note);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_EDIT_NOTE && resultCode == RESULT_OK) {
            int id = data.getIntExtra(SaveNoteActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = data.getStringExtra(SaveNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(SaveNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(SaveNoteActivity.EXTRA_PRIORITY, 1);
            Note note = new Note(title, description, priority);
            note.setId(id);
            noteViewModel.update(note);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
        }

    }

    private void anhXa() {
        rcvNotes = findViewById(R.id.rcvNotes);
        button_add = findViewById(R.id.button_add);
        rcvNotes.setLayoutManager(new LinearLayoutManager(this));
        rcvNotes.setHasFixedSize(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                noteViewModel.deleteAllNotes();
                Toast.makeText(this, "All notes deleted", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}