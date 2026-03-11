package com.simpletry.processor;

import com.google.auto.service.AutoService;
import com.simpletry.core.annotation.SimpleTry;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.simpletry.core.annotation.SimpleTry")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SimpleTryProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        Map<TypeElement, List<ExecutableElement>> classMethodMap = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(SimpleTry.class)) {

            if (element.getKind() != ElementKind.METHOD) continue;

            ExecutableElement method = (ExecutableElement) element;
            TypeElement clazz = (TypeElement) method.getEnclosingElement();

            classMethodMap
                    .computeIfAbsent(clazz, k -> new ArrayList<>())
                    .add(method);
        }

        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : classMethodMap.entrySet()) {
            generateWrapper(entry.getKey(), entry.getValue());
        }

        return true;
    }

    private void generateWrapper(TypeElement clazz,
                                 List<ExecutableElement> methods) {

        String className = clazz.getSimpleName().toString();

        String packageName =
                processingEnv.getElementUtils()
                        .getPackageOf(clazz)
                        .getQualifiedName()
                        .toString();

        String wrapperName = className + "SimpleTryWrapper";

        try {

            JavaFileObject file =
                    processingEnv.getFiler()
                            .createSourceFile(packageName + "." + wrapperName);

            Writer writer = file.openWriter();

            writer.write("package " + packageName + ";\n\n");

            writer.write("public class " + wrapperName +
                    " extends " + className + " {\n\n");

            writer.write("    private final " + className + " target;\n\n");

            writer.write("    public " + wrapperName + "(" + className + " target) {\n");
            writer.write("        this.target = target;\n");
            writer.write("    }\n\n");

            for (ExecutableElement method : methods) {
                generateMethod(writer, className, method);
            }

            writer.write("}\n");
            writer.close();

        } catch (IOException e) {
            error(null, e.getMessage());
        }
    }

    private void generateMethod(Writer writer,
                                String className,
                                ExecutableElement method) throws IOException {

        SimpleTry ann = method.getAnnotation(SimpleTry.class);

        String methodName = method.getSimpleName().toString();
        String returnType = method.getReturnType().toString();

        boolean log = ann.log();
        boolean debug = ann.debugTrace();
        int retry = ann.retry();

        String fallbackMethod = ann.fallbackMethod();
        String[] fallbackValues = ann.fallbackValue();
        String[] tags = ann.tag();

        List<? extends VariableElement> params = method.getParameters();
        List<? extends TypeMirror> thrownTypes = method.getThrownTypes();

        List<? extends TypeMirror> exceptionTypes;
        List<? extends TypeMirror> ignoreTypes;

        TypeMirror transformMirror = null;

        try {
            ann.exceptions();
            exceptionTypes = List.of();
        } catch (MirroredTypesException e) {
            exceptionTypes = e.getTypeMirrors();
        }

        try {
            ann.ignore();
            ignoreTypes = List.of();
        } catch (MirroredTypesException e) {
            ignoreTypes = e.getTypeMirrors();
        }

        try {
            ann.transformTo();
        } catch (MirroredTypeException e) {
            transformMirror = e.getTypeMirror();
        }

        StringBuilder paramDecl = new StringBuilder();
        StringBuilder paramCall = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {

            VariableElement p = params.get(i);

            paramDecl.append(p.asType())
                    .append(" ")
                    .append(p.getSimpleName());

            paramCall.append(p.getSimpleName());

            if (i < params.size() - 1) {
                paramDecl.append(", ");
                paramCall.append(", ");
            }
        }

        StringBuilder throwsClause = new StringBuilder();

        if (!thrownTypes.isEmpty()) {

            throwsClause.append(" throws ");

            for (int i = 0; i < thrownTypes.size(); i++) {

                throwsClause.append(thrownTypes.get(i).toString());

                if (i < thrownTypes.size() - 1) {
                    throwsClause.append(", ");
                }
            }
        }

        writer.write("    public " + returnType + " " + methodName +
                "(" + paramDecl + ")" + throwsClause + " {\n");

        if (debug) {
            writer.write("        com.simpletry.core.debug.DebugContext.push(\""
                    + className + "." + methodName
                    + "\", new Object[]{" + paramCall + "});\n");
        }

        if (retry > 0) {
            writer.write("        int attempts = 0;\n");
            writer.write("        while(true) {\n");
        }

        writer.write("        try {\n");

        if (!returnType.equals("void")) {
            writer.write("            return target." + methodName +
                    "(" + paramCall + ");\n");
        } else {
            writer.write("            target." + methodName +
                    "(" + paramCall + ");\n");
        }

        writer.write("        }\n");

        for (TypeMirror ignored : ignoreTypes) {

            writer.write("        catch (" + ignored + " e) {\n");
            writer.write("            throw e;\n");
            writer.write("        }\n");
        }

        for (int i = 0; i < exceptionTypes.size(); i++) {

            String exception = exceptionTypes.get(i).toString();

            writer.write("        catch (" + exception + " e) {\n");

            if (retry > 0) {

                writer.write("            attempts++;\n");
                writer.write("            if(attempts < " + retry + ") {\n");
                writer.write("                continue;\n");
                writer.write("            }\n");
            }

            if (debug) {
                writer.write(
                        "            com.simpletry.core.debug.DebugContext.dumpTrace(e);\n");
            }

            if (log) {

                String tagValue = "";

                if (tags.length == 1) tagValue = tags[0];
                else if (i < tags.length) tagValue = tags[i];

                writer.write(
                        "            com.simpletry.core.logging.SimpleTryLogger.log("
                                + "\"" + methodName + "\","
                                + "\"" + className + "\","
                                + "e,"
                                + "\"" + tagValue + "\");\n");
            }

            if (transformMirror != null &&
                    !transformMirror.toString().equals("java.lang.Throwable")) {

                writer.write("            throw new "
                        + transformMirror
                        + "(e);\n");

                writer.write("        }\n");
                continue;
            }

            if (!fallbackMethod.isEmpty()) {

                writer.write("            return target." + fallbackMethod + "(e");

                if (paramCall.length() > 0)
                    writer.write("," + paramCall);

                writer.write(");\n");
            }

            else if (fallbackValues.length > 0 && !returnType.equals("void")) {

                String value = null;

                if (fallbackValues.length == 1)
                    value = fallbackValues[0];
                else if (i < fallbackValues.length)
                    value = fallbackValues[i];

                writer.write("            return " + value + ";\n");
            }

            writer.write("        }\n");
        }

        if (retry > 0) {
            writer.write("        }\n");
        }

        if (debug) {
            writer.write(
                    "        finally { com.simpletry.core.debug.DebugContext.pop(); }\n");
        }

        writer.write("    }\n\n");
    }

    private void error(Element e, String msg) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}