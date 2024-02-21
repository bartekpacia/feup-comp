grammar Javamm;

@header {
    package pt.up.fe.comp2024;
}

EQUALS : '=';
SEMI : ';' ;
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
LT : '<';
AND : '&&';
NOT : '!';

IMPORT : 'import';
CLASS : 'class' ;
INT : 'int' ;
BOOL : 'boolean';
PUBLIC : 'public' ;
RETURN : 'return' ;
NEW : 'new';
WHILE : 'while';
IF : 'if';

INTEGER : [0-9] ;
ID : [a-zA-Z]+ ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDecl)* classDecl EOF
    ;

importDecl
    : IMPORT name+=ID (DOT ID)* SEMI
    ;

classDecl
    : CLASS name=ID
        LCURLY
        methodDecl*
        RCURLY
    ;

varDecl
    : type name=ID SEMI
    ;

type
    : name= INT ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        LPAREN param RPAREN
        LCURLY varDecl* stmt* RCURLY
    ;

param
    : type name=ID
    ;

stmt
    : expr EQUALS expr SEMI #AssignStmt //
    | RETURN expr SEMI #ReturnStmt
    ;

expr
    : op=NOT expr #BinaryExpr//
    | expr op= MUL expr #BinaryExpr //
    | expr op= DIV expr #BinaryExpr //
    | expr op= ADD expr #BinaryExpr //
    | expr op= SUB expr #BinaryExpr //
    | value=INTEGER #IntegerLiteral //
    | name=ID #VarRefExpr //

    ;



