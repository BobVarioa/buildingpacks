package com.bobvarioa.buildingpacks.capabilty;

public enum BuildingPower {
    NONE(0),
    DRAFTING_PLACE(1),
    DRAFTING_SEE(-1),
    DRAFTING_META(-1),
    PATINA_REMOVE(2),
    PATINA_ADD(3),
    WAX_ADD(4),
    WAX_REMOVE(5);

    public static final BuildingPower[] ORDER_SET = {NONE, DRAFTING_PLACE, PATINA_REMOVE, PATINA_ADD, WAX_ADD, WAX_REMOVE};

    public final int order;

    BuildingPower(int order) {
        this.order = order;
    }
}
