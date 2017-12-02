package com.moekr.jlex;

import lombok.Data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Data
class Bunch {
    private String action;
    private int actionIndex;
    private int anchor;
    private List<NfaState> nfaStateList;
    private BitSet nfaStateSet;

    Bunch(){
        anchor = Constant.NONE;
        actionIndex = Integer.MAX_VALUE;
        nfaStateList = new ArrayList<>();
        nfaStateSet = new BitSet();
    }
}
