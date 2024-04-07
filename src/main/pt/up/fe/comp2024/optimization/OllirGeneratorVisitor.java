package pt.up.fe.comp2024.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;

import java.util.stream.Collectors;

import static pt.up.fe.comp2024.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";
    private final String L_PAREN = "(";
    private final String R_PAREN = ")";


    private final SymbolTable table;

    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        exprVisitor = new OllirExprGeneratorVisitor(table);
    }


    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit("ImportDecl", this::visitImport);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        // addVisit(VAR_DECL, this::visitVarDecl);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(ID_USE_EXPR, this::visitMethodCallExpr);
        addVisit(EXPRESSION_STMT, this::visitExpression);

        setDefaultVisit(this::defaultVisit);
    }

    private String visitExpression(JmmNode node, Void unused) {
        return exprVisitor.visit(node.getJmmChild(0)).getCode();
    }

    private String visitMethodCallExpr(JmmNode node, Void unused) {
        final String methodName = node.get("name");

        // if (1==1){ return "DUPA"; }

        // TODO: Differentiate between static and virtual method call

        final String debugPrefix = "DEBUG Generator.visitMethodCallExpr(" + methodName + "): ";
        System.out.println(debugPrefix + "Recognized node kind " + node.getKind());

        final StringBuilder code = new StringBuilder();

        // First child of IdUseExpr is the package where the method comes from
        final String packageName = node.getChild(0).get("id");

        code.append("invokestatic").append(L_PAREN);
        code.append(packageName).append(", ");
        code.append("\"").append(methodName).append("\"").append(", ");

        // Visit more JmmNode children to get the actuals
        var args = node.getChildrenStream()
                .skip(2)
                .map(this::visit)
                .collect(Collectors.joining(", "));

        code.append(args);
        code.append(R_PAREN);
        code.append(R_BRACKET);
        code.append(END_STMT);

        return code.toString();
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        var lhs = node.get("id");
        final String debugPrefix = "DEBUG Generator.visitAssignStmt(" + lhs + "): ";
        System.out.println(debugPrefix + "Recognized node kind " + node.getKind() + " with id " + lhs);

        var rhs = exprVisitor.visit(node.getChild(0));
        System.out.println(debugPrefix + "Recognized rhs: " + rhs);

        StringBuilder code = new StringBuilder();

        // code.append(lhs.getComputation());
        // code.append(rhs.getComputation());

        // code to compute self
        // statement has type of lhs
        Type thisType = TypeUtils.getExprType(node.getChild(0), table);
        String typeString = OptUtils.toOllirType(thisType);

        code.append(lhs);
        code.append(typeString);
        code.append(SPACE);

        code.append(ASSIGN);
        code.append(typeString);
        code.append(SPACE);

        code.append(rhs.getCode());

        code.append(END_STMT);

        return code.toString();
    }


    private String visitReturn(JmmNode node, Void unused) {
        final String debugPrefix = "DEBUG Generator.visitReturn: ";
        System.out.println(debugPrefix + "Recognized node kind " + node.getKind());

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

        String code = id + typeCode;

        return code;
    }


    private String visitMethodDecl(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = NodeUtils.getBooleanAttribute(node, "isPublic", "false");

        if (isPublic) {
            code.append("public ");
        }

        // name
        var name = node.get("name");
        code.append(name);

        // Generate parameters
        code.append("(");
        final String methodSignatureCode = node.getChildrenStream()
                .filter((childNode) -> childNode.getKind().equals("Param"))
                .map(this::visit)
                .collect(Collectors.joining(", "));
        code.append(methodSignatureCode);

        code.append(")");

        // type
        var retType = OptUtils.toOllirType(node.getChild(0));
        code.append(retType);
        code.append(L_BRACKET);


        // rest of its children stmts
        final String methodBodyCode = node.getChildrenStream()
                .filter((childNode) -> !childNode.getKind().equals("Param"))
                .map(this::visit)
                .collect(Collectors.joining());
        code.append(methodBodyCode);

        code.append(R_BRACKET);
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

        code.append(L_BRACKET);

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
        code.append(R_BRACKET);

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
                    && previousNode.getKind().equals("ImportDecl")
                    && !childNode.getKind().equals("ImportDecl");
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
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {
        System.out.println("DEBUG Generator.defaultVisit(" + node.getKind() + ")");

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
