package pt.up.fe.comp2024.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.TypeUtils;
import java.util.*;

import static pt.up.fe.comp2024.ast.Kind.METHOD_DECL;
import static pt.up.fe.comp2024.ast.Kind.VAR_DECL;

public class JmmSymbolTableBuilder {

    public static JmmSymbolTable build(JmmNode root) {
        List<String> imports = new ArrayList<>();
        String className = "";
        String extendedName = "";
        List<Symbol> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        Map<String, Type> returnTypes = new HashMap<>();
        Map<String, List<Symbol>> params = new HashMap<>();
        Map<String, List<Symbol>> locals = new HashMap<>();

        for (var childNode : root.getChildren()) {
            if (Kind.CLASS_DECL.check(childNode)) {
                className = childNode.get("name");
                extendedName = childNode.getOptional("mainc").orElse("");
                methods = buildMethods(childNode);
                fields = buildFields(childNode);
                returnTypes = buildReturnTypes(childNode);
                params = buildParams(childNode);
                locals = buildLocals(childNode);
            } else {
                imports.add(buildImports(childNode));
            }
        }
        return new JmmSymbolTable(imports, className, extendedName, fields, methods, returnTypes, params, locals);
    }

    private static String buildImports(JmmNode importDecl) {
        List<String> names = importDecl.getObjectAsList("name", String.class);
        return String.join(".", names);
    }

    //
    private static Map<String, Type> buildReturnTypes(JmmNode classDecl) {

        Map<String, Type> map = new HashMap<>();
        System.out.println("JmmSymbolTableBuilder: printing root JmmNode");
        System.out.println(classDecl.toTree());
        classDecl.getChildren(METHOD_DECL).stream()
                .forEach(method -> map.put(method.get("name"), new Type(TypeUtils.getIntTypeName(), false)));

        return map;
    }

    private static Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();
        var intType = new Type(TypeUtils.getIntTypeName(), false);

        classDecl.getChildren(METHOD_DECL).stream()
                .forEach(method -> {
                    System.out.println("Going over parameters of method " + method.get("name"));

                    List<Symbol> parameters = new ArrayList<>();

                    method.getChildren("Param").stream()
                            .forEach(param -> parameters.add(new Symbol(intType, param.get("name"))));

                    map.put(method.get("name"), parameters);
                });

        /*
         * classDecl.getChildren(METHOD_DECL).stream()
         * .forEach(method -> map.put(method.get("name"),
         * method.getChildren("Param").stream().forEach(param -> param.get("name"))));
         */
        // method.getChildren("Param").stream().forEach(param -> new Symbol(intType,
        // param.get("name")))
        return map;
    }

    private static Map<String, List<Symbol>> buildLocals(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();

        classDecl.getChildren(METHOD_DECL).stream()
                .forEach(method -> map.put(method.get("name"), getLocalsList(method)));

        return map;
    }

    // works
    private static List<String> buildMethods(JmmNode classDecl) {
        return classDecl.getChildren(METHOD_DECL).stream()
                .map(method -> method.get("name"))
                .toList();
    }

    private static List<Symbol> buildFields(JmmNode classDecl) {
        return classDecl.getChildren(VAR_DECL).stream()
                .map(fieldDecl -> {
                    final JmmNode typeNode = fieldDecl.getChildren("Type").get(0);
                    final String type = typeNode.get("name");
                    return new Symbol(new Type(type, false), fieldDecl.get("name"));
                })
                .toList();
    }

    private static List<Symbol> getLocalsList(JmmNode methodDecl) {
        var intType = new Type(TypeUtils.getIntTypeName(), false);

        return methodDecl.getChildren(VAR_DECL).stream()
                .map(varDecl -> new Symbol(intType, varDecl.get("name")))
                .toList();
    }

}
