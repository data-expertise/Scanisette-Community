package scanisette.tools;

public class StringTool {

    public static boolean isSomething(String s) {
        if (s==null || s.isEmpty() || s.isBlank())
            return false;
        else
            return true;
    }
}
