package com.apps.offbeat.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.offbeat.tasks.Data.List;
import com.apps.offbeat.tasks.UI.MainActivity;
import com.apps.offbeat.tasks.UI.TaskDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    Context mContext;
    ArrayList<List> mListArrayList;
    public FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    public FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    int color=0;

    public TaskListAdapter(Context mContext, ArrayList<List> mListArrayList) {
        this.mContext = mContext;
        this.mListArrayList = mListArrayList;
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(mContext).inflate(R.layout.task_item_layout,parent,false);
        return new TaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskListViewHolder holder, final int position) {

        final List list=mListArrayList.get(position);
        holder.title.setText(list.getTitle());
        holder.numberTask.setText(list.getIncomplete_tasks() + " Tasks");

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,TaskDetail.class);
                intent.putExtra(mContext.getString(R.string.task_title_key), list.getTitle());
                intent.putExtra(mContext.getString(R.string.task_completed_items_key), list.getCompleted_tasks());
                intent.putExtra(mContext.getString(R.string.task_incomplete_items_key), list.getIncomplete_tasks());

                mContext.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu=new PopupMenu(mContext,holder.cardView);
                popupMenu.getMenuInflater()
                        .inflate(R.menu.popup_menu, popupMenu.getMenu());

                //registering popup with OnMenuItemClickListener
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(R.id.delete_button==item.getItemId()){
                       /* Toast.makeText(
                                context,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();*/

                            final DocumentReference taskDocumentReference = mFirebaseFirestore.collection("Tasks")
                                    .document("UserTasks")
                                    .collection(mFirebaseAuth.getCurrentUser().getUid())
                                    .document(list.getTitle());

                            taskDocumentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mListArrayList.remove(position);
                                    notifyItemRemoved(position);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }


                        return true;
                    }
                });

                popupMenu.show();


                return false;
            }
        });
        holder.color.setBackgroundColor(list.getColor());

    }

    @Override
    public int getItemCount() {
        return mListArrayList.size();
    }

    class TaskListViewHolder extends RecyclerView.ViewHolder {
        TextView title, numberTask;
        CardView cardView;
        View color;

        public TaskListViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_task_title);
            numberTask=itemView.findViewById(R.id.tv_task_detail);
            cardView=itemView.findViewById(R.id.container);
            color=itemView.findViewById(R.id.color_view);

        }
    }
}
