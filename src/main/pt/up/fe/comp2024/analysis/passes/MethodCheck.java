package pt.up.fe.comp2024.analysis.passes;


import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.comp2024.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;


public class MethodCheck extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit("IdUseExpr",this::dealWithCalltoUndeclaredMethod);

    }

    private Void dealWithCalltoUndeclaredMethod(JmmNode node, SymbolTable table) {
        System.out.println(table.getMethods());
        System.out.println(node);
        return null;
    }
}
