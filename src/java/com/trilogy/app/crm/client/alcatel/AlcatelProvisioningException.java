package com.trilogy.app.crm.client.alcatel;

public class AlcatelProvisioningException extends Exception
{
    public AlcatelProvisioningException(String message, int resultCode)
    {
        this(message, resultCode, null, null);
    }
    
    public AlcatelProvisioningException(String message, int resultCode, Throwable t)
    {
        this(message, resultCode, null, null, t);
    }

    public AlcatelProvisioningException(String message, int resultCode, String alcatelResultCode, String alcatelResultMessage)
    {
        this(message, resultCode, alcatelResultCode, alcatelResultMessage, null);
    }

    public AlcatelProvisioningException(String message, int resultCode, String alcatelResultCode, String alcatelResultMessage, Throwable t)
    {
        super(message, t);
        resultCode_ = resultCode;
        alcatelResultCode_ = alcatelResultCode;
        alcatelResultMessage_ = alcatelResultMessage;
    }
    
    public String getAlcatelResultMessage()
    {
        return alcatelResultMessage_;
    }

    public void setAlcatelResultMessage(String alcatelResultMessage)
    {
        this.alcatelResultMessage_ = alcatelResultMessage;
    }

    public String getAlcatelResultCode()
    {
        return alcatelResultCode_;
    }

    public void setAlcatelResultCode(String alcatelResultCode)
    {
        this.alcatelResultCode_ = alcatelResultCode;
    }

    public int getResultCode()
    {
        return resultCode_;
    }

    public void setResultCode(int resultCode)
    {
        this.resultCode_ = resultCode;
    }


    private int resultCode_;

    private String alcatelResultCode_;

    private String alcatelResultMessage_;
}
