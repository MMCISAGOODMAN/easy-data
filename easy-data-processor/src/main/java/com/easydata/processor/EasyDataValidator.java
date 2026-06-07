package com.easydata.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.easydata.annotation.EasyData;

/**
 * Validates {@link EasyData} model classes and field annotations at compile time.
 */
final class EasyDataValidator {

    private static final Set<String> BUILTIN_SUPPORTED_TYPES = new HashSet<String>(Arrays.asList(
            "java.lang.String",
            "java.lang.Character",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.util.Date",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.Instant"
    ));

    private static final Set<String> COLLECTION_TYPES = new HashSet<String>(Arrays.asList(
            "java.util.Collection",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
            "java.util.SortedSet",
            "java.util.NavigableSet",
            "java.util.SortedMap",
            "java.util.NavigableMap"
    ));

    private final Messager messager;
    private final Types types;

    EasyDataValidator(ProcessingEnvironment env) {
        this.messager = env.getMessager();
        this.types = env.getTypeUtils();
    }

    void validateEasyDataType(TypeElement typeElement) {
        if (typeElement.getKind() != ElementKind.CLASS) {
            error(typeElement, "@EasyData can only be applied to concrete classes, found: "
                    + typeElement.getKind().name().toLowerCase());
            return;
        }

        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(typeElement, "@EasyData target must not be abstract: " + qualifiedName(typeElement));
            return;
        }

        if (!hasNoArgConstructor(typeElement)) {
            error(typeElement, "@EasyData target must declare a no-arg constructor: "
                    + qualifiedName(typeElement));
        }

        EasyData annotation = typeElement.getAnnotation(EasyData.class);
        if (annotation != null && annotation.defaultSize() <= 0) {
            warning(typeElement, "@EasyData.defaultSize() must be positive, got: "
                    + annotation.defaultSize());
        }

        for (VariableElement field : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            validateModelField(typeElement, field);
        }

