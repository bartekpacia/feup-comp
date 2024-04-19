package pt.up.fe.comp2024.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2024.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;

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
        /* Let's say we're visiting:

            int a;
            int b;
            int c;
            c = a + b;
        */

        // tmp0.i32 := .i32 a.i32 + .i32 b.i32;
        // c.int32 := .i32 tmp0.32;


        final Type exprType = TypeUtils.getExprType(node, table);
        final String exprOllirType = OptUtils.toOllirType(exprType);

        final var lhs = visit(node.getChild(0));
        final var rhs = visit(node.getChild(1));

        final StringBuilder computation = new StringBuilder();
        final String code = OptUtils.getTemp() + exprOllirType;                       // tmp0.i32

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        computation.append(code);                                                     // tmp0.i32
        computation.append(SPACE).append(ASSIGN).append(SPACE);                       // :=
        computation.append(exprOllirType).append(SPACE).append(lhs.getCode());        // .i32 a.i32
        computation.append(node.get("op")).append(SPACE);                             // +
        computation.append(exprOllirType).append(SPACE).append(rhs.getCode());        // .i32 b.i32
        computation.append(END_STMT);

        System.out.println("DEBUG ExprGenerator.visitBinExpr:");
        System.out.println("DEBUG   lhs: " + lhs.getComputation());
        System.out.println("DEBUG   rhs: " + rhs.getComputation());
        System.out.println("DEBUG   computation:" + computation.toString().replaceAll(NL, ""));
        System.out.println("DEBUG          code:" + code);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        final String code = "this";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitIdentifier(JmmNode node, Void unused) {
        String id = node.get("id");
        final String methodName = node.getAncestor(METHOD_DECL).map(method -> method.get("name")).orElseThrow();

        // Hacky hack. Search for identifier in method's parameters
        final List<Symbol> params = new ArrayList<>(table.getParameters(methodName));
        for (int i = 0; i < params.size(); i++) {
            final Symbol param = params.get(i);

            // FIXME(bartek): Ugly hack to reference actuals. Working around the need of prefixing with $ and actual's index.
            if (param.getName().equals(id)) {
                id = "$" + i + "." + id;
                break;
            }
        }

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
        final String invocationCode = switch (firstChild.getKind()) {
            case "Identifier" -> {
                final String id = firstChild.get("id");
                final boolean receiverIsImport = table.getImports().stream().anyMatch(importName -> importName.equals(id));

                if (receiverIsImport) {
                    // Receiver is an imported class.
                    // Example:
                    //   e.g io.println(foo)
                    yield "invokestatic(" + id;
                } else {
                    final Type idType = TypeUtils.getExprType(firstChild, table);

                    yield "invokevirtual(" + id + "." + idType.getName();
                }
            }
            case "VarRefExpr" -> "invokevirtual(this";
            default -> throw new IllegalStateException("Invalid first child node of a method");
        };

        final StringBuilder code = new StringBuilder();

        final StringBuilder methodInvocationCode = new StringBuilder();
        {
            final List<String> actuals = node.getChildrenStream().skip(1).map(child -> visit(child).getCode()).toList();

            final List<String> codes = new ArrayList<>();
            codes.add(invocationCode);
            codes.add('"' + methodName + '"');
            codes.addAll(actuals);

            methodInvocationCode.append(String.join(", ", codes));
            methodInvocationCode.append(R_PAREN);
        }

        // Example code I want to generate:
        //
        // tmp0.i32 = .i32 invokevirtual(this, "constInstr").i32
        // .i32 tmp0.i32
        //
        // First line is computation.
        // Second line is code.

        code.append(methodInvocationCode);
        code.append(returnType);

        return new OllirExprResult(code.toString());
        // return new OllirExprResult(code.toString(), code);
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
