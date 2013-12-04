package us.sosia.magnet.service.provider;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public enum Priority {
    BEST(0),
    GOOD(1),
    NORMAL(2),
    OK(3),
    BAD(4);

    private final int pr;

    private Priority(int pr) {
        this.pr = pr;
    }

    public int getPr() {
        return pr;
    }

    public Priority valueOf(int pr){
        switch (pr){
            case 0:
                return BEST;
            case 1:
                return GOOD;
            case 2:
                return NORMAL;
            case 3:
                return OK;
            case 4:
                return BAD;
            default:
                return NORMAL;
        }
    }
}