        TypeElement superClass = superClass(typeElement);
        while (superClass != null && !"java.lang.Object".equals(qualifiedName(superClass))) {
            for (VariableElement field : ElementFilter.fieldsIn(superClass.getEnclosedElements())) {
                validateModelField(typeElement, field);
            }
            superClass = superClass(superClass);
        }
    }

    void validateIgnoreField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "@EasyData.Ignore can only be applied to fields");
            return;
        }

        VariableElement field = (VariableElement) element;
        if (field.getModifiers().contains(Modifier.STATIC)) {
            error(field, "@EasyData.Ignore must not be applied to static field: " + field.getSimpleName());
        }

        if (field.getModifiers().contains(Modifier.FINAL)) {
            error(field, "@EasyData.Ignore must not be applied to final field: " + field.getSimpleName());
        }

        if (field.getAnnotation(EasyData.Field.class) != null) {
            error(field, "@EasyData.Field and @EasyData.Ignore are mutually exclusive: "
                    + field.getSimpleName());
        }

        warnIfMissingClassAnnotation(field);
    }

    void validateFixedField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "@EasyData.Field can only be applied to fields");
            return;
        }

        VariableElement field = (VariableElement) element;
        validateFixedFieldValue(enclosingType(field), field);
        warnIfMissingClassAnnotation(field);
    }

    private void validateModelField(TypeElement owner, VariableElement field) {
        if (field.getModifiers().contains(Modifier.STATIC)) {
            return;
        }
        if (field.getModifiers().contains(Modifier.FINAL)) {
            warning(field, "Final field cannot be populated by EasyData: "
                    + qualifiedName(owner) + "." + field.getSimpleName());
            return;
        }

        EasyData.Field fixed = field.getAnnotation(EasyData.Field.class);
        if (fixed != null) {
            if (field.getAnnotation(EasyData.Ignore.class) != null) {
                error(field, "@EasyData.Field and @EasyData.Ignore are mutually exclusive: "
                        + field.getSimpleName());
            }
            validateFixedFieldValue(owner, field);
            return;
        }

        if (field.getAnnotation(EasyData.Ignore.class) != null) {
            return;
        }

        validateType(owner, field, field.asType(), field.getSimpleName().toString());
    }

    private void validateFixedFieldValue(TypeElement owner, VariableElement field) {
        if (field.getModifiers().contains(Modifier.STATIC)) {
            error(field, "@EasyData.Field must not be applied to static field: " + field.getSimpleName());
            return;
        }
        if (field.getModifiers().contains(Modifier.FINAL)) {
            error(field, "@EasyData.Field must not be applied to final field: " + field.getSimpleName());
            return;
        }

        TypeMirror fieldType = field.asType();
        if (!FieldValueValidator.isFixedValueType(fieldType)) {
            error(field, "@EasyData.Field only supports scalar types at "
                    + qualifiedName(owner) + "." + field.getSimpleName() + ": " + fieldType);
            return;
        }

        EasyData.Field fixed = field.getAnnotation(EasyData.Field.class);
        String issue = FieldValueValidator.validateAndNormalize(fieldType, fixed.value());
        if (issue != null) {
            error(field, "@EasyData.Field value incompatible at "
                    + qualifiedName(owner) + "." + field.getSimpleName() + ": " + issue);
        }
    }

    private void warnIfMissingClassAnnotation(VariableElement field) {
        Element enclosing = field.getEnclosingElement();
        if (enclosing instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) enclosing;
            if (typeElement.getAnnotation(EasyData.class) == null) {
                warning(field, "Field annotation without @EasyData on enclosing class: "
                        + qualifiedName(typeElement) + "." + field.getSimpleName()
                        + " (runtime still works)");
            }
        }
    }

    private TypeElement enclosingType(VariableElement field) {
        return (TypeElement) field.getEnclosingElement();
    }

    private void validateType(TypeElement owner, Element element, TypeMirror typeMirror, String path) {
        if (typeMirror.getKind().isPrimitive()) {
            return;
        }

        if (typeMirror.getKind() == TypeKind.ARRAY) {
            validateType(owner, element,
                    ((ArrayType) typeMirror).getComponentType(),
                    path + "[]");
            return;
        }

        if (typeMirror.getKind() != TypeKind.DECLARED) {
            warning(element, "Unsupported field type for EasyData at " + qualifiedName(owner) + "." + path
                    + ": " + typeMirror);
            return;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        String qualifiedName = qualifiedName(typeElement);

        if (typeElement.getKind() == ElementKind.ENUM) {
            return;
        }

        if (BUILTIN_SUPPORTED_TYPES.contains(qualifiedName)) {
            return;
        }

        if (isCollectionType(typeElement)) {
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if (typeArgs.isEmpty()) {
                warning(element, "Raw collection type at " + qualifiedName(owner) + "." + path
                        + "; prefer parameterized types like List<User>");
                return;
            }
            for (int i = 0; i < typeArgs.size(); i++) {
                validateType(owner, element, typeArgs.get(i), path + "<" + i + ">");
            }
            return;
        }

        if (typeElement.getKind() == ElementKind.INTERFACE) {
            error(element, "EasyData cannot populate interface field " + qualifiedName(owner) + "." + path
                    + ": " + qualifiedName);
            return;
        }

        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(element, "EasyData cannot populate abstract type at " + qualifiedName(owner) + "." + path
                    + ": " + qualifiedName);
            return;
        }

        if (typeElement.getNestingKind().isNested()
                && !typeElement.getModifiers().contains(Modifier.STATIC)) {
            error(element, "EasyData nested field must be static inner class at "
                    + qualifiedName(owner) + "." + path + ": " + qualifiedName);
            return;
        }

        if (!hasNoArgConstructor(typeElement)) {
            error(element, "EasyData nested type requires no-arg constructor at "
                    + qualifiedName(owner) + "." + path + ": " + qualifiedName);
        }
    }

    private boolean isCollectionType(TypeElement typeElement) {
        for (TypeMirror mirror : types.directSupertypes(typeElement.asType())) {
            if (mirror.getKind() != TypeKind.DECLARED) {
                continue;
            }
            TypeElement superType = (TypeElement) ((DeclaredType) mirror).asElement();
            if (COLLECTION_TYPES.contains(qualifiedName(superType))) {
                return true;
            }
            if (isCollectionType(superType)) {
                return true;
            }
        }
        return COLLECTION_TYPES.contains(qualifiedName(typeElement));
    }

    private boolean hasNoArgConstructor(TypeElement typeElement) {
        for (ExecutableElement constructor : ElementFilter.constructorsIn(typeElement.getEnclosedElements())) {
            if (constructor.getParameters().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private TypeElement superClass(TypeElement typeElement) {
        TypeMirror superClass = typeElement.getSuperclass();
        if (superClass.getKind() == TypeKind.DECLARED) {
            return (TypeElement) ((DeclaredType) superClass).asElement();
        }
        return null;
    }

    private String qualifiedName(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString();
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void warning(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }
}
