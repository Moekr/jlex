package com.moekr.jlex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.*;

class NfaGenerator {
	private final Log logger;
	private final DataSet dataSet;
	private final InputReader reader;

	private int currentToken;
	private char lexeme;
	private boolean advanceStop;
	private boolean inQuote;
	private boolean inCharacterClass;

	NfaGenerator(InputReader reader){
		logger = LogFactory.getLog(this.getClass());
		dataSet = DataSet.getInstance();
		this.reader = reader;
	}

	void generate() throws IOException {
		for(int i = 0;i < dataSet.getStateList().size();i++){
			dataSet.getRuleList().add(new ArrayList<>());
		}
		logger.info("Parsing nondeterministic finite automaton(NFA)...");
		parseNfaMachine();
		logger.info("Parsed " + dataSet.getNfaStateList().size() + " NFA states.");
		for(int i = 0; i < dataSet.getNfaStateList().size(); i++){
			dataSet.getNfaStateList().get(i).setLabel(i);
		}
		logger.info("Simplifying nondeterministic finite automaton(NFA)...");
		simplify();
		logger.info("Simplified NFA to " + dataSet.getRange() + " distinct character classes.");
	}

	//解析NFA状态机
	private void parseNfaMachine() throws IOException {
		NfaState current = Factory.createNfaState();
		BitSet states = parseState();
		currentToken = Constant.END_OF_STATEMENT;
		advance();
		if(currentToken != Constant.END_OF_INPUT){
			current.getNext()[0] = parseRule();
			processState(states,current.getNext()[0]);
		}
		while (currentToken != Constant.END_OF_INPUT){
			if(reader.readLine()){
				break;
			}
			states = parseState();
			advance();
			if(currentToken == Constant.END_OF_INPUT){
				break;
			}
			current.getNext()[1] = Factory.createNfaState();
			current = current.getNext()[1];
			current.getNext()[0] = parseRule();
			processState(states, current.getNext()[0]);
		}
		states = new BitSet();
		for(int i = 0;i < dataSet.getStateList().size();i++){
			states.set(i);
		}
		current.getNext()[1] = Factory.createNfaState();
		current = current.getNext()[1];
		current.getNext()[0] = Factory.createNfaState();
		current.getNext()[0].setEdge(Constant.CHARACTER_CLASS);
		current.getNext()[0].getNext()[0] = Factory.createNfaState();
		current.getNext()[0].getCharacterSet().add(Constant.BOL);
		current.getNext()[0].getCharacterSet().add(Constant.EOF);
		current.getNext()[0].getNext()[0].setAction("");
		processState(states,current.getNext()[0]);
	}

	//解析规则接受的状态列表
	private BitSet parseState() throws IOException {
		while (true){
			if (ToolKit.isSpaceLine(dataSet.getLineBuffer())){
				if(reader.readLine()){
					return null;
				}
			}else {
				break;
			}
		}
		BitSet states = new BitSet();
		if (dataSet.getLineBuffer().trim().startsWith("<")) {
			String[] stateArray = dataSet.getLineBuffer().substring(dataSet.getLineBuffer().indexOf('<') + 1, dataSet.getLineBuffer().indexOf('>')).split(",");
			for (String state : stateArray) {
				int index = dataSet.getStateList().indexOf(ToolKit.replaceSpace(state));
				if (index >= 0) {
					states.set(index);
				}
			}
			dataSet.setCharIndex(dataSet.getLineBuffer().indexOf('>') + 1);
		}else {
			for(int i = 0;i < dataSet.getStateList().size();i++){
				states.set(i);
			}
		}
		advanceStop = true;
		return states;
	}

