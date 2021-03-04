package org.jeme.config;

public class MarketConfig {

    /***
     * 用户名 小米平台需要使用
     */
    public String userName;
    /***
     * 应用id 华为平台使用
     */
    public String appId;
    /***
     * 公钥
     * 华为平台 对应AppGallery Connect API 客户端中的客户端ID
     */
    public String accessKey;
    /***
     * 私钥
     * 华为平台 对应AppGallery Connect API 客户端中的密钥
     */
    public String accessSecret;
    /**
     * 渠道名称
     */
    public String channel;
}
