# Comp2024 Project

Contains a reference implementation for an initial version of the project that supports a small portion of Java--.

## Checklist
### 3.1 - The JAVA-- Language
- [ ] Complete the JAVA-- grammar in ANTLR format
  - [X] Import declarations;
  - [ ] Class declaration (structure, fields and methods);
  - [ ] Statements (assignments, if-else, while, etc);
  - [ ] Expressions (binary expressions, literals, method calls, etc);
- [ ] Setup node names for the AST (e.g. "binaryOp" instead of "expr" for binary expressions);
- [ ] Annotate Nodes in the AST with relevant information (e.g. id, values, etc.);
- [ ] Used interfaces: JmmParser, JmmNode and JmmParserResult;

### 3.2 - Symbol Table
- [ ] Imported classes;
- [ ] Declared class;
- [ ] Fields inside the declared class;
- [ ] Methods inside the declared class;
- [ ] Parameters and return type for each method;
- [ ] Local variables for each method;
- [ ] Include type in each symbol (e.g. a local variable "a" is of type X. Also, is "a" array?);
- [ ] Used interfaces: SymbolTable, AJmmVisitor (the latter is optional);