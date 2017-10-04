package com.trilogy.app.crm.client;

/*
 * TFA Exception Class 
 * 
 * @author piyush.shirke@redknee.com
 * 
 */
public class TFAAuxiliarServiceClientException extends Exception {
	
	private static final long serialVersionUID = 1L;


    /**
     * @param string
     * @param result
     */
    public TFAAuxiliarServiceClientException(String msg, int result)
    {
        super(msg + " result="+result );
     
    }


    public TFAAuxiliarServiceClientException(String s)
    {
        super(s);
    }


    public TFAAuxiliarServiceClientException(String msg, Throwable cause)
    {
        super(msg, cause);
    }


}
