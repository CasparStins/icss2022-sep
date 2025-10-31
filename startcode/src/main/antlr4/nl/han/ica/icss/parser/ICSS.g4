 grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: (variableAssignment | stylerule)* EOF;

stylerule: selector OPEN_BRACE body CLOSE_BRACE;

selector: LOWER_IDENT | CLASS_IDENT | ID_IDENT;

body: (declaration | ifClause | variableAssignment)*;
declaration: LOWER_IDENT COLON expression SEMICOLON;
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;

// OPEN_BRACE body CLOSE_BRACE --> Dit gedeelte had naar een nieuw iets gekunt ivm repeatability.
ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE body CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE body CLOSE_BRACE;

//reken volgorde
expression
    : literal
    | CAPITAL_IDENT
    | expression MUL expression
    | expression (PLUS | MIN) expression
    ;

literal
    : COLOR
    | PIXELSIZE
    | PERCENTAGE
    | SCALAR
    | TRUE
    | FALSE
    ;


