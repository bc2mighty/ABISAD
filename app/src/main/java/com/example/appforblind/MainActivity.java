package com.example.appforblind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private String data = "Hi there. Welcome to Android App for the visually impaired. Let me walk you through the instructions quickly. At the top left side is where you can manage your contacts. At the top right corner is your text Editor. At the bottom left side is where you can get your phone's battery level. And at the bottom right is where you can get the date and time. Click once to confirm your selection and touch for long to navigate to selected option.";
    private String keeper = "";

    private static final int TTS_ENGINE_REQUEST = 101;
    private TextToSpeech textToSpeech;
    private CardView batteryLevelCard, dateAndTimeCard, textEditorCard, contactMgtCard;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private BroadcastReceiver batteryInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            MainActivity.this.setData("Your Battery Level is: " + level + "%");
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            Toast.makeText(this, "Key Volume pressed", Toast.LENGTH_LONG).show();
            keeper = "";
            speechActions();
//            speechRecognizer.startListening(speechRecognizerIntent);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        performSpeech();

        batteryLevelCard = (CardView) findViewById(R.id.batteryLevelCard);
        dateAndTimeCard = (CardView) findViewById(R.id.dateAndTimeCard);
        contactMgtCard = (CardView) findViewById(R.id.contactMgtCard);
        textEditorCard = (CardView) findViewById(R.id.textEditorCard);

        batteryLevelCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.registerReceiver(MainActivity.this.batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                performSpeech();
                Log.e("BatteryLevel", data);
            }
        });

        dateAndTimeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat time = new SimpleDateFormat("hh:mm:ss");
                MainActivity.this.setData("Today's date is: " + date.format(Calendar.getInstance().getTime()) + " and the time is " + time.format(Calendar.getInstance().getTime()));
                performSpeech();
            }
        });

        contactMgtCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Contact.class);
                startActivity(intent);
            }
        });

        textEditorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    public void performSpeech(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_ENGINE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TTS_ENGINE_REQUEST && resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
            textToSpeech = new TextToSpeech(this, this);
        }else{
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivity(installIntent);
        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int languageStatus = textToSpeech.setLanguage(Locale.US);
            if(languageStatus == TextToSpeech.LANG_MISSING_DATA || languageStatus == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(this, "Language Not Supported...", Toast.LENGTH_LONG).show();
            }else{
                int speechStatus = textToSpeech.speak(this.data, TextToSpeech.QUEUE_FLUSH, null);
                if(speechStatus == TextToSpeech.ERROR){
                    Toast.makeText(this, "Error while speaking...", Toast.LENGTH_LONG).show();
                }
            }
        }else {
            Toast.makeText(this, "Text to speech failed", Toast.LENGTH_LONG).show();
        }
    }

    public void speechActions(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                keeper = "";
            }

            @Override
            public void onBeginningOfSpeech() {
                keeper = "";
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                keeper = "";
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matchesFound != null){
                    keeper = matchesFound.get(0);
                    if (keeper.contains("battery")){
                        MainActivity.this.registerReceiver(MainActivity.this.batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        performSpeech();
                    } else if (keeper.contains("date") || keeper.contains("time")){
                        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
                        DateFormat time = new SimpleDateFormat("hh:mm:ss");
                        MainActivity.this.setData("Today's date is: " + date.format(Calendar.getInstance().getTime()) + " and the time is " + time.format(Calendar.getInstance().getTime()));
                        performSpeech();
                    } else if (keeper.contains("text") || keeper.contains("editor")){
                        Intent intent = new Intent(MainActivity.this, TextEditorActivity.class);
                        startActivity(intent);
                    } else if (keeper.contains("contact") || keeper.contains("management")){
                        Intent intent = new Intent(MainActivity.this,Contact.class);
                        startActivity(intent);
                    } else if(keeper.contains("instruction")){
                        MainActivity.this.setData("Hi there. Welcome to Android App for the blind. Let me walk you through the instructions quickly. At the top left side is where you can manage your contacts. At the top right corner is your text Editor. At the bottom left side is where you can get your phone's battery level. And at the bottom right is where you can get the date and time. Click once to confirm your selection and touch for long to navigate to selected option.");
                        performSpeech();
                    }
                    Toast.makeText(MainActivity.this, "Results - " + keeper, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        speechRecognizer.startListening(speechRecognizerIntent);
        keeper = "";
        speechRecognizer.stopListening();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        Log.i("Battery", data);
        this.data = data;
    }
}
