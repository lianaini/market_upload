package org.jeme.bean;

public class HwUploadFileInfo {

    public String authCode;
    public String uploadUrl;

    public HwUploadFileInfo(String authCode, String uploadUrl) {
        this.authCode = authCode;
        this.uploadUrl = uploadUrl;
    }

    @Override
    public String toString() {
        return "HwUploadFileInfo{" +
                "authCode='" + authCode + '\'' +
                ", uploadUrl='" + uploadUrl + '\'' +
                '}';
    }
}
