package pt.up.fe.comp2024.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2024.ast.TypeUtils;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        addVisit(NEW_OBJECT, this::visitNewObjectExpr);

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
//        .

        // String code = id + ollirType;
        String code = "this";

        return new OllirExprResult(code);
    }

    private OllirExprResult visitIdentifier(JmmNode node, Void unused) {
        final String id = node.get("id");
        final Type type = TypeUtils.getExprType(node, table);
        final String ollirType = OptUtils.toOllirType(type);
        final String code = id + ollirType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        final Type type = TypeUtils.getExprType(node, table);
        final String returnType = OptUtils.toOllirType(type);

        final String methodName = node.get("name");

        // First child of IdUseExpr is:
        //  * Identifier:
        //   * the receiver, a.k.a the object that the method is called on: obj.foo(bar)
        //  * VarRefExpr ("this"), in case it is a virtual method
        final JmmNode firstChild = node.getChild(0);
        final String receiver = switch (firstChild.getKind()) {
            case "Identifier" -> {
                final String id = firstChild.get("id");
                // bartek: Is this needed?
                final boolean receiverIsImport = table.getImports().stream().anyMatch(importName -> importName.equals(id));

                if (receiverIsImport) {
                    // Receiver is an imported class.
                    // Example:
                    //   e.g io.println(foo)
                    yield id;
                } else {
                    yield id;
                }
            }
            case "VarRefExpr" -> "this";
            default -> throw new IllegalStateException("Invalid first child node of a method");
        };

        final StringBuilder code = new StringBuilder();

        final StringBuilder methodInvocationCode = new StringBuilder();
        {
            // TODO(bartek): Differentiate between static and virtual method call
            final String leadingCode = "invokestatic" + L_PAREN;
            methodInvocationCode.append(leadingCode);

            final Stream<String> leading = Stream.of(receiver, "\"" + methodName + "\"");
            final Stream<String> args = node.getChildrenStream().skip(1).map(child -> visit(child).getCode()); // Visit more JmmNode children to get the actuals
            methodInvocationCode.append(Stream.concat(leading, args).collect(Collectors.joining(", ")));
            methodInvocationCode.append(R_PAREN);
        }

        code.append(methodInvocationCode);
        code.append(returnType);

        return new OllirExprResult(code.toString());
    }

    private OllirExprResult visitNewObjectExpr(JmmNode node, Void unused) {
        final String type = node.get("id");
        final String code = "new" + "(" + type + ")" + "." + type;
        return new OllirExprResult(code);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {
        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }
}
