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
        final JmmNode firstOperand = op.getChild(0);
        final JmmNode secondOperand = op.getChild(1);
        // Check if operand types are the same
        {
            final Type firstOperandType = TypeUtils.getExprType(firstOperand, table);
            final Type secondOperandType = TypeUtils.getExprType(secondOperand, table);
            System.out.println(op);
            System.out.println(op.getChild(0));
            System.out.println(op.getChild(1));
            System.out.println(firstOperandType.getName());
            System.out.println(secondOperandType.getName());
            if (firstOperandType != null && secondOperandType != null) {
                final boolean firstOperandTypeOk = firstOperandType.getName().equals("int") || firstOperandType.getName().equals("int[]");
                final boolean secondOperandTypeOk = secondOperandType.getName().equals("int");
                final boolean sameOperandTypes = firstOperandTypeOk && secondOperandTypeOk;
                if (sameOperandTypes) {
                    return null;
                }
            }
        }

        System.out.println("burro");
        var message = String.format("Variable '%s' does not exist - undv_opvisit", op);
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
        if(returnVar.getKind().equals("Bool")) {
            return null;
        } else if (returnVar.getKind().equals("IntegerLiteral")) {
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
