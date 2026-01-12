public class Util {

   public enum Mode {
        CHECKVALUE("Check Value", 1),
        PHRASES("Phrases", 2),
        VALUEBIGGER("Value bigger", 2),
        VALUESMALLER("Value smaller", 2),
        BIGGERTHAN("Bigger than", 3),
        SMALLERTHAN("Smaller than", 4);

        private final String label;
        private final int code;

        Mode(String label, int code) {
            this.label = label;
            this.code = code;
        }


       public String getLabel() {
           return label;
       }

       public int getCode() {
           return code;
       }
    }
}
