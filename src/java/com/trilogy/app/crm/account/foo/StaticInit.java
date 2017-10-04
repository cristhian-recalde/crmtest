package com.trilogy.app.crm.account.foo;

public class StaticInit {
    private static int other;

    StaticInit() {
        other = 1;
    }

    public int getOther() {
        return other;
    }

    public static int getStaticOther() {
        return other;
    }

    static {
        other = 2;
    }
}