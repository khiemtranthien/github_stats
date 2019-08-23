package com.ttk.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static String ALL_EVENTS = "*";

    public static String PUSH_EVENT = "PushEvent";
    public static String RELEASE_EVENT = "ReleaseEvent";
    public static String FORK_EVENT = "ForkEvent";
    public static String ISSUES_EVENT = "IssuesEvent";
    public static String PULL_REQUEST_EVENT = "PullRequestEvent";
    public static String PULL_REQUEST_REVIEW_COMMENT_EVENT = "PullRequestReviewCommentEvent";

    public static List<String> EVENT_TYPES = Arrays.asList(
            PUSH_EVENT,
            RELEASE_EVENT,
            FORK_EVENT,
            ISSUES_EVENT,
            PULL_REQUEST_EVENT,
            PULL_REQUEST_REVIEW_COMMENT_EVENT
    );

}
