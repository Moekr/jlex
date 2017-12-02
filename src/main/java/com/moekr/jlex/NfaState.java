package com.moekr.jlex;

import lombok.Data;

@Data
class NfaState {
	private int label;
	private int edge;
	private String action;
	private int anchor;
	private NfaState[] next;
	private ComplementBitSet characterSet;

	NfaState(){
		edge = Constant.EPSILON;
		anchor = Constant.NONE;
		next = new NfaState[2];
		characterSet = new ComplementBitSet();
	}

	void setAs(NfaState nfaState){
		edge = nfaState.edge;
		action = nfaState.action;
		anchor = nfaState.anchor;
		next[0] = nfaState.next[0];
		next[1] = nfaState.next[1];
		characterSet = (ComplementBitSet) nfaState.characterSet.clone();
	}
}
