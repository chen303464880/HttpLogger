package com.example.cjj.httplogger.http;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by CJJ on 2017/3/21 17:33.
 * 网络请求回调
 */

public abstract class CommonCallBack<T> {


    private final Type type;

    public CommonCallBack() {
        type = ((ParameterizedType) this.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

    }

    public CommonCallBack(Type type) {
        this.type = type;

    }


    Type getDataType() {
        return type;
    }

    /**
     * 开始
     */
    public void onStart() {
    }

    /**
     * 成功
     */
    public abstract void onSuccess(T resultBase);


    /**
     * 异常
     */
    public void onError(Throwable ex) {
//        if (ex instanceof HttpException) {
//            HttpException e = (HttpException) ex;
//            LogUtils.e("ErrorCode:" + e.getExceptionCode() + "\nErrorMessage:" + e.getMessage());
//            LogUtils.exception(e);
//        } else {
//            LogUtils.exception(ex);
//        }
    }

    /**
     * 所有的操作执行完成,执行此方法
     */
    public void onComplete() {
    }

    public interface SuccessCallBack<T> {
        void onSuccess(T result);
    }

    public interface ErrorCallBack {
        void onError(Throwable ex);
    }
}
