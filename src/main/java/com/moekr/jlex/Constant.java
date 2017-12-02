package com.moekr.jlex;

import java.util.HashMap;
import java.util.Map;

abstract class Constant {
	//BOL和EOF的字符定义，接续在US-ASCII字符集后
	static final int BOL = 128;
	static final int EOF = 129;

	//NFA边类型定义
	static final int CHARACTER_CLASS = -1;
	static final int EMPTY = -2;
	static final int EPSILON = -3;

	//Anchor类型定义
	static final int NONE = 0;
	static final int START = 1;
	static final int END = 2;

	//DFA特殊状态定义
	static final int FINAL = -1;
	static final int NOT_IN_DFA_STATES = -1;

	//Token定义
	static final int ASTERISK = 1;
	static final int PLUS = 2;
	static final int MINUS = 3;
	static final int ANY = 4;
	static final int OPTIONAL = 5;
	static final int OR = 6;
	static final int BEGIN_OF_LINE = 7;
	static final int END_OF_LINE = 8;
	static final int LEFT_PAREN = 9;
	static final int RIGHT_PAREN = 10;
	static final int LEFT_BRACKET = 11;
	static final int RIGHT_BRACKET = 12;
	static final int LEFT_BRACE = 13;
	static final int RIGHT_BRACE = 14;
	static final int END_OF_STATEMENT = 15;
	static final int END_OF_INPUT = 16;
	static final int L = 17;

	static final Map<Character,Integer> TOKEN_MAP = new HashMap<>();

	static {
		TOKEN_MAP.put('*', ASTERISK);
		TOKEN_MAP.put('+', PLUS);
		TOKEN_MAP.put('-', MINUS);
		TOKEN_MAP.put('.', ANY);
		TOKEN_MAP.put('?', OPTIONAL);
		TOKEN_MAP.put('|', OR);
		TOKEN_MAP.put('^', BEGIN_OF_LINE);
		TOKEN_MAP.put('$', END_OF_LINE);
		TOKEN_MAP.put('(', LEFT_PAREN);
		TOKEN_MAP.put(')', RIGHT_PAREN);
		TOKEN_MAP.put('[', LEFT_BRACKET);
		TOKEN_MAP.put(']', RIGHT_BRACKET);
		TOKEN_MAP.put('{', LEFT_BRACE);
		TOKEN_MAP.put('}', RIGHT_BRACE);
	}
}