	//向前读取
	private void advance() throws IOException{
		boolean sawEscape;
		if(dataSet.isReachEOF()){
			currentToken = Constant.END_OF_INPUT;
			lexeme = '\0';
			return;
		}
		if(currentToken == Constant.END_OF_STATEMENT || dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
			if(inQuote){
				throw new GenerateException(GenerateException.SYNTAX_ERROR);
			}
			while (true){
				if(!advanceStop || dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
					if(reader.readLine()){
						currentToken = Constant.END_OF_INPUT;
						lexeme = '\0';
						return;
					}
				}else {
					advanceStop = false;
				}
				while (dataSet.getCharIndex() < dataSet.getLineBuffer().length() && ToolKit.isSpace(dataSet.getLineBuffer().charAt(dataSet.getCharIndex()))){
					dataSet.setCharIndex(dataSet.getCharIndex() + 1);
				}
				if(dataSet.getCharIndex() < dataSet.getLineBuffer().length()){
					break;
				}
			}
		}
		while (true){
			if(!inQuote && dataSet.getLineBuffer().charAt(dataSet.getCharIndex()) == '{'){
				replaceMacro();
				if(dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
					currentToken = Constant.END_OF_STATEMENT;
					lexeme = '\0';
					return;
				}
			}else if(dataSet.getLineBuffer().charAt(dataSet.getCharIndex()) == '\"'){
				inQuote = !inQuote;
				dataSet.setCharIndex(dataSet.getCharIndex() + 1);
				if(dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
					currentToken = Constant.END_OF_STATEMENT;
					lexeme = '\0';
					return;
				}
			}else {
				break;
			}
		}
		sawEscape = dataSet.getLineBuffer().charAt(dataSet.getCharIndex()) == '\\';
		if(!inQuote){
			if(!inCharacterClass && ToolKit.isSpace(dataSet.getLineBuffer().charAt(dataSet.getCharIndex()))){
				currentToken = Constant.END_OF_STATEMENT;
				lexeme = '\0';
				return;
			}
			if(sawEscape){
				lexeme = parseEscape();
			}else {
				lexeme = dataSet.getLineBuffer().charAt(dataSet.getCharIndex());
				dataSet.setCharIndex(dataSet.getCharIndex() + 1);
			}
		}else {
			if(sawEscape && dataSet.getCharIndex() + 1 < dataSet.getLineBuffer().length() && dataSet.getLineBuffer().charAt(dataSet.getCharIndex() + 1) == '\"'){
				lexeme = '\"';
				dataSet.setCharIndex(dataSet.getCharIndex() + 2);
			}else {
				lexeme = dataSet.getLineBuffer().charAt(dataSet.getCharIndex());
				dataSet.setCharIndex(dataSet.getCharIndex() + 1);
			}
		}
		int code = Constant.TOKEN_MAP.getOrDefault(lexeme,-1);
		if(inQuote || sawEscape){
			currentToken = Constant.L;
		}else {
			if(code == -1){
				currentToken = Constant.L;
			}else {
				currentToken = code;
			}
		}
		if(currentToken == Constant.LEFT_BRACKET){
			inCharacterClass = true;
		}else if(currentToken == Constant.RIGHT_BRACKET){
			inCharacterClass = false;
		}
	}

	//替换规则中的宏
	private void replaceMacro(){
		String lineBuffer = dataSet.getLineBuffer();
		int charIndex = dataSet.getCharIndex();
		Map<String,String> macroMap = dataSet.getMacroMap();

		int endTag = lineBuffer.indexOf('}',charIndex);
		if(endTag == -1 || endTag == charIndex + 1){
			throw new GenerateException(GenerateException.MALFORMED_MACRO_USAGE);
		}
		String macroName = lineBuffer.substring(charIndex + 1,endTag);
		String macroValue = macroMap.get(macroName);
		if(macroValue == null){
			throw new GenerateException(GenerateException.UNDEFINED_MACRO_NAME);
		}
		String before = lineBuffer.substring(0,charIndex);
		String after = lineBuffer.substring(endTag + 1);
		dataSet.setLineBuffer(before + macroValue + after);
	}

