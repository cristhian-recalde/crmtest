package com.trilogy.app.crm.voicemail.client;

import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.hlr.CrmHlrServicePipelineImpl;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.support.VoicemailSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class SOGClient extends AbstractVoiceMailClient implements ContextAware
{
    private static final String SERVICE_NAME = "SOGClient";
    private static final String SERVICE_DESCRIPTION = "SOG Voicemail Client";
    
    private Context ctx_;

    public SOGClient(Context ctx)
    {
        setContext(ctx);
    }
        
    public ExternalProvisionResult provision(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service) 
    {
         final String msg = "Activate voicemail profile for subscriber:" + sub.getId(); 

        
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, service); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found for service " + service.getName(), null);
            return new ExternalProvisionResult(RESULT_FAIL_INVALID_VM_PLAN, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidProvCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidProvCmd(); 
        }
        return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }
    
    
    public ExternalProvisionResult unprovision(Context ctx, Subscriber sub, ServiceBase service)
    {   
        final VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        return unprovision(ctx, sub, service, configBean.isDeleteUserWait(), configBean.getDeleteUserTimeOut()); 
    }
    
    protected ExternalProvisionResult unprovision(Context ctx, Subscriber sub, ServiceBase service, boolean waitForReply, long waitTimeOut)
    
    { 
        final String msg = "Unprovisioning of voicemail service for subscriber:" + sub.getId(); 
        
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, service); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found for service " + service.getName(), null);
            return new ExternalProvisionResult(RESULT_FAIL_INVALID_VM_PLAN, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidUnProvCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidUnProvCmd(); 
        }
        
         return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }

    
    
    public ExternalProvisionResult deactivate(Context ctx, Subscriber sub, ServiceBase service) 
    {
        final String msg = "Deactivate voicemail profile for subscriber:" + sub.getId(); 
        
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, service); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found for service " + service.getName(), null);
            return new ExternalProvisionResult(RESULT_FAIL_INVALID_VM_PLAN, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidDeactiveCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidDeactiveCmd(); 
        }
         
        return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }

    
    public ExternalProvisionResult activate(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service) 
    {

        final String msg = "Activate voicemail profile for subscriber:" + sub.getId(); 
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, service); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found  for service " + service.getName(), null);
            return new ExternalProvisionResult(RESULT_FAIL_INVALID_VM_PLAN, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidActiveCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidActiveCmd(); 
        }
        return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }  
    
    
    public ExternalProvisionResult changeMsisdn(
            final Context ctx, 
            final Subscriber sub, 
            final String newMsisdn) 
    {

        final String msg = "Change voicemail msisdn for subscriber:" + sub.getId(); 
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, sub); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found  for sub" + sub.getId(), null);
            return new ExternalProvisionResult(RESULT_FAIL_NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidChangeMsisdnCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidChangeMsisdnCmd(); 
        }
        
        if ( hlrCmd != null)
        {    
            hlrCmd = hlrCmd.replaceAll(SOG_VM_COMMAND_REPLACEMENT_MSISDN, newMsisdn);        
        }
        return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }   
 
    
    
    public ExternalProvisionResult resetPassword(Context ctx, Subscriber sub,  String password)
    {
        final String msg = "Change voicemail password for subscriber:" + sub.getId(); 
        String hlrCmd = ""; 
        CrmVmPlan plan = VoicemailSupport.getCrmVmPlan(ctx, sub); 
        if (plan == null ){
            handleFailure(ctx, msg + "fail,  VM plan not found  for sub" + sub.getId(), null);
            return new ExternalProvisionResult(RESULT_FAIL_NOT_VM_SERVICE_FOUND_FOR_SUBSCRIBER, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            hlrCmd = plan.getPostpaidPasswordCmd(); 
        }
        else 
        {
            hlrCmd = plan.getPrepaidPasswordCmd(); 
        }
 
        String passString = "" ; 
        if ( password != null )
        {
            passString = password.substring(password.length()-4); 
        }
        
        if ( hlrCmd != null)
        {    
            hlrCmd = hlrCmd.replaceAll(SOG_VM_COMMAND_REPLACEMENT_PASSWORD, passString);
        }
        return this.executeCmd(ctx,  hlrCmd, sub, msg); 
    }
    
    
    
    private void handleFailure(Context ctx, String msg, Throwable exception)
    {
        new MajorLogMsg(this,msg,exception).log(ctx);
     }
    

    private ExternalProvisionResult executeCmd(
            final Context ctx,  
            final String hlrCmd, 
            final Subscriber sub, 
            final String msg
            )
    {
        int ret = RESULT_SUCCESS;

        final short hlrId = getHlrId(ctx); 
        
        if(hlrCmd == null || hlrCmd.trim().length()<1)
        {
            ret = RESULT_FAIL_VOID_COMMAND; 
            handleFailure(ctx, msg + "fail, no VM deactivation command defind", null);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;  
        }
        
        final CrmHlrServicePipelineImpl hlrService = CrmHlrServicePipelineImpl.instance();

 
        try 
        {
        	
        	String request = CommonProvisionAgentBase.replaceHLRCommand(ctx,  hlrCmd, sub, null, null);
             hlrService.process(ctx, hlrId, request);
        
        } catch(Throwable e)
        {
            ret = RESULT_FAIL_TIMEOUT; 
            handleFailure(ctx, msg + "fail, Timeout while wait for reply from VM", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;              
        }

        return new ExternalProvisionResult(ret,0); 

    }
    
    
    public short getHlrId(Context ctx)
    {
        final VoicemailServiceConfig config = (VoicemailServiceConfig)ctx.get(VoicemailServiceConfig.class);
                
        return config.getHlrId();   
    }
    
    public int mapResultCode(int orig)
    {
        
        switch (orig)
        {
            case 0:
                return RESULT_SUCCESS; 
            default : 
                return RESULT_FAIL_UNKNOWN; 
                
        }
    }
        
    public void connect()
    throws AgentException 
    {
    
    }
    
    public void disconnect()
    throws AgentException
    {
  
    }
    
    public void reconnect()
    throws AgentException
    {
    }
    
    public void acquireNewConnection() throws AgentException
    {
        
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getRemoteInfo()
     */
    public String getRemoteInfo()
    {
        return "HLR ID: " + getHlrId(getContext());
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceName()
     */
    public String getName()
    {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
    	return true; 
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAware#getContext()
     */
    public Context getContext()
    {
        return ctx_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAware#setContext(com.redknee.framework.xhome.context.Context)
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(this.getRemoteInfo(), false);
    }

    @Override
    public String getServiceStatus()
    {
        return "NA";
    }
}