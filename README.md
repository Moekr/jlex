# JLex #

1. 说明

	1. 描述文件及词法分析器的输入流编码均为`US-ASCII`（即通常所说的`ASCII`编码）

	2. 描述文件代码段中使用到的类用户将会自行解决import

	3. 输出的源代码文件为描述文件同目录下添加`.java`后缀

2. 描述文件结构

	> 描述文件共分四段，相互之间用`%%`进行分隔，分段内容可以为空但不能缺少分段，不允许注释的存在，除段落分隔符`%%`后至行末的内容及空行（空白字符包括空格、`\b`、`\t`、`\r`、`\n`等）会被忽略外，其余所有内容都将被解析，不在描述文件规范内的部分将造成解析失败（下方`//`开头的行仅为说明作用，不应出现在描述文件中）

		//第一段，代码段

		//额外代码，将被添加在输出文件的import部分之后，Lex类之前，可选
		%{
			......
		%}

		//变量代码，将被添加在Lex类的成员变量的最后部分，可选
		%variable{
			......
		%variable}

		//构造代码，将被添加在Lex类的构造方法的最后部分，可选
		%constructor{
			......
		%constructor}

		%%

		//第二段，状态列表

		//每行都将被解析为一个状态，添加到状态列表中，默认包含一个INIT状态，可选
		STATE1
		STATE2
		......

		%%

		//第三段，宏列表

		//每行都将被解析为一个宏，用于替换第四段正则规则中的宏，可选
		ALPHA=[A-Za-z]
		DIGIT=[0-9]
		......

		%%

		//第四段，规则列表

		//每一行都将被解析为一个规则，不可跨行，至少应有一个规则
		//语法为<STATE1,STATE2,......> EXPRESSION {ACTION}
		//语义为，当前状态在<STATE1,STATE2,......>内时，可以匹配该规则，如果成功匹配EXPRESSION，则执行{ACTION}
		<INIT> . {return new Token(tokenText());}
		......

3. 词法解析器相关

	1. `Lex`类对外开放两个构造方法`Lex()`和`Lex(InputStream inputStream)`，前者将使用标准输入作为输入流，后者将使用参数中的输入流

	2. `Lex`类中对外开放的解析方法为`Token lex() throws IOException`，返回值为`Token`类，该类没有给出定义，推荐在描述文件第一段额外代码中自行给出定义

	3. 规则对应的`ACTION`将在词法解析器解析出Token后被调用，如果为空（即`{}`）则当前Token将被舍弃，`lex`方法将不会返回Token且继续进行解析

	4. 规则对应的`ACTION`中可调用的方法有：

		1. `void setState(int state)`，该方法将设置当前状态（默认为`INIT`）

		2. `String tokenText()`，该方法将返回当前Token的字符串

		3. `int tokenLength()`，该方法将返回当前Token的长度

		> PS：虽然不只是能调用这些方法，但是只推荐调用这些方法，调用其他内部方法可能导致解析过程出错崩溃

4. 其他说明

	1. 使用IDE查看项目源代码时可能需要安装`lombok`插件

	2. 项目及描述文件结构参考了部分开源代码：

		[http://www.cs.princeton.edu/~appel/modern/java/JLex/](http://www.cs.princeton.edu/~appel/modern/java/JLex/ "http://www.cs.princeton.edu/~appel/modern/java/JLex/")

		[http://www.jflex.de/](http://www.jflex.de/ "http://www.jflex.de/")