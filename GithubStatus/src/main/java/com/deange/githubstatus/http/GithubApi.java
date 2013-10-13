package com.deange.githubstatus.http;

public class GithubApi {


    // URL ENDPOINTS
    private static final String BASE_URL = "https://status.github.com";
    private static final String BASE_API_URL = BASE_URL + "/api";
    private static final String JSON = ".json";

    public static final String STATUS = BASE_API_URL + "/status" + JSON;
    public static final String LAST_MESSAGE = BASE_API_URL + "/last-message" + JSON;
    public static final String LAST_MESSAGES = BASE_API_URL + "/messages" + JSON;


    // STATUS CODES
    public static final String STATUS_GOOD = "good";
    public static final String STATUS_MINOR = "minor";
    public static final String STATUS_MAJOR = "major";




}
