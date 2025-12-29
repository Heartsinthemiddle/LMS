package com.lms.dto.response;

public class ScormManifest {

    private String title;
    private String version;
    private String launchUrl;

    public ScormManifest(String title, String version, String launchUrl) {
        this.title = title;
        this.version = version;
        this.launchUrl = launchUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public String getLaunchUrl() {
        return launchUrl;
    }
}