	//解析转义字符
	private char parseEscape(){
		String lineBuffer = dataSet.getLineBuffer();
		int charIndex = dataSet.getCharIndex();

		char c;
		boolean unicode = false;

		switch (lineBuffer.charAt(charIndex + 1)){
			case ' ':
				dataSet.setCharIndex(charIndex + 2);
				return  ' ';
			case 't':
				dataSet.setCharIndex(charIndex + 2);
				return '\t';
			case 'r':
				dataSet.setCharIndex(charIndex + 2);
				return '\r';
			case 'n':
				dataSet.setCharIndex(charIndex + 2);
				return '\n';
			case 'b':
				dataSet.setCharIndex(charIndex + 2);
				return '\b';
			case 'f':
				dataSet.setCharIndex(charIndex + 2);
				return '\f';
			case '^':
				c = Character.toUpperCase(lineBuffer.charAt(charIndex + 2));
				if(c < '@' || c > '_'){
					throw new GenerateException(GenerateException.MALFORMED_CONTROL_CHARACTER);
				}
				dataSet.setCharIndex(charIndex + 3);
				return (char) (c - '@');
			case 'u':
				unicode = true;
			case 'x':
				c = 0;
				for(int i = 0;i < (unicode ? 4 : 2);i++){
					if(!ToolKit.isHex(lineBuffer.charAt(charIndex + 2 + i))){
						throw new GenerateException(GenerateException.MALFORMED_HEX_CHARACTER);
					}
					c = (char) ((c << 4) + ToolKit.hex2Dec(lineBuffer.charAt(charIndex + 2 + i)));
				}
				dataSet.setCharIndex(charIndex + 2 + (unicode ? 4 : 2));
				return c;
			default:
				if(ToolKit.isOct(lineBuffer.charAt(charIndex + 1))){
					c = 0;
					for(int i = 0;i < 3;i++){
						if(!ToolKit.isOct(lineBuffer.charAt(charIndex + 1 + i))){
							throw new GenerateException(GenerateException.MALFORMED_OCT_CHARACTER);
						}
						c = (char) ((c << 3) + ToolKit.oct2Dec(lineBuffer.charAt(charIndex + 1 + i)));
					}
					dataSet.setCharIndex(charIndex + 4);
					return c;
				}else {
					dataSet.setCharIndex(charIndex + 2);
					return lineBuffer.charAt(charIndex + 1);
				}
		}
	}

	//解析规则
	private NfaState parseRule() throws IOException{
		NfaPair pair = new NfaPair();
		NfaState start,end;
		int anchor = Constant.NONE;

		if(currentToken == Constant.BEGIN_OF_LINE){
			/*
				解析以^开头的规则

				N---------S- ..... -E
				    BOL
			*/
			anchor = anchor | Constant.START;
			advance();
			parseExpression(pair);
			start = Factory.createNfaState();
			start.setEdge(Constant.BOL);
			start.getNext()[0] = pair.getStart();
			end = pair.getEnd();
		}else {
			/*
				解析不以^开头的规则

				S- ..... -E
			*/
			parseExpression(pair);
			start = pair.getStart();
			end = pair.getEnd();
		}
		if(currentToken == Constant.END_OF_LINE){
			/*
				解析以$结尾的规则

				S- ..... -E------------------------NLE
				           \          EOF          /
				          ε \---------NLS- ..... -/
			*/
			advance();
			pair = Factory.createNewLineNfaPair();
			end.setEdge(Constant.EOF);
			end.getNext()[0] = pair.getEnd();
			end.getNext()[1] = pair.getStart();
			end = pair.getEnd();
			anchor = anchor | Constant.END;
		}
		if(end == null){
			throw new GenerateException(GenerateException.NULL_RULE);
		}
		end.setAction(parseAction());
		end.setAnchor(anchor);
		return start;
	}

	//处理状态集
	private void processState(BitSet state, NfaState current){
		for(int i = 0;i < dataSet.getStateList().size();i++){
			if(state.get(i)){
				dataSet.getRuleList().get(i).add(current);
			}
		}
	}

	/*
		解析表达式，考虑或的存在

		1-----------S-- ..... --E
		 \    ε                /
		ε \---TS- ..... -TE---/ ε
	*/
	private void parseExpression(NfaPair pair) throws IOException{
		NfaPair tmp = new NfaPair();
		NfaState nfaState;
		parseSubExpress(pair);
		while(currentToken == Constant.OR){
			advance();
			parseSubExpress(tmp);

			nfaState = Factory.createNfaState();
			nfaState.getNext()[0] = pair.getStart();
			nfaState.getNext()[1] = tmp.getStart();
			tmp.getEnd().getNext()[0] = pair.getEnd();
			pair.setStart(nfaState);
		}
	}

