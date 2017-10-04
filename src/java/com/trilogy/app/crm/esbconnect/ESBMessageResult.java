package com.trilogy.app.crm.esbconnect;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
public class ESBMessageResult {

	protected int resultCode_;
	protected String resultMessage_;
	protected Map<String,String> resultMessageMap_;
	/**
	 * 
	 */
	public ESBMessageResult()
	{
		resultCode_ = -1;   
	}
      
    public ESBMessageResult(int initial)
    {
    	resultCode_ = initial;
    }
      
    public void setResultCode(int result)
    {
    	resultCode_ = result;
    }
    
    public int getResultCode()
    {
          return resultCode_;
    }
    
    public void setResultMessage(String resultMessage)
    {
    	resultMessage_ = resultMessage;
    }
    
    public String getResultMessage()
    {
          return resultMessage_;
    }
    
    public void setResultMessageMap(Map<String, String> resultMap)
    {
    	resultMessageMap_ = resultMap;
    }
    
    public Map<String,String> getResultMessageMap()
    {
          return resultMessageMap_;
    }

   
}
