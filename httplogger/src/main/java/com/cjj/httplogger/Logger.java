package com.cjj.httplogger;

import android.util.Log;

/**
 * @author CJJ
 * Created by CJJ on 2019/1/4 13:46.
 */
class Logger {

    private static int mTagNum = 0;

    private static final String TOP_BORDER = "╔════════════════════════════════════════════════════" +
            "════════════════════════════════════════════════════════════";
    private static final String BOTTOM_BORDER = "╚═════════════════════════════════════════════════" +
            "══════════════════════════════════════════════════════════════";
    private static final String LEFT_CHAR = "║";

    /**
     * 打印网络请求信息
     */
    static synchronized void log(int priority, String tag, String msg) {
        String[] split = msg.split("\n");
        //打印开始分割线
        Log.println(priority, getTag(tag), TOP_BORDER);
        for (String line : split) {//依次打印
            Log.println(priority, getTag(tag), LEFT_CHAR + line);
        }
        //打印结束分割线
        Log.println(priority, getTag(tag), BOTTOM_BORDER);
    }

    /**
     * android studio 会隐藏相同的tag,在tag后添加0或1使其不相同
     *
     * @param tag 原tag
     * @return 新的tag
     */
    private static String getTag(String tag) {
        if (mTagNum == 0) {
            mTagNum = 1;
        } else {
            mTagNum = 0;
        }
        return tag + mTagNum;
    }
}