	//解析表达式，不考虑或的存在
	private void parseSubExpress(NfaPair pair) throws IOException {
		if(notReachEOE()){
			parseCycleExpress(pair);
		}
		NfaPair tmp = new NfaPair();
		while (notReachEOE()){
			parseCycleExpress(tmp);
			pair.getEnd().setAs(tmp.getStart());
			dataSet.getNfaStateList().remove(tmp.getStart());
			pair.setEnd(tmp.getEnd());
		}
	}

	//没有解析到子表达式的结束
	private boolean notReachEOE(){
		switch (currentToken) {
			case Constant.RIGHT_PAREN:
			case Constant.END_OF_LINE:
			case Constant.OR:
			case Constant.END_OF_STATEMENT:
				return false;
			case Constant.ASTERISK:
			case Constant.PLUS:
			case Constant.OPTIONAL:
				throw new GenerateException(GenerateException.UNEXPECTED_CYCLE_TOKEN);
			case Constant.RIGHT_BRACKET:
				throw new GenerateException(GenerateException.UNEXPECTED_CLOSE_TOKEN);
			case Constant.BEGIN_OF_LINE:
				throw new GenerateException(GenerateException.UNEXPECTED_BOL_TOKEN);
			default:
				return true;
		}
	}

	/*
		解析表达式，考虑循环的存在

		*:
			  /-------------------------\
			 /             ε             \
			1---------S- ..... -E---------2
			     ε     \       /      ε
			            \<----/
		                   ε
		+:
			1---------S- ..... -E---------2
			     ε     \       /      ε
			            \<----/
		                   ε
		?:
			  /-------------------------\
			 /             ε             \
			1---------S- ..... -E---------2
			     ε                   ε
	*/
	private void parseCycleExpress(NfaPair pair) throws IOException {
		parseBaseExpress(pair);
		if(currentToken == Constant.ASTERISK || currentToken == Constant.PLUS || currentToken == Constant.OPTIONAL){
			NfaState start = Factory.createNfaState(),end = Factory.createNfaState();
			start.getNext()[0] = pair.getStart();
			pair.getEnd().getNext()[0] = end;
			if(currentToken == Constant.ASTERISK || currentToken == Constant.OPTIONAL){
				start.getNext()[1] = end;
			}
			if(currentToken == Constant.ASTERISK || currentToken == Constant.PLUS){
				pair.getEnd().getNext()[1] = pair.getStart();
			}
			pair.setStart(start);
			pair.setEnd(end);
			advance();
		}
	}

	//解析表达式，不考虑循环
	private void parseBaseExpress(NfaPair pair) throws IOException {
		if(currentToken == Constant.LEFT_PAREN){
			advance();
			parseExpression(pair);
			if(currentToken == Constant.RIGHT_PAREN){
				advance();
			}else {
				throw new GenerateException(GenerateException.SYNTAX_ERROR);
			}
		}else{
			NfaState start = Factory.createNfaState();
			pair.setStart(start);
			start.getNext()[0] = Factory.createNfaState();
			pair.setEnd(start.getNext()[0]);
			if(!(currentToken == Constant.ANY || currentToken == Constant.LEFT_BRACKET)){
				start.setEdge(lexeme);
				advance();
			}else{
				start.setEdge(Constant.CHARACTER_CLASS);
				if(currentToken == Constant.ANY){
					start.getCharacterSet().add('\n');
					start.getCharacterSet().add('\r');
					start.getCharacterSet().add(Constant.BOL);
					start.getCharacterSet().add(Constant.EOF);
					start.getCharacterSet().complement();
				}else{
					advance();
					if(currentToken == Constant.BEGIN_OF_LINE){
						advance();
						start.getCharacterSet().add(Constant.BOL);
						start.getCharacterSet().add(Constant.EOF);
						start.getCharacterSet().complement();
					}
					if(currentToken != Constant.RIGHT_BRACKET){
						parseCharacterCluster(start.getCharacterSet());
					}
				}
				advance();
			}
		}
	}

