package devnik.trancefestivalticker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;

/**
 * Created by nik on 12.03.2018.
 */

public class ErrorActivity extends AppCompatActivity {
    TextView error;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        setContentView(R.layout.activity_error);
        error = (TextView) findViewById(R.id.error);

        error.setText(getIntent().getStringExtra("error"));
        // Your mechanism is ready now.. In this activity from anywhere
        // if you get force close error it will be redirected to the CrashActivity.
    }
}
