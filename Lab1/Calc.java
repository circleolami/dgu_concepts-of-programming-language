import java.io.*;

class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER=256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken( )  { /* tokens are characters */
        while(true) {
            try  {
	            ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else 
                    if (Character.isDigit(ch)) {
                        value = number( );
	               input.unread(ch);
		     return NUMBER;
	          }
	          else return ch;
	  } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number( )  {
    /* number -> digit { digit } */
        int result = ch - '0';
        try  {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch -'0';
                ch = input.read(); 
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error( ) {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) { 
        if (token == c) 
	    token = getToken();
        else error();
    }

    void command( ) {
    /* command -> expr '\n' */
        Object result = expr();
        if (token == '\n') {
            System.out.printf("The result is: ");
            System.out.println(result);
        }
        else error();
    }

    Object expr() {
    /* <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false */
        // token의 값에 따라 분류, match()를 실행 후 result에 저장
    	Object result;
    	if (token == '!'){
    		match('!');
    		result = !(boolean) expr();
    	}// token이 !일 때
    	else if (token == 't'){
    		match('t');
    		result = true;
    	} // token이 t일 때
    	else if (token == 'f') {
            match('f');
            result = false;
        } // token이 f일 때
    	else {
    		/* <bexp> {& <bexp> | '|'<bexp>} */
    		result = bexp();
            // token을 2번 입력받음
            // result와 bexpResult에 bexp()를 실행하여 값을 입력받고, result와 bexpResult를 비교연산하여 result에 저장
    		while (token == '&' || token == '|') {
    			if (token == '&'){
                    match('&');
                    Object bexpResult = bexp();
    				result = (boolean) result && (boolean) bexpResult;
    			} // token이 &일 때
    			else if (token == '|'){
                    match('|');
                    Object bexpResult = bexp();
    				result = (boolean) result || (boolean) bexpResult;
    			} // token이 |일 때
    		}
    	}
    	return result;  // result를 반환
	}

    Object bexp( ) {
    /* <bexp> -> <aexp> [<relop> <aexp>] */
        int aexp1 = aexp();
    	Object result = aexp1;
        // aexp()를 2번 실행하고, token의 값에 따라 비교 연산을 시행
    	if (token == '<' || token == '>' || token == '=' || token == '!'){ // <relop>
    		String op = relop();    // relop()을 이용하여 문자열로 변환
            int aexp2 = aexp();
            switch(op){
                case "<": result = (boolean)(aexp1 < aexp2); break;
                case "<=": result = (boolean)(aexp1 <= aexp2); break;
                case ">": result = (boolean)(aexp1 > aexp2); break;
                case ">=": result = (boolean)(aexp1 >= aexp2); break;
                case "==": result = (boolean)(aexp1 == aexp2); break;
                case "!=": result = (boolean)(aexp1 != aexp2); break;
            }
    	}
		else {
			error();
		}   // token이 관계 연산자가 아닐 때 error() 실행
    	return result;		
	}

    String relop() {    	
    /* <relop> -> ( < | <= | > | >= | == | != ) */    	
    	String result = "";
        // 관계 연산자 2개가 같이 쓰일 때가 있으므로 문자열로 변환
    	if (token == '<'){
            match('<');
            if(token == '='){
                match('=');
                result = "<=";
            }
            else{
                result = "<";
            }
        }
        else if (token == '>'){
            match('>');
            if(token == '='){
                match('=');
                result = ">=";
            }
            else
                result = ">";
        }
        else if(token == '='){
            match('=');
            if(token == '='){
                match('=');
                result = "==";
            }
            else
                error();
        }
        else if(token =='!'){
            match('!');
            if(token == '='){
                match('=');
                result = "!=";
            }
            else
                error();    // token이 관계 연산자가 아닐 때 error() 실행
        }
    	return result;  // result 반환
	}

    int aexp() {
    /* expr -> term { '+' term } */
        int result = term();
        // +, -의 산술 연산자를 처리, 각 산술 연산자에 맞는 연산 후 result에 저장
        while (token == '+' || token == '-') {
            int currentToken = token;
            if (token == '+') {
                match('+');
                result += term();
            } else if (token == '-') {
                match('-');
                result -= term();
            }
        }   // token에 산술 연산자가 들어올 때까지 계속 반복 실행
        return result;  // result 반환
    }

    int term( ) {
    /* term -> factor { '*' factor } */
       int result = factor();
       // *, /의 산술 연산자를 처리, 각 산술 연산자에 맞는 연산 후 result에 저장
       while (token == '*' || token == '/') {
           int currentToken = token;
           if (token == '*'){
               match('*');
               result *= factor();
           }
           else if(token == '/') {
               match('/');
               result /= factor();
           }
       }    // token에 산술 연산자가 들어올 때까지 계속 반복 실행
       return result;   // result 반환
    }

    int factor() {
    /* factor -> '(' expr ')' | number */
        int result = 0;
        if (token == '(') {
            match('(');
            result = aexp();
            match(')');
        }
        else if (token == NUMBER) {
            result = value;
	        match(NUMBER); //token = getToken();
        }
        return result;
    }

    void parse( ) {
        token = getToken(); // get the first token
        command();          // call the parsing command
    }

    public static void main(String args[]) { 
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while(true) {
            System.out.print(">> ");
            calc.parse();
        }
    }
}