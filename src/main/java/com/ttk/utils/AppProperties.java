package com.ttk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class AppProperties {
    private final Properties configProp = new Properties();

    private AppProperties()
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("app.properties");

        try {
            configProp.load(in);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LazyHolder
    {
        private static final AppProperties INSTANCE = new AppProperties();
    }

    public static AppProperties getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    public String get(String key){
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames(){
        return configProp.stringPropertyNames();
    }

    public boolean contains(String key){
        return configProp.containsKey(key);
    }
}
