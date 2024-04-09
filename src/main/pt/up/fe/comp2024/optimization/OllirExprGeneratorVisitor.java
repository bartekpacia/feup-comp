package pt.up.fe.comp2024.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2024.ast.TypeUtils;

import java.util.stream.Collectors;

import static pt.up.fe.comp2024.ast.Kind.*;
import static pt.up.fe.comp2024.optimization.OllirTokens.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private final SymbolTable table;

    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
    }

    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(IDENTIFIER, this::visitIdentifier);
        addVisit(ID_USE_EXPR, this::visitMethodCallExpr);

        setDefaultVisit(this::defaultVisit);
    }


    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = new Type(TypeUtils.getIntTypeName(), false);
        String ollirIntType = OptUtils.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getJmmChild(0));
        var rhs = visit(node.getJmmChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = TypeUtils.getExprType(node, table);
        String resOllirType = OptUtils.toOllirType(resType);
        String code = OptUtils.getTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = TypeUtils.getExprType(node, table);
        computation.append(node.get("op")).append(OptUtils.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = TypeUtils.getExprType(node, table);
        String ollirType = OptUtils.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    private OllirExprResult visitIdentifier(JmmNode node, Void unused) {
        var id = node.get("id");
        Type type = TypeUtils.getExprType(node, table);
        String ollirType = OptUtils.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        final String surroundingMethodName = node
                .getAncestor(METHOD_DECL)
                .map(method -> method.get("name"))
                .orElseThrow();

        final String methodName = node.get("name");

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
        var args = node.getChildrenStream().skip(1).map(child -> visit(child).getCode()).collect(Collectors.joining(", "));

        System.out.println("DEBUG Generator.visitMethodCallExpr(" + methodName + "): args=" + args);

        code.append(args);
        code.append(R_PAREN);

        // Determining the method's return type:
        //  Case 1. If the method is defined in current file, get its return type from the symbol table
        //  Case 2. If the method is imported AND the result is assigned to a variable, the method's return type is the variable's type
        //  Case 3. If the method is imported AND the result is not assigned to a variable, the method's return type is void
        String returnType = "";
        try {
            returnType = table.getReturnType(methodName).getName();
        } catch (NullPointerException ex) {
            // This is okay. Method is not defined in the current file, so it must be imported.
            // If the result of the method call is assigned to a variable, get the variable's type

            final String assignedVariableName = node.getAncestor(ASSIGN_STMT).map(assign -> assign.get("id")).orElse(null);
            if (assignedVariableName != null) {
                // TODO: Handle class field variables, not only local variables
                final Type assignedVariableType = table.getLocalVariables(surroundingMethodName).stream()
                        .filter(var -> var.getName().equals(assignedVariableName))
                        .findFirst()
                        .map(Symbol::getType)
                        .orElseThrow();

                returnType = OptUtils.toOllirType(assignedVariableType);
            } else {
                returnType = OptUtils.toOllirType(new Type("void", false));
            }
        }

        code.append(returnType);
        code.append(END_STMT);

        return new OllirExprResult(code.toString());
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {
        System.out.println("DEBUG ExprGenerator.defaultVisit(" + node.getKind() + "):");

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
