package com.netease.nim.uikit.business.contact.core.model;

import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nimlib.sdk.team.model.TeamMember;

/**
 */
public class TeamMemberContact extends AbsContact {

    private TeamMember teamMember;

    public TeamMemberContact(TeamMember teamMember) {
        this.teamMember = teamMember;
    }

    @Override
    public String getContactId() {
        return teamMember.getAccount();
    }

    @Override
    public int getContactType() {
        return Type.TeamMember;
    }

    @Override
    public String getDisplayName() {
        return TeamHelper.getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());
    }
}
