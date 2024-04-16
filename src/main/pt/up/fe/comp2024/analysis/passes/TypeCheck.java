package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.specs.util.SpecsCheck;

public class TypeCheck extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl); //get curr method
        addVisit(Kind.BINARY_EXPR, this::visitArithOp); //Arithmetic and NOT
        addVisit(Kind.BOOL_OP, this::visitCondOp); // AND/OR
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt); // Assignments
        addVisit(Kind.ARR_REF_EXPR, this::visitArrRefExpr); //Array ref
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt); //Returns
    }

    private Void visitArithOp(JmmNode node, SymbolTable table) {
        JmmNode leftNode = node.getChild(0);
        JmmNode rightNode = node.getChild(1);

        if (leftNode.getKind().equals("IntegerLiteral") && leftNode.getKind().equals("IntegerLiteral")) {
            return null;
        }

        var message = String.format("Variable '%s' does not exist.", node.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null)
        );
        return null;
    }
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitCondOp(JmmNode node, SymbolTable table) {
        JmmNode leftNode = node.getChild(0);
        JmmNode rightNode = node.getChild(1);

        if (leftNode.getKind().equals("Bool") && leftNode.getKind().equals("Bool")) {
            return null;
        }

        var message = String.format("Variable '%s' does not exist.", node.getChild(0));
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
        return null;
    }

    private Void visitArrRefExpr(JmmNode node, SymbolTable table) {
        return null;
    }

    private Void visitReturnStmt(JmmNode stmt, SymbolTable table) {
        JmmNode returnVar = stmt.getChild(0);
        if ((returnVar.getKind().equals("IntegerLiteral") && table.getReturnType(currentMethod).getName().equals("int")) || (returnVar.getKind().equals("IntegerLiteral") && table.getReturnType(currentMethod).getName().equals("bool"))) {
            return null;
        } else if (returnVar.getKind().equals("Identifier")) {
            SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

            for (var field : table.getFields()) {
                if(field.getName().equals(returnVar.get("id"))) {
                    if (field.getType().equals(table.getReturnType(currentMethod))) {
                        return null;
                    }
                }
            }

            for (var param : table.getParameters(currentMethod)) {
                if(param.getName().equals(returnVar.get("id"))) {
                    if (param.getType().equals(table.getReturnType(currentMethod))) {
                        return null;
                    }
                }
            }

            for (var local : table.getLocalVariables(currentMethod)) {
                if(local.getName().equals(returnVar.get("id"))) {
                    if (local.getType().equals(table.getReturnType(currentMethod))) {
                        return null;
                    }
                }
            }
        }

        var message = String.format("Variable '%s' does not exist.", stmt.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(stmt),
                NodeUtils.getColumn(stmt),
                message,
                null)
        );

        return null;
    }
}
