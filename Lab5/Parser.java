// Parser.java
// Parser for language S

public class Parser {
    Token token;          // current token 
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) { 
        lexer = scan;		  
        token = lexer.getToken(); // get the first token
    }
  
    private String match(Token t) {
        String value = token.value();
        if (token == t)
            token = lexer.getToken();
        else
            error(t);
        return value;
    }

    private void error(Token tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    public Command command() {
    // <command> ->  <decl> | <function> | <stmt>
	    if (isType()) {
	        Decl d = decl();
	        return d;
	    }
	    if (token == Token.FUN) { 
	        Function f = function();
	        return f;
	    }
	    if (token != Token.EOF) {
	        Stmt s = stmt();
            return s;
	    }
	    return null;
    }

    private Decl decl() {
       // <decl> -> <type> id [n]; 
       // <decl>  -> <type> id [=<expr>]; 
       Type t = type();
	   String id = match(Token.ID);
	   Decl d = null;
	   
	   if (token == Token.LBRACKET) {
           match(Token.LBRACKET);
           Value v = literal(); 
	       d = new Decl(id, t, v.intValue());
           match(Token.RBRACKET);
       } else if (token == Token.ASSIGN) {
	        match(Token.ASSIGN);
            Expr e = expr();
	       d = new Decl(id, t, e);
	   } else 
            d = new Decl(id, t);

		match(Token.SEMICOLON);
		return d;
    }

    private Decls decls () {
    // <decls> -> {<decl>}
        Decls ds = new Decls ();
	    while (isType()) {
	        Decl d = decl();
	        ds.add(d);
	    }
        return ds;             
    }

    // [Function]
    private Functions functions () {
    // <functions> -> { <function> }
	    Functions fs = new Functions();  
        while (token == Token.FUN) {
	        Function f = function(); 
	        fs.add(f);
        }  
        return fs;          	
    }
    
    // [Function]
    private Function function() {
    // <function>  -> fun <type> id(<params>) <stmt> 
	    match(Token.FUN);
	    Type t = type();
	    String str = match(Token.ID);
	    funId = str; 
	    Function f = new Function(str, t);
	    match(Token.LPAREN);
        if (token != Token.RPAREN)
            f.params = params();
	    match(Token.RPAREN);
	    Stmt s = stmt();		
	    f.stmt = s;
	    return f;
    }

    // [Function]
    private Decls params() {
	    Decls params = new Decls();
	    // TODO: [Implement the code of params]
        // 타입과 식별자를 가져온 뒤 매개변수 목록에 추가
        // 추가적인 매개변수가 있는 경우 ,로 구분된 것을 확인하고 파싱
	    Type t = type();
	    String id = match(Token.ID);
	    params.add(new Decl(id, t));
        while (token == Token.COMMA) {
            match(Token.COMMA);
            t = type();
            id = match(Token.ID);
            params.add(new Decl(id, t));
        }
        return params;
    }

    private Type type () {
    // <type>  ->  int | bool | void | string 
        Type t = null;
        switch (token) {
	    case INT:
            t = Type.INT; break;
        case BOOL:
            t = Type.BOOL; break;
        case VOID:
            t = Type.VOID; break;
        case STRING:
            t = Type.STRING; break;
        default:
	        error("int | bool | void | string");
	    }
        match(token);
        return t;       
    }
  
    private Stmt stmt() {
    // <stmt> -> <stmts> | <assignment> | <ifStmt> | <whileStmt> | ...
        Stmt s = new Empty();
        switch (token) {
	    case SEMICOLON:
            match(token.SEMICOLON); return s;
        case LBRACE:			
	        match(Token.LBRACE);		
            s = stmts();
            match(Token.RBRACE);	
	        return s;
        case IF: 	// if statement 
            s = ifStmt(); return s;
        case WHILE:      // while statement 
            s = whileStmt(); return s;
	    case DO:      // while statement 
            s = doStmt(); return s;
	    case FOR:      // while statement 
            s = forStmt(); return s;
        case ID:	// assignment
            s = assignment(); return s;
	    case LET:	// let statement 
            s = letStmt(); return s;
	    case READ:	// read statement 
            s = readStmt(); return s;
	    case PRINT:	// print statment 
            s = printStmt(); return s;
        // [Function]
	    case RETURN:	// return statement  
            s = returnStmt(); return s;
        default:  
	        error("Illegal stmt"); return null; 
	}
    }
  
    private Stmts stmts () {
    // <stmts> -> {<stmt>}
        Stmts ss = new Stmts();
	    while((token != Token.RBRACE) && (token != Token.END))
	        ss.stmts.add(stmt()); 
        return ss;
    }

    private Let letStmt () {
    // <letStmt> -> let <decls> <functions> in <stmts> end
	    match(Token.LET);	
        Decls ds = decls();
        
        // [Function]
        // TODO: [Implement the code for function declaration in let stmt]
        // let 키워드로 시작하는 복합 구문을 파싱한 후 파싱한 결과를 Let 클래스 인스턴스로 반환
        Functions fs = null;
        // There is a Function declaration in Let statement
        if(token == Token.FUN) {
            fs = functions();
        }
        
	    match(Token.IN);
        Stmts ss = stmts();
        match(Token.END);	
        match(Token.SEMICOLON);
        return new Let(ds, fs, ss);
    }

    private Read readStmt() {
    // <readStmt> -> read id;
        match(Token.READ);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Read(id);
    }

    private Print printStmt() {
    // <printStmt> -> print <expr>;
        match(Token.PRINT);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Print(e);
    }

    private Return returnStmt() {
    // <returnStmt> -> return <expr>; 
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }

    private Stmt assignment() {
    // <assignment> -> id = <expr>;   
	    Array ar = null;  
        Identifier id = new Identifier(match(Token.ID));
	    if (token == Token.LPAREN) 
	        return call(id);
        if (token == Token.LBRACKET) {  
            match(Token.LBRACKET); 
            ar = new Array(id, expr());
            match(Token.RBRACKET);
        }

        match(Token.ASSIGN);
        Expr e = expr();
        match(Token.SEMICOLON);

        if (ar == null)
            return new Assignment(id, e);
        else 
            return new Assignment(ar, e);
    }
    
    // [Function]
    private Call call(Identifier id) {
    // <call> -> id(<expr>{,<expr>});
    	// TODO: [Implement the code of call stmt]
        // 함수 호출 구문을 파싱한 후 함수 호출 정보를 Call 객체에 저장하여 반환
    	match(Token.LPAREN);
        Call c = new Call(id, arguments());
    	match(Token.RPAREN);
    	match(Token.SEMICOLON);
    	return c;
    }

    private If ifStmt () {
    // <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
        match(Token.IF);
	    match(Token.LPAREN);
        Expr e = expr();
	    match(Token.RPAREN);
        match(Token.THEN);
        Stmt s1 = stmt();
        Stmt s2 = new Empty();
        if (token == Token.ELSE){
            match(Token.ELSE); 
            s2 = stmt();
        }
        return new If(e, s1, s2);
    }

    private While whileStmt () {
    // <whileStmt> -> while (<expr>) <stmt>
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        Stmt s = stmt();
        return new While(e, s);
    }

    private Stmts doStmt() {
    // <doStmt> -> do <stmt> while (<expr>) 
        match(Token.DO);
        Stmt s = stmt();
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
		match(Token.SEMICOLON);
        Stmts ss = new Stmts(s); 
        ss.stmts.add(new While(e, s));
        return ss;
    }

    private Let forStmt () {
    // <forStmt> -> for (<type> id = <expr>; <expr>; id = <expr>) <stmt>
        match(Token.FOR);
        match(Token.LPAREN);
        Decl d = decl();
        Decls ds = new Decls(d); 
	    Expr e1 = expr();
        match(Token.SEMICOLON);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.ASSIGN);
	    Expr e2 = expr();
	    Assignment assign = new Assignment(id, e2);
        match(Token.RPAREN);
        Stmt s = stmt();
        Stmts s1 = new Stmts(s); 
	    s1.stmts.add(assign);
	    Stmts s2 = new Stmts(new While(e1,s1));
        return new Let(ds, null, s2);
    }

    private Expr expr () {
    // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
        switch (token) {
	    case NOT:
	        Operator op = new Operator(match(token));
	    Expr e = expr();
            return new Unary(op, e);
        case TRUE:
            match(Token.TRUE);
            return new Value(true);
        case FALSE:
            match(Token.FALSE);
            return new Value(false);
        }

        Expr e = bexp();
        // parse logical operations
        while (token == Token.AND || token == Token.OR) {
            Operator op = new Operator(match(token));
            Expr b = bexp();
            e = new Binary(op, e, b);
        }
        return e;
    }

    private Expr bexp() {
        // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ]
        Expr e = aexp();
        switch(token) {
        case LT: case LTEQ: case GT: case GTEQ: case EQUAL: case NOTEQ:
            Operator op = new Operator(match(token));
            Expr a = aexp();
            e = new Binary(op, e, a);
        }
        return e;
    }
  
    private Expr aexp () {
        // <aexp> -> <term> { + <term> | - <term> }
        Expr e = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Operator op = new Operator(match(token));
            Expr t = term();
            e = new Binary(op, e, t);
        }
        return e;
    }
  
    private Expr term () {
        // <term> -> <factor> { * <factor> | / <factor>}
        Expr t = factor();
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));
            Expr f = factor();
            t = new Binary(op, t, f);
        }
        return t;
    }
  
    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == Token.MINUS) 
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch(token) {
        case ID:
            Identifier v = new Identifier(match(Token.ID));
            e = v;
            if (token == Token.LPAREN) {  // function call
                match(Token.LPAREN); 
                Call c = new Call(v, arguments());
                match(Token.RPAREN);
                e = c;
            } else if (token == Token.LBRACKET) {  
                match(Token.LBRACKET); 
                Array a = new Array(v,expr());
                match(Token.RBRACKET);
                e = a;
            }
            break;
        case NUMBER: case STRLITERAL: 
            e = literal();
            break; 
        case LPAREN: 
            match(Token.LPAREN); 
            e = aexp();       
            match(Token.RPAREN);
            break; 
        default: 
            error("Identifier | Literal"); 
        }

        if (op != null)
            return new Unary(op, e);
        else return e;
    }
  
    private Exprs arguments() {
    // arguments -> [ <expr> {, <expr> } ]
        Exprs es = new Exprs();
        while (token != Token.RPAREN) {
            es.add(expr());
            if (token == Token.COMMA)
                match(Token.COMMA);
            else if (token != Token.RPAREN)
                error("Exprs");
        }  
        return es;  
    }

    private Value literal( ) {
        String s = null;
        switch (token) {
        case NUMBER:
            s = match(Token.NUMBER);
            return new Value(Integer.parseInt(s));
        case STRLITERAL:
            s = match(Token.STRLITERAL);
            return new Value(s);
        }
        throw new IllegalArgumentException( "no literal");
    }
 
    private boolean isType( ) {
        switch(token) {
        case INT: case BOOL: case STRING: 
            return true;
        default: 
            return false;
        }
    }
    
    public static void main(String args[]) {
	    Parser parser;
        Command command = null;
	    if (args.length == 0) {
	        System.out.print(">> ");
	        Lexer.interactive = true;
	        parser  = new Parser(new Lexer());
	        do {
	            if (parser.token == Token.EOF) 
		            parser.token = parser.lexer.getToken();

                try {
                    command = parser.command();
//		            if (command != null) command.display(0);    // display AST 
                } catch (Exception e) {
                    System.err.println(e);
                }
		        System.out.print("\n>> ");
	        } while(true);
	    }
    	else {
	        System.out.println("Begin parsing... " + args[0]);
	        parser  = new Parser(new Lexer(args[0]));
	        do {
	            if (parser.token == Token.EOF) 
                    break;

                try {
		             command = parser.command();
//		             if (command != null) command.display(0);      // display AST
                } catch (Exception e) {
                    System.err.println(e); 
                }
	        } while (command != null);
	    }
    } //main
} // Parser