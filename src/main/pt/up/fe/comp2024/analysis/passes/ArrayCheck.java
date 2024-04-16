package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.ast.Kind;

public class ArrayCheck extends AnalysisVisitor {
    @Override
    public void buildVisitor() {
        /*addVisit(Kind.BINARY_EXPR, this::visitMethodDecl);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(Kind.RETURN_STMT, this::visitReturnStmt);*/
    }

}
