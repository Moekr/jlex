package com.moekr.jlex;

import lombok.Data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Data
class DfaState {
	private int label;
	private String action;
	private int anchor;
	private List<NfaState> nfaStateList;
	private BitSet nfaStateSet;
	private boolean mark;

	DfaState(){
		anchor = Constant.NONE;
		nfaStateList = new ArrayList<>();
		nfaStateSet = new BitSet();
	}
}
