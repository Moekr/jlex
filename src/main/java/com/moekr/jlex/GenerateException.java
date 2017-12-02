package com.moekr.jlex;

import java.util.HashMap;
import java.util.Map;

class GenerateException extends RuntimeException {
	static final int SYNTAX_ERROR = 0;
	static final int UNEXPECTED_EOF = 1;
	static final int UNDEFINED_CODE_SECTION = 2;
	static final int MALFORMED_MACRO_DEFINE = 3;
	static final int MALFORMED_MACRO_USAGE = 4;
	static final int UNDEFINED_MACRO_NAME = 5;
	static final int MALFORMED_CONTROL_CHARACTER = 6;
	static final int MALFORMED_HEX_CHARACTER = 7;
	static final int MALFORMED_OCT_CHARACTER = 8;
	static final int NULL_RULE = 9;
	static final int UNEXPECTED_CYCLE_TOKEN = 10;
	static final int UNEXPECTED_CLOSE_TOKEN = 11;
	static final int UNEXPECTED_BOL_TOKEN = 12;
	static final int NULL_ACTION = 13;
	static final int MALFORMED_ACTION_DEFINE = 14;

	private static final Map<Integer, String> MESSAGE = new HashMap<>();
	static{
		MESSAGE.put(SYNTAX_ERROR, "Syntax error.");
		MESSAGE.put(UNEXPECTED_EOF, "Unexpected EOF found.");
		MESSAGE.put(UNDEFINED_CODE_SECTION, "Undefined code section.");
		MESSAGE.put(MALFORMED_MACRO_DEFINE, "Malformed macro define.");
		MESSAGE.put(MALFORMED_MACRO_USAGE, "Malformed macro usage.");
		MESSAGE.put(UNDEFINED_MACRO_NAME, "Undefined macro name.");
		MESSAGE.put(MALFORMED_CONTROL_CHARACTER, "Malformed control character.");
		MESSAGE.put(MALFORMED_HEX_CHARACTER, "Malformed hex character.");
		MESSAGE.put(MALFORMED_OCT_CHARACTER, "Malformed oct character.");
		MESSAGE.put(NULL_RULE, "Null rule.");
		MESSAGE.put(UNEXPECTED_CYCLE_TOKEN, "Unexpected cycle token.");
		MESSAGE.put(UNEXPECTED_CLOSE_TOKEN, "Unexpected close token.");
		MESSAGE.put(UNEXPECTED_BOL_TOKEN,"Unexpected BOL token.");
		MESSAGE.put(NULL_ACTION, "Null action.");
		MESSAGE.put(MALFORMED_ACTION_DEFINE, "Malformed action define.");
	}

	GenerateException(int code){
		super("Line " + DataSet.getInstance().getLineIndex() + ", index " + DataSet.getInstance().getCharIndex() + ": " + MESSAGE.getOrDefault(code, "Unknown error."));
		System.err.print(DataSet.getInstance().getLineBuffer());
		for(int i = 0;i < DataSet.getInstance().getCharIndex();i++){
			System.err.print(' ');
		}
		System.err.println("^");
	}
}
