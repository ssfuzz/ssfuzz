package org.example.generator;


import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GroupGenerator {
    static final List<String> groupTypeList = Arrays.asList("List", "ArrayList", "Set", "EnumSet", "TreeSet", "Map", "EnumMap", "Hashtable");
    static final int length = 3;
    ImmutableMap<Object, Object> implementationClassMap = ImmutableMap.builder().put("Set", "HashSet").put("List", "ArrayList").put("Map", "HashMap").build();
    public GroupGenerator() {
    }

    String getGroupTypeFromStr(String typeStr) {
        int index = typeStr.indexOf("<");
        if (index > 0) {
            typeStr = typeStr.substring(0, index);
        }

        return typeStr;
    }

    boolean isGroupType(String typeStr) {
        String groupType = this.getGroupTypeFromStr(typeStr);
        return groupTypeList.contains(groupType);
    }

    List<String> getElementType(String typeStr) {
        List<String> elementTypeList = new ArrayList();
        String paramStr = typeStr.substring(typeStr.indexOf("<") + 1, typeStr.lastIndexOf(">"));
        String[] elementTypeArray = paramStr.split(",");
        if (elementTypeArray.length == 1) {
            elementTypeList.add(elementTypeArray[0]);
        } else {
            String[] var5 = elementTypeArray;
            int var6 = elementTypeArray.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String elementType = var5[var7];
                elementTypeList.add(elementType);
            }
        }

        return elementTypeList;
    }

    GroupInfo generateGroupInit(String variableTypeStr) {
        String groupType = this.getGroupTypeFromStr(variableTypeStr);
        String implementationClass = (String)this.implementationClassMap.get(groupType);
        if (implementationClass == null) {
            implementationClass = groupType;
        }

        String groupInit = "new " + variableTypeStr.replace(groupType, implementationClass) + "()";
        return new GroupInfo(groupInit, groupType, "java.util.*");
    }

    List<VariableComponent> generateGroup(String variableTypeStr, String variableName, String paramPattern) {
        List<VariableComponent> resultList = new ArrayList();
        GroupInfo groupInfo = this.generateGroupInit(variableTypeStr);
        if (groupInfo.getImplementationClass().length() > 0) {
            resultList.add(new VariableComponent(variableTypeStr, variableName, groupInfo.getValue(), groupInfo.getImplementationClass()));
        } else {
            resultList.add(new VariableComponent(variableTypeStr, variableName, groupInfo.getValue()));
        }

        return resultList;
    }

    class GroupInfo {
        private String value;
        private String groupType;
        private String implementationClass;

        public GroupInfo(String value, String groupType, String implementationClass) {
            this.value = value;
            this.groupType = groupType;
            this.implementationClass = implementationClass;
        }

        public String getValue() {
            return this.value;
        }

        public String getGroupType() {
            return this.groupType;
        }

        public String getImplementationClass() {
            return this.implementationClass;
        }
    }
}

