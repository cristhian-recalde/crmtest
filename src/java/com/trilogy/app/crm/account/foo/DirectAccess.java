package com.trilogy.app.crm.account.foo;

public class DirectAccess {

    // 10-item
    int case10;
    public Object getCase10() {
        return case10;
    }

    // 11-item
    public int case11;
    public int getCase11() {
        return case11;
    }

    // 12-item
    public int barDifferent;
    public Object getCase12() {
        return barDifferent;
    }

    // 18-item
    static final int case18 = 123;
    public int getCase18() {
        return case18;
    }

    // 19-item
    static final int case19 = 124;
    public static int getCase19() {
        return case19;
    }

    // 24-item
    private final int case24 = 123 * 456;
    public int getCase24() {
        return case24;
    }

    private final int case24_1 = (123 + 456)*15 + "ABC";

    enum Algo {
        MD5, SHA1;
    }
}