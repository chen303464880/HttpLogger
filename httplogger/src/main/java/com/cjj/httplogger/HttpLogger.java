package com.cjj.httplogger;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * @author chenjunjie
 * Created by CJJ on 2017/11/1 9:53.
 */

public class HttpLogger implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String mTag;
    private int mLevel;
    private String mHeaderName;
    private int mMaxLines;
    private String mContain;

    private static final String LINE = "────────────────────────────────────────────────────────────────────────────────────";

    private HttpLogger(LogInfo logInfo) {
        if (TextUtils.isEmpty(logInfo.mTag)) {
            this.mTag = "TAG";
        } else {
            this.mTag = logInfo.mTag;
        }
        if (logInfo.mLevel <= 0) {
            this.mLevel = Log.INFO;
        } else {
            this.mLevel = logInfo.mLevel;
        }
        if (TextUtils.isEmpty(logInfo.mHeaderName)) {
            this.mHeaderName = "location";
        } else {
            this.mHeaderName = logInfo.mHeaderName;
        }
        if (logInfo.mMaxLines <= 0) {
            this.mMaxLines = 20;
        } else {
            this.mMaxLines = logInfo.mMaxLines;
        }

        if (TextUtils.isEmpty(logInfo.mContain)) {
            this.mContain = "\t\t\t at %s ";
        } else {
            this.mContain = "\t\t\t " + logInfo.mContain + " %s ";
        }
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        StringBuilder sb = new StringBuilder();
        Request request = chain.request();
        request = logForThread(sb, request);

        logForRequest(request, chain.connection(), sb);

        //执行请求，计算请求时间
        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        response = logForResponse(response, tookMs, sb);
        Logger.log(mLevel, mTag, sb.toString());
        return response;
    }

    private Request logForThread(StringBuilder sb, Request request) {
        String codeLocation = request.headers().get(mHeaderName);
        Thread thread = Thread.currentThread();
        sb.append("\t\tThread: ")
                .append(thread.getName())
                .append("\t\t");
        if (!TextUtils.isEmpty(codeLocation)) {
            sb.append(codeLocation);
            request = request.newBuilder().removeHeader(mHeaderName).build();
        }
        sb.append("\n");
        sb.append(LINE);
        sb.append("\n");
        return request;
    }

    private void logForRequest(Request request, Connection connection, StringBuilder sb) {
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        try {
            sb.append("\t\t")
                    .append(request.method())
                    .append("\t")
                    .append(protocol)
                    .append("\t")
                    .append(URLDecoder.decode(request.url().toString(), "UTF-8"))
                    .append("\n");

            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    sb.append(String.format(mContain, "Content-Type: " + requestBody.contentType()));
                    sb.append("\n");
                }
                if (requestBody.contentLength() != -1) {
                    sb.append(String.format(mContain, "Content-Length: " + requestBody.contentLength()));
                    sb.append("\n");
                }
            }
            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    sb.append(String.format(mContain, name + ": " + headers.value(i)));
                    sb.append("\n");

                }
            }

            if (hasRequestBody) {
                if (isPlaintext(requestBody.contentType())) {
                    sb.append("\n");
                    sb.append("\t\tRequest:");
                    sb.append("\n");
                    sb.append(bodyToString(request));
                    sb.append("\n");
                } else {
                    sb.append("\tbody: maybe [binary body], omitted!\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sb.append(LINE);
            sb.append("\n");
        }
    }

    private Response logForResponse(Response response, long tookMs, StringBuilder sb) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();

        try {
            sb.append("\t\t")
                    .append(clone.message())
                    .append("\t")
                    .append(clone.code())
                    .append("\t")
                    .append("(")
                    .append(tookMs)
                    .append("ms)")
                    .append(" \n");
            Headers headers = clone.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                sb.append(String.format(mContain, headers.name(i) + ": " + headers.value(i)));
                sb.append("\n");

            }
            if (HttpHeaders.hasBody(clone)) {
                if (responseBody == null) return response;

                if (isPlaintext(responseBody.contentType())) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    int len;
                    InputStream inputStream = responseBody.byteStream();
                    byte[] buffer = new byte[4096];
                    while ((len = inputStream.read(buffer)) != -1) output.write(buffer, 0, len);
                    output.close();
                    byte[] bytes = output.toByteArray();
                    MediaType contentType = responseBody.contentType();
                    String body = new String(bytes, getCharset(contentType));
                    sb.append("\n");
                    sb.append("\t\t");
                    sb.append("Response: ");
                    sb.append("\n");
                    body = formatJson(body);
                    sb.append(body);
                    responseBody = ResponseBody.create(responseBody.contentType(), bytes);
                    response = response.newBuilder().body(responseBody).build();
                } else {
                    sb.append("\t\t\t\tbody: maybe [binary body], omitted!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    private String formatJson(String body) throws JSONException {
        StringBuilder stringBuilder = new StringBuilder();
        String[] split;
        if (body.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(body);
            body = jsonObject.toString(2);
        } else if (body.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(body);
            body = jsonArray.toString(2);
        }

        split = body.split("\n");
        boolean is = split.length <= mMaxLines;
        for (String s : split) {
            if (is) {
                stringBuilder.append("\t\t\t");
                stringBuilder.append(s);
            } else {
                stringBuilder.append(String.format(mContain, s));
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private String bodyToString(Request request) {
        try {
            Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
            if (body == null) return "";
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            MediaType mediaType = body.contentType();
            Charset charset = getCharset(mediaType);
            String str = buffer.readString(charset);
            str = URLDecoder.decode(str, "UTF-8");
            if (mediaType != null) {
                switch (mediaType.subtype()) {
                    case "x-www-form-urlencoded"://表单
                        StringBuilder stringBuilder = new StringBuilder();
                        String[] split = str.split("&");
                        for (String s : split) {
                            stringBuilder.append("\t\t\t");
                            stringBuilder.append(s);
                            stringBuilder.append("\n");
                        }
                        str = stringBuilder.toString();
                        break;
                    case "json"://json
                        str = formatJson(str);
                        break;
                    default:
                        break;
                }
            } else {
                str = "\t\t\t" + str;
            }


            return str;
        } catch (Exception e) {
            return "error";
        }
    }

    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            return subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html");
        }
        return false;
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;
        if (charset == null) charset = UTF8;
        return charset;
    }


    private static class LogInfo {
        private String mTag;
        private int mLevel;
        private String mHeaderName;
        private int mMaxLines;
        private String mContain;

    }

    public static class Builder {
        private final LogInfo I;

        public Builder() {
            this.I = new LogInfo();
        }

        public Builder setTag(String tag) {
            this.I.mTag = tag;
            return this;
        }

        public Builder setLevel(int level) {
            this.I.mLevel = level;
            return this;
        }

        public Builder setHeaderName(String name) {
            this.I.mHeaderName = name;
            return this;
        }

        public Builder setMaxLines(int maxLines) {
            this.I.mMaxLines = maxLines;
            return this;
        }

        public Builder setContain(String mContain) {
            this.I.mContain = mContain;
            return this;
        }

        public HttpLogger build() {
            return new HttpLogger(I);
        }
    }
}
