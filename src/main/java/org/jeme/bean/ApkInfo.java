package org.jeme.bean;

public class ApkInfo {

    public String versionName;
    public long versionCode;
    public String packageName;
    public String appName;

    @Override
    public String toString() {
        return "ApkInfo{" +
                "versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }
}
