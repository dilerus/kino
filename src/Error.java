public class Error {
    String name;
    boolean fatal;

    public Error(String name, boolean fatal) {
        this.name = name;
        this.fatal = fatal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }
}
