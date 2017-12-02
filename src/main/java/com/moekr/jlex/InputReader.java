package com.moekr.jlex;

import java.io.*;

class InputReader {
	private DataSet dataSet;
	private BufferedReader reader;

	InputReader(String file) throws FileNotFoundException, UnsupportedEncodingException {
		dataSet = DataSet.getInstance();
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"US-ASCII"));
	}

	boolean readLine() throws IOException {
		if(dataSet.isReachEOF()){
			return true;
		}
		while (true){
			String buffer;
			dataSet.setCharIndex(0);
			if((buffer = reader.readLine()) == null){
				dataSet.setReachEOF(true);
				return true;
			}
			dataSet.setLineBuffer(buffer + "\n");
			dataSet.setLineIndex(dataSet.getLineIndex() + 1);
			if(!ToolKit.isSpaceLine(dataSet.getLineBuffer())){
				return false;
			}
		}
	}

	void close() throws IOException {
		reader.close();
	}
}
