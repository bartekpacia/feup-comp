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
        //addVisit(Kind.CLASS_DECL,this::dealWithClass);
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);
        addVisit("AssignStmt", this::dealWithImportedAssignment);

    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {

        currentMethod = node.get("name");
        return null;
    }

    /*
    private Void dealWithClass(JmmNode node, SymbolTable table) {

        superclass = node.get("superClass");
        //System.out.println(superclass);
        return null;
    }

     */

    private Void dealWithImportedAssignment(JmmNode node, SymbolTable table) {

        return null;
    }


}
