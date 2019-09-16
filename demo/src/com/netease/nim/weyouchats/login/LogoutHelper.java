package com.netease.nim.weyouchats.login;

import com.netease.nim.weyouchats.DemoCache;
import com.netease.nim.weyouchats.redpacket.NIMRedPacketClient;
import com.netease.nim.uikit.api.NimUIKit;

/**
 * 注销帮助类
 */
public class LogoutHelper {
    public static void logout() {
        // 清理缓存&注销监听&清除状态
        NimUIKit.logout();
        DemoCache.clear();
        NIMRedPacketClient.clear();
    }
}
