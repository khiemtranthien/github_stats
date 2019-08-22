package com.ttk;

import com.ttk.datasouce.EventTypeIngestor;
import com.ttk.datasouce.FullIngestor;
import com.ttk.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
//        FullIngestor app = new FullIngestor();
        EventTypeIngestor app = new EventTypeIngestor();
        List<String> ingestEvents = Arrays.asList(
                Constants.RELEASE_EVENT,
                Constants.PUSH_EVENT
        );

        for(String eventType: ingestEvents) {
            app.run(eventType);
        }
    }
}
