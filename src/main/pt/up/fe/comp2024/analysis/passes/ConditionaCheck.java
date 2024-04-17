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


public class ConditionaCheck extends AnalysisVisitor {


    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit("WhileStmt", this::WhileStmtAccess);
        addVisit("IfElseStmt", this::visitIfElseCondition);
        addVisit(Kind.METHOD_DECL, this::dealWithMethod);
    }

    private Void dealWithMethod(JmmNode node, SymbolTable table) {

        currentMethod = node.get("name");
        return null;
    }

    private Void WhileStmtAccess(JmmNode node, SymbolTable table){

        for(var child : table.getLocalVariables(currentMethod)){

            if(child.getName().equals(node.getChild(0).get("id"))){
                //System.out.println(child.getType().isArray());

                if(child.getType().isArray()){
                    var message = String.format("Array invalid as While Statment" );
                    addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                    return null;
                }


                if(!child.getType().getName().equals("boolean")){
                    var message = String.format("While statment must be boolean" );
                    addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                    return null;

                }

            }
        }


        return null;
    }


    private Void visitIfElseCondition(JmmNode node, SymbolTable table) {
        System.out.println("IFELSESTATMENT");
        for(var child : node.getChildren()){
            System.out.println(child);

            if(child.getKind().equals("IfStatment")){
                System.out.println("IF NODE");
                System.out.println(child.getChild(0));

                //boolean
                //identifier boolean

                if(!TypeUtils.getExprType(child.getJmmChild(0),table).getName().equals("boolean")){
                    var message = String.format("if condition is not of type boolean");
                    addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                    return null;
                }

            }

            if(child.getKind().equals("ElseStatment")){

                // Get the first child of the else statement (which should be either a block or null)
                JmmNode elseBlock = child.getChild(0);

                if (elseBlock != null && child.getChild(0).getChildren().isEmpty()) {
                    //System.out.println("Nothing in the else block");
                    var message = String.format("Nothing in the else block");
                    addReport(Report.newError(Stage.SEMANTIC, 5, 5, message, null));
                    return null;
                }

            }

        }

        return null;
    }



}
