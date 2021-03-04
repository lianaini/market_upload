package org.jeme.market;

import com.google.gson.JsonObject;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.jeme.Upload;
import org.jeme.Utils;
import org.jeme.bean.ApkInfo;
import org.jeme.config.MarketConfig;
import org.jeme.config.PushConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/***
 *
 * @author jeme
 */
public abstract class BaseMarket implements IMarket {

    protected MarketConfig config;
    protected PushConfig pushConfig;


    public BaseMarket(MarketConfig config, PushConfig pushConfig) {
        this.config = config;
        this.pushConfig = pushConfig;
    }

    protected abstract boolean pre();

    protected abstract void sync(JsonObject body);

    protected abstract String getUploadUrl();

    protected abstract String platformName();

    protected abstract Map<String, Object> getPushRequestMap();

    @Override
    public void process() {
        System.out.printf("%n===================%s市场开始===================%n", platformName());
        if (config == null || pushConfig == null) {
            System.out.println("配置信息错误，上传失败");
            System.out.printf("===================%s市场结束===================%n", platformName());
            return;
        }
        //step 1 : 准备阶段，包括获取token。。。
        if (!pre()) {
            System.out.printf("===================%s市场结束===================%n", platformName());
            return;
        }
        //step 2 : 开始上传apk
        JsonObject responseBody = push();
        if (responseBody == null) {
            System.out.printf("===================%s市场结束===================%n", platformName());
            return;
        }
        //step 4： 更新apk信息 同步（提交审核）
        System.out.printf("开始同步%s平台%n", platformName());
        sync(responseBody);
        System.out.printf("%s平台同步完成%n", platformName());

        System.out.printf("===================%s市场结束===================%n", platformName());

    }

    public JsonObject push() {
        System.out.printf("开始%s平台获取上传地址...%n", platformName());
        String uploadUrl = getUploadUrl();
        if (Utils.isEmpty(uploadUrl)) {
            System.out.printf("获取%s平台上传地址失败!%n", platformName());
            return null;
        }
        System.out.printf("开始生成%s市场渠道包...%n", platformName());
        if (!generateChannel()) {
            System.out.printf("生成%s市场渠道包失败%n", platformName());
            return null;
        } else {
            System.out.printf("生成%s市场渠道包完成%n", platformName());
        }
        System.out.printf("开始上传%s平台%n", platformName());
        JsonObject body;
        try {
            Map<String, Object> requestMap = getPushRequestMap();
            if (requestMap == null) {
                System.out.printf("%s平台上传失败,参数为空%n", platformName());
                return null;
            }
            body = Upload.post(uploadUrl, requestMap, getHeader());
            if (body == null) {
                System.out.printf("%s平台上传失败%n", platformName());
                return null;
            }
            System.out.printf("%s平台上传完成%n", platformName());
            return body;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("%s平台上传失败%n", platformName());
            return null;
        }

    }


    protected Map<String, Object> getHeader() {
        return null;
    }

    protected String getChannelApkPath() {
        return Utils.getPath() + "/channel/source-" + config.channel + ".apk";
    }

    private boolean generateChannel() {
        String cmd = "java -jar " + Utils.getPath()
                + "/libs/walle-cli-all.jar put -c "
                + config.channel + " "
                + Utils.getPath() + "/source/source.apk "
                + getChannelApkPath();
        System.out.println(cmd);
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            int status = process.waitFor();
            if (status != 0) {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        catChannel();
        return true;
    }

    private boolean catChannel() {
        String cmd = "java -jar " + Utils.getPath()
                + "/libs/walle-cli-all.jar show "
                + getChannelApkPath();
        System.out.println(cmd);

        StringBuilder result = new StringBuilder();
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            int status = process.waitFor();
            if (status != 0) {
                return false;
            }

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }
            System.out.println("walle 输出:" + result);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            Utils.closeStream(bufrIn);
            Utils.closeStream(bufrError);
        }
        return true;
    }

    protected ApkInfo readApkInfo() {
        ApkFile apkFile = null;
        try {
            apkFile = new ApkFile(getChannelApkPath());
            ApkInfo apkInfo = new ApkInfo();
            ApkMeta apkMeta = apkFile.getApkMeta();
            apkInfo.packageName = apkMeta.getPackageName();
            apkInfo.versionName = apkMeta.getVersionName();
            apkInfo.versionCode = apkMeta.getVersionCode();
            apkInfo.appName = apkMeta.getName();
            System.out.println(apkInfo.toString());
            return apkInfo;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (apkFile != null) {
                try {
                    apkFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void catMarket() {

    }
}
