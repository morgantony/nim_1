package com.netease.nim.weyouchats.main.fragment;

import com.netease.nim.weyouchats.R;

/**
 * 聊天室主TAB页
 */
public class ChatRoomListFragment extends MainTabFragment {
    private com.netease.nim.weyouchats.chatroom.fragment.ChatRoomListFragment fragment;

  /*  public ChatRoomListFragment() {
        setContainerId(MainTab.CHAT_ROOM.fragmentId);
    }*/

    @Override
    protected void onInit() {
        // 采用静态集成，这里不需要做什么了
        fragment = (com.netease.nim.weyouchats.chatroom.fragment.ChatRoomListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.chat_rooms_fragment);
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        if (fragment != null) {
            fragment.onCurrent();
        }
    }
}
