package hk.hku.cs.othello;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by XIEYan on 11/30/15.
 */
public class LoginActivity extends Activity {

    TextView txt_AccountName;
    Button btn_records;
    Button btn_matching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        txt_AccountName = (TextView)findViewById(R.id.txt_accountName);
        btn_records = (Button)findViewById(R.id.btn_records);
        btn_matching = (Button)findViewById(R.id.btn_startMatching);

        btn_records.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String AccountName = txt_AccountName.getText().toString();
                if (AccountName.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Name cannot be empty.",
                            Toast.LENGTH_SHORT).show();
                }else connectRecord(AccountName);
            }
        });

        btn_matching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getBaseContext(), VersusActivity.class);
                String AccountName = txt_AccountName.getText().toString();
                if (AccountName.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Name cannot be empty.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    connectMatch(AccountName);
                }
                //intent.putExtra("name", AccountName);
                //startActivity(intent);
            }
        });

    }


    protected void alert(String title, String mymessage) {
        new AlertDialog.Builder(this)
                .setMessage(mymessage)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                .show();
    }

    public String ReadBufferedHTML(BufferedReader reader, char[] htmlBuffer, int bufSz) throws java.io.IOException {
        htmlBuffer[0] = '\0';
        int offset = 0;
        do {
            int cnt = reader.read(htmlBuffer, offset, bufSz - offset);
            if (cnt > 0) {
                offset += cnt;
            } else {
                break;
            }
        } while (true);
        return new String(htmlBuffer);
    }

    public String getJsonPage(String url) {
        HttpURLConnection conn_cshomepage = null;
        final int HTML_BUFFER_SIZE = 2 * 1024;
        char htmlBuffer[] = new char[HTML_BUFFER_SIZE];
        try {
            ///////////////////////////////// CS Homepage ////////////////////////////////////
            URL url_cshomepage = new URL(url);
            conn_cshomepage = (HttpURLConnection) url_cshomepage.openConnection();
            conn_cshomepage.setInstanceFollowRedirects(true);
            BufferedReader reader_moodle = new BufferedReader(new InputStreamReader(conn_cshomepage.getInputStream()));
            String HTMLSource = ReadBufferedHTML(reader_moodle, htmlBuffer, HTML_BUFFER_SIZE);
            reader_moodle.close();
            return HTMLSource;
            ///////////////////////////////// CS Homepage ////////////////////////////////////
        } catch (Exception e) {
            return "Fail to login";
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if (conn_cshomepage != null) {
                conn_cshomepage.disconnect();
            }
        }
    }

    public void parse_JSON_String_and_Switch_VersusActivity(String JSONString) {
        String player1 = null;
        String player2 = null;
        String ready = null;
        try {
            JSONObject rootJSONObj = new JSONObject(JSONString);
            ready = rootJSONObj.getString("Status");

            if (ready.equals("W")){
                player1 = rootJSONObj.getString("player1");
            }else {
                player1 = rootJSONObj.getString("player1");
                player2 = rootJSONObj.getString("player2");
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getBaseContext(), VersusActivity.class);
        intent.putExtra("status",ready);
        intent.putExtra("player1",player1);
        intent.putExtra("player2", player2);
        intent.putExtra("username", txt_AccountName.getText().toString());
        startActivity(intent);

    }

    public void parse_JSON_String_and_Switch_RecordActivity(String JSONString) {
        ArrayList<String> name = new ArrayList<String>();
        ArrayList<String> opposite = new ArrayList<String>();
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> turn = new ArrayList<String>();
        ArrayList<String> scores = new ArrayList<String>();

        try {
            JSONObject rootJSONObj = new JSONObject(JSONString);
            JSONArray jsonArray = rootJSONObj.optJSONArray("name");
            //System.out.println("jsonArraylength:=" +jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i) {
                String Name = jsonArray.getString(i);
                name.add(Name);
            }
            jsonArray = rootJSONObj.optJSONArray("opposite");
            for (int i = 0; i < jsonArray.length(); ++i) {
                String Opposite = jsonArray.getString(i);
                opposite.add(Opposite);
            }
            jsonArray = rootJSONObj.optJSONArray("result");
            for (int i = 0; i < jsonArray.length(); ++i) {
                String Result = jsonArray.getString(i);
                result.add(Result);
            }

            jsonArray = rootJSONObj.optJSONArray("turn");
            for (int i = 0; i < jsonArray.length(); ++i) {
                String Turn = jsonArray.getString(i);
                turn.add(Turn);
            }
            jsonArray = rootJSONObj.optJSONArray("scores");
            for (int i = 0; i < jsonArray.length(); ++i) {
                String Scores = jsonArray.getString(i);
                scores.add(Scores);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i<name.size();i++){
            System.out.println("name ->"+name.get(i));
        }

        Intent intent = new Intent(getBaseContext(), RecordActivity.class);
        intent.putStringArrayListExtra("name", name);
        intent.putStringArrayListExtra("opposite", opposite);
        intent.putStringArrayListExtra("result", result);
        intent.putStringArrayListExtra("turn", turn);
        intent.putStringArrayListExtra("scores", scores);

        startActivity(intent);
    }

    public void connectRecord(final String name) {
        final ProgressDialog pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("Logging in ...");
        pdialog.show();
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + (name.isEmpty() ? "" : "?action=select&name=" + android.net.Uri.encode(name, "UTF-8"));
        System.out.println(url);
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            boolean success;
            String jsonString;

            @Override
            protected String doInBackground(String... arg0) { // TODO Auto-generated method stub
                success = true;
                jsonString = getJsonPage(url);
                if (jsonString.equals("Fail to login")) success = false;
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (success) {
                    parse_JSON_String_and_Switch_RecordActivity(jsonString);
                } else {
                    alert("Error", "Fail to login");
                }
                pdialog.hide();
            }
        }.execute("");
    }

    public void connectMatch(final String name) {
        final ProgressDialog pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("Loading ...");
        pdialog.show();
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=ready&name=" + android.net.Uri.encode(name, "UTF-8");
        System.out.println(url);
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            boolean success;
            String jsonString;

            @Override
            protected String doInBackground(String... arg0) { // TODO Auto-generated method stub
                success = true;
                jsonString = getJsonPage(url);
                if (jsonString.equals("Fail to login")) success = false;
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (success) {
                    parse_JSON_String_and_Switch_VersusActivity(jsonString);
                } else {
                    alert("Error", "Fail to login");
                }
                pdialog.hide();
            }
        }.execute("");

    }
}
