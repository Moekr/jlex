package com.moekr.jlex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

class Generator {
	private final Log logger;
	private final DataSet dataSet;
	private final InputReader reader;
	private final PrintWriter writer;
	private final NfaGenerator nfaGenerator;
	private final DfaGenerator dfaGenerator;
	private final LexGenerator lexGenerator;

	Generator(String file) throws FileNotFoundException, UnsupportedEncodingException {
		logger = LogFactory.getLog(this.getClass());
		dataSet = DataSet.getInstance();
		reader = new InputReader(file);
		logger.info("Input lex define file: " + file);
		writer = new PrintWriter(file + ".java","US-ASCII");
		logger.info("Output lex source code file: " + file + ".java");
		nfaGenerator = new NfaGenerator(reader);
		dfaGenerator = new DfaGenerator();
		lexGenerator = new LexGenerator(writer);
	}

	void generate() throws IOException {
		logger.info("Parsing customized code section...");
		parseCode();
		logger.info("Parsing customized state list section...");
		parseState();
		logger.info("Parsed " + (dataSet.getStateList().size() - 1) + " customized state.");
		logger.info("Parsing macro list section...");
		parseMacro();
		logger.info("Parsed " + dataSet.getMacroMap().size() + " macro.");
		logger.info("Parsing regular expression rule section...");
		parseRule();
		logger.info("Generating lexical analyzer source code...");
		lexGenerator.generate();
		logger.info("Finished, use \"javac <source file>\" to compile.");
		writer.flush();
		writer.close();
		reader.close();
	}

	private void parseCode() throws IOException {
		while (true){
			if(reader.readLine()){
				throw new GenerateException(GenerateException.UNEXPECTED_EOF);
			}
			if(dataSet.getLineBuffer().startsWith("%%")){
				return;
			}
			if(dataSet.getLineBuffer().startsWith("%")){
				String endTag;
				StringBuilder codeBuilder;
				switch (dataSet.getLineBuffer()){
					case "%{\n":
						endTag = "%}\n";
						codeBuilder = dataSet.getExternalCode();
						break;
					case "%variable{\n":
						endTag = "%variable}\n";
						codeBuilder = dataSet.getVariableCode();
						break;
					case "%constructor{\n":
						endTag = "%constructor}\n";
						codeBuilder = dataSet.getConstructorCode();
						break;
					default:
						throw new GenerateException(GenerateException.UNDEFINED_CODE_SECTION);
				}
				while (true){
					if(reader.readLine()){
						throw new GenerateException(GenerateException.UNEXPECTED_EOF);
					}
					if(dataSet.getLineBuffer().equals(endTag)){
						break;
					}
					codeBuilder.append(dataSet.getLineBuffer());
				}
			}else{
				throw new GenerateException(GenerateException.SYNTAX_ERROR);
			}
		}
	}

	private void parseState() throws IOException {
		while (true){
			if(reader.readLine()){
				throw new GenerateException(GenerateException.UNEXPECTED_EOF);
			}
			if(dataSet.getLineBuffer().startsWith("%%")){
				return;
			}
			dataSet.getStateList().add(ToolKit.replaceNewLine(dataSet.getLineBuffer()));
		}
	}

	private void parseMacro() throws IOException {
		while (true){
			if(reader.readLine()){
				throw new GenerateException(GenerateException.UNEXPECTED_EOF);
			}
			if(dataSet.getLineBuffer().startsWith("%%")){
				return;
			}
			String[] macro = dataSet.getLineBuffer().split("=");
			if(macro.length != 2){
				throw new GenerateException(GenerateException.MALFORMED_MACRO_DEFINE);
			}
			dataSet.getMacroMap().put(macro[0], ToolKit.replaceNewLine(macro[1]));
		}
	}

	private void parseRule() throws IOException {
		if(reader.readLine()){
			throw new GenerateException(GenerateException.UNEXPECTED_EOF);
		}
		nfaGenerator.generate();
		dfaGenerator.generate();
	}
}
