package com.apps.offbeat.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.offbeat.tasks.Data.Todo;
import com.apps.offbeat.tasks.UI.TaskDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder>{

     ArrayList<Todo> todoArrayList;
     Context context;
     FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
     FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    int mIncompleteCount, mCompleteCount;
   public String mTitle;

    public TodoAdapter( Context context, ArrayList<Todo> todoArrayList, String title) {
        this.todoArrayList = todoArrayList;
        this.context = context;
        this.mTitle=title;
        Context activityContext = (TaskDetail) context;
         mIncompleteCount = ((TaskDetail) activityContext).mIncompleteTasksCount;
         mCompleteCount = ((TaskDetail) activityContext).mCompleteTasksCount;
    }





    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View vw=LayoutInflater.from(context).inflate(R.layout.todo_list,parent,false);
        return new TodoViewHolder(vw);

    }


    @Override
    public void onBindViewHolder(@NonNull final TodoViewHolder holder, final int position) {
        final Todo todo=todoArrayList.get(position);

        holder.title.setText( todo.getTitle());
        if (todo.isComplete()) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.desc.setText(todo.getDescription());
        holder.date.setText(todo.getDate());
       holder.checkBox.setChecked(todo.isComplete());


    holder.checkBox.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (holder.checkBox.isChecked()){


                mCompleteCount+=1;
                 mIncompleteCount-=1;


                holder.checkBox.toggle();
                Todo temp=todo;
                final DocumentReference taskListDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle);
                final DocumentReference taskDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle).collection("Tasks").document(temp.getId());
                taskDocumentReference.update(context.getString(R.string.task_is_complete_items_key), true);
                taskListDocumentReference.update(context.getString(R.string.task_incomplete_items_key),mIncompleteCount,
                        context.getString(R.string.task_completed_items_key),mCompleteCount).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ((TaskDetail) context).mIncompleteTasksCount = mIncompleteCount;
                        ((TaskDetail) context).mCompleteTasksCount = mCompleteCount;
                    }
                });

                temp.task_complete=true;
                int pos= todoArrayList.indexOf(temp);
                todoArrayList.remove(temp);
                TodoAdapter.this.notifyItemRemoved(pos);
                // todoArrayList.add(temp);
                //  TodoAdapter.this.notifyItemInserted(todoArrayList.size()-1);
            }
            else{


                Todo temp=todo;
                final DocumentReference taskListDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle);
                final DocumentReference taskDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle).collection("Tasks").document(temp.getId());
                // taskListDocumentReference.update(context.getString(R.string.task_incomplete_items_key),mIncompleteCount-1,
                //  context.getString(R.string.task_completed_items_key),mCompleteCount+1);
                taskDocumentReference.update(context.getString(R.string.task_is_complete_items_key), false);
                temp.task_complete=false;
                //   todo.setTask_complete(false);

                int pos= todoArrayList.indexOf(temp);
                todoArrayList.remove(temp);
                TodoAdapter.this.notifyItemRemoved(pos);
            }
        }
    });




        holder.todoItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              /* LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               View popUpView=layoutInflater.inflate(R.layout.popup_menu,null,false);*/
                PopupMenu popupMenu=new PopupMenu(context,holder.todoItemLayout);

                popupMenu.getMenuInflater()
                        .inflate(R.menu.popup_menu, popupMenu.getMenu());

                final DocumentReference taskListDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle);
                final DocumentReference taskDocumentReference = mFirebaseFirestore.collection("Tasks")
                        .document("UserTasks")
                        .collection(mFirebaseAuth.getCurrentUser().getUid())
                        .document(mTitle).collection("Tasks").document(todo.getId());

                //registering popup with OnMenuItemClickListener
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                       if(R.id.delete_button==item.getItemId()){
                       /* Toast.makeText(
                                context,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();*/



                           taskDocumentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   todoArrayList.remove(position);
                                   notifyItemRemoved(position);
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   e.printStackTrace();
                               }
                           });
                           taskListDocumentReference.update(context.getString(R.string.task_incomplete_items_key),mIncompleteCount-1);
                       }

                       if(R.id.edit_button==item.getItemId()){
                           LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                           View taskView = layoutInflater.inflate(R.layout.add_task_detail_layout, null, false);
                           final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                           builder.setTitle("Edit TodoItem");
                           View rootView = taskView.getRootView();
                           final EditText titleText = rootView.findViewById(R.id.et_task_detail_title);
                           final EditText dateText = rootView.findViewById(R.id.et_task_detail_date);
                           final EditText descText = rootView.findViewById(R.id.et_task_detail_desc);
                           builder.setCancelable(true);
                           titleText.setText(todo.getTitle());
                           dateText.setText(todo.getDate());
                           descText.setText(todo.getDescription());
                         //Edit Button
                           builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {


                                   Map<String,Object> map= new HashMap<>();
                                   map.put(context.getString(R.string.task_title_key),titleText.getText().toString());
                                   map.put(context.getString(R.string.task_description_items_key),descText.getText().toString());
                                   map.put(context.getString(R.string.task_due_date_key),dateText.getText().toString());

                                   taskDocumentReference.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()) {
                                          todoArrayList.remove(position);
                                          TodoAdapter.this.notifyItemRemoved(position);
                                               Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();

                                           }
                                           else {
                                               Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                               task.getException().printStackTrace();
                                           }
                                           // run karo hao
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

                        return true;
                    }
                });

                popupMenu.show();


                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoArrayList.size();
    }

    class  TodoViewHolder extends RecyclerView.ViewHolder{

        TextView date,title,desc;
        CheckBox checkBox;
        RelativeLayout todoItemLayout;

        public TodoViewHolder(View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title);
            desc=itemView.findViewById(R.id.task_desc);
            date=itemView.findViewById(R.id.task_date);
            checkBox=itemView.findViewById(R.id.status);
            todoItemLayout=itemView.findViewById(R.id.todo_item);
        }
    }
}