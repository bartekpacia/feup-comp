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


    @Override
    public void buildVisitor() {
        addVisit("Identifier",this::dealWithImportedAssignment);
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);

    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {

        currentMethod = node.get("name");
        return null;
    }

    private Void dealWithImportedAssignment(JmmNode node, SymbolTable table) {
        //System.out.println("---------------------------------------");
        //System.out.println(node.get("id"));

        //System.out.println("VARIAVEIS DECLARADAS");
        for(var x : table.getLocalVariables(currentMethod)){
            //System.out.println(x.getName());
            if(node.get("id").equals(x.getName())){
                //System.out.println("A VARIAVEL EXISTE NA FUNCAO");
                return null;
            }
        }

        if(!table.getImports().contains(node.get("id"))){
            //System.out.println("A NAO FOI IMPORTED");
            var message = String.format("Class has not been imported");
            addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
        }

        // System.out.println("---------------------------------------");


        return null;
    }

}
