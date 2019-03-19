package com.apps.offbeat.tasks.Data;

import java.io.Serializable;
import java.util.Date;

public class Todo implements Serializable{
    public String title, description_tasks;
    public String id;
    public boolean task_complete;
    public String due_date;
    public Date timestamp;

    public Todo(String title, String description_tasks, String id, boolean task_complete, String due_date, Date timestamp) {
        this.title = title;
        this.description_tasks = description_tasks;
        this.id = id;
        this.task_complete = task_complete;
        this.due_date = due_date;
        this.timestamp = timestamp;
    }

    public Todo() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDescription() {
        return description_tasks;
    }

    public void setDescription(String description_tasks) {
        this.description_tasks = description_tasks;
    }

    public boolean isComplete() {
        return task_complete;
    }

    public void setTask_complete(boolean task_complete) {
        this.task_complete = task_complete;
    }

    public String getDate() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }
}
