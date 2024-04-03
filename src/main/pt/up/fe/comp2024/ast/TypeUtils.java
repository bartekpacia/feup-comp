package pt.up.fe.comp2024.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import java.util.ArrayList;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class TypeUtils {

    private static final String INT_TYPE_NAME = "int";

    public static String getIntTypeName() {
        return INT_TYPE_NAME;
    }

    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @param table
     * @return
     */
    public static Type getExprType(JmmNode expr, SymbolTable table) {
        // TODO: Simple implementation that needs to be expanded

        var kind = Kind.fromString(expr.getKind());

        Type type = switch (kind) {
            case BINARY_EXPR -> getBinExprType(expr);
            case VAR_REF_EXPR -> getVarExprType(expr, table);
            case INTEGER_LITERAL -> new Type(INT_TYPE_NAME, false);
            case IDENTIFIER -> {
                System.out.println("getExprType() for identifier " + expr.get("id"));

                var methodName = expr.getParent().getParent().get("name");
                var locals = new ArrayList<Symbol>();
                locals.addAll(table.getLocalVariables(methodName));
                // locals.addAll(table.getParameters(methodName));

                Type localType = null;
                for (var local : locals) {
                    System.out.println("Found local " + local.getName() + " in method " + methodName);
                    if (local.getName().equals(expr.get("id"))) {
                        System.out.println("Found matching local " + local.getName() +
                                " with type " + local.getType());
                        localType = local.getType();
                        break;
                    }
                }
                yield localType;
                // var symbol = table.getSymbol(identifier);
                // return symbol.getType();
            }
            default -> throw new UnsupportedOperationException("Can't compute type for expression kind '" + kind + "'");
        };

        return type;
    }

    private static Type getBinExprType(JmmNode binaryExpr) {
        // TODO: Simple implementation that needs to be expanded

        String operator = binaryExpr.get("op");

        return switch (operator) {
            case "+", "*" -> new Type(INT_TYPE_NAME, false);
            default ->
                throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + binaryExpr + "'");
        };
    }

    private static Type getVarExprType(JmmNode varRefExpr, SymbolTable table) {
        // TODO: Simple implementation that needs to be expanded
        return new Type(INT_TYPE_NAME, false);
    }

    /**
     * @param sourceType
     * @param destinationType
     * @return true if sourceType can be assigned to destinationType
     */
    public static boolean areTypesAssignable(Type sourceType, Type destinationType) {
        // TODO: Simple implementation that needs to be expanded
        return sourceType.getName().equals(destinationType.getName());
    }
}
