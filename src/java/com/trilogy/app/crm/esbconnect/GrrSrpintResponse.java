package com.trilogy.app.crm.esbconnect;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import com.trilogy.esb.schema.genericrequestresponse.GenericParameterType;

import com.trilogy.esb.schema.genericrequestresponse.ResponseStatusType;
import com.trilogy.esb.schema.genericrequestresponse.ResponseStatusStateType;
import com.trilogy.esb.schema.genericrequestresponse.ResponseType;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.esbconnect.ESBMessageResult;
import com.trilogy.app.crm.esbconnect.ESBMessageConstants;
import com.trilogy.app.crm.provision.service.ErrorCode;



public class GrrSrpintResponse 
{
	public GrrSrpintResponse(String rawResponse)
	{
		this.rawResponse = rawResponse; 
	}
	
	
	public ESBMessageResult getResponseCode(Context ctx)
	throws Exception
	{
	ResponseType grrResponse = this.getResponse(ctx); 
	
	//int ret = ErrorCode.SUCCESS;
	Map<String, String> gp = new HashMap<String, String>();
    esbMessageResult_ = new ESBMessageResult();
	
	if(grrResponse != null)
	{
		List<GenericParameterType> paramList = grrResponse.getResult();
		
		for(GenericParameterType param : paramList)
		{
			if(param.getName()!="")
			{
				
		        gp.put(param.getName().toString(), param.getValue().toString().replaceAll("[\\[\\],]",""));
		       
		        if(param.getName().toString().equals(ESBMessageConstants.ESB_SOAP_FAULT_CODE))
		        {
		         	esbMessageResult_.setResultCode(ESBMessageConstants.ESB_RESULT_CODE_SOAPFAULT);
		         	
		        }
		        if(param.getName().toString().equals(ESBMessageConstants.ESB_SOAP_FAULT_STRING))
				{
					esbMessageResult_.setResultMessage(param.getValue().toString());
				}
		        if (LogSupport.isDebugEnabled(ctx))
		     	{
		     		LogSupport.debug(ctx, this, "param.getName() " + param.getName().toString());
		     		LogSupport.debug(ctx, this, "param.getValue() " + param.getValue().toString().replaceAll("[\\[\\],]",""));
		                
		     	}
			}
		}
	if(esbMessageResult_.getResultCode()== -1)
	{
		resolveStatusCode(grrResponse);
	}
	if (LogSupport.isDebugEnabled(ctx))
 	{
 		LogSupport.debug(ctx, this, "esbMessageResult_.getResultCode() " + esbMessageResult_.getResultCode());
 		LogSupport.debug(ctx, this, "esbMessageResult_.getResultMessage " + esbMessageResult_.getResultMessage());
            
 	}
	
	esbMessageResult_.setResultMessageMap(gp);
	}
	
	return esbMessageResult_;
}
	
		
 private void resolveStatusCode(ResponseType response)
 { 		String  retMessage= ""; 
 		
	 	
    		if (response.getStatus().equals(ResponseStatusStateType.ERROR) || response.getStatus().getState().equals(ResponseStatusStateType.ERROR))
    		{
    			esbMessageResult_.setResultCode(ESBMessageConstants.ESB_RESULT_CODE_ERROR);
    			esbMessageResult_.setResultMessage(ResponseStatusStateType.ERROR.toString());
    		}
       
        	if (response.getStatus().equals(ResponseStatusStateType.PENDING) || response.getStatus().getState().equals(ResponseStatusStateType.PENDING))
      		{
        		esbMessageResult_.setResultCode(ESBMessageConstants.ESB_RESULT_CODE_PENDING);
        		esbMessageResult_.setResultMessage(ResponseStatusStateType.PENDING.toString());
          
      		}
 
        	  if (response.getStatus().equals(ResponseStatusStateType.COMPLETE) || response.getStatus().getState().equals(ResponseStatusStateType.COMPLETE))
        	{
        	    esbMessageResult_.setResultCode(ESBMessageConstants.ESB_RESULT_CODE_SUCCESS);
            	esbMessageResult_.setResultMessage(ResponseStatusStateType.COMPLETE.toString());
       		}
         
        	  if (response.getStatus().equals(ResponseStatusStateType.INVALID) || response.getStatus().getState().equals(ResponseStatusStateType.INVALID))
      		{
        		  esbMessageResult_.setResultCode(ESBMessageConstants.ESB_RESULT_CODE_INVALID);
        		  esbMessageResult_.setResultMessage(ResponseStatusStateType.INVALID.toString());
      		}
       
 }
	
	public ResponseType getResponse(Context ctx)
	throws Exception
	{
		ResponseType response;
			JAXBContext jaxbContext = JAXBContext.newInstance(ResponseType.class); 
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ByteArrayInputStream input = new ByteArrayInputStream (rawResponse.getBytes());
			JAXBElement<ResponseType> element =  jaxbUnmarshaller.unmarshal(new StreamSource(input), ResponseType.class) ;
			ResponseType data = element.getValue();	
			
			return data;
		
	}
	
	String rawResponse; 
	
	ESBMessageResult esbMessageResult_ = null;
}
