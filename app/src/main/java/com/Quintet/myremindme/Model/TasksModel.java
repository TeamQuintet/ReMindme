package com.Quintet.myremindme.Model;

public class TasksModel {

    private String task, description, id, date,time;

    public TasksModel() {
    }

    public TasksModel(String task, String description, String id, String date,String time) {
        this.task = task;
        this.description = description;
        this.id = id;
        this.date = date;
        this.time = time;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
