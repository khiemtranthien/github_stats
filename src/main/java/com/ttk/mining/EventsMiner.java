package com.ttk.mining;

import com.ttk.datasouce.EventTypeIngestor;
import com.ttk.utils.Constants;

import java.util.Arrays;
import java.util.List;

public class EventsMiner {

    public static void main(String[] args) {
//        FullIngestor mining = new FullIngestor();
        EventTypeIngestor app = new EventTypeIngestor();
        List<String> ingestEvents = Arrays.asList(
//                Constants.RELEASE_EVENT,
//                Constants.PUSH_EVENT,
//                Constants.FORK_EVENT,
//                Constants.ISSUES_EVENT,
                Constants.PULL_REQUEST_EVENT,
                Constants.PULL_REQUEST_REVIEW_COMMENT_EVENT
        );

        for(String eventType: ingestEvents) {
            String dateFrom = "2019-07-20";
            String dateTo = "2019-07-21";
            app.run(eventType, dateFrom, dateTo);
        }
    }
}
