package com.moekr.jlex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.util.Arrays;

class LexGenerator {
    private final Log logger;
    private final DataSet dataSet;
    private final PrintWriter writer;

    LexGenerator(PrintWriter writer){
        logger = LogFactory.getLog(this.getClass());
        dataSet = DataSet.getInstance();
        this.writer = writer;
    }

    void generate(){
        writer.println("import java.io.*;");
        writer.println("import java.util.*;");
        writer.println();
        writer.println(dataSet.getExternalCode().toString());
        writer.println("class Lex{");
        logger.info("Generating constant section...");
        generateConstant();
        logger.info("Generating variable section...");
        generateVariable();
        logger.info("Generating constructor section...");
        generateConstructor();
        logger.info("Generating function section...");
        generateFunction();
        logger.info("Generating data section...");
        generateData();
        logger.info("Generating logic section...");
        generateLogic();
        writer.println("}");
    }

    private void generateConstant(){
        writer.println("\tprivate static final int BUFFER_SIZE = 512;");
        writer.println("\tprivate static final int FINAL = -1;");
        writer.println("\tprivate static final int NO_STATE = -1;");
        writer.println("\tprivate static final int NOT_ACCEPT = 0;");
        writer.println("\tprivate static final int START = 1;");
        writer.println("\tprivate static final int END = 2;");
        writer.println("\tprivate static final int NO_ANCHOR = 4;");
        writer.println("\tprivate static final int BOL = " + Constant.BOL + ";");
        writer.println("\tprivate static final int EOF = " + Constant.EOF + ";");
        for (int i = 0;i < dataSet.getStateList().size();i++){
            writer.println("\tprivate static final int " + dataSet.getStateList().get(i) + " = " + i + ";");
        }
        writer.print("\tprivate static final int stateTransition[] = {");
        for (int i = 0; i < dataSet.getStateToTransitionMap().length; i++){
            writer.print(dataSet.getStateToTransitionMap()[i]);
            if (i < dataSet.getStateToTransitionMap().length - 1) {
                writer.print(",");
            }else{
                writer.println("};");
            }
        }
        writer.println();
    }

    private void generateVariable(){
        writer.print(dataSet.getVariableCode().toString());
        writer.println("\tprivate BufferedReader reader;");
        writer.println("\tprivate char buffer[];");
        writer.println("\tprivate int bufferIndex;");
        writer.println("\tprivate int bufferLength;");
        writer.println("\tprivate int bufferStart;");
        writer.println("\tprivate int bufferEnd;");
        writer.println("\tprivate boolean atBOL;");
        writer.println("\tprivate int lexicalState;");
        writer.println();
    }
    
    private void generateConstructor(){
        writer.println("\tLex(){");
        writer.println("\t\tthis(System.in);");
        writer.println("\t}");
        writer.println();
        writer.println("\tLex(InputStream inputStream){");
        writer.println("\t\tObjects.requireNonNull(inputStream);");
        writer.println("\t\treader = new BufferedReader(new InputStreamReader(inputStream));");
        writer.println("\t\tbuffer = new char[BUFFER_SIZE];");
        writer.println("\t\tbufferIndex = 0;");
        writer.println("\t\tbufferLength = 0;");
        writer.println("\t\tbufferStart = 0;");
        writer.println("\t\tbufferEnd = 0;");
        writer.println("\t\tatBOL = true;");
        writer.println("\t\tlexicalState = INIT;");
        writer.print(dataSet.getConstructorCode().toString());
        writer.println("\t}");
        writer.println();
    }
    
