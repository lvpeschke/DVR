class F {
  final static String SPACES = "                                                                                                    ";
    public static String format(String s, int len){
    int slen = len-s.length();

    if(slen > SPACES.length())
        slen = SPACES.length();
  
    if(slen > 0)
        return s+SPACES.substring(0,slen);
    else
        return s;

    }

    public static String format(Object x, int len){
    return format(String.valueOf(x), len);
    }

    public static String format(int[] array, int len) {
        String result = "";
        for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
            result += F.format(String.valueOf(array[i]), len);
        }
        return result;
    }


    public static String format(long x, int len){
    return format(String.valueOf(x), len);
    }

    public static String format(double x, int len){
    return format(String.valueOf(x), len);
    }

    public static String format(char x, int len){
    return format(String.valueOf(x), len);
    }
}
