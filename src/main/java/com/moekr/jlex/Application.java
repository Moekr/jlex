package com.moekr.jlex;

import java.io.IOException;

public abstract class Application {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java -jar JLex.jar <filename>");
		}else {
			Generator generator = new Generator(args[0]);
			generator.generate();
		}
	}
}
