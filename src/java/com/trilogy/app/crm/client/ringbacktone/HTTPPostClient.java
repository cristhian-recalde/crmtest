package com.trilogy.app.crm.client.ringbacktone;

import java.io.IOException;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Not intended for use outside of the RBT package
 * 
 * Check Specification for parameters of Provision Command.
 * @author Nick Landry
 *
 */
class HTTPPostClient implements RBTClient
{

	public HTTPPostClient(Context context, Long subscriberNotFoundErrorCode)
	{
	    subscriberNotFoundErrorCode_ = subscriberNotFoundErrorCode;
	}

    
    public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
    {
        UCPrbtInfo config =(UCPrbtInfo) PRBTSupport.getPrbtConfig(context);
        ProvCommand command = ProvCommand.createCommand(context, ProvCommand.RBT_ADDSUB);
        HttpProvClient client = getClient(context, config);
        
        try
        {
            command.setExternalID(config.getRbtExternalID());
            command.setAccessMediaName(config.getRbtAccessMediaName());
            command.setMsisdn(subscriber.getMSISDN());
            command.setNameValue("IMSI", subscriber.getIMSI());
            command.setNameValue("MediaID", config.getRbtMediaID());
            command.setNameValue("Type", subscriber.getSubscriberType()==SubscriberTypeEnum.PREPAID?"0":"1");
            command.setNameValue("ClassOfService", config.getRbtClassOfService());
            command.setNameValue("DoCharging", config.isRbtDoCharging()?"1":"0");
            command.setNameValue("Comment", config.getRbtComment());
            
            if (LogSupport.isDebugEnabled(context)) LogSupport.debug(context, this,"Sending Command "+command);
            
            ProvResponse response  = client.sendCommand(command);
            if (response ==null || response.getStatus()==null || !response.getStatus().equals(ProvResponse.OK))
            {
                throw new RBTClientException("Provisioning Status: "+response, parseResultCode(response));
            }
            
            if (LogSupport.isDebugEnabled(context)) LogSupport.debug(context, this,"Sending Command successfully and received response:"+response);
        }
        catch (InvalidCommandException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        catch (IOException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        
    }

    
    public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
    {
        UCPrbtInfo config =(UCPrbtInfo) PRBTSupport.getPrbtConfig(context);
        ProvCommand command = ProvCommand.createCommand(context, ProvCommand.RBT_REMOVESUB);
        HttpProvClient client = getClient(context, config);

        
        try
        {
            command.setExternalID(config.getRbtExternalID());
            command.setAccessMediaName(config.getRbtAccessMediaName());
            command.setMsisdn(msisdn);
            
            LogSupport.debug(context, this,"Sending Command "+command);
            
            ProvResponse response  = client.sendCommand(command);
            if (response ==null || response.getStatus()==null || !response.getStatus().equals(ProvResponse.OK))
            {
                throw new RBTClientException("Provisioning Status: "+response, parseResultCode(response));
            }
            
            LogSupport.debug(context, this,"Sending Command successfully and received response:"+response);
        }
        catch (InvalidCommandException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        catch (IOException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        
    }

    
    public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn) throws RBTClientException
    {
        UCPrbtInfo config = (UCPrbtInfo) PRBTSupport.getPrbtConfig(context);
        ProvCommand command = ProvCommand.createCommand(context, ProvCommand.RBT_SWITCHMSISDN);
        HttpProvClient client = getClient(context, config);

        
        try
        {
            command.setExternalID(config.getRbtExternalID());
            command.setAccessMediaName(config.getRbtAccessMediaName());
            command.setMsisdn(oldMsisdn);
            command.setNameValue("NewMSISDN", newMsisdn);
            
            LogSupport.debug(context, this,"Sending Command "+command);
            
            ProvResponse response  = client.sendCommand(command);
            if (response ==null || response.getStatus()==null || !response.getStatus().equals(ProvResponse.OK))
            {
                throw new RBTClientException("Provisioning Status: "+response, parseResultCode(response));
            }
            
            LogSupport.debug(context, this,"Sending Command successfully and received response:"+response);
        }
        catch (InvalidCommandException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        catch (IOException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        
    }

    
    public void updateSubscriberReactivate(Context context, String msisdn) throws RBTClientException
    {
        UCPrbtInfo config = (UCPrbtInfo) PRBTSupport.getPrbtConfig(context);
        ProvCommand command = ProvCommand.createCommand(context, ProvCommand.RBT_UNSUSPENDSUB);
        HttpProvClient client = getClient(context, config);

        try
        {
            command.setExternalID(config.getRbtExternalID());
            command.setAccessMediaName(config.getRbtAccessMediaName());
            command.setMsisdn(msisdn);
            
            LogSupport.debug(context, this,"Sending Command "+command);
            
            ProvResponse response  = client.sendCommand(command);
            if (response ==null || response.getStatus()==null || !response.getStatus().equals(ProvResponse.OK))
            {
                throw new RBTClientException("Provisioning Status: "+response, parseResultCode(response));
            }
            
            LogSupport.debug(context, this,"Sending Command successfully and received response:"+response);
        }
        catch (InvalidCommandException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        catch (IOException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        
    }

    
    public void updateSubscriberSuspend(Context context, String msisdn) throws RBTClientException
    {
        UCPrbtInfo config = (UCPrbtInfo) PRBTSupport.getPrbtConfig(context);
        ProvCommand command = ProvCommand.createCommand(context, ProvCommand.RBT_SUSPENDSUB);
        HttpProvClient client = getClient(context, config); 
            
        try
        {
            command.setExternalID(config.getRbtExternalID());
            command.setAccessMediaName(config.getRbtAccessMediaName());
            command.setMsisdn(msisdn);
            
            LogSupport.debug(context, this,"Sending Command "+command);
            
            ProvResponse response  = client.sendCommand(command);
            if (response ==null || response.getStatus()==null || !response.getStatus().equals(ProvResponse.OK))
            {
                throw new RBTClientException("Provisioning Status: "+response, parseResultCode(response));
            }
            
            LogSupport.debug(context, this,"Sending Command successfully and received response:"+response);
        }
        catch (InvalidCommandException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        catch (IOException e)
        {
            throw new RBTClientException(e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e);
        }
        
    }
    
    private HttpProvClient getClient(Context ctx, UCPrbtInfo config)
    {
        return  new HttpProvClient(
                ctx, 
                config.getRbtUrl(), 
                config.isRbtAuth(), 
                config.getRbtUser(), 
                config.getRbtPass(), 
                config.getAuthType());
    }

    private int parseResultCode(ProvResponse response)
    {
        int result = ExternalAppSupport.UNKNOWN;
        try
        {
            if (response!=null && response.getStatus()!=null)
            {
                String resultCode = response.getStatus();
                result = Integer.parseInt(resultCode);
            }
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

}