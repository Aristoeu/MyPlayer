package com.learn.lister.pagerslide.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.learn.lister.pagerslide.R;
import com.learn.lister.pagerslide.Service.MusicService;
import com.learn.lister.pagerslide.activity.DetailActivity;
import com.learn.lister.pagerslide.activity.MainActivity;
import com.learn.lister.pagerslide.utils.MusicList;
import com.learn.lister.pagerslide.utils.Music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aristo in 2019
 */

public class Fragment1 extends BaseFragment {
    private ListView mListView;
    private List<Music> list;
    public MyAdapter adapter;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    View view;
    TextView textView;
    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment1, null);
        return view;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment1,null);
        textView=view.findViewById(R.id.localMusic);
        mListView =view.findViewById(R.id.listView);
        return view;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 2);
        }else
            inits();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(),list.get(i).path,Toast.LENGTH_LONG).show();
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.reset(); // 停止播放
                }
                /*try {
                    init(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                //Intent intent1 = new Intent();
                //LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent1);
                Bundle bundle= new Bundle();
                bundle.putInt("position",i);
                Intent intent=new Intent();
                intent.putExtras(bundle);
                intent.putExtra("position",i);
                intent.setClass(getActivity(), DetailActivity.class);
                startActivity(intent);

            }
        });
    }
    public void init(int i) throws IOException {
        File file = new File(list.get(i).path);
        mediaPlayer.setDataSource(file.getPath()); // 指定音频文件的路径
        mediaPlayer.prepare(); // 让MediaPlayer进入到准备状态
        mediaPlayer.start();
    }
    private void inits() {

        list = new ArrayList<>();
        //把扫描到的音乐赋值给list
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        //assert cursor != null;
        //Toast.makeText(this,cursor.toString(),Toast.LENGTH_LONG).show();
        list = MusicList.getMusicData(getContext());
        if (list==null)
            Toast.makeText(getContext(),"null",Toast.LENGTH_LONG).show();
        //else Toast.makeText(this,list.get(79).path,Toast.LENGTH_LONG).show();

        adapter = new MyAdapter(list);
        mListView.setAdapter(adapter);
    }

    public class MyAdapter extends BaseAdapter {
        private Context context;
        private List<Music> list;

        public MyAdapter(List<Music> list) {
            this.context = getContext();
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                //引入布局
                view = View.inflate(context, R.layout.item_music_listview, null);
                //实例化对象
                holder.music = (TextView) view.findViewById(R.id.item_mymusic_music);
                holder.singer = (TextView) view.findViewById(R.id.item_mymusic_singer);
                holder.duration = (TextView) view.findViewById(R.id.item_mymusic_duration);
                holder.position = (TextView) view.findViewById(R.id.item_mymusic_postion);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            //给控件赋值
            holder.music.setText(list.get(i).music.toString());
            holder.singer.setText(list.get(i).singer.toString());
            //时间需要转换一下
            int duration = list.get(i).duration;
            String time = MusicList.formatTime(duration);
            holder.duration.setText(time);
            holder.position.setText(i + 1 + "");

            return view;
        }

        class ViewHolder {
            TextView music;//歌曲名
            TextView singer;//歌手
            TextView duration;//时长
            TextView position;//序号

        }

    }
}
