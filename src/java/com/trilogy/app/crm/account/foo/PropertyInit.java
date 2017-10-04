package com.trilogy.app.crm.account.foo;

public class PropertyInit {

    // 20- item
    public int case20;
    public PropertyInit(int bar) {
        case20 = bar;
    }

    public int getCase20() {
        return case20;
    }

    // 21- item
    public int case21;
    public PropertyInit(int bar, int x, int z) {
        case21 = bar;
    }

    public int getCase21() {
        return case21;
    }

    // 25- item
    public PropertyInit() { }
    public class Bar {
        private final int case25 = 25;
        public int getCase25() {
            return case25;
        }
    }

    // 26- item
    private final static int case26;
    static {
        case26 = 26;
    }

    public int getCase26() {
        return case26;
    }

}