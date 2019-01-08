package com.example.cjj.httplogger.http;

import com.example.cjj.httplogger.bean.ResultBase;
import com.google.gson.Gson;
import com.lzy.okgo.exception.HttpException;

import okhttp3.ResponseBody;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 16:43.
 */
public class ContentConvert implements com.lzy.okgo.convert.Converter<ResultBase> {

    @Override
    public ResultBase convertResponse(okhttp3.Response response) {
        ResponseBody body = response.body();
        if (body != null) {
            return new Gson().fromJson(body.charStream(), ResultBase.class);
        }
        throw new HttpException("response.body == null!");

    }
}
