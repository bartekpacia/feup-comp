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



public class UndecleclaretionCheck extends AnalysisVisitor {
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);
        addVisit("VarDecl", this::varDeclCheckDeclaration);

    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {

        currentMethod = node.get("name");
        return null;
    }

    private Void varDeclCheckDeclaration(JmmNode node, SymbolTable table) {
        final var varType = node.getChild(0).get("name");
        final var methods = table.getMethods();
        if(varType.equals("int") || varType.equals("boolean") || varType.equals("String")){
            return null;
        }

        if(!methods.contains(varType)){
            var message = String.format("Method in Vardeclaration does not exist" );
            addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
            return null;
        }

        return null;
    }

}
