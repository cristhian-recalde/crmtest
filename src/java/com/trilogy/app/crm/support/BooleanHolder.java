package com.trilogy.app.crm.support;


public class BooleanHolder
{
    public BooleanHolder(boolean value)
    {
        booleanValue_ = value;
    }
    
    private boolean booleanValue_;

    
    public boolean isBooleanValue()
    {
        return booleanValue_;
    }

    
    public void setBooleanValue(boolean booleanValue)
    {
        this.booleanValue_ = booleanValue;
    }
    
}
