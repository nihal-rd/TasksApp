package com.apps.offbeat.tasks.UI;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.apps.offbeat.tasks.Data.List;
import com.apps.offbeat.tasks.R;
import com.apps.offbeat.tasks.Data.Todo;
import com.apps.offbeat.tasks.TodoAdapter;
import com.apps.offbeat.tasks.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

public class TaskDetail extends AppCompatActivity {

    private static final String TAG = TaskDetail.class.getSimpleName();

    RecyclerView mIncompleteTasksRecyclerView;
    RecyclerView mCompleteTasksRecyclerView;
    TodoAdapter  mCompletedTaskTodoAdapter;
    TodoAdapter mTodoAdapter;
    ArrayList<Todo> mTodoArrayList = new ArrayList<>();
    ArrayList<Todo> mCompletedTaskTodoArrayList= new ArrayList<>();
   public int mCompleteTasksCount = 0, mIncompleteTasksCount = 0;
    Utils mUtils;
    String mTaskTitle;
    FloatingActionButton mFloatingActionButton;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLightStatusBar(this, getWindow().getDecorView(), this);
        mUtils.setTypeFace(this);
        setContentView(R.layout.activity_task_detail);
        mTodoArrayList.clear();
        mIncompleteTasksRecyclerView = findViewById(R.id.rv_task_detail);
        mIncompleteTasksRecyclerView.setLayoutManager(new LinearLayoutManager(TaskDetail.this, LinearLayoutManager.VERTICAL, false));
        mCompleteTasksRecyclerView = findViewById(R.id.rv_completed_task);
        mCompleteTasksRecyclerView.setLayoutManager(new LinearLayoutManager(TaskDetail.this, LinearLayoutManager.VERTICAL, false));
        if (getIntent().hasExtra(getString(R.string.task_title_key))){
            mTaskTitle = getIntent().getStringExtra(getString(R.string.task_title_key));
            mCompleteTasksCount = getIntent().getIntExtra(getString(R.string.task_completed_items_key),0);
            mIncompleteTasksCount = getIntent().getIntExtra(getString(R.string.task_incomplete_items_key),0);
            mTodoAdapter = new TodoAdapter(this, mTodoArrayList,mTaskTitle);
            mCompletedTaskTodoAdapter =new TodoAdapter(this,mCompletedTaskTodoArrayList,mTaskTitle);
            mIncompleteTasksRecyclerView.setAdapter(mTodoAdapter);
            mCompleteTasksRecyclerView.setAdapter(mCompletedTaskTodoAdapter);

            getTasks(mTaskTitle);
        } else{
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        mFloatingActionButton = findViewById(R.id.fab_detail);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater layoutInflater = (LayoutInflater) TaskDetail.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View taskView = layoutInflater.inflate(R.layout.add_task_detail_layout, null, false);
                final AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetail.this);
                builder.setTitle(getString(R.string.new_task));
                View rootView = taskView.getRootView();
                final EditText titleText = rootView.findViewById(R.id.et_task_detail_title);
                final EditText dateText = rootView.findViewById(R.id.et_task_detail_date);
                final EditText descText = rootView.findViewById(R.id.et_task_detail_desc);
                final ImageView datePicker=rootView.findViewById(R.id.iv_date_picker);
                datePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater mLayoutInflater=(LayoutInflater) TaskDetail.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View datePicker=layoutInflater.inflate(R.layout.date_picker_layout,null,false);
                        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TaskDetail.this);
                        mBuilder.setTitle("Select Date");
                        View mRootView= datePicker.getRootView();
                        final DatePicker mDatePicker= mRootView.findViewById(R.id.datePickerExample);

                        Calendar currCalendar = Calendar.getInstance();
                        // Set the timezone which you want to display time.
                        currCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));

                      int   year = currCalendar.get(Calendar.YEAR);
                        int month = currCalendar.get(Calendar.MONTH);
                        int day = currCalendar.get(Calendar.DAY_OF_MONTH);

                        mDatePicker.init(year - 1, month + 1, day + 5, new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                StringBuffer strBuffer = new StringBuffer();
                                strBuffer.append(year);
                                strBuffer.append(monthOfYear);
                                strBuffer.append(dayOfMonth);

                              dateText.setText(strBuffer.toString());


                            }
                        });
                        mBuilder.setCancelable(true);

                        mBuilder.setView(datePicker);
                        mBuilder.create();
                        mBuilder.show();

                    }
                });
                builder.setCancelable(true);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final DocumentReference taskDocumentReference = mFirebaseFirestore.collection("Tasks")
                                .document("UserTasks")
                                .collection(mFirebaseAuth.getCurrentUser().getUid())
                                .document(mTaskTitle).collection("Tasks").document();
                        DocumentReference taskListDocumentReference = mFirebaseFirestore.collection("Tasks")
                                .document("UserTasks")
                                .collection(mFirebaseAuth.getCurrentUser().getUid())
                                .document(mTaskTitle);
                        Map<String,Object> map= new HashMap<>();
                        map.put("id", taskDocumentReference.getId());
                        map.put(getString(R.string.task_title_key),titleText.getText().toString());
                            map.put(getString(R.string.task_description_items_key), descText.getText().toString());
                            map.put(getString(R.string.task_due_date_key), dateText.getText().toString());
                        map.put(getString(R.string.task_is_complete_items_key),false);
                        map.put("timestamp", Timestamp.now());
                        mIncompleteTasksCount+=1;
                        taskDocumentReference.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TaskDetail.this, "Success", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(TaskDetail.this, "Failed", Toast.LENGTH_SHORT).show();
                                    task.getException().printStackTrace();
                                }
                            }
                        });
                        taskListDocumentReference.update(getString(R.string.task_incomplete_items_key), mIncompleteTasksCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(TaskDetail.this, "Success", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(TaskDetail.this, "Failed", Toast.LENGTH_SHORT).show();
                                    task.getException().printStackTrace();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setView(taskView);
                builder.create();
                builder.show();

            }
        });
    }

    void getTasks(String title){
        mTodoArrayList.clear();

        CollectionReference tasksCollectionReference = mFirebaseFirestore.collection("Tasks")
                .document("UserTasks")
                .collection(mFirebaseAuth.getCurrentUser().getUid())
                .document(title).collection("Tasks");


        tasksCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    e.printStackTrace();
                    Toast.makeText(TaskDetail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else{
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                        if (documentChange.getType().equals(DocumentChange.Type.ADDED)){
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Todo todo = documentSnapshot.toObject(Todo.class);
                           if(todo.task_complete == false){
                            mTodoArrayList.add(todo);
                            mTodoAdapter.notifyItemInserted(mTodoArrayList.size()-1);}
                            else{
                               mCompletedTaskTodoArrayList.add(todo);
                               mCompletedTaskTodoAdapter.notifyItemInserted(mCompletedTaskTodoArrayList.size()-1);
                           }
                        } else if (documentChange.getType().equals(DocumentChange.Type.MODIFIED)){
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Todo todo = documentSnapshot.toObject(Todo.class);
                            if (todo.task_complete == false) {
                              /*  String id = todo.id;
                                int index = 0;
                                for (int i = 0; i < mTodoArrayList.size(); i++) {
                                    if ((mTodoArrayList.get(i) != null) && id.equals(mTodoArrayList.get(i).id)) {
                                        index = i;
                                        break;
                                    }
                                }
                                mTodoArrayList.set(index, todo);
                                mTodoAdapter.notifyItemChanged(index, todo);*/
                                mTodoArrayList.add(todo);
                                mTodoAdapter.notifyDataSetChanged();

                            }
                           else {


                              //  mTodoAdapter.notifyItemRemoved(index);

                                mCompletedTaskTodoArrayList.add(todo );
                                mCompletedTaskTodoAdapter.notifyItemInserted(mCompletedTaskTodoArrayList.size()-1);

                            }
                        }
                    }
                }
            }
        });
    }
}
