package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;
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
        addVisit(Kind.ID_USE_EXPR, this::visitIDUseExpr);
        addVisit(Kind.BINARY_EXPR, this::visitOp);
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt);
    }

    private Void visitIDUseExpr(JmmNode node, SymbolTable table) {
        System.out.println(node);
        System.out.println(node.get("name"));
        for (var method : table.getMethods()) {
            if (method.equals(node.get("name"))) {
                return null;
            }
        }

        final var type = TypeUtils.getExprType(node.getChild(0), table);
        final var importedName = node.getChild(0).get("id");
        for (var tableImport : table.getImports()) {
            if (tableImport.equals(table.getSuper()) || tableImport.equals(type.getName())) {
                return null;
            }

            // Check if method comes from an imported class
            final JmmNode childNode = node.getChild(0);
            if (childNode.getOptional("id").isPresent() && childNode.getOptional("id").get().equals(tableImport)) {
                return null;
            }
        }

        var message = String.format("Variable '%s' does not exist - undv_iduseexprvisit", node);
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null)
        );

        return null;
    }

    private Void visitOp(JmmNode op, SymbolTable table) {

        if (TypeUtils.getExprType(op.getChild(0),table).getName().equals("int") && TypeUtils.getExprType(op.getChild(1),table).getName().equals("int")) {
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

        var message = String.format("Variable '%s' does not exist - undv_opvisit", op.getChild(0));
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
        } else if (returnVar.getKind().equals("ArrayIndex") ) {
            return null;
        }

        var message = String.format("Variable '%s' does not exist - undv_retvisit", stmt.getChild(0));
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
        method.toTree();
        for (var tableMethod : table.getMethods()) {
            if (tableMethod.equals(currentMethod)) {
                return null;
            }
        }

        var message = String.format("Variable '%s' does not exist - undv_methodvisit", method.getChild(0));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(method),
                NodeUtils.getColumn(method),
                message,
                null)
        );
        return null;
    }
}
