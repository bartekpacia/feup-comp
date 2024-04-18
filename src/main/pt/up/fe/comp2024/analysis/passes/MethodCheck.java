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

        //THIS IS FOR TEST CallToUndeclraredMethods

        if(!table.getMethods().contains(node.get("name"))){  // checks if the method beeing called has been declared in this clas before

            if(!table.getImports().contains(TypeUtils.getExprType(node.getJmmChild(0),table).getName())){  // checks if the method beeing called doesnt bellong to a class that has been imporeted
                var message = String.format("function beeing called hasnt been declared");
                addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                return null;

            }

        }
        return null;
    }
}
