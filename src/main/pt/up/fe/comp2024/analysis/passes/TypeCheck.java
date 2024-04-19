package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

public class TypeCheck extends AnalysisVisitor {

    private String currentMethod;
    private JmmNode currentMethodNode;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.BINARY_EXPR, this::visitArithmeticOp);
        addVisit(Kind.BOOL_OP, this::visitCondOp);
        addVisit(Kind.NOT_OP, this::visitCondOp);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitVarRefExpr(JmmNode node, SymbolTable table) {
        final var locals = table.getLocalVariables(currentMethod);
        final var params = table.getParameters(currentMethod);
        final var fields = table.getFields();

        for(var local : locals) {
            if(local.getName().equals(node.get("name"))) {
                return null;
            }
        }

        for(var param : params) {
            if(param.getName().equals(node.get("name"))) {
                return null;
            }
        }

        for (var field : fields) {
            if (field.getName().equals(node.get("name"))) {
                if(currentMethodNode.get("isStatic").equals("true")) {
                    var message = String.format("Variable '%s' does not exist.", node.get("name"));
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            NodeUtils.getLine(node),
                            NodeUtils.getColumn(node),
                            message,
                            null)
                    );
                    return null;
                }
                return null;
            }
        }

        
        return null;
    }
    private Void visitArithmeticOp(JmmNode node, SymbolTable table) {
        final Type leftType = TypeUtils.getExprType(node.getChild(0), table);
        final Type rightType = TypeUtils.getExprType(node.getChild(1), table);

        if (leftType != null && rightType != null && (leftType.getName().equals("int") || leftType.getName().equals("int[]")) && (rightType.getName().equals("int") || rightType.getName().equals("int[]"))) {
            return null;
        }


        var message = String.format("Variable '%s' does not exist - type_arithopvisit", node.getChild(0));
        addReport(Report.newError(Stage.SEMANTIC, NodeUtils.getLine(node), NodeUtils.getColumn(node), message, null)
        );
        return null;
    }
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethodNode = method;
        currentMethod = method.get("name");
        return null;
    }

    private Void visitCondOp(JmmNode node, SymbolTable table) {
        var leftType = TypeUtils.getExprType(node.getChild(0),table);
        if(!node.getKind().equals("NOT_OP")) {
            var rightType = TypeUtils.getExprType(node.getChild(1),table);
            if (leftType.getName().equals("bool") && rightType.getName().equals("bool")) {
                return null;
            }
        }

        if (leftType.getName().equals("bool")) {
            return null;
        }

        var message = String.format("Variable '%s' does not exist - type_condopvisit", node.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null)
        );
        return null;
    }

    private Void visitAssignStmt(JmmNode node, SymbolTable table) {
        JmmNode rightNode = node.getChild(0);

        var rightType = TypeUtils.getExprType(rightNode,table);
        var leftType = TypeUtils.getVarExprAssignType(node, table);
        if (rightType.equals(leftType)) {
            return null;
        }
        final var imports = table.getImports();
        final var superName = table.getSuper();

        if((imports.contains(leftType.getName()) && imports.contains(rightType.getName())) || (imports.contains(superName))) {
            return null;
        }
        var message = String.format("Variable '%s' does not exist - type_assignopvisit", node.getChild(0));
        addReport(Report.newError(Stage.SEMANTIC, NodeUtils.getLine(node), NodeUtils.getColumn(node), message, null)
        );

        return null;
    }

    private Void visitReturnStmt(JmmNode stmt, SymbolTable table) {
        JmmNode returnVar = stmt.getChild(0);
        final boolean isIntegerLiteral = returnVar.getKind().equals("IntegerLiteral");
        final boolean isIdentifier = returnVar.getKind().equals("Identifier");
        final boolean isArrayIndex = returnVar.getKind().equals("ArrayIndex");
        final boolean isReturnTypeInt = table.getReturnType(currentMethod).getName().equals("int");
        final boolean isReturnTypeBool = table.getReturnType(currentMethod).getName().equals("bool");
        if ((isIntegerLiteral && isReturnTypeInt) || (isIntegerLiteral && isReturnTypeBool)) {
            return null;
        } else if (isIdentifier && varIsReturnType(returnVar, table)) {
            return null;
        } else if (isArrayIndex) {
            return null; }

        var message = String.format("Variable '%s' does not exist - type_retpvisit", stmt.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(stmt),
                NodeUtils.getColumn(stmt),
                message,
                null)
        );
        return null;
    }

    private boolean varIsReturnType(JmmNode var, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        //TODO(goncalo) Not sure if 100% correct but avoids a private test - also fix the spaghetti nesting.
        if(currentMethodNode.get("isStatic").equals("false")) {
            for (var field : table.getFields()) {
                if (field.getName().equals(var.get("id"))) {
                    if (field.getType().equals(table.getReturnType(currentMethod))) {
                        return true;
                    }
                }
            }
        }

        for (var param : table.getParameters(currentMethod)) {
            if(param.getName().equals(var.get("id"))) {
                if (param.getType().equals(table.getReturnType(currentMethod))) {
                    return true;
                }
            }
        }

        for (var local : table.getLocalVariables(currentMethod)) {
            if(local.getName().equals(var.get("id"))) {
                if (local.getType().equals(table.getReturnType(currentMethod))) {
                    return true;
                }
            }
        }
        return false;
    }
}