    private void generateFunction(){
        //setState 设置当前状态
        writer.println("\tprivate void setState(int state){");
        writer.println("\t\tlexicalState = state;");
        writer.println("\t}");
        writer.println();

        //advance 向前读取
        writer.println("\tprivate int advance()throws IOException{");
        writer.println("\t\tif(bufferIndex < bufferLength){");
        writer.println("\t\t\treturn buffer[bufferIndex++];");
        writer.println("\t\t}");
        writer.println("\t\tif(bufferStart != 0){");
        writer.println("\t\t\tint i = bufferStart,j = 0;");
        writer.println("\t\t\twhile(i < bufferLength){");
        writer.println("\t\t\t\tbuffer[j] = buffer[i];");
        writer.println("\t\t\t\ti++;");
        writer.println("\t\t\t\tj++;");
        writer.println("\t\t\t}");
        writer.println("\t\t\tbufferEnd = bufferEnd - bufferStart;");
        writer.println("\t\t\tbufferStart = 0;");
        writer.println("\t\t\tbufferLength = j;");
        writer.println("\t\t\tbufferIndex = j;");
        writer.println("\t\t\tint length = reader.read(buffer,bufferLength,buffer.length - bufferLength);");
        writer.println("\t\t\tif(length == -1){");
        writer.println("\t\t\t\treturn EOF;");
        writer.println("\t\t\t}");
        writer.println("\t\t\tbufferLength = bufferLength + length;");
        writer.println("\t\t}");
        writer.println("\t\twhile(bufferIndex >= bufferLength){");
        writer.println("\t\t\tif(bufferIndex >= buffer.length){");
        writer.println("\t\t\t\tbuffer = doubleBuffer(buffer);");
        writer.println("\t\t\t}");
        writer.println("\t\t\tint length = reader.read(buffer,bufferLength,buffer.length - bufferLength);");
        writer.println("\t\t\tif(length == -1){");
        writer.println("\t\t\t\treturn EOF;");
        writer.println("\t\t\t}");
        writer.println("\t\t\tbufferLength = bufferLength + length;");
        writer.println("\t\t}");
        writer.println("\t\treturn buffer[bufferIndex++];");
        writer.println("\t}");
        writer.println();

        //moveEnd 移除换行符
        writer.println("\tprivate void moveEnd(){");
        writer.println("\t\tif(bufferEnd > bufferStart && buffer[bufferEnd - 1] == '\\n'){");
        writer.println("\t\t\tbufferEnd--;");
        writer.println("\t\t}");
        writer.println("\t\tif(bufferEnd > bufferStart && buffer[bufferEnd - 1] == '\\r'){");
        writer.println("\t\t\tbufferEnd--;");
        writer.println("\t\t}");
        writer.println("\t}");
        writer.println();

        //markStart 设置当前位置为Token起始位置
        writer.println("\tprivate void markStart(){");
        writer.println("\t\tbufferStart = bufferIndex;");
        writer.println("\t}");
        writer.println();

        //markEnd 设置当前位置为Token结束位置
        writer.println("\tprivate void markEnd(){");
        writer.println("\t\tbufferEnd = bufferIndex;");
        writer.println("\t}");
        writer.println();

        //toEnd 设置Token结束位置为当前位置
        writer.println("\tprivate void toEnd(){");
        writer.println("\t\tbufferIndex = bufferEnd;");
        writer.println("\t\tatBOL = bufferEnd > bufferStart && (buffer[bufferEnd - 1] == '\\r' || buffer[bufferEnd - 1] == '\\n');");
        writer.println("\t}");
        writer.println();

        //tokenText 获取当前Token的内容
        writer.println("\tprivate String tokenText(){");
        writer.println("\t\treturn new String(buffer,bufferStart,bufferEnd - bufferStart);");
        writer.println("\t}");
        writer.println();

        //tokenLength 获取当前Token的长度
        writer.println("\tprivate int tokenLength(){");
        writer.println("\t\treturn bufferEnd - bufferStart;");
        writer.println("\t}");
        writer.println();

        //doubleBuffer 将缓冲区容量翻倍
        writer.println("\tprivate char[] doubleBuffer(char buffer[]){");
        writer.println("\t\tchar[] newBuffer = new char[2 * buffer.length];");
        writer.println("\t\tfor(int i = 0;i < buffer.length;i++){");
        writer.println("\t\t\tnewBuffer[i] = buffer[i];");
        writer.println("\t\t}");
        writer.println("\t\treturn newBuffer;");
        writer.println("\t}");
        writer.println();
    }

    private void generateData(){
        writer.println("\tprivate int action[] = {");
        for (int i = 0; i < dataSet.getActionList().size(); i++) {
            String action = dataSet.getActionList().get(i);
            if (action != null) {
                boolean isStart = ((dataSet.getAnchorArray()[i] & Constant.START) != 0);
                boolean isEnd = ((dataSet.getAnchorArray()[i] & Constant.END) != 0);
                if (isStart && isEnd) {
                    writer.print("\t\tSTART | END");
                }else if(isStart) {
                    writer.print("\t\tSTART");
                } else if(isEnd) {
                    writer.print("\t\tEND");
                } else {
                    writer.print("\t\tNO_ANCHOR");
                }
            } else {
                writer.print("\t\tNOT_ACCEPT");
            }
            if (i < dataSet.getActionList().size() - 1) {
                writer.print(",");
            }
            writer.println();
        }
        writer.println("\t};");
        writer.println();

        int[] columnMap = new int[dataSet.getCharacterToIndexMap().length];
        for (int i = 0; i < dataSet.getCharacterToIndexMap().length; ++i){
            columnMap[i] = dataSet.getColumnMap()[dataSet.getCharacterToIndexMap()[i]];
        }
        writer.println("\tprivate int columnMap[] = new int[]" + Arrays.toString(columnMap).replace('[','{').replace(']','}') + ";");
        writer.println("\tprivate int rowMap[] = new int[]" + Arrays.toString(dataSet.getRowMap()).replace('[','{').replace(']','}') + ";");
        writer.println("\tprivate int moveMap[][] = new int[][]{");
        for (int i = 0;i < dataSet.getTransitionList().size();i++) {
            Transition transition = dataSet.getTransitionList().get(i);
            writer.print("\t\tnew int[]" + Arrays.toString(transition.getMoveMap()).replace('[','{').replace(']','}'));
            if(i < dataSet.getTransitionList().size() - 1){
                writer.print(",");
            }
            writer.println();
        }
        writer.println("\t};");
        writer.println();
    }
    
