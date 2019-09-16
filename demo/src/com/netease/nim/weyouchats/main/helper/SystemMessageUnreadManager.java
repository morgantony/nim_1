package com.netease.nim.weyouchats.main.helper;

/**
 */
public class SystemMessageUnreadManager {

    private static SystemMessageUnreadManager instance = new SystemMessageUnreadManager();

    public static SystemMessageUnreadManager getInstance() {
        return instance;
    }

    private int sysMsgUnreadCount = 0;

    public int getSysMsgUnreadCount() {
        return sysMsgUnreadCount;
    }

    public synchronized void setSysMsgUnreadCount(int unreadCount) {
        this.sysMsgUnreadCount = unreadCount;
    }
}
