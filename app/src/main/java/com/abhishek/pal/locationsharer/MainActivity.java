package com.abhishek.pal.locationsharer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //private Handler mHandler = new Handler();
    private static final String TAG = MainActivity.class.getSimpleName();
    final int[] progressStatus = {0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Handler handler = new Handler();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv = (TextView) findViewById(R.id.tv);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setScaleY(3f);

                /*
                    A Thread is a concurrent unit of execution. It has its own call stack for
                    methods being invoked, their arguments and local variables. Each application
                    has at least one thread running when it is started, the main thread,
                    in the main ThreadGroup. The runtime keeps its own threads
                    in the system thread group.
                */
        // Start the lengthy operation in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus[0] < 100){
                    // Update the progress status
                    progressStatus[0] +=1;

                    // Try to sleep the thread for 20 milliseconds
                    try{
                        Thread.sleep(20);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pb.setProgress(progressStatus[0]);
                            // Show the progress on TextView
                            tv.setText(progressStatus[0] +"%");
                            Log.e(TAG,"Progress "+progressStatus[0]);

                        }
                    });

                    try {
                        // Sleep for 200 milliseconds.
                        //Just to display the progress slowly
                        Thread.sleep(2);
                        if (progressStatus[0] == 100){
                            Intent i = new Intent(MainActivity.this, SignInActivity.class);
                            startActivity(i);
                            break;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

//        Intent intent = new Intent(this, SignInActivity.class);
//        startActivity(intent);

    }
    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"In onStart()");

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"In onResume()");
//        mHandler.postDelayed(new Runnable() {
//            public void run() {
//                if (progressStatus[0] == 100)
//                    startActivity(intent);
//            }
//        }, 2000);
//
    }
}
