package pt.up.fe.comp2024.backend;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class JasminUtils {
    /**
     * Converts OLLIR type into Jasmin type.
     */
    public static String toJasminType(String ollirType) {
        // TODO(bartek): Change parameter type to org.specs.comp.ollir.Type

        final StringBuilder code = new StringBuilder();

        if (ollirType.startsWith("args.array.")) {
            code.append("[");
            ollirType = ollirType.replaceFirst("args.array.", "");
        }

        final String type = switch (ollirType) {
            case "int" -> "I";
            case "void" -> "V";
            case "bool" -> "Z";
            case "String" -> "Ljava/lang/String";
            case "Object" -> "Ljava/lang/Object";
            default -> throw new NotImplementedException("OLLIR type " + ollirType);
        };

        code.append(type);

        return code.toString();
    }

    /**
     * Converts OLLIR type into Jasmin "superclass" type (without the L).
     */
    public static String toJasminSuperclassType(String ollirType) {
        if (ollirType.equals("Object")) {
            return "Ljava/lang/Object";
        }

        return ollirType;
    }
}