package hk.hku.cs.othello;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordActivity extends ListActivity {
ArrayList<Map<String, Object>> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        ArrayList<String> name = intent.getStringArrayListExtra("name");
        ArrayList<String> opposite = intent.getStringArrayListExtra("opposite");
        ArrayList<String> result = intent.getStringArrayListExtra("result");
        ArrayList<String> turn = intent.getStringArrayListExtra("turn");
        ArrayList<String> scores = intent.getStringArrayListExtra("scores");

        for( int i = 0; i < name.size(); i++ ){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put( "name", name.get(i) );
            map.put( "opposite", opposite.get(i) );
            map.put( "result", result.get(i) );
            map.put( "turn", turn.get(i) );
            map.put( "scores", scores.get(i) );
            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.activity_record,
                                                    new String[]{"name","opposite","result","turn","scores"},
                                                    new int[]{R.id.record_name,R.id.record_opposite,R.id.record_result,R.id.record_turn,R.id.record_scores});
        setListAdapter(adapter);

    }


}
