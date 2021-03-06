package uz.music.capstone.offline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import uz.music.capstone.Music;
import uz.music.capstone.MusicAdapter;
import uz.music.capstone.R;

/**
 * Created by Nemo on 11/12/2017.
 */

public class FragmentTracks extends Fragment {

    LinearLayout layout;
    TextView txt_name, txt_artist;
    ImageButton btn_prev, btn_play, btn_next;
    SeekBar seek_bar;
    ListView list_view;
    MusicPlayer musicPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_offline_tracks, container, false);
        MusicAdapter adapter = new MusicAdapter();
        layout = (LinearLayout) view.findViewById(R.id.container_offline_tracks);
        txt_name = (TextView)view.findViewById(R.id.txt_title_offline_tracks);
        txt_artist = (TextView)view.findViewById(R.id.txt_artist_offline_tracks);
        btn_prev = (ImageButton)view.findViewById(R.id.btn_prev_offline_tracks);
        btn_play = (ImageButton)view.findViewById(R.id.btn_pause_offline_tracks);
        btn_next = (ImageButton)view.findViewById(R.id.btn_next_offline_tracks);
        seek_bar = (SeekBar)view.findViewById(R.id.seek_bar_offline_tracks);
        musicPlayer = new MusicPlayer(getActivity());

        list_view = (ListView) view.findViewById(R.id.offline_track_list);
        list_view.setAdapter(adapter);
        final MusicPlayer musicPlayer = new MusicPlayer(getActivity());
        musicPlayer.getAllSongs(adapter, 0);


        musicPlayer.seekBarControl(seek_bar);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = (Music) list_view.getItemAtPosition(position);
                layout.setVisibility(View.VISIBLE);
                txt_name.setText(music.getMusic_name());
                txt_artist.setText(music.getArtist());
                musicPlayer.playMusic(list_view, position);
            }
        });
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.playPauseButtonAction();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.nextButtonAction();
            }
        });
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer.prevButtonAction();
            }
        });

        return view;
    }
}