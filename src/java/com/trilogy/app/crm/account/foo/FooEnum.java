package com.trilogy.app.crm.account.foo;

import java.math.BigInteger;

public enum FooEnum{
    STATIC, NOT_STATIC;

    public int case8(){
        return 1;
    }

    int getValue(){
        return Math.max(STATIC.ordinal(), NOT_STATIC.ordinal());
    }

    static int getValue2(){
        return 34;
    }

    String valueStr(){
        return FooEnum.class.getName();
    }

    BigInteger valueBig(){
        return BigInteger.TEN;
    }
}
