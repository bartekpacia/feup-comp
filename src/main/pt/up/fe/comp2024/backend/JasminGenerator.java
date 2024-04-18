package pt.up.fe.comp2024.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    List<Report> reports;

    String code;

    Method currentMethod;

    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;

        // Each of these visitors must be stack-neutral.
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(CallInstruction.class, this::generateCall);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
    }

    public List<Report> getReports() {
        return reports;
    }

    public String build() {
        // This way, build is idempotent
        if (code == null) {
            code = generators.apply(ollirResult.getOllirClass());
        }

        return code;
    }

    private String generateClassUnit(ClassUnit classUnit) {
        final StringBuilder code = new StringBuilder();

        // generate class name
        final ClassUnit ollirClass = ollirResult.getOllirClass();
        final String className = ollirClass.getClassName();
        code.append(".class ").append(className).append(NL);


        final String superClass = JasminUtils.toJasminSuperclassType(ollirClass.getSuperClass());
        code.append(".super ").append(superClass).append(NL).append(NL);

        // generate a single constructor method
        String defaultConstructor = ";default constructor" + NL +
                ".method public <init>()V" + NL +
                "   aload_0" + NL +
                "   invokespecial " + superClass + "/" + "<init>()V" + NL +
                "   return" + NL +
                ".end method" + NL;

        code.append(defaultConstructor);

        // generate code for all other methods
        for (final Method method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(generators.apply(method));
        }

        return code.toString();
    }

    private String generateMethod(Method method) {
        // set method
        currentMethod = method;

        final StringBuilder code = new StringBuilder();

        // calculate access accessModifier
        final String accessModifier = method.getMethodAccessModifier() != AccessModifier.DEFAULT ?
                method.getMethodAccessModifier().name().toLowerCase() + " " :
                "";

        final String nonAccessModifier = method.isStaticMethod() ? "static " : "";
        final String methodName = method.getMethodName();

        code.append("\n.method ").append(accessModifier).append(nonAccessModifier).append(methodName);

        // generate parameters
        code.append("(");
        for (final Element param : method.getParams()) {
            code.append(JasminUtils.toJasminType(param.getType()));
        }
        code.append(")");

        code.append(JasminUtils.toJasminType(method.getReturnType())); // Return type

        code.append(NL);

        // add limits
        code.append(TAB).append(".limit stack 99").append(NL);
        code.append(TAB).append(".limit locals 99").append(NL);

        for (final Instruction inst : method.getInstructions()) {
            final String instCode = StringLines.getLines(generators.apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            code.append(instCode);
        }

        code.append(".end method\n");

        // unset method
        currentMethod = null;

        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        final StringBuilder code = new StringBuilder();

        // generate code for loading what's on the right
        code.append(generators.apply(assign.getRhs()));

        // store value in the stack in destination
        final Element lhs = assign.getDest();

        if (!(lhs instanceof Operand operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        // get register
        final int reg = currentMethod.getVarTable().get(operand.getName()).getVirtualReg();

        final var elementType = lhs.getType().getTypeOfElement();
        final String storeOpcode = switch (elementType) {
            case INT32, BOOLEAN -> "istore"; // There is no separate boolean type on the JVM.
            case OBJECTREF, STRING -> "astore";
            case THIS -> "astore_0"; // bartek: this seems invalid. Assignment to "this" is impossible.
            case ARRAYREF, CLASS, VOID -> throw new NotImplementedException(elementType);
        };
        code.append(storeOpcode).append(" ").append(reg).append(NL);

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return generators.apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        // get register
        final int reg = currentMethod.getVarTable().get(operand.getName()).getVirtualReg();
        return "iload " + reg + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        final StringBuilder code = new StringBuilder();

        // load values on the left and on the right
        code.append(generators.apply(binaryOp.getLeftOperand()));
        code.append(generators.apply(binaryOp.getRightOperand()));

        // apply operation
        final String op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "iadd";
            case MUL -> "imul";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        final StringBuilder code = new StringBuilder();

        final Element operand = returnInst.getOperand();
        if (operand != null) {
            code.append(generators.apply(operand));
            code.append("ireturn").append(NL);
        } else {
            code.append("return");
        }


        return code.toString();
    }

    private String generateCall(CallInstruction callInst) {
        // TODO(bartek): Implement
        final StringBuilder code = new StringBuilder();

        switch (callInst.getInvocationType()) {
            case invokevirtual -> {
                // In virtual method call, first local variable is "this". See JVMS section 2.6.1.
                code.append("aload_0").append(NL);

                code.append("invokevirtual ");

                final String classname = ((ClassType) callInst.getCaller().getType()).getName();
                final String methodname = ((LiteralElement) callInst.getMethodName()).getLiteral().replace("\"", "");
                code.append(classname).append("/").append(methodname);

                // Find matching method by name. Java-- does not support method overloading.
                // TODO(bartek): Support imported and inherited methods (i.e. methods not present in ClassUnit)
                final Method method = ollirResult.getOllirClass().getMethods().stream()
                        .filter(m -> m.getMethodName().equals(methodname))
                        .findFirst().orElseThrow();

                code.append("(");
                code.append(method.getParams().stream()
                        .map(p -> JasminUtils.toJasminType(p.getType()))
                        .collect(Collectors.joining())
                );
                code.append(")");
                code.append(JasminUtils.toJasminType(method.getReturnType()));

                code.append(NL);
            }
            case invokeinterface -> throw new NotImplementedException("Not supported by Java--.");
            case invokespecial -> {
                // invokespecial was named invokenonvirtual in the past.
                // The difference to invokevirtual is that invokespecial is resolved at compile time.

                // The first argument to invokespecial is an objectref.
                // We assume

                final Operand operand = ((Operand) callInst.getCaller());
                final String classname = ((ClassType) operand.getType()).getName();
                final String methodname = ((LiteralElement) callInst.getMethodName()).getLiteral().replace("\"", "");
                final String descriptor = "()V";

                code.append("invokespecial ").append(classname).append("/").append(methodname).append(descriptor).append(NL);

            }
            case invokestatic -> {
                final String classname = ((Operand) callInst.getCaller()).getName();
                final String methodname = ((LiteralElement) callInst.getMethodName()).getLiteral().replace("\"", "");
                final String descriptor = "()V";
                code.append("invokestatic ").append(classname).append("/").append(methodname).append(descriptor).append(NL);
            }
            case NEW -> {
                final String classname = ((Operand) callInst.getCaller()).getName();

                code.append("new ").append(classname).append(NL);
                code.append("dup").append(NL);
            }
            case arraylength -> throw new NotImplementedException("arraylength is not yet implemented");
            case ldc -> throw new NotImplementedException("Not suported by Java--");
        }

        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putFieldInst) {
        // TODO(bartek): Implement
        final StringBuilder code = new StringBuilder();

        // Push last operand onto the stack. Last operand is the value.
        final var value = (LiteralElement) putFieldInst.getOperands().getLast();
        code.append("bipush ").append(value.getLiteral());

        // Example:
        // putfield ClassName/fieldName I
        // code.append("putfield ").append();

        for (final Element operand : putFieldInst.getOperands()) {
            System.out.println("operand: " + operand.toString());
        }


        return code.toString();
    }

    private String generateGetField(GetFieldInstruction putFieldInst) {
        // TODO(bartek): Implement
        final StringBuilder code = new StringBuilder();


        return code.toString();
    }
}
