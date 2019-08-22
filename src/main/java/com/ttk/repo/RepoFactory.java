package com.ttk.repo;

import com.ttk.utils.Constants;

public class RepoFactory {
    public static MongoBaseRepo getRepo(String eventType) {
        if(Constants.ALL_EVENTS.equals(eventType)) {
            return new AllEventsRepo();

        } else if(Constants.PUSH_EVENT.equals(eventType)) {
            return new PushEventRepo();

        } else if(Constants.RELEASE_EVENT.equals(eventType)) {
            return new ReleaseEventRepo();

        } else {
            throw new IllegalArgumentException(String.format("Event type: %s invalid", eventType));
        }
    }
}
