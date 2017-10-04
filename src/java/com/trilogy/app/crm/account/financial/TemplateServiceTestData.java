package com.trilogy.app.crm.account.financial;

import java.io.Serializable;
import java.util.Date;
import com.trilogy.app.crm.account.filter.Member;

public /*abstract*/ class TemplateServiceTestData {

    static class someClassIntern {
        protected int valuable;

        public void setValuable(int valuable) {
            this.valuable = valuable;
        }
    }

    transient int variableInIf;
    private Member member;

    public void methodWithSingleMockNoWhenClause(Serializable ser, Date date) {
        ser.wait();
    }

    // abstract void proceed();

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean variableInIfMethod() {
        if (variableInIf == 5) {
            return true;
        } else {
            System.out.println("!= 5");
        }
        return false;
    }

    public boolean ifSize() {
        if (size > 3) {
            return false;
        }

        return true;
    }

    // public int getdata() {
    //    return variableInIf;
    //}

    final public void init(int variableInIf) { this.variableInIf = variableInIf; }

    public Member getMem() {
        return member;
    }

    public void saveMem(Member member) {
        this.member = member;
    }

    private int size;

    public void initiate(int size) {
        this.size = size;
    }

    public int size() {
        return this.size;
    }

    private double price;

    public void putprice(double price) {
        this.price = price;
    }

    // public double price() { return this.price; }
}