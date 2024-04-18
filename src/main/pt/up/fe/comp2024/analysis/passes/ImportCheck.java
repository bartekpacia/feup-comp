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
        for(var local : table.getLocalVariables(currentMethod)){
            //System.out.println(x.getName());
            if(node.get("id").equals(local.getName())){
                //System.out.println("A VARIAVEL EXISTE NA FUNCAO");
                return null;
            }
        }

        for (var param : table.getParameters(currentMethod)) {
            if(node.get("id").equals(param.getName())){
                return null;
            }
        }

        for (var field : table.getFields()) {
            if(node.get("id").equals(field.getName())){
                return null;
            }
        }

        if(!table.getImports().contains(node.get("id"))){
            var message = String.format("Class %s has not been imported", node.get("id"));
            addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
        }

        // System.out.println("---------------------------------------");


        return null;
    }

}
