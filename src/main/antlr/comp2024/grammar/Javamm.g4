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
ID : [a-zA-Z]+ ;
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
    //: (PUBLIC {$isPublic=true;})? type name=ID LPAREN param RPAREN LCURLY varDecl* stmt* RETURN expr SEMI RCURLY
    :  ('public')? type methodName=ID '(' (param (',' param)* )? ')' '{'(varDecl)* (stmt)* 'return' expr ';' '}'
    | (PUBLIC {$isPublic=true;})? STATIC VOID MAIN LPAREN STRING RSQPAREN LSQPAREN name=ID RPAREN RCURLY varDecl* stmt* LCURLY
    // | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' parameterName=ID ')' '{'(varDecl)* (stmt)* '}' //#MainMethod
    ;

param
    : type name=ID
    | type '... ints'  // para passar o teste da linha 67 // n sei se isto esta certo // esta syntax teria que aceitar diversos argumentos
    ;

stmt
    : LCURLY stmt* RCURLY #CurlyStmt//
    //| IF LPAREN expr RPAREN stmt ELSE stmt #IfStmt //  a substitutir com o que escrebo na linha abaixo
    | 'if' '(' expr ')' stmt 'else' stmt #IfElseStmt
    | WHILE LPAREN expr RPAREN stmt #WhileStmt //
    | expr SEMI #ExprStmt //
    | expr EQUALS expr SEMI #AssignStmt //
    | expr LSQPAREN expr RSQPAREN EQUALS expr SEMI #AssignStmt //
    | RETURN expr SEMI #ReturnStmt
    //| 'if''Stmt1'SEMI #NestesIf


    ;


expr
    : LPAREN expr RPAREN #ParenExpr //
    | expr LSQPAREN expr RSQPAREN #ArrRefExpr //
    | LSQPAREN (expr (COL expr)*)? RSQPAREN #ArrRefExpr //
    //| expr DOT length=LENGTH #LenCheckExpr //
    | expr 'a.length' #LenCheckExpr
    | expr DOT name=ID RPAREN (expr (COL expr)*)? LPAREN #IdUseExpr //
    | op=NOT expr #BinaryExpr //
    | expr op=MUL expr #BinaryExpr | expr op=DIV expr #BinaryExpr //
    | expr op=ADD expr #BinaryExpr | expr op=SUB expr #BinaryExpr //
    | expr op=AND expr #BoolExpr //
    | expr op=GT expr #RelExpr | expr op=LT expr #RelExpr //
    | value=INTEGER #IntegerLiteral //
    | name=ID #VarRefExpr | name=THIS #VarRefExpr//
    | NEW INT LSQPAREN expr RSQPAREN #NewExpr | NEW name=ID RPAREN LPAREN #NewExpr//
    | TRUE #BoolExpr | FALSE #BoolExpr | THIS #ThisExpr | name=ID #IDExpr | INT #INTExpr
    ;



