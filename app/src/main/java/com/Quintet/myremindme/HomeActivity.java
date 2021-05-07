package com.Quintet.myremindme;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


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
    private EditText date;
    private EditText time;

    private static final String TAG = "HomeActivity";
    private Calendar myCalendar;

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
        final EditText date = v.findViewById(R.id.adddate);
        final EditText time = v.findViewById(R.id.addtime);


        Button save = v.findViewById(R.id.addSaveBtn);
        Button cancel = v.findViewById(R.id.addCancelBtn);

        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener datePicker = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };


        date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(HomeActivity.this, datePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(HomeActivity.this, (timePicker, selectedHour, selectedMinute)
                        -> time.setText( selectedHour + ":" + selectedMinute), hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        cancel.setOnClickListener((view) -> dialog.dismiss());

        save.setOnClickListener(view -> {
            String mTask = task.getText().toString().trim();
            String mDesc = desc.getText().toString().trim();
            String id = reference.push().getKey();
            String mDate = date.getText().toString().trim();
            String mTime = time.getText().toString().trim();

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

            TasksModel model = new TasksModel(mTask, mDesc, id, mDate,mTime);
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
    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date.setText(sdf.format(myCalendar.getTime()));
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

                holder.view.setOnClickListener(v -> {
                    key = getRef(position).getKey();
                    task = model.getTask();
                    description = model.getDescription();

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

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.update_task, null);
        myDialog.setView(v);

        AlertDialog dialog = myDialog.create();

        EditText uTask = v.findViewById(R.id.updateTask);
        EditText uDesc = v.findViewById(R.id.updateDescription);

        uTask.setText(task);
        uTask.setSelection(task.length());

        uDesc.setText(description);
        uDesc.setSelection(description.length());

        Button delBtn = v.findViewById(R.id.deleteBtn);
        Button updBtn = v.findViewById(R.id.updateBtn);

        updBtn.setOnClickListener(view -> {
            task = uTask.getText().toString().trim();
            description = uDesc.getText().toString().trim();

            String date = DateFormat.getDateInstance().format(new Date());

            TasksModel model = new TasksModel(task, description, key, date,null);
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