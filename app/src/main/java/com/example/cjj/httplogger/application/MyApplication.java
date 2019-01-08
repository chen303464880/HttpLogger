package com.example.cjj.httplogger.application;

import android.app.Application;
import android.util.Log;

import com.cjj.httplogger.HttpLogger;
import com.example.cjj.httplogger.BuildConfig;
import com.lzy.okgo.OkGo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 16:23.
 */
public class MyApplication extends Application {

    public static OkHttpClient HTTP_CLIENT;


    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLogger interceptor = new HttpLogger.Builder()
                    //logcat输出的tag
                    .setTag("TAG")
                    //logcat输出的级别
                    .setLevel(Log.INFO)
                    //返回的结果的最大行数,超过将折叠
                    .setMaxLines(30)
                    //将当前位置传递给Interceptor的HeaderName
                    .setHeaderName("location")
                    //logcat检测到包含此字符串的行将被折叠,默认为"\t\t\t at %s "最后一个空格为\#u00A0的空格
                    //修改之后需要在File->Settings->Editor->General->Console中添加相应的字段
                    //.setContain("HttpLogger")
                    .build();
            builder.addInterceptor(interceptor);
        }
        HTTP_CLIENT = builder.build();
        OkGo.getInstance().init(this).setOkHttpClient(HTTP_CLIENT);
    }
}