	//解析字符簇
	private void parseCharacterCluster(ComplementBitSet complementBitSet) throws IOException {
		int first = -1;
		while (currentToken != Constant.END_OF_STATEMENT && currentToken != Constant.RIGHT_BRACKET){
			if(currentToken == Constant.MINUS && first != -1){
				advance();
				if(currentToken == Constant.RIGHT_BRACKET){
					complementBitSet.add('-');
					break;
				}
				for(;first < lexeme;first++){
					complementBitSet.add(first);
				}
			}else {
				first = lexeme;
				complementBitSet.add(lexeme);
			}
			advance();
		}
	}

	//解析匹配规则后的行为
	private String parseAction() {
		if (dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
			throw new GenerateException(GenerateException.NULL_ACTION);
		}
		while (ToolKit.isSpace(dataSet.getLineBuffer().charAt(dataSet.getCharIndex()))){
			dataSet.setCharIndex(dataSet.getCharIndex() + 1);
			if (dataSet.getCharIndex() >= dataSet.getLineBuffer().length()){
				throw new GenerateException(GenerateException.NULL_ACTION);
			}
		}
		if(dataSet.getLineBuffer().charAt(dataSet.getCharIndex()) != '{'){
			throw new GenerateException(GenerateException.MALFORMED_ACTION_DEFINE);
		}
		int end = dataSet.getLineBuffer().lastIndexOf('}');
		if(end == -1 || end < dataSet.getCharIndex()){
			throw new GenerateException(GenerateException.MALFORMED_ACTION_DEFINE);
		}
		String action = dataSet.getLineBuffer().substring(dataSet.getCharIndex(), end + 1);
		dataSet.setCharIndex(end + 1);
		return action;
	}

	private void simplify(){
		int[] characterClassMap = new int[dataSet.getRange()];
		int nextCharacter = 1;
		BitSet characterClassA = new BitSet(),characterClassB = new BitSet();
		for (NfaState nfaState :dataSet.getNfaStateList()){
			if (nfaState.getEdge() == Constant.EMPTY || nfaState.getEdge() == Constant.EPSILON){
				continue;
			}
			characterClassA.clear();
			characterClassB.clear();
			for (int i = 0;i < characterClassMap.length;i++){
				if (nfaState.getEdge() == i || (nfaState.getEdge() == Constant.CHARACTER_CLASS && nfaState.getCharacterSet().contains(i))){
					characterClassA.set(characterClassMap[i]);
				}else {
					characterClassB.set(characterClassMap[i]);
				}
			}
			characterClassA.and(characterClassB);
			if(characterClassA.length() == 0){
				continue;
			}
			Map<Integer,Integer> map = new HashMap<>();
			for (int i = 0;i < characterClassMap.length;i++){
				if (characterClassA.get(characterClassMap[i])){
					if (nfaState.getEdge() == i || (nfaState.getEdge() == Constant.CHARACTER_CLASS && nfaState.getCharacterSet().contains(i))){
						int split = characterClassMap[i];
						if (!map.containsKey(split)){
							map.put(split, nextCharacter);
							nextCharacter++;
						}
						characterClassMap[i] = map.get(split);
					}
				}
			}
		}
		dataSet.setRange(nextCharacter);
		for (NfaState nfaState :dataSet.getNfaStateList()){
			if (nfaState.getEdge() == Constant.EMPTY || nfaState.getEdge() == Constant.EPSILON){
				continue;
			}
			if (nfaState.getEdge() == Constant.CHARACTER_CLASS) {
				ComplementBitSet bitSet = new ComplementBitSet();
				bitSet.map(nfaState.getCharacterSet(), characterClassMap);
				nfaState.setCharacterSet(bitSet);
			} else {
				nfaState.setEdge(characterClassMap[nfaState.getEdge()]);
			}
		}
		dataSet.setCharacterToIndexMap(characterClassMap);
	}
}
