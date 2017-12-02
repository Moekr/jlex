%{
class Application {
    public static void main(String[] args) throws IOException {
        Lex lex = new Lex();
        Token token;
        while ((token = lex.lex()) != null) {
            System.out.println(token);
        }
    }
}

class Token {
    private int index;
    private String text;
    Token(int index,String text){
        this.index = index;
        this.text = text;
    }
    public String toString() {
        return "< " + text + " >";
    }
}
%}

%variable{
    private int commentCount = 0;
%variable}

%%

COMMENT

%%

ALPHA=[A-Za-z]
DIGIT=[0-9]
NON_NEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
WHITE_SPACE_CHAR=[\n\ \t\b\012]
STRING_TEXT=(\\\"|[^\n\"]|\\{WHITE_SPACE_CHAR}+\\)*
COMMENT_TEXT=([^/*\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*

%%

<INIT> "," {return new Token(0,tokenText());}
<INIT> ":" {return new Token(1,tokenText());}
<INIT> ";" {return new Token(2,tokenText());}
<INIT> "(" {return new Token(3,tokenText());}
<INIT> ")" {return new Token(4,tokenText());}
<INIT> "[" {return new Token(5,tokenText());}
<INIT> "]" {return new Token(6,tokenText());}
<INIT> "{" {return new Token(7,tokenText());}
<INIT> "}" {return new Token(8,tokenText());}
<INIT> "." {return new Token(9,tokenText());}
<INIT> "+" {return new Token(10,tokenText());}
<INIT> "-" {return new Token(11,tokenText());}
<INIT> "*" {return new Token(12,tokenText());}
<INIT> "/" {return new Token(13,tokenText());}
<INIT> "=" {return new Token(14,tokenText());}
<INIT> "<>" {return new Token(15,tokenText());}
<INIT> "<"  {return new Token(16,tokenText());}
<INIT> "<=" {return new Token(17,tokenText());}
<INIT> ">"  {return new Token(18,tokenText());}
<INIT> ">=" {return new Token(19,tokenText());}
<INIT> "&"  {return new Token(20,tokenText());}
<INIT> "|"  {return new Token(21,tokenText());}
<INIT> ":=" {return new Token(22,tokenText());}
<INIT> {NON_NEWLINE_WHITE_SPACE_CHAR}+ {}
<INIT,COMMENT> \r {}
<INIT,COMMENT> \n {}
<INIT> "/*" {setState(COMMENT);commentCount++;}
<COMMENT> "/*" {commentCount++;}
<COMMENT> "*/" {commentCount--;if(commentCount == 0)setState(INIT);}
<COMMENT> {COMMENT_TEXT} {}
<INIT> \"{STRING_TEXT}\" {return new Token(23,tokenText().substring(1,tokenText().length() - 1));}
<INIT> \"{STRING_TEXT} {return new Token(24,tokenText().substring(1,tokenText().length()));}
<INIT> {DIGIT}+ {return new Token(25,tokenText());}
<INIT> {ALPHA}({ALPHA}|{DIGIT}|_)* {return new Token(26,tokenText());}
<INIT,COMMENT> . {return new Token(27,tokenText());}
