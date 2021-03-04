package org.jeme.market;

import com.google.gson.JsonObject;
import org.jeme.Upload;
import org.jeme.Utils;
import org.jeme.bean.ApkInfo;
import org.jeme.config.MarketConfig;
import org.jeme.config.PushConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/***
 * https://dev.vivo.com.cn/documentCenter/doc/343
 */
public class VIVOMarket extends BaseMarket {

    private static final String SIGN_METHOD_HMAC = "hmac";

    public VIVOMarket(MarketConfig config, PushConfig pushConfig) {
        super(config, pushConfig);
    }

    @Override
    protected boolean pre() {
        return true;
    }

    @Override
    public String getUploadUrl() {
        return "https://developer-api.vivo.com.cn/router/rest";
    }


    @Override
    public String platformName() {
        return "VIVO";
    }

    @Override
    protected Map<String, Object> getPushRequestMap() {

        ApkInfo apkInfo = readApkInfo();
        if (apkInfo == null) {
            System.out.println("读取App信息失败");
            return null;
        }
        try {
            Map<String, Object> params = getPublicParams();
            params.put("method", "app.upload.apk.app");
            params.put("packageName", apkInfo.packageName);
//            params.put("packageName", "com.bingtian.sweetweathe");
            params.put("fileMd5", Utils.getFileMD5(new File(getChannelApkPath())));
            // 签名参数
            params.put("sign", sign(params, config.accessSecret, SIGN_METHOD_HMAC));
            //file 不参与sign计算，需要在sign计算后加入
            params.put("file", new File(getChannelApkPath()));
            return params;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 获取公共参数
     */
    private Map<String, Object> getPublicParams() throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("access_key", config.accessKey);
        params.put("timestamp", System.currentTimeMillis());
        params.put("format", "json");
        params.put("v", "1.0");
        params.put("target_app_key", "developer");
        params.put("sign_method", SIGN_METHOD_HMAC);
        return params;
    }

    /**
     * 对TOP请求进行签名。
     */
    private String sign(Map<String, Object> paramsMap, String secret, String signMethod)
            throws IOException {
        // 第一步：参数排序
        String params = getUrlParamsFromMap(paramsMap);

        // 第二步：使用HMAC加密
        if (SIGN_METHOD_HMAC.equals(signMethod)) {
            return hmacSHA256(params, secret);
        }
        return null;
    }

    @Override
    protected void sync(JsonObject response) {
        if (response == null) {
            return;
        }

        try {
            int code = response.get("code").getAsInt();
            if (code != 0) {
                return;
            }
            Upload.post(getUploadUrl(), getSyncRequestMap(response), null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Map<String, Object> getSyncRequestMap(JsonObject response) {
        Map<String, Object> params = null;
        try {
            JsonObject data = response.get("data").getAsJsonObject();
            params = getPublicParams();
            params.put("method", "app.sync.update.app");
            params.put("packageName", data.get("packageName").getAsString());
            params.put("versionCode", data.get("versionCode").getAsInt());
            //流水号
            params.put("apk", data.get("serialnumber").getAsString());
            params.put("fileMd5", data.get("fileMd5").getAsString());
            //实时上架
            params.put("onlineType", 1);
            params.put("updateDesc", pushConfig.updateDesc);
            // 签名参数
            params.put("sign", sign(params, config.accessSecret, SIGN_METHOD_HMAC));


        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 根据传入的map，把map里的key value转换为接口的请求参数，并给参数按ascii码排序
     *
     * @param paramsMap 传入的map
     * @return 按ascii码排序的参数键值对拼接结果
     */
    public static String getUrlParamsFromMap(Map<String, Object> paramsMap) {
        List<String> keysList = new ArrayList<>(paramsMap.keySet());
        Collections.sort(keysList);
        List<String> paramList = new ArrayList<>();
        for (String key : keysList) {
            Object object = paramsMap.get(key);
            if (object == null) {
                continue;
            }
            String value = key + "=" + object;
            paramList.add(value);
        }
        return String.join("&", paramList);
    }

    /**
     * HMAC_SHA256 验签加密
     **/
    public static String hmacSHA256(String data, String key) {
        try {
            byte[] secretByte = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec signingKey = new SecretKeySpec(secretByte, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] dataByte = data.getBytes(StandardCharsets.UTF_8);
            byte[] by = mac.doFinal(dataByte);
            return byteArr2HexStr(by);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HMAC_SHA256 加密后的数组进行16进制转换
     *
     * @return String 返回加密后字符串
     */
    private static String byteArr2HexStr(byte[] bytes) {
        int length = bytes.length;
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
        StringBuilder sb = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            // 将得到的字节转16进制
            String strHex = Integer.toHexString(bytes[i] & 0xFF);
            // 每个字节由两个字符表示，位数不够，高位补0
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString();
    }
}
