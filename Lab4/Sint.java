 // Sint.java
// Interpreter for S
import java.util.Scanner;
import java.util.Arrays;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
	    if (c instanceof Decl) {
	        Decls decls = new Decls();
	        decls.add((Decl) c);
	        return allocate(decls, state);
	    }
	    if (c instanceof Stmt)
	        return Eval((Stmt) c, state); 
		
	    throw new IllegalArgumentException("no command");
    }
  
    State Eval(Stmt s, State state) {
        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	        return Eval((Let)s, state);
	    if (s instanceof Read)  
	        return Eval((Read)s, state);
	    if (s instanceof Print)  
	        return Eval((Print)s, state);
        throw new IllegalArgumentException("no statement");
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    State Eval(Assignment a, State state) {
    	// replace array element in array represented by array name
        Value v = V(a.expr, state);
        
        if (a.ar == null)
	        return state.set(a.id, v);
        else {	// id[<expr>] = <expr>;
            // TODO: [Fill the code for assignment of an array element]
            // 배열 요소에 새 값 할당, 배열과 인덱스를 찾고 해당 위치에 값 할당
            Identifier arrayId = a.ar.id;
            Expr indexExpr = a.ar.expr;
            Value index = V(indexExpr, state);
            Value[] values = state.get(arrayId).arrValue();
            if (index.intValue() >= 0 && index.intValue() < values.length) {
                values[index.intValue()] = v;
            } else {
                throw new ArrayIndexOutOfBoundsException("Array index out of bounds");
            }
            return state;
        }
    }

    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) {
	        int i = sc.nextInt();
	        state.set(r.id, new Value(i));
	    } 
 
	    if (r.id.type == Type.BOOL) {
	        boolean b = sc.nextBoolean();	
            state.set(r.id, new Value(b));
	    }

	    if (r.id.type == Type.STRING) {
	        String s = (String) sc.next();
	        state.set(r.id, new Value(s));
	    } 
	    return state;
    }

    State Eval(Print p, State state) {
	    System.out.println(V(p.expr, state));
        return state; 
    }
  
    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
        }
        return state;
    }
  
    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue( ))
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }
 
    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue( ))
            return Eval(l, Eval(l.stmt, state));
        else 
	        return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        s = Eval(l.stmts,s);
	    return free(l.decls, s);
    }

    State allocate (Decls ds, State state) {
    	// add entries for declared variables (ds) on the state
        if (ds != null) {
        	for (Decl decl : ds) 
			    if (decl.arraysize > 0) { // <type> id[n];
                    // TODO: [Fill the code for array allocation]
                    // 배열 할당 및 초기화, 새 배열 생성, 초기값으로 채우고 state에 추가
                    Value[] array = new Value[decl.arraysize];
                    Arrays.fill(array, new Value(0));
                    state.push(decl.id, new Value(array));
                }
	            else if (decl.expr == null)
	                state.push(decl.id, new Value(decl.type));
		        else
			        state.push(decl.id, V(decl.expr, state));
        }
        return state;
    }

    State free (Decls ds, State state) {
    	// free the entries for declared variables (ds) from the state
        if (ds != null) {
	        for (Decl decl : ds) 
	            state.pop();
        }
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef,"reference to undef value");
	    switch (op.val) {
	    case "+":
            return new Value(v1.intValue() + v2.intValue());
        case "-": 
            return new Value(v1.intValue() - v2.intValue());
        case "*": 
            return new Value(v1.intValue() * v2.intValue());
        case "/": 
            return new Value(v1.intValue() / v2.intValue());
        case "==": 
            return new Value(v1.intValue() == v2.intValue());
        case "!=": 
            return new Value(v1.intValue() != v2.intValue());
        case "<": 
            return new Value(v1.intValue() < v2.intValue());
        case "<=": 
            return new Value(v1.intValue() <= v2.intValue());
        case ">": 
            return new Value(v1.intValue() > v2.intValue());
        case ">=": 
            return new Value(v1.intValue() >= v2.intValue());
        case "&": 
            return new Value(v1.boolValue() && v2.boolValue());
        case "|": 
            return new Value(v1.boolValue() || v2.boolValue());
	    default:
	        throw new IllegalArgumentException("no operation");
	    }
    } 
    
    Value unaryOperation(Operator op, Value v) {
        check( !v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
	    case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) 
            return (Value) e;
        
        if (e instanceof Identifier) {
	        Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }
        if (e instanceof Array) { // id[<expr>]
            // TODO: [Fill the code for array expr]
            // 배열 표현식 처리, 인덱스 계산 후 해당 배열 요소 값 반환
            Array arr = (Array) e;
            Value index = V(arr.expr, state);
            Value[] values = state.get(arr.id).arrValue();
            if (index.intValue() >= 0 && index.intValue() < values.length) {
                return values[index.intValue()];
            } else {
                throw new ArrayIndexOutOfBoundsException("Array index out of bounds");
            }
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); 
			Lexer.interactive = true;
            System.out.println("Language S Interpreter 1.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		            parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
	                 //    if (command != null) command.display(0);    // display AST    
					 if (command == null) 
						 throw new Exception();
                } catch (Exception e) {
                    System.out.println("Error: " + e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println("Error: " + e);  
                    }
                }
		        System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;
	         
                try {
		            command = parser.command();
//					if (command != null) command.display(0);      // display AST
					if (command == null) 
						 throw new Exception();
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println("Error: " + e);  
                    }
                }
	        } while (command != null);
        }        
    }
}