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
        int lastIdx = table.getParameters(node.get("node")).size()-1;
        for (int i = 0; i < table.getParameters(node.get("node")).size(); i++) {
            if(TypeUtils.getExprType(node,table).getName().equals("int...") && (i == lastIdx)) {
                return null;
            }
        }

        var message = String.format("Variable '%s' does not exist.", node.get("id"));
        addReport(Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null)
        );
        return null;
    }
}
