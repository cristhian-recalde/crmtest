package com.trilogy.app.crm.account.foo;

import java.math.BigDecimal;


public class Literals {
    final int val = -5;
    final int x = 5;
/*
    //3- item
    public String getCase3() {
        return "A" + "BC";
    }

    //4- item
    public String getCase4() {
        return "A" + 123 + "BC";
    }

    //5-item
    public float getCase5() {
        return 123.0f;
    }

    //6-item
    public BigDecimal getCase6() {
        return BigDecimal.TEN;
    }

    //7-item
    public int getCase7() {
        return 42;
    }

    //17-item
    public String getCase17() throws Exception {
        return "ABC";
    }

    public String getValue() {
        return (123 + 456)*15 + "ABC";
    }

    public float getFloat() {
        return 123/456.0f;
    }
*/
    public float getInvalid() {
        return 123/ (x - 5);
    }

    public float getInvalid2() {
        return 124 / getVal() ;
    }

    public float getInfinity() {
        return 123/0;
    }

    public int getVal() { return 0; }

    public long getValueMax() {
        return Math.max(123, 456);
    }

    public long getValueMaxIn() {
        return Math.max(5, x);
    }

    public long getValueMaxIn2() {
        return Math.max(val, 5);
    }

    public double getPI() {
        return Math.PI;
    }

    public BigDecimal getOneString() {
        return new BigDecimal("1.00");
    }

    public Integer getValueInt() {
        return new Integer("10");
    }

    public Integer getValueMaxInteger() {
        return Integer.MAX_VALUE;
    }

    public int getAbs() {
        return Math.abs(val);
    }

    public double getE() {
        return Math.E;
    }

    public boolean isActive() {
        return Boolean.FALSE;
    }

}