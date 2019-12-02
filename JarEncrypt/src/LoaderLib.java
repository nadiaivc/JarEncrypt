public class LoaderLib {
        private native boolean IsDebug2(boolean a1, boolean a2);
        private native boolean IsDebug(boolean a1, boolean a2);
        public static void main(String[] args) {

        }
        static {
            System.loadLibrary("ForJava");
        }
        public static boolean checkDebug1()
        {
            boolean Debug = new LoaderLib().IsDebug(true, false);
            return Debug;
        }
    public static boolean checkDebug2()
    {
        boolean Debug = new LoaderLib().IsDebug2(true, false);
        return Debug;
    }

}
