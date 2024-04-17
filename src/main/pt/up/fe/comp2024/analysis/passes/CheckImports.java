package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.comp2024.ast.TypeUtils;

/**
 * Checks for imports
 *
 * @author DiogoTomaz
 */

public class CheckImports extends AnalysisVisitor {

    public void buildVisitor() {

        //addVisit(Kind.CLASS_DECL, this::dealWithClass);
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);

    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {
        System.out.println(table.getImports());

        System.out.println(node);
        for(var child : node.getChildren()){
            System.out.println(child);
            if(child.getKind().equals("VarDecl")){
                System.out.println("CHECKING VAR DECLARATION");
                System.out.println(TypeUtils.getExprType(child,table));
            }

        }


        if(!table.getImports().stream().anyMatch(param -> param.equals(node.get("name")))) {

            // Create error if Class has not been imported
            var message = String.format("Super class  '%s' has not been imported.",node.get("name"));
            addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));

        }

        return null;
    }




}
