package com.example.cjj.httplogger.bean;

import com.google.gson.JsonElement;

/**
 * @author CJJ
 * Created by CJJ on 2018/9/4 15:08.
 */
public class ResultBase {


    private int code;

    private JsonElement data;

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
