package com.Quintet.myremindme;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Quintet.myremindme.Model.TasksModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


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
    private String id;
    private String task;
    private String description;
    private String date;
    private String time;

    private EditText addTask;
    private EditText addDesc;
    private EditText addDate;
    private EditText addTime;

    private EditText uTask;
    private EditText uDesc;
    private EditText uDate;
    private EditText uTime;

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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });
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
                holder.setTime(model.getTime());

                Log.i("Adapter", "onbindviewholder");

                holder.view.setOnClickListener(v -> {
                    key = getRef(position).getKey();
                    task = model.getTask();
                    description = model.getDescription();
                    date = model.getDate();
                    time = model.getTime();

                    updateTask();
                });
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
            TextView viewDesc = view.findViewById(R.id.viewDesc);
            viewDesc.setText(desc);
        }

        public void setDate(String date){
            TextView viewDate = view.findViewById(R.id.viewDate);
            viewDate.setText(date);
        }

        public void setTime(String time){
            TextView viewTime = view.findViewById(R.id.viewTime);
            viewTime.setText(time);
        }
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.add_task,null);
        myDialog.setView(v);
        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        addTask = v.findViewById(R.id.addTask);
        addDesc = v.findViewById(R.id.addDesc);
        addDate = v.findViewById(R.id.addDate);
        addTime = v.findViewById(R.id.addTime);

        Button save = v.findViewById(R.id.addSaveBtn);
        Button cancel = v.findViewById(R.id.addCancelBtn);

        addDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                setDate(addDate);
            }
        });

        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(addTime);
            }
        });

        cancel.setOnClickListener((view) -> dialog.dismiss());

        save.setOnClickListener(view -> {
            task = addTask.getText().toString().trim();
            description = addDesc.getText().toString().trim();
            id = reference.push().getKey();
            date = addDate.getText().toString();
            time = addTime.getText().toString();

            if(TextUtils.isEmpty(task)){
                addTask.setError("Task Required");
                return;
            }
            if(TextUtils.isEmpty(description)){
                addDesc.setError("Description Required");
                return;
            }

            loader.setMessage("Saving your task");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            TasksModel model = new TasksModel(task, description, id, date,time);
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

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.update_task, null);
        myDialog.setView(v);

        AlertDialog dialog = myDialog.create();

        uTask = v.findViewById(R.id.updateTask);
        uDesc = v.findViewById(R.id.updateDesc);

        uTask.setText(task);
        uTask.setSelection(task.length());

        uDesc.setText(description);
        uDesc.setSelection(description.length());

        Button delBtn = v.findViewById(R.id.updateDeleteBtn);
        Button updBtn = v.findViewById(R.id.updateBtn);

        uDate = v.findViewById(R.id.updateDate);
        uDate.setText(date);
        uTime = v.findViewById(R.id.updateTime);
        uTime.setText(time);

        uDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(uDate);
            }
        });

        uTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(uTime);
            }
        });

        updBtn.setOnClickListener(view -> {
            task = uTask.getText().toString().trim();
            description = uDesc.getText().toString().trim();
            date = uDate.getText().toString().trim();
            time = uTime.getText().toString().trim();

            TasksModel model = new TasksModel(task, description, key, date,time);
            reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(HomeActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    }else{
                        String err = task.getException().toString();
                        Toast.makeText(HomeActivity.this, "Update failed: "+err, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.dismiss();
        });

        delBtn.setOnClickListener(view ->{
            reference.child(key).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(HomeActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                }else{
                    String err = task.getException().toString();
                    Toast.makeText(HomeActivity.this, "Deletion failed: "+err, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        dialog.show();

    }

    private void setDate(EditText v){
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDayOfMonth = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        if(v==uDate){
            String[] val = uDate.getText().toString().split("/");
            mDayOfMonth = Integer.parseInt(val[0]);
            mMonth = Integer.parseInt(val[1]);
            mYear = Integer.parseInt(val[2]);
        }
        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(HomeActivity.this, (datePicker, year, month, dayOfMonth) -> {
            mcurrentDate.set(Calendar.YEAR, year);
            mcurrentDate.set(Calendar.MONTH, month);
            mcurrentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            v.setText( new StringBuilder().append(dayOfMonth).append("/")
                    .append(month+1).append("/").append(year));
        }, mYear, mMonth, mDayOfMonth);
        mDatePicker.show();
    }

    private void setTime(EditText v){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        if(v==uTime){
            String[] val = uTime.getText().toString().split(":");
            hour = Integer.parseInt(val[0]);
            minute = Integer.parseInt(val[1]);
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(HomeActivity.this, (timePicker, selectedHour, selectedMinute)
                -> v.setText( selectedHour + ":" + selectedMinute), hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}