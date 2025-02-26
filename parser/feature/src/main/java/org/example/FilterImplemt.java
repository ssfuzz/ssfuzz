package org.example;

public class FilterImplemt implements DirExplorer.Filter {
    @Override
    public boolean interested(String path) {
        String temp[]=path.split("/");
        String fileName=temp[temp.length-1];
        String[] strArray = fileName.split("\\.");
        int suffixIndex = strArray.length-1;
        String type = strArray[suffixIndex];

        if(!type.equals("java"))
            return false;

        return true;
    }
}
