package com.example.cjj.httplogger.http;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 17:37.
 */
public interface Api {
    @FormUrlEncoded
    @POST("service/getIpInfo2.php")
    Observable<String> getIp(@Header("location") String codeLine, @Field("ip") String params);
}
