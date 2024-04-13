package pt.up.fe.comp2024.backend;

import org.specs.comp.ollir.ArrayType;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import org.specs.comp.ollir.Type;

public class JasminUtils {
    /**
     * Converts OLLIR type into Jasmin type.
     */
    public static String toJasminType(Type ollirType) {
        return switch (ollirType.getTypeOfElement()) {
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case ARRAYREF -> {
                final ArrayType ollirArrayType = ((ArrayType) ollirType);
                final StringBuilder code = new StringBuilder();

                code.append("[".repeat(ollirArrayType.getNumDimensions()));

                final String elementType = toJasminType(ollirArrayType.getElementType());
                code.append(elementType);

                yield code.toString();
            }
            case OBJECTREF -> throw new NotImplementedException("ElementType.OBJECTREF");
            case CLASS -> throw new NotImplementedException("ElementType.CLASS");
            case THIS -> throw new NotImplementedException("ElementType.THIS");
            case STRING -> "Ljava/lang/String;";
            case VOID -> "V";
        };
    }

    /**
     * Converts OLLIR type into Jasmin "superclass" type (without the L).
     */
    public static String toJasminSuperclassType(String ollirType) {
        if (ollirType == null || ollirType.equals("Object")) {
            return "java/lang/Object";
        }

        return ollirType;
    }
}