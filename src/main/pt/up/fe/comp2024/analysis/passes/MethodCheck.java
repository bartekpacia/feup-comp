package pt.up.fe.comp2024.analysis.passes;


import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.ast.TypeUtils;


public class MethodCheck extends AnalysisVisitor {

    private JmmNode superClass;
    @Override
    public void buildVisitor() {
        addVisit(Kind.ID_USE_EXPR,this::dealWithCalltoUndeclaredMethod);
        addVisit(Kind.CLASS_DECL,this::classDec);


    }

    private Void classDec(JmmNode node, SymbolTable table) {
        superClass = node;
        return null;

    }


    private Void dealWithCalltoUndeclaredMethod(JmmNode node, SymbolTable table) {
        if(!table.getMethods().contains(node.get("name"))){  // checks if the method beeing called has been declared in this clas before
            if(TypeUtils.getExprType(node.getJmmChild(0),table)==null){
                return null;
            }
            final var type = TypeUtils.getExprType(node.getJmmChild(0),table).getName();
            if((type.equals(superClass.get("name"))) && (superClass.hasAttribute("superClass")) && (table.getImports().contains(superClass.get("superClass")))){
                return null;
            }
            if(!table.getImports().contains(TypeUtils.getExprType(node.getJmmChild(0),table).getName())){  // checks if the method beeing called doesnt bellong to a class that has been imporeted
                var message = String.format("function beeing called hasnt been declared");
                addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                return null;
            }
        }
        return null;
    }
}
