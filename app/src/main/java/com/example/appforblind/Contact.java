package com.example.appforblind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telecom.Call;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appforblind.myadapater.MyAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Contact extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    ListView listView;
    ArrayList<Integer> idImages;
    ArrayList<String> nameList;
    ArrayList<String> numberList;
    MyAdapter myAdapter;

    private TextToSpeech textToSpeech;
    private static final int TTS_ENGINE_REQUEST = 101;

    private String data = "Welcome to Contact Section. Say Create to add a new contact. . Say delete to delete a number and Call to call a number. . ";
    private String keeper = "", other_data = ". . The names on your current contacts are: .  ";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        listView = (ListView) findViewById(R.id.listView);
        idImages = new ArrayList<>();
        nameList = new ArrayList<>();
        numberList = new ArrayList<>();

        idImages = getList();
//        nameList = getNameList();
//        numberList = getNumberList();
        loadContacts();

        for(int i = 0;i < nameList.size(); i++){
            other_data += ", " + nameList.get(i);
        }

//        data += other_data;

        myAdapter = new MyAdapter(Contact.this, idImages, nameList, numberList);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phone = numberList.get(position);
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null));

                if (ContextCompat.checkSelfPermission(Contact.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                    startActivity(intent);
                }else{
                    ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });

        performSpeech();
    }

    public ArrayList<Integer> getList() {
        idImages = new ArrayList<>();
        idImages.add(R.drawable.img1);
        idImages.add(R.drawable.img2);
        idImages.add(R.drawable.img3);
        idImages.add(R.drawable.img4);
        idImages.add(R.drawable.img5);
        return idImages;
    }

    public ArrayList<String> getNumberList() {
        numberList = new ArrayList<>();

        /*
        try{
            String url_response = "{\"message\":\"All User data gotten successfully!\",\"data\":[{\"_id\":\"5da09e81de3748248fcc4e52\",\"phone_number\":\"0708\",\"full_name\":\"fever\",\"createdAt\":\"2019-10-11T15:23:45.714Z\",\"__v\":0},{\"_id\":\"5da3d3f629b8a7503da8dc4f\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:38.167Z\",\"__v\":0},{\"_id\":\"5da3d3f729b8a7503da8dc50\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:39.432Z\",\"__v\":0}]}";
            JSONObject jsonObject = new JSONObject(url_response);

            Toast.makeText(getApplicationContext(), jsonObject.getString("data"), Toast.LENGTH_LONG).show();

            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Log.i("ARRAY_ARR", jsonArray.toString());
            Log.i("ARRAY_MES", jsonObject.getString("message"));
            for (int i = 0;i < jsonArray.length(); i++){
                JSONObject object = jsonArray.getJSONObject(i);
                numberList.add(object.getString("phone_number"));
                Log.i("STRING_NAME", object.getString("full_name"));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        */
        numberList.add("09053316078");
        numberList.add("081019819134");
        numberList.add("07082912358");
        numberList.add("08096616358");
        numberList.add("08130908090");
        return numberList;
    }

    public ArrayList<String> getNameList(){
        nameList = new ArrayList<>();

        /*

        try{
            String url_response = "{\"message\":\"All User data gotten successfully!\",\"data\":[{\"_id\":\"5da09e81de3748248fcc4e52\",\"phone_number\":\"0708\",\"full_name\":\"fever\",\"createdAt\":\"2019-10-11T15:23:45.714Z\",\"__v\":0},{\"_id\":\"5da3d3f629b8a7503da8dc4f\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:38.167Z\",\"__v\":0},{\"_id\":\"5da3d3f729b8a7503da8dc50\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:39.432Z\",\"__v\":0}]}";
            JSONObject jsonObject = new JSONObject(url_response);

            Toast.makeText(getApplicationContext(), jsonObject.getString("data"), Toast.LENGTH_LONG).show();

            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Log.i("ARRAY_ARR", jsonArray.toString());
            Log.i("ARRAY_MES", jsonObject.getString("message"));
            for (int i = 0;i < jsonArray.length(); i++){
                JSONObject object = jsonArray.getJSONObject(i);
                nameList.add(object.getString("full_name"));
                Log.i("STRING_NAME", object.getString("full_name"));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_ALL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

//                            String url_response = "{\"message\":\"All User data gotten successfully!\",\"data\":[{\"_id\":\"5da09e81de3748248fcc4e52\",\"phone_number\":\"0708\",\"full_name\":\"fever\",\"createdAt\":\"2019-10-11T15:23:45.714Z\",\"__v\":0},{\"_id\":\"5da3d3f629b8a7503da8dc4f\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:38.167Z\",\"__v\":0},{\"_id\":\"5da3d3f729b8a7503da8dc50\",\"phone_number\":\"07\",\"full_name\":\"favour\",\"createdAt\":\"2019-10-14T01:48:39.432Z\",\"__v\":0}]}";
//                            JSONObject jsonObject = new JSONObject(url_response);

                            Toast.makeText(getApplicationContext(), jsonObject.getString("data"), Toast.LENGTH_LONG).show();

                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Log.i("ARRAY_ARR", jsonArray.toString());
                            Log.i("ARRAY_MES", jsonObject.getString("message"));
                            for (int i = 0;i < jsonArray.length(); i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERR_RESPONSE",error.getMessage());
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        */

        nameList.add("Michael");
        nameList.add("Frank");
        nameList.add("John");
        nameList.add("Oscar");
        nameList.add("Hazard");
        return nameList;
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

    public void speechActions(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(Contact.this);
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
                        Contact.this.setData("Welcome to Contact Section. Say Create to add a new contact. . Say delete to delete a number and Call to call a number");
                        performSpeech();
                    } else if(keeper.contains("create")){
                        Intent intent = new Intent(Contact.this, CreateContact.class);
                        startActivity(intent);
                    } else if(keeper.contains("home")){
                        Intent intent = new Intent(Contact.this, MainActivity.class);
                        startActivity(intent);
                    } else if(keeper.contains("back")){
                        Intent intent = new Intent(Contact.this, MainActivity.class);
                        startActivity(intent);
                    } else{
                        String words[] = keeper.split(" ");
                        if(words.length > 1 && nameList.indexOf(words[1]) != -1){
                            if (keeper.contains("edit")){
                                
                            }
                            String phone = numberList.get(nameList.indexOf(words[1]));
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null));

                            if (ContextCompat.checkSelfPermission(Contact.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                                startActivity(intent);
                            }else{
                                ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "Person Requested For Calling Not Found: ", Toast.LENGTH_LONG).show();
                        }
                    }
                    Toast.makeText(Contact.this, "Results - " + keeper, Toast.LENGTH_LONG).show();
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

    public void givePhonePermission(){
        if (ContextCompat.checkSelfPermission(Contact.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
        }else{
            ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    public void loadContacts(){
        givePhonePermission();

        StringBuilder builder = new StringBuilder();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if(cursor.getCount() > 0){
            int i = 0;
            while (cursor.moveToNext()){
                if (i > 100) break;
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                nameList.add(name);

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if(hasPhoneNumber > 0){
                    Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    if (cursor2.getCount() > 0){
                        int k = 0;
                        while (cursor2.moveToNext()){
                            if(k > 1) break;
                            String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(phoneNumber).append("\n\n");
                            numberList.add(phoneNumber);
                            k++;
                        }
                    }else{
                        numberList.add("000");
                    }
                    cursor2.close();
                }else{
                    numberList.add("000");
                }
                i++;
            }
            cursor.close();
//            Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();
        }
        Log.i("TOTAL_NUM", "" + numberList.size());
        Log.i("TOTAL_NAME", "" + nameList.size());
    }
}
