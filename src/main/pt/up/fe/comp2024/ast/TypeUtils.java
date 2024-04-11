package pt.up.fe.comp2024.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import static pt.up.fe.comp2024.ast.Kind.ASSIGN_STMT;
import static pt.up.fe.comp2024.ast.Kind.METHOD_DECL;

public class TypeUtils {

    private static final String INT_TYPE_NAME = "int";
    private static final String VOID_TYPE_NAME = "void";

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
            case INTEGER_LITERAL -> {
                final String value = expr.get("value");
                final String debugPrefix = "DEBUG   TypeUtils.getExprType(INTEGER_LITERAL " + value + "): ";
                System.out.println(debugPrefix);


                final Type retType = new Type(INT_TYPE_NAME, false);
                System.out.println(debugPrefix + "yield type " + retType.toString());
                yield retType;
            }
            case ID_USE_EXPR -> {
                String name = expr.get("name");

                // Determining the method's return type:
                //  Case 1. If the method is defined in current file, get its return type from the symbol table
                //  Case 2. If the method is imported AND the result is assigned to a variable, the method's return type is the variable's type
                //  Case 3. If the method is imported AND the result is not assigned to a variable, the method's return type is void
                String returnType = "";
                try {
                    returnType = table.getReturnType(name).getName();
                } catch (NullPointerException ex) {
                    // This is okay. Method is not defined in the current file, so it must be imported.
                    // If the result of the method call is assigned to a variable, get the variable's type

                    final String assignedVariableName = expr.getAncestor(ASSIGN_STMT).map(assign -> assign.get("id")).orElse(null);
                    if (assignedVariableName != null) {
                        // TODO(bartek): Handle class field variables, not only local variables
                        String surroundingMethodNname = expr.getAncestor(METHOD_DECL).map(method -> method.get("name")).orElseThrow();

                        final Type assignedVariableType = table.getLocalVariables(surroundingMethodNname).stream()
                                .filter(var -> var.getName().equals(assignedVariableName))
                                .findFirst()
                                .map(Symbol::getType)
                                .orElseThrow();

                        returnType = assignedVariableType.getName();
                    } else {
                        returnType = "void";
                    }
                }

                yield new Type(returnType, false);
            }
            case IDENTIFIER -> {
                final String ident = expr.get("id");

                final String debugPrefix = "DEBUG   TypeUtils.getExprType(IDENTIFIER " + ident + "): ";
                System.out.println(debugPrefix);

                String methodName = expr.getAncestor(METHOD_DECL).map(method -> method.get("name")).orElseThrow();

                Type localType = null;

                // Search for identifier in method's locals
                var locals = new ArrayList<>(table.getLocalVariables(methodName));
                for (var local : locals) {
                    System.out.print(debugPrefix + "Found local " + local.getName() + " in method " + methodName + " with type " + local.getType().getName());
                    if (local.getName().equals(ident)) {
                        System.out.println(" - MATCH");
                        localType = local.getType();
                        break;
                    } else {
                        System.out.println(" - NO MATCH");
                    }
                }

                // Search for identifier in method's parameters
                var params = new ArrayList<>(table.getParameters(methodName));
                for (var param : params) {
                    System.out.print(debugPrefix + "Found param " + param.getName() + " in method " + methodName + " with type " + param.getType().getName());
                    if (param.getName().equals(ident)) {
                        System.out.println(" - MATCH");
                        localType = param.getType();
                        break;
                    } else {
                        System.out.println(" - NO MATCH");
                    }
                }

                // Search for identifier in file's imports
                var imports = new ArrayList<>(table.getImports());
                for (var imp : imports) {
                    System.out.print(debugPrefix + "Found import " + imp + " in file");
                    if (imp.equals(ident)) {
                        System.out.println(" - MATCH");
                        localType = new Type(VOID_TYPE_NAME, false);
                        break;
                    } else {
                        System.out.println(" - NO MATCH");
                    }
                }

                System.out.println(debugPrefix + "yield type " + localType.toString());
                yield localType;
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
