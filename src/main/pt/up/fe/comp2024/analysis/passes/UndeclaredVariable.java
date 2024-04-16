package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.specs.util.SpecsCheck;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class UndeclaredVariable extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.BINARY_EXPR, this::visitOp);
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt);
    }

    private Void visitOp(JmmNode op, SymbolTable table) {

        if (op.getChild(0).getKind().equals("IntegerLiteral") && op.getChild(1).getKind().equals("IntegerLiteral")) {
            return null;
        }

        if (op.getChild(0).getKind().equals("Identifier") && op.getChild(1).getKind().equals("IntegerLiteral")) {
            SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

            // Var is a field, return
            if (table.getFields().stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(0).get("id")))){
                return null;
            }

            // Var is a parameter, return
            if (table.getParameters(currentMethod).stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(0).get("id")))) {
                return null;
            }

            // Var is a declared variable, return
            if ((table.getLocalVariables(currentMethod).stream()
                    .anyMatch(varDecl -> varDecl.getName().equals(op.getChild(0).get("id")))))  {
                return null;
            }
        }

        if (op.getChild(0).getKind().equals("IntegerLiteral") && op.getChild(1).getKind().equals("Identifier")) {
            // Var is a field, return
            if (table.getFields().stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(1).get("id")))){
                return null;
            }

            // Var is a parameter, return
            if (table.getParameters(currentMethod).stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(1).get("id")))) {
                return null;
            }

            // Var is a declared variable, return
            if ((table.getLocalVariables(currentMethod).stream()
                    .anyMatch(varDecl -> varDecl.getName().equals(op.getChild(1).get("id")))))  {
                return null;
            }
        }

        if (op.getChild(0).getKind().equals("Identifier") && op.getChild(1).getKind().equals("Identifier")) {
            SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

            // Var is a field, return
            if ((table.getFields().stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(0).get("id")))) &&
                    (table.getFields().stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(1).get("id"))))){
                return null;
            }

            // Var is a parameter, return
            if ((table.getParameters(currentMethod).stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(0).get("id")))) &&
                    (table.getParameters(currentMethod).stream()
                    .anyMatch(param -> param.getName().equals(op.getChild(1).get("id"))))) {
                return null;
            }

            // Var is a declared variable, return
            if ((table.getLocalVariables(currentMethod).stream()
                    .anyMatch(varDecl -> varDecl.getName().equals(op.getChild(0).get("id")))) &&
                    (table.getLocalVariables(currentMethod).stream()
                    .anyMatch(varDecl -> varDecl.getName().equals(op.getChild(1).get("id"))))) {
                return null;
            }
        }

        var message = String.format("Variable '%s' does not exist.", op.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(op),
                NodeUtils.getColumn(op),
                message,
                null)
        );

        return null;
    }
    private Void visitReturnStmt(JmmNode stmt, SymbolTable table) {
        JmmNode returnVar = stmt.getChild(0);
        if (returnVar.getKind().equals("IntegerLiteral")) {
            return null;
        } else if (returnVar.getKind().equals("Identifier")) {
                SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

                // Var is a field, return
                if (table.getFields().stream()
                        .anyMatch(param -> param.getName().equals(returnVar.get("id")))) {
                    return null;
                }

                // Var is a parameter, return
                if (table.getParameters(currentMethod).stream()
                        .anyMatch(param -> param.getName().equals(returnVar.get("id")))) {
                    return null;
                }

                // Var is a declared variable, return
                if (table.getLocalVariables(currentMethod).stream()
                        .anyMatch(varDecl -> varDecl.getName().equals(returnVar.get("id")))) {
                    return null;
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

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");

        return null;
    }
}
