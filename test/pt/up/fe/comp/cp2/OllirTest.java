package pt.up.fe.comp.cp2;

import org.junit.Test;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class OllirTest {

    @Test
    public void compileBasic() {
        testJmmCompilation("pt/up/fe/comp/cp2/ollir/CompileBasic.jmm", this::compileBasic);
    }

    @Test
    public void compileArithmetic() {
        testJmmCompilation("pt/up/fe/comp/cp2/ollir/CompileArithmetic.jmm", this::compileArithmetic);
    }

    @Test
    public void compileMethodInvocation() {
        testJmmCompilation("pt/up/fe/comp/cp2/ollir/CompileMethodInvocation.jmm", this::compileMethodInvocation);
    }

    @Test
    public void compileAssignment() {
        testJmmCompilation("pt/up/fe/comp/cp2/ollir/CompileAssignment.jmm", this::compileAssignment);
    }

    @Test
    public void compileExtra() {
        testJmmCompilation("pt/up/fe/comp/cp2/ollir/CompileExtra.jmm", this::compileExtra);
    }

    public static void testJmmCompilation(String resource, Consumer<ClassUnit> ollirTester, String executionOutput) {
        // If AstToJasmin pipeline, generate Jasmin
        if (TestUtils.hasAstToJasminClass()) {

            var result = TestUtils.backend(SpecsIo.getResource(resource));

            var testName = new File(resource).getName();
            printFilename(testName, result.getJasminCode());
            var runOutput = result.runWithFullOutput();
            assertEquals("Error while running compiled Jasmin: " + runOutput.getOutput(), 0,
                    runOutput.getReturnValue());
            System.out.println("\n Result: " + runOutput.getOutput());

            if (executionOutput != null) {
                assertEquals(executionOutput, runOutput.getOutput());
            }

            return;
        }

        var result = TestUtils.optimize(SpecsIo.getResource(resource));
        var testName = new File(resource).getName();
        printFilename(testName, result.getOllirCode());

        if (ollirTester != null) {
            ollirTester.accept(result.getOllirClass());
        }
    }

    private static void printFilename(String testFileName, String code) {
        System.out.println("\n---\n" + testFileName + "\n---\n" + code);
    }

    public static void testJmmCompilation(String resource, Consumer<ClassUnit> ollirTester) {
        testJmmCompilation(resource, ollirTester, null);
    }

    public void compileBasic(ClassUnit classUnit) {
        // Test name of the class and super
        assertEquals("Class name not what was expected", "CompileBasic", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());

        // Test fields
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        var fieldNames = new HashSet<>(Arrays.asList("intField", "boolField"));
        assertThat(fieldNames, hasItem(classUnit.getField(0).getFieldName()));
        assertThat(fieldNames, hasItem(classUnit.getField(1).getFieldName()));

        // Test method 1
        Method method1 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method1"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method1", method1);

        var retInst1 = method1.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method1", retInst1.isPresent());

        // Test method 2
        Method method2 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method2"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method2'", method2);

        var retInst2 = method2.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method2", retInst2.isPresent());
    }

    public void compileArithmetic(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileArithmetic", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var binOpInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(instr -> (AssignInstruction) instr)
                .filter(assign -> assign.getRhs() instanceof BinaryOpInstruction)
                .findFirst();

        assertTrue("Could not find a binary op instruction in method " + methodName, binOpInst.isPresent());

        var retInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method " + methodName, retInst.isPresent());

    }

    public void compileMethodInvocation(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileMethodInvocation", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var callInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof CallInstruction)
                .map(CallInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find a call instruction in method " + methodName, callInst.isPresent());

        assertEquals("Invocation type not what was expected", CallType.invokestatic,
                callInst.get().getInvocationType());
    }

    public void compileAssignment(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileAssignment", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var assignInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(AssignInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find an assign instruction in method " + methodName, assignInst.isPresent());

        assertEquals("Assignment does not have the expected type", ElementType.INT32,
                assignInst.get().getTypeOfAssign().getTypeOfElement());
    }

    public void compileExtra(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileExtra", classUnit.getClassName());

        // Test method foo
        {
            final String methodName = "foo";
            Method actualMethod = classUnit.getMethods().stream()
                    .filter(method -> method.getMethodName().equals(methodName))
                    .filter(Method::isStaticMethod)
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find static method " + methodName, actualMethod);

            // Assert there are 3 calls to nonexist.func()
            final List<CallInstruction> assignInstructions = actualMethod.getInstructions().stream()
                    .filter(instr -> instr instanceof CallInstruction)
                    .map(CallInstruction.class::cast)
                    .filter(instr -> instr.getInvocationType() == CallType.invokestatic)
                    .filter(instr -> instr.getReturnType().getTypeOfElement() == ElementType.VOID)
                    .filter(instr -> ((Operand) instr.getCaller()).getName().equals("nonexist"))
                    .toList();

            assertEquals("Expected 3 calls to nonexist.func()", 3, assignInstructions.size());
        }


        // Test method bar
        {
            final String methodName = "bar";
            Method actualMethod = classUnit.getMethods().stream()
                    .filter(method -> method.getMethodName().equals(methodName))
                    .filter(method -> !method.isStaticMethod())
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, actualMethod);

            final AssignInstruction assignInst = actualMethod.getInstructions().stream()
                    .filter(inst -> inst instanceof AssignInstruction)
                    .map(AssignInstruction.class::cast)
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find an assign instruction in method " + methodName, assignInst);

            assertEquals(
                    "Assignment does not have the expected type",
                    ElementType.INT32,
                    assignInst.getTypeOfAssign().getTypeOfElement()
            );
        }

        // Test method baz
        {
            final String methodName = "baz";
            Method actualMethod = classUnit.getMethods().stream()
                    .filter(method -> method.getMethodName().equals(methodName))
                    .filter(method -> !method.isStaticMethod())
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, actualMethod);

            final AssignInstruction assignInst = actualMethod.getInstructions().stream()
                    .filter(inst -> inst instanceof AssignInstruction)
                    .map(AssignInstruction.class::cast)
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find an assign instruction in method " + methodName, assignInst);

            assertEquals(
                    "Assignment does not have the expected type",
                    ElementType.BOOLEAN,
                    assignInst.getTypeOfAssign().getTypeOfElement()
            );
        }

        // Test method foo
        {
            final String methodName = "qux";
            Method actualMethod = classUnit.getMethods().stream()
                    .filter(method -> method.getMethodName().equals(methodName))
                    .filter(method -> !method.isStaticMethod())
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, actualMethod);


            final CallInstruction callInstr = actualMethod.getInstructions().stream()
                    .filter(instr -> instr instanceof AssignInstruction)
                    .map(AssignInstruction.class::cast)
                    .map(AssignInstruction::getRhs)
                    .filter(instr -> instr instanceof CallInstruction)
                    .map(CallInstruction.class::cast)
                    .filter(instr -> instr.getInvocationType() == CallType.invokestatic)
                    .filter(instr -> ((Operand) instr.getCaller()).getName().equals("nonexist"))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find the right call instruction in method " + methodName, callInstr);

            assertEquals(
                    "Method call does not have the expected type",
                    ElementType.INT32,
                    callInstr.getReturnType().getTypeOfElement()
            );
        }
    }
}