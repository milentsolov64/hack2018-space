package tsventine.stamat;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static BufferedReader in = null;
    public static PrintWriter out = null;
    public static Socket socket = null;
    String serverAddress = "192.168.4.1"/*"192.168.1.112"*/;
    int port=2121;


    private class socketConnection extends AsyncTask<String, Void,Integer > {

        public socketConnection() {
            this.activity = activity;
            dialog = new ProgressDialog(MainActivity.this);
        }

        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        /** application context. */
        private ListActivity activity;

        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
            this.dialog.setMessage("Trying to Connect");
            this.dialog.show();
            System.out.println("Pochvam");
        }

        protected Integer doInBackground(String... strings) {
            try {
                socket = new Socket(serverAddress, port);
            } catch (IOException e) {
                return 1;
            }

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                return 2;
            }

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                return 3;
            }
            for (int i = 0; i < 3; i++)
            {
                try {
                    System.out.println(in.readLine() + "\n");
                } catch (IOException e) {
                    return 4;
                }
            }
            return 0;
        }

        protected void onPostExecute(Integer a) {
            // This method is executed in the UIThread
            // with access to the result of the long running task
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(a==0)Toast.makeText(MainActivity.this, ("Connected to Stamat"+serverAddress), Toast.LENGTH_LONG).show();
            else if(a==1) Toast.makeText(MainActivity.this, ("FACK SOCKET"+serverAddress), Toast.LENGTH_LONG).show();
            else if(a==2) Toast.makeText(MainActivity.this, "FACK IN", Toast.LENGTH_LONG).show();
            else if(a==3) Toast.makeText(MainActivity.this, "FACK OUT", Toast.LENGTH_LONG).show();
            else if(a==4) Toast.makeText(MainActivity.this, "FACK MY LIFE", Toast.LENGTH_LONG).show();

        }
    }




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
            TextView text=(TextView)findViewById(R.id.textView);
            text.setText(r);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Scanner sc=new Scanner(System.in);
        final String ggg="";

        // Make connection and initialize streams

        // Consume the initial welcoming messages from the server
        System.out.println("PREDI");
        AsyncTask task = new socketConnection().execute();
        System.out.println("SLED");


        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent start=new Intent(MainActivity.this,Main2Activity.class);
                startActivity(start);
            }
        });

        ToggleButton led = (ToggleButton) findViewById(R.id.ledButton);
        led.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    new sendMessage().execute("LEDON");
                } else {
                    // The toggle is disabled
                    new sendMessage().execute("LEDOFF");
                }
            }
        });


        ToggleButton glow = (ToggleButton) findViewById(R.id.glowButton);
        glow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    new sendMessage().execute("GLOWON");
                } else {
                    // The toggle is disabled
                    new sendMessage().execute("GLOWOFF");
                }
            }
        });

        Button reset = (Button) findViewById(R.id.resetButton);
        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new sendMessage().execute("RESET");
            }
        });

        final SeekBar sliderLR=(SeekBar) findViewById((R.id.leftRight));
        final SeekBar sliderFB=(SeekBar) findViewById((R.id.forwardBackward));
        sliderFB.setRotation(-90);
         sliderLR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                sliderLR.setProgress(progress);
                if(progress>100) {
                    new sendMessage().execute(String.valueOf("LR"+(progress-(progress*2)+100)));
                }
                else if(progress<100) {
                    new sendMessage().execute(String.valueOf("LR"+(progress+(progress*2)-100)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress=100;
                sliderLR.setProgress(progress);
                new sendMessage().execute(String.valueOf("LR0"));
            }

        });

        sliderFB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                sliderFB.setProgress(progress);
                    new sendMessage().execute(String.valueOf("FB"+(progress-80)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress=80;
                sliderFB.setProgress(progress);
                new sendMessage().execute(String.valueOf("FB0"));
            }

        });

        WebView web=(WebView)findViewById(R.id.webView);
        web.getSettings().setJavaScriptEnabled(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
        });

        web.loadUrl("http://172.24.1.1:25566");
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        //setContentView(web);
        /*
        for(;;)
        {
            ggg=sc.nextLine();
            out.println(ggg);
            String response;
            try
            {
                response = in.readLine();
                if (response == null || response.equals(""))
                {
                    System.exit(0);
                }
            } catch (IOException ex)
            {
                response = "Error: " + ex;
            }
            System.out.println(response);
        }*/

    }


}
