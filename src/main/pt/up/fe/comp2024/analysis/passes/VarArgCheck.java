package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;

public class VarArgCheck extends AnalysisVisitor {
    
    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }
    
    private Void visitMethodDecl(JmmNode node, SymbolTable table) {
        var params = table.getParameters(node.get("name"));
        var lastIdx = params.size()-1;
        for (var param : params) {
            if((params.get(lastIdx) != param) && (param.getType().getName().equals("int..."))) {
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
