package com.trilogy.app.crm.creditcard;

import com.trilogy.app.crm.bean.CreditCardEntry;


public interface CreditCardEntryAware
{
    public boolean isCreditCardPayment();
    public CreditCardEntry getCreditCardInfo();
    public void setCreditCardInfo(CreditCardEntry creditCardInfo) throws IllegalArgumentException;
}
