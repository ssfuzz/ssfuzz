package org.example.generator;

import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;

public class PrimitiveGenerator {
    Random random = new Random();
    String[] basicType = new String[]{"int", "double", "float", "long", "boolean", "byte", "char", "short"};

    public PrimitiveGenerator() {
    }

    public String generateInt() {
        int intNumber = this.random.nextInt();
        return String.valueOf(intNumber);
    }

    public String generateInt(int max) {
        int intNumber = this.random.nextInt(max);
        return String.valueOf(intNumber);
    }

    public String generateShort() {
        int shortNumber = this.random.nextInt(Byte.MAX_VALUE);
        return String.valueOf(shortNumber);
    }

    public String generateFloat() {
        float floatNumber = this.random.nextFloat();
        return floatNumber + "f";
    }

    public String generateLong() {
        long longNumber = this.random.nextLong();
        return longNumber + "L";
    }

    public String generateDouble() {
        double doubleNumber = this.random.nextDouble();
        return String.valueOf(doubleNumber);
    }

    public String generateBoolean() {
        boolean booleanNumber = this.random.nextBoolean();
        return String.valueOf(booleanNumber);
    }

    public String generateByte() {
        byte[] bytes = new byte[]{1};
        this.random.nextBytes(bytes);
        return String.valueOf(bytes[0]);
    }