    private void generateLogic(){
        writer.println("\tToken lex()throws IOException{");
        writer.println("\t\tint lookAhead;");
        writer.println("\t\tint anchor = NO_ANCHOR;");
        writer.println("\t\tint state = stateTransition[lexicalState];");
        writer.println("\t\tint nextState = NO_STATE;");
        writer.println("\t\tint lastAcceptState = NO_STATE;");
        writer.println("\t\tboolean initTable = true;");
        writer.println("\t\tint acceptThis;");
        writer.println();
        writer.println("\t\tmarkStart();");
        writer.println("\t\tacceptThis = action[state];");
        writer.println("\t\tif (acceptThis != NOT_ACCEPT) {");
        writer.println("\t\t\tlastAcceptState = state;");
        writer.println("\t\t\tmarkEnd();");
        writer.println("\t\t}");
        writer.println("\t\twhile(true) {");
        writer.println("\t\t\tif(initTable && atBOL){");
        writer.println("\t\t\t\tlookAhead = BOL;");
        writer.println("\t\t\t}else{");
        writer.println("\t\t\t\tlookAhead = advance();");
        writer.println("\t\t\t}");
        writer.println("\t\t\tnextState = moveMap[rowMap[state]][columnMap[lookAhead]];");
        writer.println("\t\t\tif (EOF == lookAhead && initTable){");
        writer.println("\t\t\t\treturn null;");
        writer.println("\t\t\t}");
        writer.println("\t\t\tif (FINAL != nextState) {");
        writer.println("\t\t\t\tstate = nextState;");
        writer.println("\t\t\t\tinitTable = false;");
        writer.println("\t\t\t\tacceptThis = action[state];");
        writer.println("\t\t\t\tif (NOT_ACCEPT != acceptThis) {");
        writer.println("\t\t\t\t\tlastAcceptState = state;");
        writer.println("\t\t\t\t\tmarkEnd();");
        writer.println("\t\t\t\t}");
        writer.println("\t\t\t}else {");
        writer.println("\t\t\t\tif (NO_STATE == lastAcceptState) {");
        writer.println("\t\t\t\t\tthrow new RuntimeException(\"Unmatched Input.\");");
        writer.println("\t\t\t\t}else {");
        writer.println("\t\t\t\t\tanchor = action[lastAcceptState];");
        writer.println("\t\t\t\t\tif (0 != (END & anchor)) {");
        writer.println("\t\t\t\t\t\tmoveEnd();");
        writer.println("\t\t\t\t\t}");
        writer.println("\t\t\t\t\ttoEnd();");
        writer.println("\t\t\t\t\tswitch (lastAcceptState) {");
        for (int i = 0; i < dataSet.getActionList().size(); ++i) {
            String action =  dataSet.getActionList().get(i);
            if (null != action) {
                writer.println("\t\t\t\t\tcase " + i + ":" + action);
                writer.println("\t\t\t\t\tcase " + (-i) + ":break;");
            }
        }
        writer.println("\t\t\t\t\tdefault:");
        writer.println("\t\t\t\t\t\tthrow new RuntimeException(\"Internal Error.\");");
        writer.println("\t\t\t\t\t}");
        writer.println("\t\t\t\t\tinitTable = true;");
        writer.println("\t\t\t\t\tstate = stateTransition[lexicalState];");
        writer.println("\t\t\t\t\tnextState = NO_STATE;");
        writer.println("\t\t\t\t\tlastAcceptState = NO_STATE;");
        writer.println("\t\t\t\t\tmarkStart();");
        writer.println("\t\t\t\t\tacceptThis = action[state];");
        writer.println("\t\t\t\t\tif (NOT_ACCEPT != acceptThis) {");
        writer.println("\t\t\t\t\t\tlastAcceptState = state;");
        writer.println("\t\t\t\t\t\tmarkEnd();");
        writer.println("\t\t\t\t\t}");
        writer.println("\t\t\t\t}");
        writer.println("\t\t\t}");
        writer.println("\t\t}");
        writer.println("\t}");
    }
}
