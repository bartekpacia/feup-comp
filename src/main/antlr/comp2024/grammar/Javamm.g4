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
    : name=INT LSQPAREN RSQPAREN
    | name=INT
    | name=BOOL
    | name=ID
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        LPAREN param RPAREN
        LCURLY varDecl* stmt*
        RETURN expr SEMI RCURLY
    | (PUBLIC {$isPublic=true;})?
        STATIC VOID MAIN
        RPAREN STRING RSQPAREN LSQPAREN name=ID LPAREN
        RCURLY varDecl* stmt* LCURLY
    ;

param
    : type name=ID
    ;

stmt
    : LCURLY stmt* RCURLY #CurlyStmt//
    | IF RPAREN expr LPAREN stmt ELSE stmt #IfStmt //
    | WHILE LPAREN expr RPAREN stmt #WhileStmt //
    | expr SEMI #ExprStmt //
    | expr EQUALS expr SEMI #AssignStmt //
    | expr LSQPAREN expr RSQPAREN EQUALS expr SEMI #AssignStmt //
    | RETURN expr SEMI #ReturnStmt
    ;

expr
    : LPAREN expr RPAREN #ParenExpr //
    | expr LSQPAREN expr RSQPAREN #ArrRefExpr //
    | LSQPAREN (expr (COL expr)*)? RSQPAREN #ArrRefExpr //
    | expr DOT length=LENGTH #LenCheckExpr //
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



