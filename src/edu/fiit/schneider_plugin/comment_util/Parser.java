package edu.fiit.schneider_plugin.comment_util;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    /*
    Regex taken from
    https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
    */
    public static String[] camelCaseSplitter(String str){
        return str.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])}");
    }
    public static String[] snakeCaseSplitter(String str){
        return str.split("_");
    }

    public static List<String> specialCheck(List<String> targetWordList) {
        List<String> newList = new ArrayList<>();
        int startingIndexUpper;
        int length;
        for (String string : targetWordList) {
            startingIndexUpper = -1;
            length = 0;
            for (int i = 0; i < string.length() - 1; i++) {
                if (isUppper(string.charAt(i)) && isUppper(string.charAt(i + 1))) {
                    if (startingIndexUpper == -1)
                        startingIndexUpper = i;
                    length++;
                }
            }
            if (startingIndexUpper != -1) {
                newList.add(string.substring(startingIndexUpper, startingIndexUpper + length));
                newList.add(string.substring(startingIndexUpper + length));
            } else
                newList.add(string);
        }

        return newList;
    }
    //KKKE 0 3

    private static boolean isUppper(Character character) {
        return character >= 'A' && character <= 'Z';
    }
}
