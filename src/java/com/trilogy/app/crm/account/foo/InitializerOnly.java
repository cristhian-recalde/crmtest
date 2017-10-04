package com.trilogy.app.crm.account.foo;

public class InitializerOnly {

    private static int other;
    private static final int init;

    private final int defaultFinalInt;
    private int memberValue;

    InitializerOnly() {
        other = 1;
        this.memberValue = 5;
    }

    public int getOther() {
        return other;
    }

    public static int getStaticOther() {
        return other;
    }

    public static int getInit() {
        return init;
    }

    static {
        other = 2;
        init = 6;
    }

    {
        defaultFinalInt = 4;
        memberValue = 3;
    }

    public int getDefaultFinalInt() {
        return this.defaultFinalInt;
    }

    public int getMemberValue() {
        return memberValue;
    }

}