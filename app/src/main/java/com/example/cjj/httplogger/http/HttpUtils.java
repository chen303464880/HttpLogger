package com.example.cjj.httplogger.http;

import com.example.cjj.httplogger.application.MyApplication;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 16:24.
 */
public class HttpUtils {

    public static Api getInstance() {
        return SingletonHolder.INSTANCE.create(Api.class);
    }

    public static OkHttpClient getOkHttpClient() {
        return MyApplication.HTTP_CLIENT;
    }


    private static class SingletonHolder {
        private static final Retrofit INSTANCE = create();

        private static Retrofit create() {
            return new Retrofit.Builder().baseUrl("http://ip.taobao.com/")
                    .client(getOkHttpClient())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
    }

}
