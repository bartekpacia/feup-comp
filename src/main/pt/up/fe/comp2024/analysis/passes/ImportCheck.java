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

public class ImportCheck extends AnalysisVisitor {
    private String currentMethod;
    private String superclass;

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL,this::dealWithClass);
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);
        addVisit("AssignStmt", this::dealWithImportedAssignment);

    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {

        currentMethod = node.get("name");
        return null;
    }

    private Void dealWithClass(JmmNode node, SymbolTable table) {

        superclass = node.get("superClass");
        //System.out.println(superclass);
        return null;
    }

    private Void dealWithImportedAssignment(JmmNode node, SymbolTable table) {

        if(node.getChild(0).getKind().equals("IdUseExpr")){
            if(!table.getImports().contains(node.getChild(0).getChild(0).get("id"))){
                var message = String.format("Class has %s not been imported",node.getChild(0).getChild(0).get("id"));
                addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));

                return null;
            }

        }

        System.out.println("SUPERCLASS:");
        System.out.println(superclass);

        System.out.println(node);
        //System.out.println(TypeUtils.getExprType(node,table));

        System.out.println(node.getChild(0));
        System.out.println(TypeUtils.getExprType(node.getChild(0),table));

        System.out.println(" ");

        /*
        if(node.getChild(0).getKind().equals("Identifier")){  // se houver uma igualdade entre duas variaveis
            System.out.println("INGUALDADE DE DOIS INDENTIFIERS");
            if(TypeUtils.getExprType(node.getChild(0),table).getName().equals(TypeUtils.getExprType(node,table).getName())){

            }
        }

         */

        System.out.println(TypeUtils.getExprType(node,table));





        return null;
    }


}
