package com.moekr.jlex;

import lombok.Data;

import java.util.*;

@Data
class DataSet {
	//附加代码
	private StringBuilder externalCode = new StringBuilder();
	//Lex类的成员变量
	private StringBuilder variableCode = new StringBuilder();
	//Lex类的构造方法中调用
	private StringBuilder constructorCode = new StringBuilder();
	//状态列表
	private List<String> stateList = new ArrayList<>();
	//宏转换表
	private Map<String,String> macroMap = new HashMap<>();
	//NFA状态列表
	private List<NfaState> nfaStateList = new ArrayList<>();
	//DFA状态列表
	private List<DfaState> dfaStateList = new ArrayList<>();
	//DFA状态转换列表
	private List<Transition> transitionList = new ArrayList<>();
	//NFA状态集与DFA状态转换表
	private Map<BitSet,DfaState> dfaStateMap = new HashMap<>();
	//Action列表
	private List<String> actionList = new ArrayList<>();
	//规则对应的NFA状态集合列表
	private List<List<NfaState>> ruleList = new ArrayList<>();

	private int[] anchorArray;
	private int[] columnMap;
	private int[] rowMap;
	private int[] characterToIndexMap;
	private int[] stateToTransitionMap;

	//字符集范围，US-ASCII字符集 + BOL + EOF
	private int range = 128 + 2;

	//reader是否读到文件尾
	private boolean reachEOF;
	//当前行号
	private int lineIndex;
	//当前行缓冲区
	private String lineBuffer;
	//当前行字符指针
	private int charIndex;

	private static DataSet instance;
	private DataSet(){
		stateList.add("INIT");
	}
	static DataSet getInstance(){
		if(instance == null){
			instance = new DataSet();
		}
		return instance;
	}
}
