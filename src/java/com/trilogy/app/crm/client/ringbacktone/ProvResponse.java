package com.trilogy.app.crm.client.ringbacktone;


public class ProvResponse
{
    public static final String OK = "0";
    
    public ProvResponse()
    {}
    
    public ProvResponse(String status, String description, String cause)
    {
        super();
        status_ = status;
        description_ = description;
        cause_ = cause;
    }

    public ProvResponse(String response)
    {
        
        if (response != null)
        {
            response = response.trim();
            if (response.endsWith("'")) response = response.substring(0, response.length()-1);
            
            String[] temp = response.substring(0,response.length()-1).split(":");
            if (temp.length>=3)
            {
                status_ = temp[0];
                description_ = temp[1];
                cause_  = temp[2];
            }
        }
        
        if (status_ == null)
        {
            cause_ = "Invalid Response:"+response;
        }
    }

    public String getStatus()
    {
        return status_;
    }
    
    public String getDescription()
    {
        return description_;
    }
    
    public String getCause()
    {
        return cause_;
    }
    
    public void setStatus(String status)
    {
        status_ = status;
    }

    
    public void setDescription(String description)
    {
        description_ = description;
    }

    
    public void setCause(String cause)
    {
        cause_ = cause;
    }

    public String toString()
    {
        return status_+":"+description_+":"+cause_;
    }
    
    private String status_ = null;
    private String description_;
    private String cause_;
    
}
