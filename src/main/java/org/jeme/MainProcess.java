package org.jeme;

import com.google.gson.Gson;
import org.jeme.config.Config;
import org.jeme.market.HuaWeiMarket;
import org.jeme.market.MiMarket;
import org.jeme.market.VIVOMarket;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class MainProcess {

    private final LinkedHashMap<String,String> platformMap = new LinkedHashMap<>();
    /***
     * 支持的平台列表
     */
    public MainProcess() {
        platformMap.put("1","全部");
        platformMap.put("2","小米(mi)");
        platformMap.put("3","VIVO");
        platformMap.put("4","华为（HuaWei）");
        platformMap.put("exit","退出");
    }

    public void control() {

        System.out.println("请选择需要上传的平台");
        System.out.println("直接回车表示全部");
        String inputContent;
        boolean canContinue;
        do {
            System.out.println("*********");
            for(Map.Entry<String,String> entry : platformMap.entrySet() ){
                System.out.printf("%s.%s%n",entry.getKey(),entry.getValue());
            }
            System.out.println("*********");
            inputContent = new Scanner(System.in).nextLine();
            if(Utils.isEmpty(inputContent)) {
                System.out.println("默认上传所有平台");
                inputContent = "1";
            }
            if("exit".equals(inputContent)) {
                return;
            }

            if(!platformMap.containsKey(inputContent)) {
                System.out.println("请输入正确的编号");
                canContinue = false;

            }else {
                canContinue = true;
            }

        } while (!canContinue);
        System.out.println("您选择了" + platformMap.get(inputContent));
        Config config = getConfigFromLocal();
        if(config == null) {
            System.out.println("配置文件有错，请先检查！");
            return;
        }
        switchPlatform(inputContent,config);
    }


    private Config getConfigFromLocal() {
        String configJson = Utils.readFile(new File(Utils.getPath() + "/config.json"));
        if(Utils.isEmpty(configJson)){
            return null;
        }
        return new Gson().fromJson(configJson,Config.class);
    }

    private void switchPlatform(String platformIndex,Config config) {
        if(config == null || config.pushInfo == null || config.market == null) {
            return;
        }
        switch (platformIndex) {
            case "1":
                MarketFactory.getInstance().add(new MiMarket(config.market.get("mi"),config.pushInfo));
                MarketFactory.getInstance().add(new VIVOMarket(config.market.get("vivo"),config.pushInfo));
                MarketFactory.getInstance().add(new HuaWeiMarket(config.market.get("hw"),config.pushInfo));
                break;
            case "2":
                MarketFactory.getInstance().add(new MiMarket(config.market.get("mi"),config.pushInfo));
                break;
            case "3" :
                MarketFactory.getInstance().add(new VIVOMarket(config.market.get("vivo"),config.pushInfo));
                break;
            case "4":
                MarketFactory.getInstance().add(new HuaWeiMarket(config.market.get("hw"),config.pushInfo));
                break;
            default:
                System.out.println("您输入了错误的编号");
                break;
        }
        MarketFactory.getInstance().process();
    }
}
