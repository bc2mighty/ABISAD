package com.example.appforblind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateContact extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private String data = "What is the name of the contact you want to save";
    private String keeper = "";
    private EditText editTextName, editTextPhone;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextToSpeech textToSpeech;

    private ProgressDialog progressDialog;

    private static final int TTS_ENGINE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);

        progressDialog = new ProgressDialog(this);
    }

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
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(CreateContact.this);
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
                    if(keeper.contains("home")){
                        Intent intent = new Intent(CreateContact.this, MainActivity.class);
                        startActivity(intent);
                    } else if(keeper.contains("contact")){
                        Intent intent = new Intent(CreateContact.this, Contact.class);
                        startActivity(intent);
                    } else if(keeper.contains("name")){
                        String words[] = keeper.split(" ");
                        if(words.length > 1){
                            editTextName.setText(words[1]);
                        }
                    } else if(keeper.contains("read") || keeper.contains("number")){
                        CreateContact.this.setData("The content of the number currently is: " +  editTextPhone.getText().toString());
                        performSpeech();
                    } else if(keeper.contains("empty")){
                        editTextPhone.setText("");
                        data = "Number cleared successfully.";
                        performSpeech();
                    } else if(keeper.contains("save")){
                        createContact();
                    }else{
                        if(keeper.matches("[0-9]+")){
                            editTextPhone.setText(editTextPhone.getText().toString() + keeper);
                        }
                    }
                    Toast.makeText(CreateContact.this, "Results - " + keeper, Toast.LENGTH_LONG).show();
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

    public void createContact(){
        progressDialog.setMessage("Creating Contact");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG);
                        }catch (JSONException e){
                            e.printStackTrace();
                            Log.e("JSONObject",e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "This is from error", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("full_name",editTextName.getText().toString());
                params.put("phone_number", editTextPhone.getText().toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        Log.i("Battery", data);
        this.data = data;
    }
}
