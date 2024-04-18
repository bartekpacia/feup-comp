package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
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
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl); //get curr method
        addVisit(Kind.BINARY_EXPR, this::visitArithOp); //Arithmetic and NOT
        addVisit(Kind.BOOL_OP, this::visitCondOp); // AND/OR
        addVisit(Kind.NOT_OP, this::visitCondOp); // AND/OR
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt); // Assignments
        //addVisit(Kind.ARR_REF_EXPR, this::visitArrRefExpr); //Array ref
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt); //Returns
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr); //Variable reference
    }

    private Void visitVarRefExpr(JmmNode node, SymbolTable table) {
        if(currentMethodNode.get("isStatic").equals("false")) {
            return null;
        }

        var message = String.format("Variable '%s' does not exist.", node.get("id"));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null)
        );

        return null;
    }
    private Void visitArithOp(JmmNode node, SymbolTable table) {
        var leftType = TypeUtils.getExprType(node.getChild(0),table);
        var rightType = TypeUtils.getExprType(node.getChild(1),table);

        if (leftType.getName().equals("int") && rightType.getName().equals("int")) {
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
        System.out.println(rightType);
        System.out.println(leftType);
        if (rightType.equals(leftType)) {
            return null;
        }
        final var imports = table.getImports();
        if(imports.contains(leftType.getName()) && imports.contains(rightType.getName())) {
            return null;
        }
        var message = String.format("Variable '%s' does not exist - type_assignopvisit", node.getChild(0));
        addReport(Report.newError(Stage.SEMANTIC, NodeUtils.getLine(node), NodeUtils.getColumn(node), message, null)
        );

        return null;
    }

/*    private Void visitArrRefExpr(JmmNode node, SymbolTable table) {

        return null;
    }*/

    private Void visitReturnStmt(JmmNode stmt, SymbolTable table) {
        JmmNode returnVar = stmt.getChild(0);
        if ((returnVar.getKind().equals("IntegerLiteral") && table.getReturnType(currentMethod).getName().equals("int")) || (returnVar.getKind().equals("IntegerLiteral") && table.getReturnType(currentMethod).getName().equals("bool"))) {
            return null;
        } else if (returnVar.getKind().equals("Identifier") && varIsReturnType(returnVar, table)) {
            return null;
        } else if (returnVar.getKind().equals("ArrayIndex")) {
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
        for (var field : table.getFields()) {
            if(field.getName().equals(var.get("id"))) {
                if (field.getType().equals(table.getReturnType(currentMethod))) {
                    return true;
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
