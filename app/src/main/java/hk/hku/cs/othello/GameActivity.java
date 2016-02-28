package hk.hku.cs.othello;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class GameActivity extends Activity{

    Thread opponentListener;

    TableLayout tableLayout_chessboard;
    TextView textView_BlackGrade;
    TextView textView_WhiteGrade;
    TextView textView_player1;
    TextView textView_player2;
    TextView textViewTime;

    TextView textView_Turn;
    ImageView imgView_turn;
    Button btn_newGame;
    Button btn_hintsOn;
    Button btn_surrender;
    //boolean whoseturn;
    boolean hints;
    MediaPlayer mediaPlayer;
    MediaPlayer bgmPlayer;
    AlertDialog.Builder builder_blackwin;
    AlertDialog.Builder builder_whitewin;
    CounterClass timer;
    //NEW INFORMATION-------------------------
    boolean currentTurn;

    boolean JSONStringIsEmpty=false;
    boolean end=false;
    int myStep = 0;
    boolean myTurn;
    String player1;
    String player2;
    String username;
    //NEW INFORMATION-------------------------
    int[][] chess = {{0,0,0,0,0,0,0,0},
                     {0,0,0,0,0,0,0,0},
                     {0,0,0,0,0,0,0,0},
                     {0,0,0,-1,1,0,0,0},
                     {0,0,0,1,-1,0,0,0},
                     {0,0,0,0,0,0,0,0},
                     {0,0,0,0,0,0,0,0},
                     {0,0,0,0,0,0,0,0}};

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textViewTime = (TextView)findViewById(R.id.txt_counter);

        timer = new CounterClass(30000, 1000);


        Intent intent = this.getIntent();
        player1 = intent.getStringExtra("player1");
        player2 = intent.getStringExtra("player2");
        username = intent.getStringExtra("username");

        textView_player1 = (TextView)findViewById(R.id.txtView_player1);
        textView_player2 = (TextView)findViewById(R.id.txtView_player2);
        textView_player1.setText(": "+player1);
        textView_player2.setText(player2+": ");
        if (username.equals(player1)) {
            textView_player1.setTextColor(getResources().getColor(R.color.color_orange));
            myTurn = true;
        }else {
            textView_player2.setTextColor(getResources().getColor(R.color.color_orange));
            myTurn = false;
        }

        tableLayout_chessboard = (TableLayout)findViewById(R.id.table_chessboard);
        textView_BlackGrade = (TextView)findViewById(R.id.textview_BlackGrade);
        textView_WhiteGrade = (TextView)findViewById(R.id.textview_WhiteGrade);
        textView_Turn = (TextView)findViewById(R.id.textview_Turn);
        imgView_turn = (ImageView)findViewById(R.id.imgview_turn);
        btn_hintsOn = (Button)findViewById(R.id.btn_hintsOn);
        btn_newGame = (Button)findViewById(R.id.btn_newGame);
        btn_surrender = (Button)findViewById(R.id.btn_surrender);
        currentTurn = true;
        hints = false;
        mediaPlayer = MediaPlayer.create(this, R.raw.setchess);
        bgmPlayer = MediaPlayer.create(this, R.raw.background);
        if(!bgmPlayer.isLooping()) bgmPlayer.start();
        builder_blackwin = new AlertDialog.Builder(this);
        builder_blackwin.setTitle("is the winner!");
        builder_blackwin.setIcon(R.drawable.black_chess);
        builder_blackwin.setNegativeButton("Restart",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Toast.makeText(GameActivity.this, "restart the game",
                                Toast.LENGTH_SHORT).show();
                        newGame();
                        connectNewGame(player1, player2);
                    }
                });
        builder_whitewin = new AlertDialog.Builder(this);
        builder_whitewin.setTitle("is the winner!");
        builder_whitewin.setIcon(R.drawable.white_chess);
        builder_whitewin.setNegativeButton("Restart",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Toast.makeText(GameActivity.this, "restart the game",
                                Toast.LENGTH_SHORT).show();
                        newGame();
                        connectNewGame(player1,player2);
                        connectNewgame(player1, player2);

                    }
                });

        //------------newGame()-------------
        newGame();

        //------------newGame()-------------
        //---------------Thread listener--------------------------------------
            opponentListener = new Thread(new Runnable() {
                @Override
                public void run() {
                        while(true) {
                            connectOppoentListener(player1, player2);
                            try {
                                opponentListener.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            });
            opponentListener.start();
        //---------------Thread listener--------------------------------------

        btn_newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end = true;

                newGame();
                connectNewgame(player1, player2);
                if(!bgmPlayer.isLooping()) bgmPlayer.start();
            }
        });

        btn_hintsOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hints = !hints;
                if (hints) btn_hintsOn.setText("Hints Off");
                else btn_hintsOn.setText("Hints On");
                if (hints) setHintsOn();
                else setHintsOff();
            }
        });

        btn_surrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectSurrender(player1,player2,myTurn);
                if (username.equals(player1)){
                    builder_whitewin.show();
                    connectEndgame(player1,player2,"LOSE","black","surrender");
                    connectEndgame(player2,player1,"WIN","white","surrender");
                }else{
                    builder_blackwin.show();
                    connectEndgame(player2,player1,"LOSE","white","surrender");
                    connectEndgame(player1,player2,"WIN","black","surrender");
                }
            }
        });

    }

    public void setHintsOn(){
        int checkcolor;
        if (currentTurn) checkcolor = 1;
        else checkcolor = -1;
        for (int i = 0 ; i<8 ; i++)
            for (int j = 0 ; j<8 ; j++){
                if ( checkValid(i,j,currentTurn,false) ){
                     chess[i][j] = checkcolor*2;
                     //System.out.println(i + "@@@" + j);
                }
            }
        //for (int i = 0;i < 8; i++){
        //    for (int j = 0; j < 8 ;j++){
        //        System.out.print(chess[i][j]+" ");
        //    }
        //    System.out.println();
        //}
        setChessOnChessboard();
    }

    public void setHintsOff(){
        for (int i = 0 ; i<8 ; i++)
            for (int j = 0 ; j<8 ; j++){
                if (chess[i][j] == -2) chess[i][j] =0;
                if (chess[i][j] == 2) chess[i][j] =0;
            }
        setChessOnChessboard();
    }

    public void setChessOnChessboard(){
        for (int i = 0;i < 8; i++)
            for (int j = 0; j < 8 ;j++){

                String tagname = String.valueOf(i) + String.valueOf(j);
                ImageButton imgbtn = (ImageButton)tableLayout_chessboard.findViewWithTag("imgbtn"+tagname);

                if (chess[i][j] == -1){
                    imgbtn.setImageResource(R.drawable.white_chess);
                }
                if (chess[i][j] == 1){
                    imgbtn.setImageResource(R.drawable.black_chess);
                }
                if(chess[i][j] == 0) {
                    imgbtn.setImageResource(R.drawable.transparent);
                }
                if (chess[i][j] == -2){
                    imgbtn.setImageResource(R.drawable.white_chess_t);
                    chess[i][j] = 0;
                }
                if (chess[i][j] == 2){
                    imgbtn.setImageResource(R.drawable.black_chess_t);
                    chess[i][j] = 0;
                }


        }
    }

    public void newGame(){
        textView_BlackGrade.setText(": 2");
        textView_WhiteGrade.setText(": 2");
        imgView_turn.setImageResource(R.drawable.black_chess);
        currentTurn = true;
        JSONStringIsEmpty = false;
        timer.cancel();
        textViewTime.setText("30");
        if (currentTurn == myTurn) timer.start();
        end = false;
        hints = false;
        myStep = 0;
        btn_hintsOn.setText("Hints On");
        textView_Turn.setText("Turn :");
        for (int i =0 ; i<8 ; i++)
            for(int j=0 ; j<8 ; j++){
                chess[i][j] = 0;
            }
        chess[3][3] = -1;
        chess[3][4] = 1;
        chess[4][3] = 1;
        chess[4][4] = -1;

        setChessOnChessboard();
        if(!bgmPlayer.isLooping()) bgmPlayer.start();
    }

    public void onButtonClick(View v) throws InterruptedException {
        if (currentTurn == myTurn) {
            String btnID = v.getResources().getResourceEntryName(v.getId());
            //System.out.println(btnID);
            ImageButton onClickBtn = (ImageButton) v.findViewWithTag(btnID);
            textView_Turn = (TextView) findViewById(R.id.textview_Turn);
            int x = btnID.charAt(6) - '0';
            int y = btnID.charAt(7) - '0';
            System.out.printf("%d,%d\n", x, y);

            if (checkValid(x, y, currentTurn, true)) {
                if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                setChessOnChessboard();
                timer.cancel();
                timer.onPause();
                myStep++;
                System.out.println("x="+String.valueOf(x)+" y="+String.valueOf(y));

                //----------------send position-------------------------
                    connectGame(player1, player2, String.valueOf(x), String.valueOf(y),currentTurn);
                //----------------send position-------------------------

                textView_WhiteGrade.setText(": " + String.valueOf(result(-1)));
                textView_BlackGrade.setText(": " + String.valueOf(result(1)));
                if (!hintsOn(!currentTurn) && !hintsOn(currentTurn)) {
                    JSONStringIsEmpty = true;
                    String opponent;
                    opponent = username.equals(player1)?player2:player1;
                    String turn;
                    turn = username.equals(player1)?"black":"white";
                    String res = null;
                    textView_Turn.setText("Win :");
                    if (result(-1) > result(1)) {
                        imgView_turn.setImageResource(R.drawable.white_chess);
                        res = username.equals(player1)?"LOSE":"WIN";
                        builder_whitewin.show();
                    }
                    if (result(-1) < result(1)) {
                        builder_blackwin.show();
                        imgView_turn.setImageResource(R.drawable.black_chess);
                        res = username.equals(player1)?"WIN":"LOSE";
                    }
                    if (result(-1) == result(1)) {
                        textView_Turn.setText("Game Draw");
                        res = "DRAW";
                    }
                    String scores = String.valueOf(result(1))+":"+String.valueOf(result(-1));
                    connectEndgame(username,opponent,res,turn,scores);
                    connectEndgame(player1,player2);

                }

                if (hintsOn(!currentTurn)) {
                    if (currentTurn) imgView_turn.setImageResource(R.drawable.white_chess);
                    else imgView_turn.setImageResource(R.drawable.black_chess);
                    currentTurn = !currentTurn;
                }

                if (hints) setHintsOn();
            }
        }else{
            Toast.makeText(GameActivity.this, "It's not your turn.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void connectSurrender(String player1, String player2, boolean myTurn) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=surrender&p1=" + android.net.Uri.encode(player1, "UTF-8")
                + "&p2=" + android.net.Uri.encode(player2, "UTF-8") + "&t=" + (myTurn?"1":"0");
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

    public void connectEndgame(String player1, String player2) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=end&p1=" + android.net.Uri.encode(player1, "UTF-8")
                + "&p2=" + android.net.Uri.encode(player2, "UTF-8");
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

    public void connectEndgame(String username, String opponent, String result, String turn,String scores) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=endgame&username=" + android.net.Uri.encode(username, "UTF-8")
                + "&opponent=" + android.net.Uri.encode(opponent, "UTF-8")
                + "&result=" + android.net.Uri.encode(result, "UTF-8")
                + "&turn=" + android.net.Uri.encode(turn, "UTF-8")
                + "&scores=" + android.net.Uri.encode(scores, "UTF-8");
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

    public void connectOppoentListener(String p1, String p2) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=gamelistener&p1=" + android.net.Uri.encode(p1, "UTF-8") + "&p2=" + android.net.Uri.encode(p2, "UTF-8");
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

    public void parse_JSON_String(String JSONString) {
        String step;
        try {

            JSONObject rootJSONObj = new JSONObject(JSONString);
            if (JSONStringIsEmpty){}
            else{
            step = rootJSONObj.getString("step");
                if (step.isEmpty()){}
                else{
                int currentStep = Integer.parseInt(step);

            int x = Integer.parseInt(rootJSONObj.getString("x"));
            int y = Integer.parseInt(rootJSONObj.getString("y"));
            String n = rootJSONObj.getString("new");
            String t = rootJSONObj.getString("t");
            String s = rootJSONObj.getString("surrender");
            boolean f;
            f = t.equals("1")?true:false;
            System.out.println("****new = "+n+"****");
            if (s.equals("1") && myTurn != f){
                end = true;
                connectEndgame(player1,player2);
                if (username.equals(player1)) builder_blackwin.show();
                else builder_whitewin.show();
                JSONStringIsEmpty =true;
            }
            if (n.equals("1")){
                end = true;
                newGame();
                connectNewgameButtton(player1,player2);
                currentStep = 0;
            }
            System.out.println("@@@@@currentstep:="+currentStep+"@@@@@@");
            System.out.println("&&&&myturn = "+myTurn+"&&&&&&");
            System.out.println("******currentturn ="+t+"******");
            if (currentStep > myStep && myTurn != f){
                timer.start();
                checkValid(x,y,currentTurn,true);
                setChessOnChessboard();
                myStep++;
                textView_WhiteGrade.setText(": " + String.valueOf(result(-1)));
                textView_BlackGrade.setText(": " + String.valueOf(result(1)));
                if (!hintsOn(!currentTurn) && !hintsOn(currentTurn)) {
                    JSONStringIsEmpty =true;
                    String opponent;
                    opponent = username.equals(player1)?player2:player1;
                    String turn;
                    turn = username.equals(player1)?"black":"white";
                    String res = null;
                    textView_Turn.setText("Win :");
                    if (result(-1) > result(1)) {
                        imgView_turn.setImageResource(R.drawable.white_chess);
                        res = username.equals(player1)?"LOSE":"WIN";
                        builder_whitewin.show();
                    }
                    if (result(-1) < result(1)) {
                        builder_blackwin.show();
                        imgView_turn.setImageResource(R.drawable.black_chess);
                        res = username.equals(player1)?"WIN":"LOSE";
                    }
                    if (result(-1) == result(1)) {
                        textView_Turn.setText("Game Draw");
                        res = "DRAW";
                    }
                    String scores = String.valueOf(result(1))+":"+String.valueOf(result(-1));
                    connectEndgame(username,opponent,res,turn,scores);
                    connectEndgame(player1,player2);
                }

                if (hintsOn(!currentTurn)) {
                    if (currentTurn) imgView_turn.setImageResource(R.drawable.white_chess);
                    else imgView_turn.setImageResource(R.drawable.black_chess);
                    currentTurn = !currentTurn;
                }

                if (hints) setHintsOn();

            }}}
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void connectNewgameButtton(String p1, String p2) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=newgamebtn&p1=" + android.net.Uri.encode(p1, "UTF-8") + "&p2=" + android.net.Uri.encode(p2, "UTF-8");
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

    public void connectNewgame(String p1, String p2) {
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=new&p1=" + android.net.Uri.encode(p1, "UTF-8") + "&p2=" + android.net.Uri.encode(p2, "UTF-8");
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

    public void connectGame(String p1, String p2,String x, String y,boolean turn) {
        String tmp;
        tmp = turn?"1":"0";
        final String url = "http://i.cs.hku.hk/~yxie/game.php"
                + "?action=game&p1=" + android.net.Uri.encode(p1, "UTF-8") + "&p2=" + android.net.Uri.encode(p2, "UTF-8")+
                "&px="+android.net.Uri.encode(x, "UTF-8")+"&py="+android.net.Uri.encode(y, "UTF-8")+"&t="+android.net.Uri.encode(tmp, "UTF-8");
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

    public boolean checkValid(int x, int y , boolean whoseturn , boolean setchess){
        int currentTurnChess;
        int counterPartyChess;
        boolean valid = false;
        if (whoseturn) { currentTurnChess = 1; counterPartyChess = -1;}
        else { currentTurnChess = -1; counterPartyChess = 1;}
        if (chess[x][y] != 0) return false;
        else {
            for (int i = -1; i < 2; i++)
                for (int j = -1; j < 2; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    int checkpoint = 0;
                    int checkposition_x = i * (checkpoint + 1);
                    int checkposition_y = j * (checkpoint + 1);
                    while (x + checkposition_x >= 0 && x + checkposition_x < 8 && y + checkposition_y >= 0 && y + checkposition_y < 8
                            && chess[x + checkposition_x][y + checkposition_y] == counterPartyChess) {
                        checkpoint++;
                        checkposition_x = i * (checkpoint + 1);
                        checkposition_y = j * (checkpoint + 1);
                    }
                    if (checkpoint > 0) {
                        //System.out.println("@@@@@@@"+i+"@"+j+"checkpoint = "+checkpoint);
                        int nextcheckpoint = checkpoint + 1;
                        int nextcheckpoint_x = nextcheckpoint * i;
                        int nextcheckpoint_y = nextcheckpoint * j;
                        if (x + nextcheckpoint_x >= 0 && x + nextcheckpoint_x < 8 && y + nextcheckpoint_y >= 0 && y + nextcheckpoint_y < 8
                                && chess[x + nextcheckpoint_x][y + nextcheckpoint_y] == currentTurnChess) {
                            valid = true;
                            if (setchess) {
                                for (int kk = 0; kk < nextcheckpoint; kk++) {
                                    chess[x + kk * i][y + kk * j] = currentTurnChess;
                                }
                            }
                        }
                    }

                }
        }
        return valid;
    }

    public boolean hintsOn(boolean whoseturn){
        boolean keepgoing = false;
        for (int i = 0 ; i<8 ; i++)
            for (int j = 0 ; j<8 ; j++){
                if (chess[i][j] == 0) {
                    if ( checkValid(i,j,whoseturn,false) ) keepgoing = true;
                }
            }
        return keepgoing;
    }

    public int result(int chessColor){
        int tot = 0;
        for (int i =0 ; i<8 ; i++)
            for (int j = 0; j<8 ; j++){
                if (chess[i][j] == chessColor) tot++;
            }
        return tot;
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

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer{

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = String.format("%02d",
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms);
            textViewTime.setText(hms);
        }

        @Override
        public void onFinish() {
            textViewTime.setText("00");
            Toast.makeText(GameActivity.this, "Time is up!",
                    Toast.LENGTH_SHORT).show();

        }

        public void onPause(){
            textViewTime.setText("30");
        }
    }

}
