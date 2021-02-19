package com.example.recorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; //request permissions
import androidx.core.content.ContextCompat; //check self permissions

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Environment; //get storage path

import android.os.Bundle; //savedInstanceState which caches data needed to reload the state of a UI controller

//UI widgets
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException; //for logging
import java.util.Random; //to generate file names

//necessary permissions
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {
    //UI
    ImageButton buttonStart;
    ImageButton buttonStop;
    ImageButton buttonPlayLastRecordAudio;
    Button buttonStopPlayingRecording;

    //two android libraries
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    //for file saving
    String AudioSavePathInDevice = null;
    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP"; //our dictionary of symbols for file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init UI
        setContentView(R.layout.activity_main);
        buttonStart = findViewById(R.id.record);
        buttonStop = findViewById(R.id.stop);
        buttonPlayLastRecordAudio = findViewById(R.id.play);
        buttonStopPlayingRecording = findViewById(R.id.reset);

        //set components that should not be allowed to play yet to false
        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

        random = new Random();

        //RECORD
        buttonStart.setOnClickListener(view -> {

            //if permission granted record, else ask for permission
            if(checkPermission()) {
                //set external path to save files at, AudioSavePathInDevice is global for mediaPlayer
                //just random 6 letters, can make it longer (replace 5)
                AudioSavePathInDevice =
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                CreateRandomAudioFileName(5) + "AudioRecording.3gp";


                //helps set audio file format, encoding, source, file path,
                mediaRecorder=new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //mobile mp4
                mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB); //The Adaptive Multi-Rate audio codec is an audio compression format optimized for speech coding.
                mediaRecorder.setOutputFile(AudioSavePathInDevice);

                try {
                    mediaRecorder.prepare(); // Instructs the recorder to prepare to begin playback.
                    mediaRecorder.start();  //Starts the playback.
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //enable buttons accordingly
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonPlayLastRecordAudio.setEnabled(false);

                //notify that recording started
                Toast.makeText(MainActivity.this, "Recording started",
                        Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }

        });

        //STOP
        buttonStop.setOnClickListener(view -> {
            mediaRecorder.stop();  //stops the playback.
            mediaRecorder.release(); //releases resources once done recording

            //UI
            buttonStop.setEnabled(false);
            buttonPlayLastRecordAudio.setEnabled(true);
            buttonStart.setEnabled(true);
            buttonStopPlayingRecording.setEnabled(false);

            //notification
            Toast.makeText(MainActivity.this, "Recording Completed",
                    Toast.LENGTH_LONG).show();
        });

        //PLAY
        buttonPlayLastRecordAudio.setOnClickListener(view -> {

            //UI
            buttonStop.setEnabled(false);
            buttonStart.setEnabled(false);
            buttonStopPlayingRecording.setEnabled(true);
            //buttonPlayLastRecordAudio.setEnabled(false);


            //done with recorder, next is to read recorder media
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //start playing audio file
            mediaPlayer.start();

            //notification
            Toast.makeText(MainActivity.this, "Recording Playing",
                    Toast.LENGTH_LONG).show();
        });

        //RESET
        buttonStopPlayingRecording.setOnClickListener(view -> {
            //UI
            buttonStop.setEnabled(false);
            buttonStart.setEnabled(true);
            buttonStopPlayingRecording.setEnabled(false);
            buttonPlayLastRecordAudio.setEnabled(true);

            //stop playing, reset recorder
            mediaPlayer.stop();
            mediaPlayer.release();  //To be called when the player instance is no longer needed. This method ensures that any resources held by the player are released.
        });

    }

    //example: 'FJFFCA'
    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));
            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 1);
    }

    //to have toasts related to permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean StoragePermission = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                boolean RecordPermission = grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED;

                if (StoragePermission && RecordPermission) {
                    Toast.makeText(MainActivity.this, "Permission Granted",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //recheck permission just in case it has been recalled
    public boolean checkPermission() {
        int result_s = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result_r = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result_s == PackageManager.PERMISSION_GRANTED &&
                result_r == PackageManager.PERMISSION_GRANTED;
    }

}