package com.easydata.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Validates that {@link com.easydata.annotation.EasyData.Field} literal values match field types.
 */
final class FieldValueValidator {

    private FieldValueValidator() {
    }

    static boolean isFixedValueType(TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        TypeElement typeElement = (TypeElement) ((javax.lang.model.type.DeclaredType) typeMirror).asElement();
        if (typeElement.getKind() == javax.lang.model.element.ElementKind.ENUM) {
            return true;
        }
        String name = typeElement.getQualifiedName().toString();
        return "java.lang.String".equals(name)
                || "java.lang.Character".equals(name)
                || "java.lang.Boolean".equals(name)
                || "java.lang.Byte".equals(name)
                || "java.lang.Short".equals(name)
                || "java.lang.Integer".equals(name)
                || "java.lang.Long".equals(name)
                || "java.lang.Float".equals(name)
                || "java.lang.Double".equals(name)
                || "java.math.BigDecimal".equals(name)
                || "java.math.BigInteger".equals(name);
    }

    static String validateAndNormalize(TypeMirror typeMirror, String rawValue) {
        if (rawValue == null) {
            return "value must not be null";
        }

        try {
            switch (typeMirror.getKind()) {
                case BOOLEAN:
                    parseBoolean(rawValue);
                    return null;
                case BYTE:
                    parseByte(rawValue);
                    return null;
                case SHORT:
                    parseShort(rawValue);
                    return null;
                case INT:
                    parseInt(rawValue);
                    return null;
                case LONG:
                    parseLong(rawValue);
                    return null;
                case FLOAT:
                    parseFloat(rawValue);
                    return null;
                case DOUBLE:
                    parseDouble(rawValue);
                    return null;
                case CHAR:
                    if (rawValue.length() != 1) {
                        return "char value must be a single character";
                    }
                    return null;
                case DECLARED:
                    return validateDeclared(
                            (TypeElement) ((javax.lang.model.type.DeclaredType) typeMirror).asElement(),
                            rawValue);
                default:
                    return "unsupported @EasyData.Field type: " + typeMirror;
            }
        } catch (IllegalArgumentException ex) {
            return "invalid value '" + rawValue + "' for type " + typeMirror;
        }
    }

    private static String validateDeclared(TypeElement typeElement, String rawValue) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        if ("java.lang.String".equals(qualifiedName)) {
            return null;
        }
        if ("java.lang.Character".equals(qualifiedName)) {
            return rawValue.length() == 1 ? null : "Character value must be a single character";
        }
        if ("java.lang.Boolean".equals(qualifiedName)) {
            parseBoolean(rawValue);
            return null;
        }
        if ("java.lang.Byte".equals(qualifiedName)) {
            parseByte(rawValue);
            return null;
        }
        if ("java.lang.Short".equals(qualifiedName)) {
            parseShort(rawValue);
            return null;
        }
        if ("java.lang.Integer".equals(qualifiedName)) {
            parseInt(rawValue);
            return null;
        }
        if ("java.lang.Long".equals(qualifiedName)) {
            parseLong(rawValue);
            return null;
        }
        if ("java.lang.Float".equals(qualifiedName)) {
            parseFloat(rawValue);
            return null;
        }
        if ("java.lang.Double".equals(qualifiedName)) {
            parseDouble(rawValue);
            return null;
        }
        if ("java.math.BigDecimal".equals(qualifiedName)) {
            try {
                new java.math.BigDecimal(rawValue);
                return null;
            } catch (NumberFormatException e) {
                return "invalid BigDecimal value: " + rawValue;
            }
        }
        if ("java.math.BigInteger".equals(qualifiedName)) {
            try {
                new java.math.BigInteger(rawValue);
                return null;
            } catch (NumberFormatException e) {
                return "invalid BigInteger value: " + rawValue;
            }
        }
        if (typeElement.getKind() == javax.lang.model.element.ElementKind.ENUM) {
            for (javax.lang.model.element.Element constant : typeElement.getEnclosedElements()) {
                if (constant.getSimpleName().toString().equals(rawValue)) {
                    return null;
                }
            }
            return "unknown enum constant '" + rawValue + "' for " + qualifiedName;
        }
        return "unsupported @EasyData.Field type: " + qualifiedName;
    }

    private static void parseBoolean(String rawValue) {
        if (!"true".equals(rawValue) && !"false".equals(rawValue)) {
            throw new IllegalArgumentException("invalid boolean");
        }
    }

    private static void parseByte(String rawValue) {
        Byte.parseByte(rawValue);
    }

    private static void parseShort(String rawValue) {
        Short.parseShort(rawValue);
    }

    private static void parseInt(String rawValue) {
        Integer.parseInt(rawValue);
    }

    private static void parseLong(String rawValue) {
        Long.parseLong(rawValue);
    }

    private static void parseFloat(String rawValue) {
        Float.parseFloat(rawValue);
    }

    private static void parseDouble(String rawValue) {
        Double.parseDouble(rawValue);
    }
}
