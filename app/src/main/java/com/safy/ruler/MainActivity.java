package com.safy.ruler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView scaleTv = (TextView) findViewById(R.id.scale);
        RulerParent ruler = (RulerParent) findViewById(R.id.ruler);
        ruler.setRulerListener(new RulerParent.RulerListener() {
            @Override
            public void onChanged(String scale) {
                scaleTv.setText(String.format("%s厘米",scale));
            }
        });
    }
}
