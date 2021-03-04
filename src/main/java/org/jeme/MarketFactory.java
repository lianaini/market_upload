package org.jeme;

import org.jeme.market.IMarket;

import java.util.ArrayList;
import java.util.List;

/***
 * 应用市场
 */
public class MarketFactory {

    private static class INSTANCE {
        public static MarketFactory instance = new MarketFactory();
    }
    public static MarketFactory getInstance() {
        return INSTANCE.instance;
    }

    private final List<IMarket> pushMarket = new ArrayList<>();
    public void add(IMarket market){
        pushMarket.add(market);
    }

    /***
     * 处理上传更新的逻辑
     */
    public void process(){
        for (IMarket iMarket : pushMarket) {
            iMarket.process();
        }
    }
}
