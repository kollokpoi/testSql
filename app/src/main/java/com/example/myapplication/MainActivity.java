package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity implements AsyncTaskListener{

    public enum Types{
        events,
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QueryTask task = new QueryTask(this, Types.events);
        task.execute("Select * from Events");

        new Insert(this,1).execute("insert into UserRole([Name]) values(?)","pops");
    }
    @Override
    public void onTaskComplete(JSONArray result,Types type) {
        // Ваш код для обработки результата
        TextView textView = findViewById(R.id.tv);
        if (result != null && result.length() > 0) {

            if (type == Types.events){
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject firstObject = result.getJSONObject(i);
                        String name = firstObject.getString("Name");

                        String desctiption = firstObject.getString("Description");
                        textView.setText(textView.getText().toString() + "\n"+name + " " +desctiption);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public void onInsert() {

    }

    private class QueryTask extends AsyncTask<String, Void, JSONArray> {
        private AsyncTaskListener listener;
        private Types type;
        public QueryTask(AsyncTaskListener listener,Types type) {
            this.listener = listener;
            this.type = type;
        }
        @Override
        protected JSONArray doInBackground(String... query) {
            // Ваш код, выполняемый в фоновом потоке
            String instanceName = "192.168.1.6:1433";
            String db = "EventDB";
            String username = "sa";
            String password = "12332155";
            String connectionUrl = "jdbc:jtds:sqlserver://%1$s;databaseName=%2$s;user=%3$s;password=%4$s;Encrypt=false;trusted_connection=false";
            String connectionString = String.format(connectionUrl, instanceName, db, username, password);
            JSONArray resultSet = new JSONArray();
            try {
                Connection con = DriverManager.getConnection(connectionString);
                StringBuilder result = new StringBuilder();

                if (con != null) {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery(query[0]);

                    if (rs != null) {
                        int columnCount = rs.getMetaData().getColumnCount();
                        // Сохранение данных в JSONArray
                        while (rs.next()) {
                            JSONObject rowObject = new JSONObject();
                            for (int i = 1; i <= columnCount; i++) {
                                rowObject.put(rs.getMetaData().getColumnName(i), (rs.getString(i) != null) ? rs.getString(i) : "");
                            }
                            resultSet.put(rowObject);
                        }
                    }
                }
            } catch (SQLException ex) {
                Log.w("SQLException error: ", ex.getMessage());
            } catch (Exception ex) {
                Log.w("Exception error: ", ex.getMessage());
            }

            return resultSet;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (listener != null) {
                listener.onTaskComplete(result,type);
            }
        }
    }
    private class Insert extends AsyncTask<String, Void, String> {
        private AsyncTaskListener listener;
        int colCount = 0;
        public Insert(AsyncTaskListener listener,int colCount) {
            this.listener = listener;
            this.colCount = colCount;
        }
        @Override
        protected String doInBackground(String... query) {
            // Ваш код, выполняемый в фоновом потоке
            String instanceName = "192.168.1.6:1433";
            String db = "bd";
            String username = "sa";
            String password = "12332155";
            String connectionUrl = "jdbc:jtds:sqlserver://%1$s;databaseName=%2$s;user=%3$s;password=%4$s;Encrypt=false;trusted_connection=false";
            String connectionString = String.format(connectionUrl, instanceName, db, username, password);
            PreparedStatement prepared = null;
            try {
                Connection con = DriverManager.getConnection(connectionString);
                String SQL = query[0];
                if (con != null) {
                    prepared = con.prepareStatement(SQL);

                    if (prepared != null) {
                        for (int i = 1; i <= colCount; i++) {
                            prepared.setString(i, query[i]);
                        }
                        prepared.executeUpdate();
                    }
                }
            } catch (SQLException ex) {
                Log.w("SQLException error: ", ex.getMessage());
            } catch (Exception ex) {
                Log.w("Exception error: ", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            if (listener != null) {
                listener.onInsert();
            }
        }
    }
    class Item{
        private String name;
        private int id;
        private byte[] priviewImage;
        private String description;

    }
}