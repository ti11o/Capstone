package uz.music.capstone;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView listview1;
    private MusicAdapter adapter1;
    private MediaPlayer current_playing = null, mp = null;
    private Button btn_play_pause, btn_next, btn_prev;
    private SeekBar music_seek_bar;
    private Handler music_handler;
    private int music_paused_position;
    private ArrayList<Music> ordered_musics;
    private Music music_current = null, music_prev = null, music_next = null;
    private int music_current_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview1  = (ListView)findViewById(R.id.list_view); // listview of memo items

        adapter1 = new MusicAdapter();
        listview1.setAdapter(adapter1);
        btn_play_pause = (Button) findViewById(R.id.btn_pause);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_prev = (Button) findViewById(R.id.btn_prev);
        adapter1.notifyDataSetChanged();
        music_seek_bar = (SeekBar) findViewById(R.id.music_seek_bar);
        music_handler = new Handler();
        music_paused_position = -1;
        ordered_musics = new ArrayList<Music>();

        //get JSON and parse JSON starts ------------------------------------
        File jsons_file = new File("jsons.txt");
        if(jsons_file.exists()){
            String result = readFromFile();
            parseJson(result);
            Toast.makeText(MainActivity.this, "Json read from file", Toast.LENGTH_SHORT).show();
        }else{
            new GetJson().execute("http://192.168.173.1:8000/daily/?format=json");
        }
        //get JSON and parse JSON ends --------------------------------------

        /////////////////////////////////////////


        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                music_current_position = position;
                music_current = (Music) listview1.getItemAtPosition(music_current_position);
                Toast.makeText(MainActivity.this, music_current.getMusic_name(), Toast.LENGTH_SHORT).show();
                mp = MediaPlayer.create(MainActivity.this, Uri.parse(music_current.getLink()));
                if(current_playing != null){
                    current_playing.stop();
                }
                mp.start();
                current_playing = mp;
                music_next = ordered_musics.get((music_current_position + 1) % ordered_musics.size());
                if(music_current_position == 0){
                    music_prev = ordered_musics.get(ordered_musics.size() - 1);
                }else{
                    music_prev = ordered_musics.get(music_current_position - 1);
                }

            }
        });

        btn_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp != null){
                    if(music_paused_position == -1){
                        if(mp.isPlaying()) {
                            music_paused_position = mp.getCurrentPosition();
                            mp.pause();
                        }else{
                            mp.seekTo(music_paused_position);
                        }
                    }

                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_playing != null && music_next != null){
                    mp = MediaPlayer.create(MainActivity.this, Uri.parse(music_next.getLink()));
                    current_playing.stop();
                    mp.start();
                    current_playing = mp;
                    music_prev = music_current;
                    music_current = music_next;
                    music_next = ordered_musics.get((music_current_position + 1) % ordered_musics.size());
                }
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_playing != null && music_prev != null){
                    mp = MediaPlayer.create(MainActivity.this, Uri.parse(music_prev.getLink()));
                    current_playing.stop();
                    mp.start();
                    music_next = music_current;
                    music_current = music_prev;
                    if(music_current_position == 0){
                        music_prev = ordered_musics.get(ordered_musics.size() - 1);
                    }else{
                        music_prev = ordered_musics.get(music_current_position - 1);
                    }
                }

            }
        });
        //control seekbar starts -----------------------------------------
//        MainActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(mp != null){
//                    int music_current_position = mp.getCurrentPosition()/1000;
//                    music_seek_bar.setProgress(music_current_position);
//                }
//                music_handler.postDelayed(this, 1000);
//            }
//        });

//        music_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int seek_bar_position = 0;
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(fromUser){
//                    seek_bar_position = progress * 1000;
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if(mp != null){
//                    mp.seekTo(seek_bar_position);
//                }else{
//                    music_seek_bar.setProgress(0);
//                }
//
//            }
//        });

        //control seekbar ends -------------------------------------------

        ////////////////////////////////////////////////
    }


    //reading and writing JSON string to file starts ----------------------------------------
    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("jsons.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Writing Json to file", "File write failed: " + e.toString());
        }
    }


    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("jsons.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Reading Json from file", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Reading Json from file", "Can not read file: " + e.toString());
        }

        return ret;
    }
    //reading and writing JSON string to file ends ----------------------------------------

    private void parseJson(String result){
        JSONParser jsonParser = new JSONParser(result);
        ordered_musics = jsonParser.getMusicsArray();
        for(int i = 0; i < ordered_musics.size(); i++){
            adapter1.addItem(ordered_musics.get(i).getMusic_name());
        }
    }

    //class to GET and Parse from server starts ---------------------------------
    private class GetJson extends AsyncTask<String, String, String> {
        private ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Downloading JSON data", Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.e("Response: ", "> " + line);
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                Log.e("MafformedURLException", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "Download process finish", Toast.LENGTH_SHORT).show();

            //Parsing the JSON starts --------------------------------------
            parseJson(result);
            writeToFile(result);
            //Parsing the JSON ends --------------------------------------
        }
    }

    //class to GET and Parse from server ends ---------------------------------



}