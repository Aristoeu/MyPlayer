package com.learn.lister.pagerslide.Model;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Upload implements MusicModel {
    public void upload(String m, final OnMusicListener onMusicListener){
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("keyword","henry-trap-中文版")
                .add("type","song")
                .add("pageSize","1")
                .add("page","0")
                .build();

        final Request request = new Request.Builder()
                .url("https://v1.itooi.cn/netease/search")
                .post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("<<<>>>","<<<<e="+e);
                onMusicListener.onFailed();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    String d = response.body().string();
                    Log.d("<<<>>>","<<<<d="+d);
                    onMusicListener.onSuccess(d);
                }
                else onMusicListener.onError();
            }
        });
    }
}
