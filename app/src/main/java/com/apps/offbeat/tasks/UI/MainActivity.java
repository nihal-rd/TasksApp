package com.apps.offbeat.tasks.UI;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.offbeat.tasks.Data.List;
import com.apps.offbeat.tasks.R;
import com.apps.offbeat.tasks.TaskListAdapter;
import com.apps.offbeat.tasks.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity  {

    int color=0;
    View mBottomSheetView;
    BottomSheetDialog mBottomSheetDialog;
    ImageView navButtonDrawer;
    ArrayList<List> listArrayList = new ArrayList<>();
    int totalTasks = 0;
    RecyclerView mRecyclerView;
    private static final String TAG = MainActivity.class.getSimpleName();
    Utils mUtils;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    TaskListAdapter taskListAdapter;
    TextView textViewTotalTask;
    SimpleDraweeView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLightStatusBar(this, getWindow().getDecorView(), this);
        mUtils.setTypeFace(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listArrayList.clear();

        NestedScrollView mNestedScrollView = findViewById(R.id.nested_scroll_main);
        mNestedScrollView.setNestedScrollingEnabled(true);

        profilePhoto=findViewById(R.id.main_profilepic);
        TextView userName=findViewById(R.id.name_wave);
        TextView greetings=findViewById(R.id.greetings);
         textViewTotalTask=findViewById(R.id.number_of_task);
        TextView todaysDate=findViewById(R.id.todays_date);

     //getting profile photo from email
          Uri uri=(mFirebaseAuth.getCurrentUser().getPhotoUrl());
          profilePhoto.setImageURI(uri);

          userName.setText("Hi "+ nameTrim(mFirebaseAuth.getCurrentUser().getDisplayName()) +"!");
          Calendar c=new GregorianCalendar();
          int hour=c.get(Calendar.HOUR_OF_DAY);
          int min=c.get(Calendar.MINUTE);

          if((hour>=0&&min>0) && (hour<12&&min<60))
              greetings.setText("Good Morniing!");

        if((hour>=12&&min>0) && (hour<16&&min<60))
            greetings.setText("Good Afternoon!");

        if((hour>=16&&min>0) && (hour<24&&min<60))
            greetings.setText("Good Evening!");


        Date now = new Date();



       SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        todaysDate.setText(simpleDateformat.format(now)+", "+ c.get(Calendar.DAY_OF_MONTH)+
                new SimpleDateFormat("MMMM").format(now)+ " "+c.get(Calendar.YEAR));





        mRecyclerView = findViewById(R.id.rv_main);
        taskListAdapter = new TaskListAdapter(MainActivity.this, listArrayList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(taskListAdapter);
       navButtonDrawer = toolbar.getRootView().findViewById(R.id.toolbar_nav);

        getTasks();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater)MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View taskView = layoutInflater.inflate(R.layout.add_task_layout,null,false);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Create Task");
                View rootView = taskView.getRootView();
                final EditText editText=rootView.findViewById(R.id.et_task_title);
              final RadioGroup colorSelection= rootView.findViewById(R.id.radio_color_select);
             // String color;



               colorSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                   @Override
                   public void onCheckedChanged(RadioGroup group, int checkedId) {
                   switch (colorSelection.getCheckedRadioButtonId()) {
                       case R.id.button_red_color:
                           Log.d(TAG, "my color red");
                          color= Color.parseColor("#f44336");

                           break;
                       case R.id.button_olive_color:
                           Log.d(TAG, "my color olive");
                           color= Color.parseColor("#808000");

                           break;
                       case R.id.button_yellow_color:
                           Log.d(TAG, "my color yellow");
                           color= Color.parseColor("#ffff00");
                           break;
                       case R.id.button_blue_color:
                           Log.d(TAG, "my color blue");
                           color= Color.parseColor("#0000ff");
                           break;
                       case R.id.button_violet_color:
                           Log.d(TAG, "red");
                           color= Color.parseColor("#7a2dff");
                           break;

                           default:
                               color= Color.parseColor("#f44336");


                   }
                   }
               });
                



// TODO fix for error
                builder.setCancelable(true);  // dissmiss on clcking outside the dialog
                //OKButton
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(editText.getText().toString())){
                            final DocumentReference newTask = mFirebaseFirestore.collection("Tasks")
                                    .document("UserTasks")
                                    .collection(mFirebaseAuth.getCurrentUser().getUid())
                                    .document(editText.getText().toString());
                            final Map<String, Object> map = new HashMap<>();
                            map.put("title", editText.getText().toString());
                            map.put(getString(R.string.exist_key), true);
                            map.put(getString(R.string.task_color),color);

                            newTask.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "Task list created", Toast.LENGTH_SHORT).show();
                                    }

                                    else{
                                        Toast.makeText(MainActivity.this, "List creation failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            dialog.dismiss();
                        } else {
                            editText.setError("Required");
                        }

                    }
                });
                //cancleButton
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


        mBottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.BottomSheetDialogTheme);
        mBottomSheetView= getLayoutInflater().inflate(R.layout.bottom_sheet_main,null);
        mBottomSheetDialog.setContentView(mBottomSheetView);
        navButtonDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.show();
            }
        });
        
        

       TextView name=mBottomSheetView.findViewById(R.id.bottom_sheet_user_name);
        name.setText(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getDisplayName());
        TextView email=mBottomSheetView.findViewById(R.id.bottom_sheet_user_email);
        email.setText(mFirebaseAuth.getCurrentUser().getEmail());
        SimpleDraweeView simpleDraweeView=mBottomSheetView.findViewById(R.id.bottom_sheet_profile_pic);
        simpleDraweeView.setImageURI(mFirebaseAuth.getCurrentUser().getPhotoUrl());

        LinearLayout logout=mBottomSheetView.findViewById(R.id.log_out);
        LinearLayout about=mBottomSheetView.findViewById(R.id.about);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this,Splash.class));
                finish();
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
            }
        });

       
    }
    void getTasks(){
        listArrayList.clear();
        final CollectionReference taskRef = mFirebaseFirestore.collection("Tasks")
                .document("UserTasks")
                .collection(mFirebaseAuth.getCurrentUser().getUid());
        taskRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();
                        List list = documentSnapshot.toObject(List.class);
                        listArrayList.add(list);
                        taskListAdapter.notifyDataSetChanged();
                        totalTasks+=list.getIncomplete_tasks();
                    }
                }
                textViewTotalTask.setText("You have " + totalTasks +" tasks pending");
            }
        });
    }

    String nameTrim(String fullName){
        int index=fullName.lastIndexOf(' ');
        if (index == -1)
            throw new IllegalArgumentException("Only a single name: " + fullName);
        String firstName = fullName.substring(0, index);

        return firstName;
    }

}
