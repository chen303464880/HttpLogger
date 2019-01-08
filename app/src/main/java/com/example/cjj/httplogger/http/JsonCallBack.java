package com.example.cjj.httplogger.http;

import com.example.cjj.httplogger.bean.ResultBase;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 16:42.
 */
public class JsonCallBack extends AbsCallback<ResultBase> {
    @Override
    public void onSuccess(Response<ResultBase> response) {

    }

    @Override
    public ResultBase convertResponse(okhttp3.Response response) throws Throwable {
        return new ContentConvert().convertResponse(response);
    }
}
