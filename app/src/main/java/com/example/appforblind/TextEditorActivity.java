package com.example.appforblind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appforblind.myadapater.TextEditorAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class TextEditorActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private ProgressDialog progressDialog;

    private ListView listView;
    private ArrayList<String> titles;
    private ArrayList<String> contents;
    private TextEditorAdapter textEditorAdapter;

    private TextToSpeech textToSpeech;
    private static final int TTS_ENGINE_REQUEST = 101;

    private String data = "Welcome to Text Editor Section. Say Create to add a new note. . Say delete to delete a note and say edit to edit a note. . ";
    private String keeper = "", other_data = ". . The names on your current contacts are: .  ";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        listView = (ListView) findViewById(R.id.textEditorListView);
        progressDialog = new ProgressDialog(this);

        titles = new ArrayList<>();
        contents = new ArrayList<>();

        titles = getTitles();
        contents = getContents();

        textEditorAdapter = new TextEditorAdapter(TextEditorActivity.this, titles, contents);
        listView.setAdapter(textEditorAdapter);

        loadNotes();

        performSpeech();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editNoteAction(titles.get(position), contents.get(position));
                Intent intent = new Intent(TextEditorActivity.this, UpdateTextEditor.class);
                startActivity(intent);
            }
        });

    }

    private ArrayList<String> getContents(){
        contents = new ArrayList<>();
        contents.add("Twinkle, twinkle, little star. How I wonder what you are. Up above the world so high");
        contents.add("Like a diamond in the sky Twinkle, twinkle little star. How I wonder what you are");
        contents.add("When the blazing sun is gone When he nothing shines upon. Then you show your little light");
        contents.add("Twinkle, twinkle, all the night Twinkle, twinkle, little star. How I wonder what you are");
        return contents;
    }

    private ArrayList<String> getTitles(){
        titles = new ArrayList<>();
        titles.add("Twinkle twinkle");
        titles.add("Like a diamond");
        titles.add("When the blazing sun");
        titles.add("All the night");
        return titles;
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

    public void speechActions(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(TextEditorActivity.this);
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
                    if(keeper.contains("instruction")){
                        TextEditorActivity.this.setData("Welcome to Text Editor Section. Say Create to add a new note. . Say delete to delete a note and say edit to edit a note. . ");
                        performSpeech();
                    } else if(keeper.contains("create")){
                        Intent intent = new Intent(TextEditorActivity.this, CreateDocument.class);
                        startActivity(intent);
                    } else if(keeper.contains("back")){
                        Intent intent = new Intent(TextEditorActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if(keeper.contains("home")){
                        Intent intent = new Intent(TextEditorActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if(keeper.contains("edit")){
                        String[] words = keeper.split(" ");
                        Log.i("TAG_CHECKER_STRING","Yes: " + words.length);
                        if (words.length > 1){
                            for (int i = 0; i < titles.size(); i++){
                                Boolean checker = titles.get(i).contains(words[1]);

                                if (checker){
                                    Toast.makeText(getApplicationContext(), "Checker is true", Toast.LENGTH_SHORT).show();
                                    Log.i("TAG_CHECKER_STRING",titles.get(i));
                                    editNoteAction(titles.get(i), contents.get(i));
                                    Intent intent = new Intent(TextEditorActivity.this, UpdateTextEditor.class);
                                    startActivity(intent);
                                    break;
                                }
                            }
                        }
                    } else{

                        if(keeper.contains("read") || keeper.contains("note")){
                            String string = "These are the notes. .";
                            for (int x = 0;x < titles.size(); x++){
                                Log.i("DOC_VAL", "" + x);
                                string += "This is the content of: " + titles.get(x) + ". . . . . ." + contents.get(x) + ". . . . . . . . . . .  . . . .";
                            }
                            TextEditorActivity.this.setData(string);
                            performSpeech();
                        }
                    }
                    Toast.makeText(TextEditorActivity.this, "Results - " + keeper, Toast.LENGTH_LONG).show();
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

    public void editNoteAction(String title, String content){
        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", title);
        editor.putString("content", content);
        editor.apply();
    }

    public void loadNotes(){

        progressDialog.setMessage("Loading Your Notes...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_DOCUMENTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("RESPONSE", response);
                        progressDialog.dismiss();
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            Log.i("ARRAY_ARR", jsonArray.toString());

                            for (int i = 0;i < jsonArray.length(); i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                TextEditorActivity.this.titles.add(object.getString("title"));
                                Log.i("STRING_TITLES", object.getString("title"));
                                Log.i("TITLES_LAST_ITEM", titles.get(titles.size() - 1));

                                TextEditorActivity.this.contents.add(object.getString("document"));
                                Log.i("STRING_CONTENT", object.getString("document"));
                                Log.i("TITLES_LAST_ITEM", contents.get(contents.size() - 1));
                            }

                            TextEditorActivity.this.textEditorAdapter = new TextEditorAdapter(TextEditorActivity.this, titles, contents);
                            TextEditorActivity.this.listView.setAdapter(textEditorAdapter);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR_RESPONSE", error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void performSpeech(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_ENGINE_REQUEST);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        Log.i("Battery", data);
        this.data = data;
    }
}
