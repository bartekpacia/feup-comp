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

import java.sql.SQLOutput;


public class MethodCheck extends AnalysisVisitor {

    private JmmNode sclass;
    @Override
    public void buildVisitor() {
        addVisit(Kind.ID_USE_EXPR,this::dealWithCalltoUndeclaredMethod);
        addVisit(Kind.CLASS_DECL,this::classDec);


    }

    private Void classDec(JmmNode node, SymbolTable table) {
        System.out.println(node);
        sclass=node;
        return null;

    }


    private Void dealWithCalltoUndeclaredMethod(JmmNode node, SymbolTable table) {

        //THIS IS FOR TEST CallToUndeclraredMethods
        System.out.println("I AM HEREEEEEEEEEEEEEE");
        System.out.println(node);

        if(!table.getMethods().contains(node.get("name"))){  // checks if the method beeing called has been declared in this clas before
            System.out.println("CHECKED THAT THE METHOD WAS NOT DECLARED ON THIS CLASS BEFORE");

            if(TypeUtils.getExprType(node.getJmmChild(0),table)==null){
                return null;
            }

            System.out.println(TypeUtils.getExprType(node.getJmmChild(0),table).getName());
            System.out.println(sclass);
            if(TypeUtils.getExprType(node.getJmmChild(0),table).getName().equals(sclass.get("name"))){
                System.out.println("ENTREI NO IF");
                if(sclass.hasAttribute("superClass")){
                    if(table.getImports().contains(sclass.get("superClass"))){
                        System.out.println("ENTREI");
                        return null;
                    }

                }

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
