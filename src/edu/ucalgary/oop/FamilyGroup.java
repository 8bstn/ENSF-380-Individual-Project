package edu.ucalgary.oop;

import java.util.ArrayList;
import java.util.List;

class FamilyGroup {
    private int groupId;
    private String headName;
    private List<DisasterVictim> members;

    public FamilyGroup(int groupId, String headName) {
        this.groupId = groupId;
        this.headName = headName;
        this.members = new ArrayList<>();
    }

    public void addMember(DisasterVictim victim) {
        members.add(victim);
    }

    public int getGroupId() {
        return groupId;
    }

    public String getHeadName() {
        return headName;
    }

    public List<DisasterVictim> getMembers() {
        return members;
    }
}