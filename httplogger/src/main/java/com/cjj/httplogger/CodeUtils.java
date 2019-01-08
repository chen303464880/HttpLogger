package com.cjj.httplogger;

import android.util.Log;

import java.util.Locale;

/**
 * @author CJJ
 * Created by CJJ on 2018/12/17 16:17.
 */
public class CodeUtils {


    /**
     * 获取代码位置
     *
     * @return 代码位置
     */
    public static String getCodeLine() {
        return getCodeLine(1);
    }

    /**
     * 获取代码位置,不清楚偏移量可调用printStackTrace方法
     *
     * @param offset 栈中的偏移量
     * @return 代码位置
     */
    public static String getCodeLine(int offset) {
        return formatCode(getStackTraceElement(offset));
    }

    /**
     * 获取栈中下标4的代码位置,如果为excludeClass则会向下寻找,直到不为excludeClass
     *
     * @param excludeClass 需要排除的类
     * @return 排除excludeClass的代码位置
     */
    public static String getCodeLine(Class excludeClass) {
        return formatCode(getStackTraceElement(excludeClass));
    }


    /**
     * 打印栈中的方法,并标出偏移量
     */
    public static void printStackTrace() {
        int length = Thread.currentThread().getStackTrace().length - 4;
        StringBuilder sb = new StringBuilder();
        for (int i = -4; i < length; i++) {
            sb.append(" ");
            sb.append(String.format(Locale.getDefault(), "%2d", i));
            sb.append("\t");
            sb.append(getStackTraceElement(i));
            sb.append("\n");
        }
        Logger.log(Log.INFO, "TAG", sb.toString());
    }

    private static StackTraceElement getStackTraceElement(Class excludeClass) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[4];
        for (int i = 4, z = stackTrace.length; i < z; i++) {
            StackTraceElement traceElement = stackTrace[i];
            if (!traceElement.getClassName().equals(excludeClass.getName())) {
                stackTraceElement = traceElement;
                break;
            }
            if (i == z - 1) {
                stackTraceElement = stackTrace[4];
            }
        }
        return stackTraceElement;
    }

    private static StackTraceElement getStackTraceElement(int offset) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[4 + offset];
    }

    private static String formatCode(StackTraceElement stackTraceElement) {
        StringBuilder builder = new StringBuilder();
        String className = stackTraceElement.getClassName().substring(stackTraceElement.getClassName().lastIndexOf(".") + 1);
        String methodName = stackTraceElement.getMethodName();
        String fileName = stackTraceElement.getFileName();
        int lineNumber = stackTraceElement.getLineNumber();

        builder.append(className)
                .append(".")
                .append(methodName)
                .append("(")
                .append(fileName)
                .append(":")
                .append(lineNumber)
                .append(")");
        return builder.toString();
    }
}
