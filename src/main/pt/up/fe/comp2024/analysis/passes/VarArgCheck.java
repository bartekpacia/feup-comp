package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;

public class VarArgCheck extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode node, SymbolTable table) {
        final var params = table.getParameters(node.get("name"));
        final var lastIdx = params.size() - 1;
        for (var param : params) {
            var paramType = param.getType().getName();
            System.out.println("EHHEHEEH paramType is: " + paramType);
            if ((params.get(lastIdx) != param) && (paramType.equals("int..."))) {
                var message = String.format("Variable '%s' does not exist - vararg_methodvisit", node);
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        NodeUtils.getLine(node),
                        NodeUtils.getColumn(node),
                        message,
                        null)
                );
            }
        }

        return null;
    }
}
