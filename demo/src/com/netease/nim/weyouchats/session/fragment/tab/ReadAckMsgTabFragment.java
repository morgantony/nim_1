package com.netease.nim.weyouchats.session.fragment.tab;

import android.os.Bundle;

import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.session.fragment.ReadAckMsgFragment;
import com.netease.nim.weyouchats.session.model.AckMsgTab;

/**
 * Created by winnie on 2018/3/15.
 */

public class ReadAckMsgTabFragment extends AckMsgTabFragment  {

    ReadAckMsgFragment fragment;

    public ReadAckMsgTabFragment() {
        this.setContainerId(AckMsgTab.READ.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    protected void onInit() {
        findViews();
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
    }

    private void findViews() {
        fragment = (ReadAckMsgFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.read_ack_msg_fragment);

    }
}
