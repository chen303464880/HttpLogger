package com.example.cjj.httplogger.http;

import android.support.annotation.NonNull;

import com.cjj.httplogger.CodeUtils;
import com.example.cjj.httplogger.BuildConfig;
import com.example.cjj.httplogger.bean.ResultBase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.BodyRequest;
import com.lzy.okgo.request.base.Request;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author chenjunjie
 * Created by CJJ on 2017/11/1 16:29.
 */

public class OkGoHttpUtils {
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
    private Request request;

    private static final ContentConvert CONTENT_CONVERT = new ContentConvert();


    public OkGoHttpUtils(String url, int type) {
        switch (type) {
            case GET:
                request = OkGo.<String>get(url).tag(url);
                break;
            case POST:
                request = OkGo.<String>post(url).tag(url);
                break;
            case PUT:
                request = OkGo.<String>put(url).tag(url);
                break;
            case DELETE:
                request = OkGo.<String>delete(url).tag(url);
                break;

        }

    }

    public static OkGoHttpUtils get(String url) {
        return new OkGoHttpUtils(url, GET);
    }

    public static OkGoHttpUtils post(String url) {
        return new OkGoHttpUtils(url, POST);
    }

    public static OkGoHttpUtils put(String url) {
        return new OkGoHttpUtils(url, PUT);
    }

    public static OkGoHttpUtils delete(String url) {
        return new OkGoHttpUtils(url, DELETE);
    }

    public OkGoHttpUtils params(String key, String value, boolean... isReplace) {
        request.params(key, value, isReplace);
//        post.params()
        return this;
    }

    public OkGoHttpUtils params(String key, int value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(String key, long value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(String key, float value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(String key, double value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(String key, char value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(String key, boolean value, boolean... isReplace) {
        request.params(key, value, isReplace);
        return this;
    }

    public OkGoHttpUtils params(Map<String, String> params, boolean... isReplace) {
        request.params(params, isReplace);
        return this;
    }

    public OkGoHttpUtils upJson(Object object) {
        return upJson(GSON.toJson(object));
    }

    public OkGoHttpUtils upJson(String jsonStr) {
        if (request instanceof BodyRequest) {
            ((BodyRequest) request).upJson(jsonStr);
        }
        return this;
    }

    public void removeAllParams() {
        request.removeAllParams();
    }

    public <T> OkGoHttpUtils execute(@NonNull Class<T> clazz, @NonNull CommonCallBack.SuccessCallBack<T> callBack) {
        return execute(clazz, callBack, null);
    }

    public <T> OkGoHttpUtils execute(@NonNull Class<T> clazz, @NonNull CommonCallBack.SuccessCallBack<T> success, CommonCallBack.ErrorCallBack error) {
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
    public <T> OkGoHttpUtils execute(final CommonCallBack<T> callBack) {

        //Debug状态下添加代码位置信息
        if (BuildConfig.DEBUG) {
            //添加代码位置信息,排除了当前类,代码位置为 MainActivity.okGo(MainActivity.java:59)
            request.headers("location", CodeUtils.getCodeLine(this.getClass()));
        }


        request.converter(CONTENT_CONVERT)
                .execute(new JsonCallBack() {
                    @Override
                    public void onStart(Request<ResultBase, ? extends Request> request) {
                        super.onStart(request);
                        callBack.onStart();
                    }

                    @Override
                    public void onSuccess(Response<ResultBase> response) {

                        try {
                            ResultBase resultBase = response.body();
                            if (resultBase.getCode() == 0) {
                                T t = GSON.fromJson(response.body().getData(), callBack.getDataType());
                                callBack.onSuccess(t);
                            }
                            callBack.onComplete();
                        } catch (Exception ex) {
                            response.setException(ex);
                            onError(response);
                        }
                    }

                    @Override
                    public void onError(Response<ResultBase> response) {
                        super.onError(response);
                        callBack.onError(response.getException());
                        callBack.onComplete();
                    }
                });
        return this;
    }
}
