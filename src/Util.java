public class Util {

   public enum Mode {
        CHECKVALUE("Check Value", 1),
        PHRASES("Phrases", 2),
        VALUEBIGGER("Value bigger", 3),
        VALUESMALLER("Value smaller", 4),
        BIGGERTHAN("Bigger than", 5),
        SMALLERTHAN("Smaller than", 6);

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
