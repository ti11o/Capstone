package uz.music.capstone.IndexBottomSheetFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


import uz.music.capstone.CustomAdapterForRecyclerItem;
import uz.music.capstone.Genre;
import uz.music.capstone.ListedMusicsActivity;
import uz.music.capstone.Playlist;
import uz.music.capstone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import uz.music.capstone.json.JSONParserGenres;
import uz.music.capstone.json.JSONParserPlaylists;
import uz.music.capstone.profile.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class HomeFragment extends Fragment {

    @BindView(R.id.recyclerViewTopPlaylist) RecyclerView topPlaylistrecyclerView;
    @BindView(R.id.recyclerViewTopGenres) RecyclerView topGenresrecyclerView;
    @BindView(R.id.recyclerViewDiscover) RecyclerView DiscoverrecyclerView;

    private boolean parsing_playlsts = false, parsing_genres = false;

    ArrayList musicNames = new ArrayList<>(Arrays.asList("This Is The Name", "This Is The Name", "This Is The Name", "Music 4", "Music 5", "Music 6", "Music 7"));

    ArrayList artistNames = new ArrayList<>(Arrays.asList("Dua Lipa", "Dua Lipa", "Dua Lipa", "Artist 4", "Artist 5", "Artist 6", "Artist 7"));

    ArrayList musicImages = new ArrayList<>(Arrays.asList(R.drawable.a6, R.drawable.a9, R.drawable.a12,
            R.drawable.a19, R.drawable.a2, R.drawable.a4, R.drawable.a12 ));


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        CustomAdapterForRecyclerItem customAdapterRecycler = new CustomAdapterForRecyclerItem(getContext(), musicNames,artistNames, musicImages, false);
//
//        topPlaylistrecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        topPlaylistrecyclerView.setAdapter(customAdapterRecycler);
//
//
//        topGenresrecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        topGenresrecyclerView.setAdapter(customAdapterRecycler);
//
//        DiscoverrecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        DiscoverrecyclerView.setAdapter(customAdapterRecycler);

        SharedPreferences sp = getActivity().getSharedPreferences(User.FILE_PREFERENCES, Context.MODE_PRIVATE);
        String json_playlists = sp.getString(User.KEY_JSON_PLAYLISTS, "");
        String json_genres = sp.getString(User.KEY_JSON_GENRES, "");

        if(json_playlists != "" && json_genres != ""){
            parsePlaylists(json_playlists);
            parseGenres(json_genres);
        }else{
            parsing_playlsts = true;
            new GetJson().execute("https://moozee.pythonanywhere.com/top-playlists/");
            parsing_genres = true;
            new GetJson().execute("https://moozee.pythonanywhere.com/top-genres/");
        }

        
    }


    private void parsePlaylists(String result){
        JSONParserPlaylists parser = new JSONParserPlaylists(result);
        ArrayList<Playlist> playlists = parser.getPlaylistsArray();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> photoLinks = new ArrayList<String>();
        for(int i = 0; i < playlists.size(); i++){
            names.add(playlists.get(i).getName());
            descriptions.add(playlists.get(i).getDescription());
            photoLinks.add(playlists.get(i).getPhotoLink());
        }
        CustomAdapterForRecyclerItem customAdapterRecycler = new CustomAdapterForRecyclerItem(getContext(),
                names, descriptions, photoLinks, false, 1);
        topPlaylistrecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topPlaylistrecyclerView.setAdapter(customAdapterRecycler);
    }

    private void parseGenres(String result){
        JSONParserGenres parser = new JSONParserGenres(result);
        ArrayList<Genre> playlists = parser.getGenresArray();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> photoLinks = new ArrayList<String>();
        for(int i = 0; i < playlists.size(); i++){
            names.add(playlists.get(i).getName());
            photoLinks.add(playlists.get(i).getPhotoLink());
        }
        CustomAdapterForRecyclerItem customAdapterRecycler = new CustomAdapterForRecyclerItem(getContext(),
                names, null, photoLinks, false, 2);
        topGenresrecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topGenresrecyclerView.setAdapter(customAdapterRecycler);
    }

    private class GetJson extends AsyncTask<String, String, String> {
        private ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "Downloading JSON data", Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                SharedPreferences sp = getActivity().getSharedPreferences(User.FILE_PREFERENCES, Context.MODE_PRIVATE);
                String token = sp.getString(User.KEY_TOKEN, "");
                connection.setRequestProperty("Authorization", "Token " + token);

                connection.setRequestMethod("POST");
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
            Toast.makeText(getActivity(), "Download process finish", Toast.LENGTH_SHORT).show();

            //Parsing the JSON starts --------------------------------------
            //parseJson(result);

            SharedPreferences shp = getActivity().getSharedPreferences(User.FILE_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shp.edit();
            if(parsing_playlsts){
                parsePlaylists(result);
                editor.putString(User.KEY_JSON_PLAYLISTS, result);
                editor.commit();
                parsing_playlsts = false;
            }else if(parsing_genres){
                parseGenres(result);
                editor.putString(User.KEY_JSON_GENRES, result);
                editor.commit();
                parsing_genres = false;
            }


            //Parsing the JSON ends --------------------------------------
        }
    }


}