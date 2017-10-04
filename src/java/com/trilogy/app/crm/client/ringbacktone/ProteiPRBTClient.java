package com.trilogy.app.crm.client.ringbacktone;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class ProteiPRBTClient
implements RBTClient
{
    
    public ProteiPRBTClient(Long subscriberNotFoundErrorCode)
    {
        subscriberNotFoundErrorCode_ = subscriberNotFoundErrorCode;
    }

    @Override
    public void createSubscriber(Context ctx, Subscriber subscriber) throws RBTClientException
    {
        LogSupport.debug(ctx, this, "Create RBT profile for subscriber=" + subscriber.getId());
        ProteiPrbtInfo config =(ProteiPrbtInfo) PRBTSupport.getPrbtConfig(ctx);
        String requestString = config.getCreateScript();
        requestString = requestString.replaceAll(MSISDN_VARIABLE, subscriber.getMsisdn());

        doRequest(ctx, requestString, config); 

        
    }

    @Override
    public void deleteSubscriber(Context ctx, String msisdn) throws RBTClientException
    {
        LogSupport.debug(ctx, this, "delete RBT profile for msisdn=" + msisdn);
        ProteiPrbtInfo config =(ProteiPrbtInfo) PRBTSupport.getPrbtConfig(ctx);

        String requestString =  config.getDeleteScript();
        requestString = requestString.replaceAll(MSISDN_VARIABLE, msisdn);
        doRequest(ctx, requestString, config);         
        
    }

    @Override
    public void updateSubscriberMSISDN(Context ctx, String oldMsisdn, String newMsisdn) throws RBTClientException
    {
        LogSupport.debug(ctx, this, "switch RBT msisdn " + newMsisdn + " to " + oldMsisdn);
        ProteiPrbtInfo config =(ProteiPrbtInfo) PRBTSupport.getPrbtConfig(ctx);

        String requestString =  config.getSwitchMsisdnScript();
        requestString = requestString.replaceAll(MSISDN_VARIABLE, newMsisdn);
        requestString = requestString.replaceAll(OLD_MSISDN_VARIABLE, oldMsisdn);
        doRequest(ctx, requestString, config);
        
    }

    @Override
    public void updateSubscriberReactivate(Context ctx, String msisdn) throws RBTClientException
    {
        LogSupport.debug(ctx, this, "reactivate RBT profile for msisdn " + msisdn);
        ProteiPrbtInfo config =(ProteiPrbtInfo) PRBTSupport.getPrbtConfig(ctx);

        String requestString =  config.getReactivateScript();
        requestString = requestString.replaceAll(MSISDN_VARIABLE, msisdn);
        doRequest(ctx, requestString, config); 
        
    }

    @Override
    public void updateSubscriberSuspend(Context ctx, String msisdn) throws RBTClientException
    {
        LogSupport.debug(ctx, this, "suspend RBT profile for msisdn=" + msisdn);
        ProteiPrbtInfo config =(ProteiPrbtInfo) PRBTSupport.getPrbtConfig(ctx);

        String requestString =  config.getSuspendScript();
        requestString = requestString.replaceAll(MSISDN_VARIABLE, msisdn);
        doRequest(ctx, requestString, config);
        
    }
  
    public String sendCommand(Context ctx, String command, ProteiPrbtInfo config)
    throws IOException
    {
        HttpClient client = new HttpClient(); 
        client.setConnectionTimeout(config.getSocketTimeout());
        PostMethod postRequest = new PostMethod(config.getRbtUrl());

        postRequest.addRequestHeader("Content-Length", String.valueOf(command.length()));
        postRequest.addRequestHeader("Content-Type", "text/xml");
        postRequest.setRequestBody(command);
          
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "protei prbt command " + command, null).log(ctx); 
        }    
        if (client != null)
        {    
            int code = client.executeMethod(postRequest);
                      
            if (code == HttpStatus.SC_OK/*HTTP OK*/) 
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "protei prbt response " + postRequest.getResponseBodyAsString(), null).log(ctx); 
                }
                return postRequest.getResponseBodyAsString();
            }
            else
            {
                return "Http Error:" + code;
            }
        }    
        
        return "Http Error: fail to find HTTP client";
        
    }
    
  
    public String  parseResponse( Context ctx, String rawLine, String xpathString)
    throws RBTClientException
    {
        try
        {
            ProteiResponseParseXml parser = new ProteiResponseParseXml(rawLine.trim(), xpathString); 
            return parser.getResultCode(); 
        } catch (Exception e)
        {
            RBTClientException ex = new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION);
            ex.setStackTrace(e.getStackTrace()); 
            throw ex;  
        }
    }
    

    
    
    public void doRequest(Context ctx, String requestString, ProteiPrbtInfo config)
    throws RBTClientException
    {
        try
        {
            
            String responseString = sendCommand(ctx, requestString, config);
            String result = parseResponse(ctx, responseString, config.getResponseXpath());
            
            if (result ==null ||  !result.equals(RESULT_SUCCESS))
            {
                throw new RBTClientException("Provisioning Status: "+ result, parseResultCode(result));
            }
        }
        catch (Throwable e)
        {
            RBTClientException ex = new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION);
            ex.setStackTrace(e.getStackTrace()); 
            throw ex; 
        }
    }
    
    private int parseResultCode(String resultCode)
    {
        int result = ExternalAppSupport.UNKNOWN;
        try
        {
            result = Integer.parseInt(resultCode);
        }
        catch (Throwable t)
        {
            // Ignore;
        }
        return result;
    }
    
    public Long getSubscriberNotFoundErrorCode()
    {
        return subscriberNotFoundErrorCode_;
    }

    
    private Long subscriberNotFoundErrorCode_;
    
    public static final String RESULT_SUCCESS = "0"; 
    public static final String MSISDN_VARIABLE = "%MSISDN%";
    public static final String OLD_MSISDN_VARIABLE = "%OLD_MSISDN%";

}
