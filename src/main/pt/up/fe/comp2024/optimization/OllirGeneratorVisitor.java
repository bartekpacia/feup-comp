package pt.up.fe.comp2024.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;

import java.util.stream.Collectors;

import static pt.up.fe.comp2024.ast.Kind.*;
import static pt.up.fe.comp2024.optimization.OllirTokens.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private final SymbolTable table;

    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        exprVisitor = new OllirExprGeneratorVisitor(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(IMPORT_DECL, this::visitImport);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(EXPRESSION_STMT, this::visitExpression);

        setDefaultVisit(this::defaultVisit);
    }

    private String visitExpression(JmmNode node, Void unused) {
        final String expressionCode = exprVisitor.visit(node.getJmmChild(0)).getCode();

        return expressionCode + END_STMT;
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        final String variableName = node.get("id");

        final JmmNode expressionNode = node.getChild(0);
        final OllirExprResult exprResult = exprVisitor.visit(expressionNode);

        StringBuilder code = new StringBuilder();

        // OllirExprResult.code references temporaries from OllirExprResult.computation, so
        // computation must be executed first.
        // code.append(variableName.getComputation());
        code.append(exprResult.getComputation());                           // tmp0.i32 :=.i32 a.i32 +.i32 b.i32;

        // The statement has the same type as the type of variableName.
        final Type type = TypeUtils.getExprType(expressionNode, table);
        final String ollirType = OptUtils.toOllirType(type);

        // For example: c.i32 := .i32 tmp0.i32;
        code.append(variableName).append(ollirType).append(SPACE);          // c.i32
        code.append(ASSIGN);                                                // :=
        code.append(ollirType).append(SPACE).append(exprResult.getCode());  // .i32 tmp0.i32

        code.append(END_STMT);

        System.out.println("DEBUG Generator.visitAssignStmt:");
        System.out.println("DEBUG          code (below): ");
        System.out.println(code);

        return code.toString();
    }

    private String visitReturn(JmmNode node, Void unused) {
        String methodName = node.getAncestor(METHOD_DECL).map(method -> method.get("name")).orElseThrow();
        Type retType = table.getReturnType(methodName);

        StringBuilder code = new StringBuilder();

        var expr = OllirExprResult.EMPTY;

        if (node.getNumChildren() > 0) {
            expr = exprVisitor.visit(node.getJmmChild(0));
        }

        code.append(expr.getComputation());
        code.append("ret");
        code.append(OptUtils.toOllirType(retType));
        code.append(SPACE);

        code.append(expr.getCode());

        code.append(END_STMT);

        return code.toString();
    }

    private String visitParam(JmmNode node, Void unused) {
        var typeCode = OptUtils.toOllirType(node.getJmmChild(0));
        var id = node.get("name");

        return id + typeCode;
    }

    private String visitMethodDecl(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = NodeUtils.getBooleanAttribute(node, "isPublic", "false");
        if (isPublic) {
            code.append("public ");
        }

        boolean isStatic = NodeUtils.getBooleanAttribute(node, "isStatic", "false");
        if (isStatic) {
            code.append("static ");
        }

        // name
        var name = node.get("name");
        code.append(name);


        code.append("(");
        if (name.equals("main")) {
            // MainMethod has hardcoded parameters
            // TODO(bartek): Once we support array parameters in grammar, this should be unified.
            code.append("args.array.String");
        } else {
            // Generate parameters in a normal way
            final String methodSignatureCode = node.getChildrenStream()
                    .filter((childNode) -> childNode.getKind().equals("Param"))
                    .map(this::visit)
                    .collect(Collectors.joining(", "));
            code.append(methodSignatureCode);
        }
        code.append(")");

        // type
        var retType = OptUtils.toOllirType(node.getChild(0));
        code.append(retType);
        code.append(L_CURLY);

        // rest of its children stmts
        final String methodBodyCode = node.getChildrenStream()
                .filter((childNode) -> !childNode.getKind().equals("Param"))
                .map(this::visit)
                .collect(Collectors.joining());
        code.append(methodBodyCode);

        // Make sure to add ReturnStmt, even if it's not present in the AST.
        boolean hasReturnStmt = node.getChildrenStream()
                .anyMatch((childNode) -> childNode.getKind().equals("ReturnStmt"));
        if (!hasReturnStmt) {
            code.append("ret.V").append(END_STMT);
        }

        code.append(R_CURLY);
        code.append(NL);

        return code.toString();
    }

    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(table.getClassName());

        if (table.getSuper().equals("Object") || table.getSuper().isEmpty()) {
            code.append(" extends Object");
        } else {
            code.append(" extends ");
            code.append(table.getSuper());
        }

        code.append(L_CURLY);

        code.append(NL);
        var needNl = true;

        for (var child : node.getChildren()) {
            var result = visit(child);

            if (METHOD_DECL.check(child) && needNl) {
                code.append(NL);
                needNl = false;
            }

            if (VAR_DECL.check(child)) {
                code.append(".field");
                code.append(SPACE);
                code.append("public");
                code.append(SPACE);
                code.append(child.get("name"));
                var typeNode = child.getChildren("Type").get(0);
                code.append(OptUtils.toOllirType(typeNode));
                code.append(END_STMT);
            }

            code.append(result);
        }

        code.append(buildConstructor());
        code.append(R_CURLY);

        return code.toString();
    }

    private String buildConstructor() {
        return ".construct " + table.getClassName() + "().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n";
    }

    private String visitProgram(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        JmmNode previousNode = null;
        for (var childNode : node.getChildren()) {
            var importsAreOver = previousNode != null
                    && previousNode.getKind().equals(IMPORT_DECL.getNodeName())
                    && !childNode.getKind().equals(IMPORT_DECL.getNodeName());
            if (importsAreOver) {
                code.append(NL);
            }

            code.append(visit(childNode));
            previousNode = childNode;
        }
        return code.toString();
    }

    private String visitImport(JmmNode node, Void unused) {
        return "import " + node.get("ID") + ";\n";
    }

    /**
     * Default visitor. Visits every child node and return an empty string.
     */
    private String defaultVisit(JmmNode node, Void unused) {
        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
