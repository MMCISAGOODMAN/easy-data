package com.easydata.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

class EasyDataProcessorTest {

    private static final JavaFileObject VALID_MODEL = JavaFileObjects.forSourceString(
            "com.example.ValidUser",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public class ValidUser {\n"
                    + "    private String name;\n"
                    + "    @EasyData.Ignore private String password;\n"
                    + "    @EasyData.Field(\"demo@example.com\") private String email;\n"
                    + "}\n");

    private static final JavaFileObject ABSTRACT_MODEL = JavaFileObjects.forSourceString(
            "com.example.AbstractUser",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public abstract class AbstractUser {\n"
                    + "    private String name;\n"
                    + "}\n");

    private static final JavaFileObject NO_DEFAULT_CTOR = JavaFileObjects.forSourceString(
            "com.example.NoDefaultCtor",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public class NoDefaultCtor {\n"
                    + "    private String name;\n"
                    + "    public NoDefaultCtor(String name) { this.name = name; }\n"
                    + "}\n");

    private static final JavaFileObject INTERFACE_FIELD = JavaFileObjects.forSourceString(
            "com.example.BadNested",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "interface Repository {}\n"
                    + "@EasyData\n"
                    + "public class BadNested {\n"
                    + "    private Repository repository;\n"
                    + "}\n");

    private static final JavaFileObject STATIC_IGNORE = JavaFileObjects.forSourceString(
            "com.example.StaticIgnore",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "public class StaticIgnore {\n"
                    + "    @EasyData.Ignore private static String token;\n"
                    + "}\n");

    private static final JavaFileObject INVALID_DEFAULT_SIZE = JavaFileObjects.forSourceString(
            "com.example.InvalidSize",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData(defaultSize = 0)\n"
                    + "public class InvalidSize {\n"
                    + "    private String name;\n"
                    + "}\n");

    private static final JavaFileObject INVALID_FIELD_VALUE = JavaFileObjects.forSourceString(
            "com.example.InvalidFieldValue",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public class InvalidFieldValue {\n"
                    + "    @EasyData.Field(\"abc\") private int age;\n"
                    + "}\n");

    private static final JavaFileObject FIELD_ON_POJO = JavaFileObjects.forSourceString(
            "com.example.FieldOnPojo",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public class FieldOnPojo {\n"
                    + "    @EasyData.Field(\"x\") private Address address;\n"
                    + "    static class Address {}\n"
                    + "}\n");

    private static final JavaFileObject FIELD_AND_IGNORE = JavaFileObjects.forSourceString(
            "com.example.FieldAndIgnore",
            ""
                    + "package com.example;\n"
                    + "import com.easydata.annotation.EasyData;\n"
                    + "@EasyData\n"
                    + "public class FieldAndIgnore {\n"
                    + "    @EasyData.Field(\"x\") @EasyData.Ignore private String token;\n"
                    + "}\n");

    @Test
    void acceptsValidModel() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(VALID_MODEL);

        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void rejectsAbstractClass() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(ABSTRACT_MODEL);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must not be abstract");
    }

    @Test
    void rejectsMissingNoArgConstructor() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(NO_DEFAULT_CTOR);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("no-arg constructor");
    }

    @Test
    void rejectsInterfaceFieldType() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(INTERFACE_FIELD);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("cannot populate interface field");
    }

    @Test
    void rejectsIgnoreOnStaticField() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(STATIC_IGNORE);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must not be applied to static field");
    }

    @Test
    void warnsOnInvalidDefaultSize() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(INVALID_DEFAULT_SIZE);

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("defaultSize() must be positive");
    }

    @Test
    void rejectsIncompatibleFieldValue() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(INVALID_FIELD_VALUE);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("value incompatible");
    }

    @Test
    void rejectsFieldOnNonScalarType() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(FIELD_ON_POJO);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("only supports scalar types");
    }

    @Test
    void rejectsFieldCombinedWithIgnore() {
        Compilation compilation = javac()
                .withProcessors(new EasyDataProcessor())
                .compile(FIELD_AND_IGNORE);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("mutually exclusive");
    }
}
