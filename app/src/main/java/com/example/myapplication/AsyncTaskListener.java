package com.example.myapplication;

import org.json.JSONArray;

public interface AsyncTaskListener {
    void onTaskComplete(JSONArray result, MainActivity.Types type);
    void onInsert();
}
