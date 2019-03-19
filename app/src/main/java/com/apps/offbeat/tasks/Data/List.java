package com.apps.offbeat.tasks.Data;

import java.io.Serializable;

public class List implements Serializable {
    String title;
    int completed_tasks;
    int incomplete_tasks;
    int color;

    public List(String title, int completed_tasks, int incomplete_tasks, int color) {
        this.title = title;
        this.completed_tasks = completed_tasks;
        this.incomplete_tasks = incomplete_tasks;
        this.color = color;
    }

    public List() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCompleted_tasks() {
        return completed_tasks;
    }

    public void setCompleted_tasks(int completed_tasks) {
        this.completed_tasks = completed_tasks;
    }

    public int getIncomplete_tasks() {
        return incomplete_tasks;
    }

    public void setIncomplete_tasks(int incomplete_tasks) {
        this.incomplete_tasks = incomplete_tasks;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
