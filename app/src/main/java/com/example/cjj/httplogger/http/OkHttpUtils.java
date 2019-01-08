package com.example.cjj.httplogger.http;

import android.support.annotation.NonNull;

import com.cjj.httplogger.CodeUtils;
import com.example.cjj.httplogger.BuildConfig;
import com.example.cjj.httplogger.bean.ResultBase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.exception.HttpException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * @author CJJ
 * Created by CJJ on 2019/1/4 17:02.
 */
public class OkHttpUtils {

    private static final int GET = 1;
    private static final int POST = 2;
    private static final int PUT = 3;
    private static final int DELETE = 4;

    /**
     * gson对象
     */
    private static final Gson GSON = new Gson();

    private static final Type STRING_TYPE = new TypeToken<String>() {
    }.getType();

    private String url;
    private int type;

    public ConcurrentHashMap<String, String> urlParams = new ConcurrentHashMap<>();

    private OkHttpUtils(String url, int type) {
        this.type = type;
        this.url = url;
    }

    public static OkHttpUtils get(String url) {
        return new OkHttpUtils(url, GET);
    }

    public static OkHttpUtils post(String url) {
        return new OkHttpUtils(url, POST);
    }

    public static OkHttpUtils put(String url) {
        return new OkHttpUtils(url, PUT);
    }

    public static OkHttpUtils delete(String url) {
        return new OkHttpUtils(url, DELETE);
    }


    public OkHttpUtils params(String key, String value) {
        urlParams.put(key, value);
        return this;
    }

    public OkHttpUtils params(String key, int value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(String key, long value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(String key, float value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(String key, double value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(String key, char value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(String key, boolean value) {
        urlParams.put(key, String.valueOf(value));
        return this;
    }

    public OkHttpUtils params(Map<String, String> params) {
        urlParams.putAll(params);
        return this;
    }

    public <T> Request execute(@NonNull Class<T> clazz, @NonNull CommonCallBack.SuccessCallBack<T> callBack) {
        return execute(clazz, callBack, null);
    }

    public <T> Request execute(@NonNull Class<T> clazz, @NonNull CommonCallBack.SuccessCallBack<T> success, CommonCallBack.ErrorCallBack error) {
        return execute(new CommonCallBack<T>(clazz) {
            @Override
            public void onSuccess(T resultBase) {
                success.onSuccess(resultBase);
            }

            @Override
            public void onError(Throwable ex) {
                super.onError(ex);
                if (error != null) {
                    error.onError(ex);
                }
            }
        });
    }


    @SuppressWarnings("unchecked")
    public <T> Request execute(final CommonCallBack<T> callBack) {
        Request.Builder builder;
        if (type == GET) {
            StringBuilder urlBuilder = new StringBuilder(url);

            if (urlParams.size() != 0) {
                urlBuilder.append("?");

                for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                    urlBuilder
                            .append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("&");
                }
                urlBuilder.delete(urlBuilder.length(), urlBuilder.length() - 1);
            }
            builder = new okhttp3.Request.Builder()
                    .url(url)
                    .get();
        } else {
            FormBody.Builder mFromBodyBuilder = new FormBody.Builder();
            //添加表单
            if (urlParams != null) {
                for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                    mFromBodyBuilder.add(entry.getKey(), entry.getValue());
                }
            }
            builder = new Request.Builder()
                    .url(this.url);

            switch (type) {
                case POST:
                    builder = builder.post(mFromBodyBuilder.build());
                    break;
                case PUT:
                    builder = builder.put(mFromBodyBuilder.build());
                    break;
                case DELETE:
                    builder = builder.delete(mFromBodyBuilder.build());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        //Debug状态下添加代码位置信息
        //添加代码位置信息,排除了当前类,代码位置为 MainActivity.okHttp(MainActivity.java:46)
        if (BuildConfig.DEBUG) {
            builder.addHeader("location", CodeUtils.getCodeLine(OkHttpUtils.class));
        }

        Request request = builder.build();

        callBack.onStart();//开始发出网络请求

        HttpUtils.getOkHttpClient().newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        //异常调用
                        callBack.onError(e);
                        callBack.onComplete();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) {
                        ResponseBody body = response.body();
                        if (body == null) {
                            return;
                        }
                        try {
                            ResultBase resultBase = GSON.fromJson(body.charStream(), ResultBase.class);
                            if (resultBase.getCode() == 0) {
                                T t = GSON.fromJson(resultBase.getData(), callBack.getDataType());
                                callBack.onSuccess(t);
                                callBack.onComplete();
                            } else {
                                callBack.onError(new HttpException("code=" + resultBase.getCode()));
                                callBack.onComplete();
                            }
                        } catch (Exception ex) {
                            callBack.onError(ex);
                            callBack.onComplete();
                        }
                    }
                });

        return request;
    }


}
