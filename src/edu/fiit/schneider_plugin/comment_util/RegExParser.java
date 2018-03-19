package edu.fiit.schneider_plugin.comment_util;

public class RegExParser {

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
}
