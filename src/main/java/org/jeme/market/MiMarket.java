package org.jeme.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jeme.bean.ApkInfo;
import org.jeme.Utils;
import org.jeme.config.Config;
import org.jeme.config.MarketConfig;
import org.jeme.config.PushConfig;

import javax.crypto.Cipher;
import java.io.*;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/***
 * https://dev.mi.com/console/doc/detail?pId=33
 */
public class MiMarket extends BaseMarket {

    private static final String DOMAIN = "http://api.developer.xiaomi.com/devupload";

    private static final String PUSH = DOMAIN + "/dev/push";

    private static final int KEY_SIZE = 1024;
    private static final int GROUP_SIZE = KEY_SIZE / 8;
    private static final int ENCRYPT_GROUP_SIZE = GROUP_SIZE - 11;
    public static final String KEY_ALGORITHM = "RSA/NONE/PKCS1Padding";

    private static PublicKey pubKey;

    // 加载BC库
    static {
    }

    public MiMarket(MarketConfig config, PushConfig pushConfig) {
        super(config,pushConfig);
    }

    @Override
    protected boolean pre() {
        try {
            // 加载BC库
            Security.addProvider(new BouncyCastleProvider());
            pubKey = getPublicKeyByX509Cer(Utils.getPath() + "/mi.dev.api.public.cer");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public String getUploadUrl() {
        return PUSH;
    }

    @Override
    public String platformName() {
        return "小米（MI)";
    }

    @Override
    protected Map<String, Object> getPushRequestMap() {

        ApkInfo apkInfo = readApkInfo();
        if (apkInfo == null) {
            System.out.println("读取App信息失败");
            return null;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            JsonObject requestData = new JsonObject();

            requestData.addProperty("userName", config.userName);
            requestData.addProperty("synchroType", 1);

            JsonObject appInfo = new JsonObject();
            appInfo.addProperty("appName", apkInfo.appName);
            appInfo.addProperty("packageName", apkInfo.packageName);
//            appInfo.addProperty("packageName", "com.bingtian.sweetweathe");
            appInfo.addProperty("updateDesc", pushConfig.updateDesc);

            requestData.addProperty("appInfo", appInfo.toString());

            JsonObject sign = new JsonObject();
            JsonArray sig = new JsonArray();

            JsonObject requestDataSign = new JsonObject();
            requestDataSign.addProperty("name", "RequestData");
            requestDataSign.addProperty("hash", Utils.md5(requestData.toString().getBytes()));
//        requestDataSign.addProperty("hash", DigestUtils.md5Hex(requestData.toString()));
            sig.add(requestDataSign);

            JsonObject apkSign = new JsonObject();
            apkSign.addProperty("name", "apk");
            apkSign.addProperty("hash", Utils.getFileMD5(new File(getChannelApkPath())));
//            apkSign.addProperty("hash", getFileMD5(new File(getChannelApkPath())));
            sig.add(apkSign);

            sign.addProperty("password", config.accessSecret);
            sign.add("sig", sig);

            params.put("RequestData", requestData.toString());
            params.put("SIG", encryptByPublicKey(sign.toString(), pubKey));
            params.put("apk", new File(getChannelApkPath()));

            System.out.println("mi request map:" + requestData.toString());
            System.out.println("mi request sign map:" + sign.toString());
            return params;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void sync(JsonObject body) {

    }
    public String encryptByPublicKey(String str, PublicKey publicKey) throws Exception {
        byte[] data = str.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] segment = new byte[ENCRYPT_GROUP_SIZE];
        int idx = 0;
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        while (idx < data.length) {
            int remain = data.length - idx;
            int segsize = Math.min(remain, ENCRYPT_GROUP_SIZE);
            System.arraycopy(data, idx, segment, 0, segsize);
            baos.write(cipher.doFinal(segment, 0, segsize));
            idx += segsize;
        }
//        return Hex.encodeHexString(baos.toByteArray());
        return Utils.bytes2HexString(baos.toByteArray());
    }

    /**
     * 读取公钥
     */
    public static PublicKey getPublicKeyByX509Cer(String cerFilePath) throws Exception {
        InputStream x509Is = new FileInputStream(cerFilePath);
        try {
            CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificatefactory.generateCertificate(x509Is);
            return cert.getPublicKey();
        } finally {
            try {
                x509Is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
