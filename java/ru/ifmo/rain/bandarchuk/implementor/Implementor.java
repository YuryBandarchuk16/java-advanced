package ru.ifmo.rain.bandarchuk.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements Impler, JarImpler {

    /* Region: PARSE CLASS INFO */

    private static final String GENERATED_CLASS_SUFFIX = "Impl";

    private static final String DEFAULT_PRIMITIVE_VALUE = "0";
    private static final String DEFAULT_BOOLEAN_VALUE = "true";
    private static final String DEFAULT_VOID_VALUE = "";
    private static final String DEFAULT_OBJECT_VALUE = "null";

    private static String getClassName(Class<?> classDefinition) {
        return classDefinition.getSimpleName() + GENERATED_CLASS_SUFFIX;
    }

    private static String getPackage(Class<?> classDefinition) {
        return "package " + classDefinition.getPackage().getName();
    }

    private static String getDefaultValue(Class<?> classDefinition) {
        if (classDefinition.equals(boolean.class)) {
            return DEFAULT_BOOLEAN_VALUE;
        } else if (classDefinition.isPrimitive()) {
            return DEFAULT_PRIMITIVE_VALUE;
        } else if (classDefinition.equals(void.class)) {
            return DEFAULT_VOID_VALUE;
        }

        return DEFAULT_OBJECT_VALUE;
    }

    private static String getParameters(Executable executable, boolean needType) {
        StringBuilder builder = new StringBuilder();

        Parameter[] parameters = executable.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(COMMA)
                    .append(SPACE);
            }

            if (needType) {
                builder.append(parameters[i].getType().getCanonicalName())
                    .append(SPACE);
            }

            builder.append(parameters[i].getName());
        }

        return builder.toString();
    }

    private static String getMethodExceptions(Executable executable) {
        StringBuilder builder = new StringBuilder();

        Class<?> exceptions[] = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            builder.append(SPACE)
                .append("throws")
                .append(SPACE);
        }

        for (int i = 0; i < exceptions.length; i++) {
            if (i > 0) {
                builder.append(COMMA)
                    .append(SPACE);
            }
            builder.append(exceptions[i].getCanonicalName());
        }

        return builder.toString();
    }

    private static String getMethodDeclaration(Class<?> classDefinition, Executable executable) {
        StringBuilder builder = new StringBuilder();

        String methodModifiers = Modifier.toString(executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.INTERFACE & ~Modifier.TRANSIENT & ~Modifier.NATIVE);

        builder.append(getTabulation(1))
            .append(methodModifiers)
            .append(SPACE);

        if (executable instanceof Method) {
            Method method = (Method) executable;
            builder.append(method.getReturnType().getCanonicalName())
                .append(SPACE)
                .append(method.getName());
        } else {
            builder.append(getClassName(classDefinition));
        }

        builder.append(LEFT_BRACKET)
            .append(getParameters(executable, true))
            .append(RIGHT_BRACKET);

        builder.append(getMethodExceptions(executable));

        return builder.toString();
    }

    private static String getMethodAnnotations(Annotation[] annotations) {
        StringBuilder builder = new StringBuilder();

        for (Annotation annotation : annotations) {
            builder.append(annotation)
                .append(NEW_LINE);
        }

        return builder.toString();
    }

    private static String getMethodFull(Class<?> classDefinition, Method method) {
        StringBuilder builder = new StringBuilder();

        builder.append(getMethodAnnotations(method.getAnnotations()));
        builder.append(getMethodDeclaration(classDefinition, method))
            .append(SPACE)
            .append(LEFT_CURVE_BRACKET)
            .append(NEW_LINE)
            .append(getTabulation(2))
            .append("return");

        if (!method.getReturnType().equals(void.class)) {
            builder.append(SPACE)
                .append(getDefaultValue(method.getReturnType()));
        }

        builder.append(";");

        builder.append(NEW_LINE)
            .append(getTabulation(1))
            .append(RIGHT_CURVE_BRACKET)
            .append(NEW_LINE)
            .append(NEW_LINE);

        return builder.toString();
    }

    private static String getClassDeclaration(Class<?> classDefinition) {
        StringBuilder builder = new StringBuilder();

        int modifiers = classDefinition.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.INTERFACE;
        builder.append(Modifier.toString(modifiers))
            .append(SPACE)
            .append("class")
            .append(SPACE)
            .append(getClassName(classDefinition))
            .append(SPACE);

        if (classDefinition.isInterface()) {
            builder.append("implements");
        } else {
            builder.append("extends");
        }

        builder.append(SPACE);

        builder.append(classDefinition.getSimpleName())
            .append(SPACE)
            .append(LEFT_CURVE_BRACKET)
            .append(NEW_LINE)
            .append(NEW_LINE);

        return builder.toString();
    }

    private static String getConstructors(Class<?> classDefinition) throws ImplerException {
        StringBuilder builder = new StringBuilder();

        boolean allPrivate = true;
        for (Constructor<?> constructor : classDefinition.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            allPrivate = false;
            builder.append(getMethodAnnotations(classDefinition.getAnnotations()));
            builder.append(getMethodDeclaration(classDefinition, constructor));
            builder.append(SPACE)
                .append(LEFT_CURVE_BRACKET)
                .append(NEW_LINE)
                .append(getTabulation(2))
                .append("super")
                .append(LEFT_BRACKET)
                .append(getParameters(constructor, false))
                .append(RIGHT_BRACKET)
                .append(";")
                .append(NEW_LINE)
                .append(getTabulation(1))
                .append(RIGHT_CURVE_BRACKET)
                .append(NEW_LINE)
                .append(NEW_LINE);

        }

        if (!classDefinition.isInterface() && allPrivate) {
            throw new ImplerException("No constructor at all");
        }

        return builder.toString();
    }

    /* End Region: PARSE CLASS INFO */


    /* Region: CREATE CLASS */

    private static void printMethod(Class<?> classDefinition, Method method, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(getMethodFull(classDefinition, method));
    }

    private static void printClassDeclaration(Class<?> classDefinition, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(getClassDeclaration(classDefinition));
    }

    private static void printMethods(Class<?> classDefinitions, BufferedWriter bufferedWriter) throws IOException {
        Set<MethodWrapper> set = new HashSet<>();

        for (Method method : classDefinitions.getMethods()) {
            set.add(new MethodWrapper(method));
        }

        Class<?> ancestor = classDefinitions;
        while (ancestor != null && !ancestor.equals(Object.class)) {
            for (Method method : ancestor.getDeclaredMethods()) {
                set.add(new MethodWrapper(method));
            }
            ancestor = ancestor.getSuperclass();
        }

        for (MethodWrapper wrapper : set) {
            if (!Modifier.isAbstract(wrapper.getMethod().getModifiers())) {
                continue;
            }
            printMethod(classDefinitions, wrapper.getMethod(), bufferedWriter);
        }

        bufferedWriter.write(RIGHT_CURVE_BRACKET + NEW_LINE);
    }

    private static void printConstructors(Class<?> classDefinition, BufferedWriter bufferedWriter) throws ImplerException, IOException {
        bufferedWriter.write(getConstructors(classDefinition));
    }

    private static void printPackage(Class<?> classDefinition, BufferedWriter BufferedWriter) throws IOException {
        BufferedWriter.write(getPackage(classDefinition) + ";" + NEW_LINE);
    }

    private static void printFile(Class<?> classDefinition, BufferedWriter bufferedWriter) throws ImplerException, IOException {
        printPackage(classDefinition, bufferedWriter);
        printClassDeclaration(classDefinition, bufferedWriter);
        printConstructors(classDefinition, bufferedWriter);
        printMethods(classDefinition, bufferedWriter);
    }

    /* End Region: CREATE CLASS */


    /* Region: HELPER */

    private static final String JAR_OPTION = "-jar";
    private static final String SPACE = " ";
    private static final String NEW_LINE = "\n";
    private static final String LEFT_BRACKET = "(";
    private static final String RIGHT_BRACKET = ")";
    private static final String COMMA = ",";
    private static final String DEFAULT_TAB = "    ";
    private static final String LEFT_CURVE_BRACKET = "{";
    private static final String RIGHT_CURVE_BRACKET = "}";

    private enum FileExtension {
        JAVA(".java"),
        CLASS(".class");

        private String value;

        FileExtension(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static String getTabulation(int tabs) {
        return repeat(DEFAULT_TAB, tabs);
    }

    private static String repeat(String target, int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- > 0) {
            builder.append(target);
        }

        return builder.toString();
    }

    private static Path addPackageToPath(Class<?> classDefinition, Path root) {
        if (classDefinition.getPackage() != null) {
            root = root.resolve(classDefinition.getPackage()
                .getName().replace('.', '/') + "/");
        }

        return root;
    }

    private static Path getFilePath(Class<?> classDefinition, Path root, FileExtension extension) throws IOException {
        root = addPackageToPath(classDefinition, root);
        Files.createDirectories(root);

        return root.resolve(getClassName(classDefinition) + extension.getValue());
    }

    @Override
    public void implement(Class<?> classDefinition, Path root) throws ImplerException {
        if (classDefinition == null || root == null) {
            throw new ImplerException("Invalid arguments: " + ((classDefinition == null) ? "class definition " : "root path ") + "is null");
        }

        if (classDefinition.isPrimitive() || classDefinition == Enum.class || classDefinition.isArray()
            || Modifier.isFinal(classDefinition.getModifiers())) {
            throw new ImplerException("Invalid class definition was passed");
        }

        try (BufferedWriter BufferedWriter =
                 new BufferedWriter(
                     new OutputStreamWriter(
                         new FileOutputStream(getFilePath(classDefinition, root, FileExtension.JAVA).toString()),
                         StandardCharsets.UTF_8))) {
            printFile(classDefinition, BufferedWriter);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    private void compileClass(Path root, Path javaFilePath) throws ImplerException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new ImplerException("Java compiler was not found");
        }
        int returnCode = javaCompiler.run(null, null, null,
            javaFilePath.toString(),
            "-cp", root + File.pathSeparator + System.getProperty("java.class.path"),
            "-encoding", "UTF-8"
        );
        if (returnCode != 0) {
            throw new ImplerException("Error with code: " + returnCode + " occurred during compilation");
        }
    }

    private void writeJarFile(Path className, Path root, Path jarPath) throws ImplerException {
        className = className.normalize();
        root = root.normalize();
        jarPath = jarPath.normalize();
        Path classFile = root.resolve(className);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            out.putNextEntry(new ZipEntry(className.toString()));
            Files.copy(classFile, out);
        } catch (IOException e) {
            throw new ImplerException("Error occurred when creating jar file", e);
        }
    }

    @Override
    public void implementJar(Class<?> classDefinition, Path jarFile) throws ImplerException {
        try {
            Path temporaryDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
            temporaryDirectory = temporaryDirectory.normalize();
            jarFile = jarFile.normalize();

            //generate .java file for needed class
            Implementor implementor = new Implementor();
            implementor.implement(classDefinition, temporaryDirectory);

            compileClass(temporaryDirectory, Implementor.getFilePath((classDefinition), temporaryDirectory, FileExtension.JAVA));
            writeJarFile(Implementor.getFilePath(classDefinition, Paths.get("."), FileExtension.CLASS), temporaryDirectory, jarFile);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    private static class MethodWrapper {

        private static final int PRIME = 1031;

        private final Method method;

        Method getMethod() {
            return method;
        }

        MethodWrapper(Method method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            Parameter[] parameters = method.getParameters();

            int hashCode = Integer.hashCode(parameters.length) * PRIME + method.getName().hashCode();
            for (Parameter parameter : parameters) {
                hashCode = hashCode * PRIME + parameter.toString().hashCode();
            }

            return hashCode;
        }

        private boolean parametersEquals(Parameter[] a, Parameter[] b) {
            if (a.length != b.length) {
                return false;
            }

            for (int i = 0; i < a.length; i++) {
                if (!a[i].toString().equals(b[i].toString())) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodWrapper)) {
                return false;
            }
            MethodWrapper that = (MethodWrapper) obj;
            return this.method.getName().equals(that.method.getName()) &&
                parametersEquals(method.getParameters(), that.method.getParameters());
        }
    }

    /* End Region: HELPER */

    /* Region: MAIN */

    public static void main(String[] args) {
        Implementor implementor = new Implementor();

        try {
            if (args[0].equals(JAR_OPTION)) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Not enough arguments");
        } catch (ClassNotFoundException e) {
            System.err.println("Incorrect class name");
        } catch (InvalidPathException e) {
            System.err.println("Incorrect root path");
        } catch (ImplerException e) {
            System.err.println("Error occurred during the implementation " + e.getMessage());
        }
    }

    /* End Region: MAIN */
}
