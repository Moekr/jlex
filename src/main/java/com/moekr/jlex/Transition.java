package com.moekr.jlex;

import lombok.Data;

@Data
class Transition {
	private int label;
	private String action;
	private int anchor;
	private int[] moveMap;

	Transition(){
		anchor = Constant.NONE;
	}
}
