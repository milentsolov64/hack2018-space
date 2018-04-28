package tsventine.stamat;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Main2Activity extends AppCompatActivity {

    Socket socket=MainActivity.socket;
    PrintWriter out = MainActivity.out;
    BufferedReader in = MainActivity.in;
    int progressServo2 = 55;
    int progressServo6 = 97;
    int progressServo3 = 55;

    public class sendMessage extends AsyncTask<String, Void,String > {

        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            System.out.println("Pochvam");
        }

        protected String doInBackground(String... strings) {
            out.println(strings[0]);
            String response;
            try
            {
                response = in.readLine();
                if (response == null || response.equals(""))
                {
                    //System.exit(0);
                }
            } catch (IOException ex)
            {

                response = "Error: " + ex;
            }
            return response;
        }

        protected void onPostExecute(String r) {
            // This method is executed in the UIThread
            // with access to the result of the long running task
            TextView text=(TextView)findViewById(R.id.output2);
            text.setText(r);

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button back=(Button)findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent start=new Intent(Main2Activity.this,MainActivity.class);
                start.putExtra("S2", progressServo2);
                startActivity(start);
                finish();
            }
        });

        final SeekBar servo3=(SeekBar) findViewById((R.id.servo3));
        final SeekBar servo4=(SeekBar) findViewById((R.id.servo2));
        servo4.setRotation(-90);
        final SeekBar servo2=(SeekBar) findViewById((R.id.servo2));
        final SeekBar servo6=(SeekBar) findViewById((R.id.servo6));

        servo3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progressServo3 = progresValue;
                servo3.setProgress(progressServo3);
                new sendMessage().execute(String.valueOf("S3"+(progressServo3-55)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        servo4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                servo4.setProgress(progress);
                new sendMessage().execute(String.valueOf("S4"+(70-progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        servo2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progressServo2 = progresValue;
                servo2.setProgress(progressServo2);
                new sendMessage().execute(String.valueOf("S2"+(progressServo2-55)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        servo6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progressServo6 = progresValue;
                servo6.setProgress(progressServo6);
                new sendMessage().execute(String.valueOf("S6"+(progressServo6-97)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
    }
}
