package com.Quintet.myremindme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Quintet.myremindme.Model.TasksModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;


public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userID;

    private ProgressDialog loader;

    private String key = "";
    private String task;
    private String description;

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tasks");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("tasks").child(userID);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view -> addTask());
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.add_task,null);
        myDialog.setView(v);
        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = v.findViewById(R.id.task);
        final EditText desc = v.findViewById(R.id.description);
        Button save = v.findViewById(R.id.addSaveBtn);
        Button cancel = v.findViewById(R.id.addCancelBtn);

        cancel.setOnClickListener((view) -> dialog.dismiss());

        save.setOnClickListener(view -> {
            String mTask = task.getText().toString().trim();
            String mDesc = desc.getText().toString().trim();
            String id = reference.push().getKey();
            String date = DateFormat.getDateInstance().format(new Date());

            if(TextUtils.isEmpty(mTask)){
                task.setError("Task Required");
                return;
            }
            if(TextUtils.isEmpty(mDesc)){
                desc.setError("Description Required");
                return;
            }

            loader.setMessage("Saving your task");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            TasksModel model = new TasksModel(mTask, mDesc, id, date);
            reference.child(id).setValue(model).addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Your task has been added successfully", Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }else{
                    String error = task1.getException().toString();
                    Toast.makeText(HomeActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
            });

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "Before firebase recycler");

        FirebaseRecyclerOptions<TasksModel> options = new FirebaseRecyclerOptions.Builder<TasksModel>()
                .setQuery(reference, TasksModel.class)
                .build();

        Log.i(TAG, "After firebase recycler");

        FirebaseRecyclerAdapter<TasksModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<TasksModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull TasksModel model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDesc(model.getDescription());
                Log.i("Adapter", "onbindviewholder");
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Log.i("Adapter", "oncreateviewholder");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_task, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View view;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTask(String task){
            TextView viewTask = view.findViewById(R.id.viewTask);
            viewTask.setText(task);
        }

        public void setDesc(String desc){
            TextView viewDesc = view.findViewById(R.id.viewDescription);
            viewDesc.setText(desc);
        }

        public void setDate(String date){
            TextView viewData = view.findViewById(R.id.viewDate);
        }
    }
}