    public String generateChar() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-={}|:\"<>?[]\\;',./";
        int intNumber = this.random.nextInt(92);
        String charStr = str.substring(intNumber, intNumber + 1);
        if (charStr.equals("\\")) {
            return "'\\\\'";
        } else {
            return charStr.equals("'") ? "'\\''" : "'" + charStr + "'";
        }
    }

    public String generateBasic(String type) {
        String value = "null";
        if (this.isIntegral(type)) {
            value = this.generateIntegral(type);
        } else if (this.isFloatingPoint(type)) {
            value = this.generateFloatingPoint(type);
        } else if (this.isBoolean(type)) {
            value = this.generateBoolean();
        } else if (this.isString(type)) {
            value = this.generateString();
        }

        return value;
    }

    public int generateLength() {
        int len = this.random.nextInt();
        if (len > 0 && len < 100) {
            return len;
        } else {
            return len < 0 && len > -100 ? -len : 10;
        }
    }

    public String generateBytes() {
        byte[] nbyte = new byte[this.generateLength()];
        this.random.nextBytes(nbyte);
        String value = "{";

        for(int i = 0; i < nbyte.length; ++i) {
            value = value + String.valueOf(nbyte[i]);
            if (i < nbyte.length - 1) {
                value = value + ",";
            }
        }

        return value + "}";
    }

    public String linearArray(int len, String type) {
        if (type.equals("byte")) {
            return this.generateBytes();
        } else {
            String arrStr = "{";

            for(int i = 0; i < len; ++i) {
                arrStr = arrStr + this.generateBasic(type);
                if (i < len - 1) {
                    arrStr = arrStr + ",";
                }
            }

            arrStr = arrStr + "}";
            return arrStr;
        }
    }

    public String generateArray(int count, String type) {
        int len = this.generateLength();
        int i;
        if (type.equals("T")) {
            i = this.random.nextInt(7);
            type = this.basicType[i];
        }

        String rsArray;
        if (count == 1) {
            rsArray = this.linearArray(len, type);
        } else {
            rsArray = "{";

            for(i = 0; i < count; ++i) {
                rsArray = rsArray + this.linearArray(len, type);
                if (i < count - 1) {
                    rsArray = rsArray + ",";
                }
            }

            rsArray = rsArray + "}";
        }

        return rsArray;
    }

    public String generateString() {
        int len = this.generateLength();
        String randomStr = RandomStringUtils.randomAscii(len);
        String resultStr = "";

        for(int i = 0; i < len; ++i) {
            char ch = randomStr.charAt(i);
            if (ch == '"' || ch == '\\') {
                resultStr = resultStr + "\\";
            }

            resultStr = resultStr + String.valueOf(ch);
        }

        return "\"" + resultStr + "\"";
    }

    public String generateStrongString() {
        String[] specialCharacter = new String[]{"\\\b", "\\\f", "\\\\", "\\'", "\\\\*"};
        String value = "";
        int len = this.generateLength();

        for(int i = 0; i < len; ++i) {
            int index = this.random.nextInt(5);
            value = value + specialCharacter[index];
        }

        return value;
    }

    public String generateRandom(String type) {
        switch (type) {
            case "int":
            case "Integer":
            case "Object":
                return this.generateInt();
            case "short":
            case "Short":
                return this.generateShort();
            case "float":
            case "Float":
                return this.generateFloat();
            case "double":
            case "Double":
                return this.generateDouble();
            case "long":
            case "Long":
                return this.generateLong();
            case "boolean":
            case "Boolean":
                return this.generateBoolean();
            case "byte":
            case "Byte":
                return this.generateByte();
            case "char":
            case "Character":
                return this.generateChar();
            default:
                return "";
        }
    }

    public String generateMAX(String type) {
        switch (type) {
            case "int":
            case "Integer":
            case "Object":
                return String.valueOf(Integer.MAX_VALUE);
            case "short":
            case "Short":
                return Short.toString((short)32767);
            case "long":
            case "Long":
                return Long.toString(Long.MAX_VALUE) + "L";
            case "byte":
            case "Byte":
                return Byte.toString((byte)127);
            case "char":
            case "Character":
                return "Character.MAX_VALUE";
            case "float":
            case "Float":
                return "Float.MAX_VALUE";
            case "double":
            case "Double":
                return "Double.MAX_VALUE";
            default:
                return "";
        }
    }

    public String generateMIN(String type) {
        switch (type) {
            case "int":
            case "Integer":
            case "Object":
                return String.valueOf(Integer.MIN_VALUE);
            case "short":
            case "Short":
                return Short.toString(Short.MIN_VALUE);
            case "long":
            case "Long":
                return Long.toString(Long.MIN_VALUE) + "L";
            case "byte":
            case "Byte":
                return Byte.toString((byte)-128);
            case "float":
            case "Float":
                return "Float.MIN_VALUE";
            case "double":
            case "Double":
                return "Double.MIN_VALUE";
            case "char":
            case "Character":
                return "Character.MIN_VALUE";
            default:
                return "";
        }
    }

    public String generatePositiveInfinity(String type) {
        switch (type) {
            case "float":
            case "Float":
                return "Float.POSITIVE_INFINITY";
            case "double":
            case "Double":
                return "Double.POSITIVE_INFINITY";
            default:
                return "";
        }
    }

    public String generateNegativeInfinity(String type) {
        switch (type) {
            case "float":
            case "Float":
                return "Float.NEGATIVE_INFINITY";
            case "double":
            case "Double":
                return "Double.NEGATIVE_INFINITY";
            default:
                return "";
        }
    }

    public String generateNaN(String type) {
        switch (type) {
            case "float":
            case "Float":
                return "Float.NaN";
            case "double":
            case "Double":
                return "Double.NaN";
            default:
                return "";
        }
    }

    public String generateZero(String type) {
        if (!type.equals("int") && !type.equals("long") && !type.equals("short") && !type.equals("byte") && !type.equals("Object") && !type.equals("Integer") && !type.equals("Long") && !type.equals("Short") && !type.equals("Byte")) {
            if (!type.equals("char") && !type.equals("Character")) {
                if (!type.equals("double") && !type.equals("Double")) {
                    return !type.equals("float") && !type.equals("Float") ? "" : "0f";
                } else {
                    return "0d";
                }
            } else {
                return "'0'";
            }
        } else {
            return "0";
        }
    }

    public String generateIntegral(String type) {
        String value = "";
        int choose = this.random.nextInt(4);
        switch (choose) {
            case 0:
                value = this.generateRandom(type);
                break;
            case 1:
                value = this.generateMAX(type);
                break;
            case 2:
                value = this.generateMIN(type);
                break;
            case 3:
                value = this.generateZero(type);
                break;
        }

        return value;
    }

    public String generateFloatingPoint(String type) {
        String value = "";
        int choose = this.random.nextInt(7);
        switch (choose) {
            case 0:
                value = this.generateRandom(type);
                break;
            case 1:
                value = this.generateMAX(type);
                break;
            case 2:
                value = this.generateMIN(type);
                break;
            case 3:
                value = this.generateNaN(type);
                break;
            case 4:
                value = this.generatePositiveInfinity(type);
                break;
            case 5:
                value = this.generateNegativeInfinity(type);
                break;
            case 6:
                value = this.generateZero(type);
        }

        return value;
    }

    public boolean isIntegral(String type) {
        if (!type.equals("int") && !type.equals("long") && !type.equals("short") && !type.equals("byte") && !type.equals("char") && !type.equals("Object")) {
            return type.equals("Integer") || type.equals("Long") || type.equals("Short") || type.equals("Byte") || type.equals("Character") || type.equals("Object");
        } else {
            return true;
        }
    }

    public boolean isFloatingPoint(String type) {
        if (!type.equals("float") && !type.equals("double")) {
            return type.equals("Float") || type.equals("Double");
        } else {
            return true;
        }
    }

    public boolean isString(String type) {
        return type.equals("String");
    }

    public boolean isBoolean(String type) {
        if (type.equals("boolean")) {
            return true;
        } else {
            return type.equals("Boolean");
        }
    }

    public boolean isArray(String type) {
        return type.contains("[]") && !type.contains("<");
    }
}
