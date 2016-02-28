package hk.hku.cs.othello;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersusActivity extends Activity {

    TextView txt_player1Name;
    TextView txt_player2Name;
    Thread threadStatusListener;
    boolean f = true;
    String username= null;
    String player1=null;
    String player2=null;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus);
        txt_player1Name = (TextView)findViewById(R.id.txt_player1Name);
        txt_player2Name = (TextView)findViewById(R.id.txt_player2Name);

        Intent intent = this.getIntent();
        player1 = intent.getStringExtra("player1");
        player2 = intent.getStringExtra("player2");
        String status = intent.getStringExtra("status");
        username = intent.getStringExtra("username");

        if (status.equals("W")) f=false;

        System.out.println("%%%%%%%%%%%%%%%" +status);
        txt_player1Name.setText(player1);
        if (player2 != null) txt_player2Name.setText(player2);

        if (f) {
            TextView txt_ready = (TextView)findViewById(R.id.txt_ready2);
            txt_ready.setText("READY!");

            connectNewGame(player1,player2);
            new CountDownTimer(2000,1000){

                public void onTick(long millisUntilFinished){}

                public void onFinish(){
                    Intent intentnew = new Intent(getBaseContext(), GameActivity.class);
                    intentnew.putExtra("player1", player1);
                    intentnew.putExtra("player2", player2);
                    intentnew.putExtra("username", username);
                    startActivity(intentnew);
                }

            }.start();

        }
        threadStatusListener = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!f){
                    connectStatus();
                    System.out.println("*****");
                try {
                    threadStatusListener.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}
        });
        threadStatusListener.start();

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


    public void parse_JSON_String(String JSONString) {
        String ready = null;
        try {
            JSONObject rootJSONObj = new JSONObject(JSONString);
            ready = rootJSONObj.getString("Status");

            if (ready.equals("W")){
                player1 = rootJSONObj.getString("player1");
            }else {
                player1 = rootJSONObj.getString("player1");
                player2 = rootJSONObj.getString("player2");
                txt_player1Name.setText(player1);
                txt_player2Name.setText(player2);
                TextView txt_ready = (TextView)findViewById(R.id.txt_ready2);
                txt_ready.setText("READY!");

                f=true;

                new CountDownTimer(2000,1000){

                    public void onTick(long millisUntilFinished){}

                    public void onFinish(){
                        Intent intentnew = new Intent(getBaseContext(), GameActivity.class);
                        intentnew.putExtra("player1", player1);
                        intentnew.putExtra("player2", player2);
                        intentnew.putExtra("username", username);
                        startActivity(intentnew);
                    }

                }.start();

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void connectStatus() {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=status";
        //System.out.println(url);
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
                    parse_JSON_String(jsonString);
                } else {
                    alert("Error", "Fail to login");
                }
            }
        }.execute("");
    }

    public void connectNewGame(String p1, String p2) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=newgame&p1=" + android.net.Uri.encode(p1, "UTF-8") + "&p2=" + android.net.Uri.encode(p2, "UTF-8");
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
                } else {
                    alert("Error", "Fail to login");
                }
            }
        }.execute("");

    }
}
