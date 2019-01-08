package com.example.cjj.httplogger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cjj.httplogger.CodeUtils;
import com.example.cjj.httplogger.bean.IpInfoBean;
import com.example.cjj.httplogger.bean.ResultBase;
import com.example.cjj.httplogger.http.HttpUtils;
import com.example.cjj.httplogger.http.OkGoHttpUtils;
import com.example.cjj.httplogger.http.OkHttpUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    EditText etIp;
    TextView tvResult;
    private static final String URL = "http://ip.taobao.com/service/getIpInfo2.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIp = findViewById(R.id.et_ip);
        tvResult = findViewById(R.id.tv_result);
        //打印栈信息
        CodeUtils.printStackTrace();

    }

    /**
     * OkHttp发起网络请求
     */
    public void okHttp(View view) {
        //统一请求
        OkHttpUtils.post(URL)
                .params("ip", etIp.getText().toString())
                //execute中封装了添加Header的代码 (OkHttpUtils.java:181)
                .execute(IpInfoBean.class, this::setResult);
    }


    /**
     * OkGo发起网络请求
     */
    public void okGo(View view) {
        OkGoHttpUtils.post(URL)
                .params("ip", etIp.getText().toString())
                //execute中封装了添加Header的代码 (OkGoHttpUtils.java:161)
                .execute(IpInfoBean.class, this::setResult);
    }

    /**
     * Retrofit发起网络请求
     */
    @SuppressLint("CheckResult")
    public void retrofit(View view) {
        //将当前代码位置放入Header,代码位置为 MainActivity.retrofit(MainActivity.java:69)
        //noinspection ResultOfMethodCallIgnored
        HttpUtils.getInstance().getIp(CodeUtils.getCodeLine(), etIp.getText().toString())
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> new Gson().fromJson(s, ResultBase.class))
                .map(s -> new Gson().fromJson(s.getData(), IpInfoBean.class))
                .subscribe(this::setResult);
    }

    /**
     * 设置结果
     */
    private void setResult(IpInfoBean ipInfo) {
        String s = new Gson().toJson(ipInfo);
        JSONObject jsonObject;
        try {
            //格式化json串
            jsonObject = new JSONObject(s);
            tvResult.setText(jsonObject.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

