package com.trilogy.app.crm.account.foo;

public class StaticInitOther {
    private final static int privateFinalStaticInt;
    final int defaultFinalInt;
    protected int memberValue;

    static {
        privateFinalStaticInt = 2;
    }

    {
        defaultFinalInt = 4;
        memberValue = 3;
    }

    public StaticInitOther() { }

    public StaticInitOther(int bar) {
        this.memberValue = bar;
    }

    public static int getPrivateFinalStaticInt() {
        return privateFinalStaticInt;
    }

    public int getDefaultFinalInt() {
        return defaultFinalInt;
    }

    public int getMemberValue() {
        return memberValue;
    }
}