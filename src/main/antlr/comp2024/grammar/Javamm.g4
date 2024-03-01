grammar Javamm;

@header {
    package pt.up.fe.comp2024;
}

EQUALS : '=';
SEMI : ';' ;
COL : ',';
LCURLY : '{' ;
RCURLY : '}' ;
RSQPAREN : '[';
LSQPAREN : ']';
LPAREN : '(' ;
RPAREN : ')' ;
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-';
DOT : '.';
GT : '>';
LT: '<';
AND : '&&';
NOT : '!';

IMPORT : 'import';
EXTENDS : 'extends';
CLASS : 'class' ;
INT : 'int' ;
STRING : 'String';
BOOL : 'boolean';
TRUE : 'true';
FALSE: 'false';
THIS: 'this';
PUBLIC : 'public' ;
RETURN : 'return' ;
NEW : 'new';
WHILE : 'while';
IF : 'if';
ELSE: 'else';
STATIC: 'static';
VOID: 'void';
MAIN: 'main';
LENGTH: 'lenght';

INTEGER : [0] | [1-9]+[0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]*;
WS : [ \t\n\r\f]+ -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;



program
    : (importDecl)* classDecl EOF
    ;

importDecl
    : IMPORT name+=ID (DOT name+=ID)* SEMI
    ;

classDecl
    : CLASS name=ID (EXTENDS mainc=ID)?
        LCURLY
        varDecl* methodDecl*
        RCURLY
    ;

varDecl
    : type name=ID SEMI
    ;

//lacks 1 of them
type
    : name=INT RSQPAREN LSQPAREN
    | name=INT
    | name=BOOL
    | name = ('String' | ID )
    ;

methodDecl locals[boolean isPublic=false]
    :  ('public')? type methodName=ID '(' (param (',' param)* )? ')' '{'(varDecl)* (stmt)* 'return' expr ';' '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' parameterName=ID ')' '{'(varDecl)* (stmt)* '}' //#MainMethod
    ;

param
    : type name=ID
    | type '... ints'  // para passar o teste da linha 67 // n sei se isto esta certo // esta syntax teria que aceitar diversos argumentos
    ;

stmt
    : LCURLY stmt* RCURLY #CurlyStmt//
    |  '{' '}'   #BlankExpression
    | ifStatment elseStatment? #IfElseStmt
    | 'while' '(' expr ')' stmt #WhileStmt
    | expr ';' stmt #ExpressionStmt
    | expr ';' #ExpressionStmt
    | id=ID '=' expr ';' #Assignment
    | id=ID '[' expr ']' '=' expr ';' #Assignment
    ;

ifStatment
    : 'if' '(' expr ')' stmt
    ;
elseStatment
    : 'else' stmt
    ;


expr
    : LPAREN expr RPAREN #ParenExpr //
    | expr LSQPAREN expr RSQPAREN #ArrRefExpr //
    | RSQPAREN (expr (COL expr)*)? LSQPAREN #ArrRefExpr //
    | expr DOT 'length' #LenCheckExpr
    | expr DOT name=ID LPAREN (expr (COL expr)*)? RPAREN #IdUseExpr //
    | op=NOT expr #BinaryExpr //
    | expr (op='*' | op='/' ) expr  #BinaryOp //mudei aqui
    | expr (op='+' | op='-' ) expr #BinaryOp //mudei aqui
    | expr (op='<'| op='>' ) expr #BoolOp  //mudei aqui
    | expr op = '&&' expr #AndOp //mudei aqui
    | expr op =  '||' expr #OrOp //mudei aqui
    | expr '[' expr ']' #ArrayIndex
    | value=INTEGER #IntegerLiteral //
    | id = ID #Identifier
    | name=THIS #VarRefExpr//
    | 'new' 'int' '[' expr ']' #NewIntArr
    | 'new' id = ID '(' ')'  #NewClass
    | TRUE #BoolExpr
    | FALSE #BoolExpr
    | THIS #ThisExpr
    | name=ID #IDExpr
    | INT #INTExpr
    ;



