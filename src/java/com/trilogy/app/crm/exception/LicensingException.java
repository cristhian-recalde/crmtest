package com.trilogy.app.crm.exception;

public class LicensingException extends Exception {
    public LicensingException(String msg)
    {
        super(msg);
    }
    
    public LicensingException(String s, Throwable t)
    {
         super(s, t);
    }
        
    public LicensingException(Throwable t)
    {
         super(t);
    }
}
