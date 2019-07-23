package com.learn.lister.pagerslide.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.learn.lister.pagerslide.R;
import com.learn.lister.pagerslide.Service.MusicService;
import com.learn.lister.pagerslide.utils.MusicButton;
import com.learn.lister.pagerslide.utils.MusicList;
import com.learn.lister.pagerslide.utils.Music;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {
    private List<Music> listMusic;
    private static SeekBar seekBar;
    private MusicService.MyBinder musicControl;
    private MyConnection conn;
    private String TAG = "DetailActivity";
    private Button btn_pre;
    private Button btn_play;
    private Button btn_next;
    private MusicButton imageView;
    private TextView tv_title,tv_cur_time,tv_total_time;
    private static final int UPDATE_UI = 0;
    private String dd;
    MyReceiver myReceiver;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI:
                    updateUI();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        verifyStoragePermissions(this);
        listMusic = MusicList.getMusicData(getApplicationContext());
        seekBar=findViewById(R.id.sb);
        btn_play = findViewById(R.id.playStop);
        btn_pre = findViewById(R.id.pre);
        btn_next = findViewById(R.id.next);
        tv_title = findViewById(R.id.tv_title);
        tv_cur_time =findViewById(R.id.tv_cur_time);
        tv_total_time = findViewById(R.id.tv_total_time);
        imageView = findViewById(R.id.imageview);

        Intent intent = new Intent(this,MusicService.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);
        conn = new MyConnection();
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
        myReceiver = new MyReceiver(new Handler());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    musicControl.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("DetailActivity",seekBar.getProgress()+"<<<>>>");
                //musicControl.seekTo(seekBar.getProgress());
            }
        });
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(view);
            }
        });
        btn_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pre(view);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next(view);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.INVISIBLE);
            }
        });
        myReceiver = new MyReceiver(new Handler());
        IntentFilter itFilter = new IntentFilter();
        itFilter.addAction(MusicService.MAIN_UPDATE_UI);
        getApplicationContext().registerReceiver(myReceiver, itFilter);
//upload();

    }
    private void upload(String s){
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("keyword",s)
                .add("type","song")
                .add("pageSize","1")
                .add("page","0")
                .build();

        final Request request = new Request.Builder()
                .url("https://v1.itooi.cn/netease/search")
                .post(body).build();
        Call call = client.newCall(request);
        Log.d("<<<>>>","begin");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"<<<<>>>e="+e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    String d = response.body().string();
                    Log.d(TAG,"<<<<>>>d="+d);
                    dd=d;
                    if(d.length()>=100){
                        String url;

                        //Toast.makeText(DetailActivity.this,url,Toast.LENGTH_LONG).show();
                        (DetailActivity.this).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String str = dd;
                                String regex = "\"picUrl\":\"(.*)\",\"name\"";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(str);
                                while (matcher.find()) {
                                    imageView.setImageURL(matcher.group(1));
                                }
                                // 在这里执行你要想的操作 比如直接在这里更新ui或者调用回调在 在回调中更新ui
                            }
                        });

                    }
                }
            }
        });
    }
    //同步get请求

    private class MyConnection implements ServiceConnection {

        //This method will be entered after the service is started.
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.i(TAG, "::MyConnection::onServiceConnected");
            //Get MyBinder in service
            musicControl = (MusicService.MyBinder) service;
            //Update button text
            updatePlayText();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "::MyConnection::onServiceDisconnected");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Start the update UI bar after entering the interface
        if (musicControl != null) {
            handler.sendEmptyMessage(UPDATE_UI);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unbind from the service after exiting
        unbindService(conn);
        getApplicationContext().unregisterReceiver(myReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop the progress of the update progress bar
        handler.removeCallbacksAndMessages(null);
    }
    public class MyReceiver extends BroadcastReceiver {
        private final Handler handler;
        public MyReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            // Post the UI updating code to our Handler
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int play_pause = intent.getIntExtra(MusicService.KEY_MAIN_ACTIVITY_UI_BTN, -1);
                    int songid = intent.getIntExtra(MusicService.KEY_MAIN_ACTIVITY_UI_TEXT, -1);
                    tv_title.setText(listMusic.get(songid).music);
                    //imageView.setImageURL("http://p1.music.126.net/uKCPsSmDu4ffpnzc-h-5zA==/2488194813716715.jpg");
                    upload(listMusic.get(songid).music);

                    switch (play_pause) {
                        case MusicService.VAL_UPDATE_UI_PLAY:
                            btn_play.setText(R.string.pause);
                            imageView.play();
                            break;
                        case MusicService.VAL_UPDATE_UI_PAUSE:
                            btn_play.setText(R.string.play);
                            imageView.pause();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }
    private  final int REQUEST_EXTERNAL_STORAGE = 1;
    private  String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public  void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void play(View view) {
        Intent intent = new Intent(MusicService.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(MusicService.KEY_USR_ACTION,MusicService.ACTION_PLAY_PAUSE);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        updatePlayText();
    }

    public void next(View view) {
        Intent intent = new Intent(MusicService.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(MusicService.KEY_USR_ACTION,MusicService.ACTION_NEXT);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        updatePlayText();
    }

    public void pre(View view) {
        Intent intent = new Intent(MusicService.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(MusicService.KEY_USR_ACTION,MusicService.ACTION_PRE);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        updatePlayText();
    }

    public void updateUI(){

        //Set the maximum value of the progress bar
        int cur_time = musicControl.getCurrenPostion(), total_time = musicControl.getDuration();
        seekBar.setMax(total_time);
        //Set the progress of the progress bar
        seekBar.setProgress(cur_time);

        String str = musicControl.getName();
        tv_title.setText(str);
        tv_cur_time.setText(timeToString(cur_time));
        tv_total_time.setText(timeToString(total_time));

        updateProgress();

        //Update the UI bar every 500 milliseconds using Handler
        handler.sendEmptyMessageDelayed(UPDATE_UI, 500);
    }

    private String timeToString(int time) {
        time /= 1000;
        return String.format("%02d:%02d",time/60,time%60);
    }
    //Update progress bar
    private void updateProgress() {
        int currenPostion = musicControl.getCurrenPostion();
        seekBar.setProgress(currenPostion);
    }


    //Update button text
    public void updatePlayText() {
        if(MusicService.mlastPlayer!=null &&MusicService.mlastPlayer.isPlaying()){
            imageView.play();
            btn_play.setText(R.string.pause);
        }else{
            imageView.pause();
            btn_play.setText(R.string.play);
        }
    }
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        paint.setColor(color);

        // 以下有两种方法画圆,drawRounRect和drawCircle
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }


}
