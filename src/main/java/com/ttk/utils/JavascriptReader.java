package com.ttk.utils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavascriptReader {
    public static String getQueryString(String fileName) throws ScriptException, NoSuchMethodException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        InputStream in = JavascriptReader.class.getClassLoader().getResourceAsStream(String.format("queries/%s", fileName));
        engine.eval(new InputStreamReader(in));

        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getQuery");

        return String.valueOf(result);
    }

    public static void main(String[] args) throws Exception {
        String query = JavascriptReader.getQueryString("push.js");
        System.out.print(query);
    }
}
