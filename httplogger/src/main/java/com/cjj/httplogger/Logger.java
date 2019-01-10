package com.cjj.httplogger;

import android.util.Log;

/**
 * @author CJJ
 * Created by CJJ on 2019/1/4 13:46.
 */
class Logger {


    private static final String TOP_BORDER = "╔════════════════════════════════════════════════════" +
            "════════════════════════════════════════════════════════════";
    private static final String BOTTOM_BORDER = "╚═════════════════════════════════════════════════" +
            "═══════════════════════════════════════════════════════════════";
    private static final String LEFT_CHAR = "║";

    /**
     * 打印网络请求信息
     */
    static synchronized void log(int priority, String tag, String msg) {
        String[] split = msg.split("\n");
        //打印开始分割线
        Log.println(priority, tag, TOP_BORDER);
        for (String line : split) {//依次打印
            Log.println(priority, tag, LEFT_CHAR + line);
        }
        //打印结束分割线
        Log.println(priority, tag, BOTTOM_BORDER);
    }

}
