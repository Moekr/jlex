package com.moekr.jlex;

abstract class ToolKit {
	static boolean isSpace(char c){
		return '\b' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c || ' ' == c;
	}

	static boolean isSpaceLine(String line){
		return line.chars().allMatch(value -> isSpace((char)value));
	}

	static String replaceSpace(String str){
		StringBuilder builder = new StringBuilder();
		str.chars().filter(value -> !isSpace((char)value)).forEachOrdered(value -> builder.append((char)value));
		return builder.toString();
	}

	static String replaceNewLine(String str){
		return str.replace("\n","");
	}

	static boolean isHex(char c){
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	static boolean isOct(char c){
		return c >= '0' && c <= '7';
	}

	static int hex2Dec(char c){
		if(c >= '0' && c <= '9'){
			return c - '0';
		}else if(c >= 'a' && c <= 'f'){
			return c - 'a' + 10;
		}else if(c >= 'A' && c <= 'F'){
			return c - 'A' + 10;
		}else {
			return 0;
		}
	}

	static int oct2Dec(char c){
		if(c >= '0' && c <= '7'){
			return c - '0';
		}else {
			return 0;
		}
	}
}
