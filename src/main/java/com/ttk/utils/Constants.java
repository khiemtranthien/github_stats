package com.ttk.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static String ALL_EVENTS = "*";

    public static String PUSH_EVENT = "PushEvent";
    public static String RELEASE_EVENT = "ReleaseEvent";

    public static List<String> EVENT_TYPES = Arrays.asList(
            PUSH_EVENT,
            RELEASE_EVENT
    );

}
