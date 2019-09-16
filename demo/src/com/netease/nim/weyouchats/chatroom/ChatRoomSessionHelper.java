package com.netease.nim.weyouchats.chatroom;

import com.netease.nim.weyouchats.chatroom.viewholder.ChatRoomMsgViewHolderGuess;
import com.netease.nim.weyouchats.session.action.GuessAction;
import com.netease.nim.weyouchats.session.extension.GuessAttachment;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomSessionCustomization;

import java.util.ArrayList;

/**
 * UIKit自定义聊天室消息界面用法展示类
 * <p>
 */

public class ChatRoomSessionHelper {

    public static void init() {
        registerViewHolders();
        NimUIKit.setCommonChatRoomSessionCustomization(getChatRoomSessionCustomization());
    }

    private static void registerViewHolders() {
        NimUIKit.registerChatRoomMsgItemViewHolder(GuessAttachment.class, ChatRoomMsgViewHolderGuess.class);
    }

    private static ChatRoomSessionCustomization getChatRoomSessionCustomization() {
        ArrayList<BaseAction> actions = new ArrayList<>();
//        actions.add(new GuessAction());
        ChatRoomSessionCustomization customization = new ChatRoomSessionCustomization();
        customization.actions = actions;
        return customization;
    }
}
