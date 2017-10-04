package com.trilogy.app.crm.voicemail.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.agent.Install;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.VMPlan;
import com.trilogy.app.crm.bean.VMPlanHome;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.home.VMPlanIdMpathixServiceHome;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.support.VoicemailSupport;
import com.trilogy.driver.voicemail.mpathix.MpathixAllPlansTask;
import com.trilogy.driver.voicemail.mpathix.MpathixConstants;
import com.trilogy.driver.voicemail.mpathix.MpathixParameter;
import com.trilogy.driver.voicemail.mpathix.MpathixSelTask;
import com.trilogy.driver.voicemail.mpathix.MpathixTask;
import com.trilogy.driver.voicemail.mpathix.xgen.MpathixConnectionInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.service.voicemail.mpathix.MpathixService;

public class MpathixClient extends AbstractVoiceMailClient implements RemoteServiceStatus
{

    private static final String SERVICE_NAME = "Mpathix";
    private static final String SERVICE_DESCRIPTION = "Client for Mpathix Voicemail services";
    
    public MpathixClient(Context ctx)
    {
        this.ctx = ctx; 
        init(ctx); 
    }
    
    
    public void init(Context ctx)
    {
        try
        {
            ctx.put(VMPlanHome.class,new VMPlanIdMpathixServiceHome(ctx));
            
            final MpathixConnectionInfo info = VoicemailSupport.getConnInfo(ctx);
            vmService = new MpathixService(ctx, info);
            vmService.startConnection();
         }
        catch (final Exception e)
        {
            new MajorLogMsg(this,
                "Could not get the voicmeail service, please check the Mpathix connection configuration", e).log(ctx);
            Install.failAndContinue(ctx, "MpathixService", e);
        }
    
    }
    
    public ExternalProvisionResult resetPassword(Context ctx, Subscriber sub,  String password)
    {
        int ret = RESULT_SUCCESS; 
        final String msg = "Deactivate voicemail profile for subscriber:" + sub.getId(); 
      
        MpathixTask returnTask = null;

        MpathixService mpathixVMService = null; 
        
        try {
            mpathixVMService = getVMService(); 
        } 
        catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;  
        }
  
        // Get the configuration
        VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String paramList = "The parameters passed to service(to deactivate account):: msisdn(userid):" + 
            sub.getMSISDN()+ 
            "waitOnTask:" + 
            configBean.getModifyUserWait() + 
            " timeoutInterval:" + 
            configBean.getModifyUserTimeOut();
            
            new DebugLogMsg(this, paramList, null).log(ctx);
        }
        
        try
        {
            returnTask = mpathixVMService.updatePassword(sub.getMSISDN(), password, configBean.getModifyUserWait(), configBean.getModifyUserTimeOut());
            ret = handleReturn(ctx, configBean.getModifyUserWait(), returnTask, msg); 
        }
        catch (Throwable thr)
        {
             ret = RESULT_FAIL_UNKNOWN;
             handleFailure(ctx, msg + " failed.", thr);
        }
        
        return new ExternalProvisionResult(ret, returnTask.getErrorCode()); 
    }

    
    public ExternalProvisionResult provision(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service) 
    {
        final String msg = "Provisioning of voicemail service for subscriber:" + sub.getId(); 
        final VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        final int planId = Integer.parseInt(VoicemailSupport.getVoicemailPlanID(ctx, service));
        
        int ret = RESULT_SUCCESS;       
        MpathixService mpathixVMService = null; 
        MpathixTask returnTask = null;
        
        try {
            mpathixVMService = getVMService(); 
        } catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ; 
        }
                 
        unprovision(ctx,sub, service, true, configBean.getDeleteUserTimeOut());
                
         
        if (LogSupport.isDebugEnabled(ctx))
        {    
            final String paramList = "The parameters passed to service:: msisdn(userid):" + sub.getMSISDN()+ " planId:" + planId
            + " waitOnTask:" + configBean.getAddUserWait() + " timeoutInterval:" + configBean.getAddUserTimeOut();
            new DebugLogMsg(this, "Starting provisioning of voicemail service for subscriber:" + sub.getId()
                    + " " + paramList, null).log(ctx);
        }
        
            // fire the add user task
        try
        {
             returnTask = mpathixVMService.addUser(sub.getMSISDN(), sub.getMSISDN(), planId, configBean.getAddUserWait(), configBean
                  .getAddUserTimeOut());
             ret = handleReturn(ctx, configBean.getAddUserWait(), returnTask, msg); 
        }
        catch (Throwable thr)
        {
             ret = RESULT_FAIL_UNKNOWN;
             handleFailure(ctx, msg + " failed.", thr);
        }
        
        return new ExternalProvisionResult(ret, returnTask.getErrorCode()); 
    }
    
    
    public ExternalProvisionResult unprovision(Context ctx, Subscriber subscriber, ServiceBase service)
    {   
        final VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        return unprovision(ctx, subscriber, service, configBean.isDeleteUserWait(), configBean.getDeleteUserTimeOut()); 
    }
    
    protected ExternalProvisionResult unprovision(Context ctx, Subscriber subscriber, ServiceBase service, boolean waitForReply, long waitTimeOut)
    
    {
        int ret = RESULT_SUCCESS; 
        final String msg = "Unprovisioning of voicemail service for subscriber:" + subscriber.getId(); 
      
        MpathixTask returnTask = null;

        MpathixService mpathixVMService = null; 
        
        try {
            mpathixVMService = getVMService(); 
        } 
        catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;  
        }
  
                 
        if (LogSupport.isDebugEnabled(ctx))
        {    
            String paramList = "The parameters passed to service:: msisdn(userid):" + subscriber.getMSISDN() + " waitOnTask:"
            + waitForReply + " timeoutInterval:" + waitTimeOut;
        
           new DebugLogMsg(this, "Starting unprovisioning of voicemail service for subscriber:" + subscriber.getId()
                    + " " + paramList, null).log(ctx);
        }
        
        boolean waitOnDeleteTask = true;

        // fire the delete user task and make it wait until finishes delete
        try
        {
            returnTask = mpathixVMService.deleteUser(subscriber.getMSISDN(), waitOnDeleteTask,waitTimeOut);
            ret = handleReturn(ctx, waitForReply, returnTask, msg); 
        }
        catch (Throwable thr)
        {
            ret = RESULT_FAIL_UNKNOWN; 
            handleFailure(ctx, msg + " failed.", thr);
        }
         
           
        return new ExternalProvisionResult(ret, returnTask.getErrorCode());  
    }

    
    
    public ExternalProvisionResult deactivate(Context ctx, Subscriber sub, ServiceBase service) 
    {

        int ret = RESULT_SUCCESS; 
        final String msg = "Deactivate voicemail profile for subscriber:" + sub.getId(); 
      
        MpathixTask returnTask = null;

        MpathixService mpathixVMService = null; 
        
        try {
            mpathixVMService = getVMService(); 
        } 
        catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;  
        }
  
        // Get the configuration
        VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String paramList = "The parameters passed to service(to deactivate account):: msisdn(userid):" + 
            sub.getMSISDN()+ 
            "waitOnTask:" + 
            configBean.getModifyUserWait() + 
            " timeoutInterval:" + 
            configBean.getModifyUserTimeOut();
            
            new DebugLogMsg(this, paramList, null).log(ctx);
        }
        
        try
        {
            returnTask = mpathixVMService.deactivateAccount(sub.getMSISDN(), configBean.getModifyUserWait(), configBean
                    .getModifyUserTimeOut());
            ret = handleReturn(ctx, configBean.getModifyUserWait(), returnTask, msg); 
        }
        catch (Throwable thr)
        {
             ret = RESULT_FAIL_UNKNOWN;
             handleFailure(ctx, msg + " failed.", thr);
        }
        
        return new ExternalProvisionResult(ret, returnTask.getErrorCode());  
    }

    
    public ExternalProvisionResult activate(
            final Context ctx, 
            final Subscriber sub, 
            final ServiceBase service) 
    {

        int ret = RESULT_SUCCESS; 
        final String msg = "Activate voicemail profile for subscriber:" + sub.getId(); 
      
        MpathixTask returnTask = null;

        MpathixService mpathixVMService = null; 
        
        try {
            mpathixVMService = getVMService(); 
        } 
        catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ;  
        }
  
        // Get the configuration
        VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
         
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String   paramList = "The parameters passed to service(to activate account):: msisdn(userid):" + 
            sub.getMSISDN() + 
            "waitOnTask:" + 
            configBean.getModifyUserWait() + 
            " timeoutInterval:" + configBean.getModifyUserTimeOut();
           new DebugLogMsg(this, paramList, null).log(ctx);
        }
        
        try
        {
            returnTask = mpathixVMService.activateAccount(sub.getMSISDN(), configBean.getModifyUserWait(), configBean
                    .getModifyUserTimeOut());
            ret = handleReturn(ctx, configBean.getModifyUserWait(), returnTask, msg); 
        }
        catch (Throwable thr)
        {
             ret = RESULT_FAIL_UNKNOWN;
             handleFailure(ctx, msg + " failed.", thr);
        }
        
        return new ExternalProvisionResult(ret, returnTask.getErrorCode());  
    }  
    
    
    public ExternalProvisionResult changeMsisdn(
            final Context ctx, 
            final Subscriber sub, 
            final String newMsisdn) 
    {

        int ret = RESULT_SUCCESS; 
        final String msg = "Change voicemail msisdn for subscriber:" + sub.getId(); 
      
        MpathixTask returnTask = null;

        MpathixService mpathixVMService = null; 
        
        try {
            mpathixVMService = getVMService(); 
        } 
        catch (Exception e )    
        {
            ret = RESULT_FAIL_NO_VM_SERVER_CONNECTED; 
            handleFailure(ctx, msg + "fail, no VM Connection", e);
            return new ExternalProvisionResult(ret, ORIG_VM_RETURN_UNKNOWN) ; 
        }
  
        // Get the configuration
        VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
        List mvmParamList = new ArrayList();
        MpathixParameter mpParam = new MpathixParameter();
        mpParam.key = MpathixConstants.NEW_USER_ID;
        mpParam.value = newMsisdn;
        mvmParamList.add(mpParam);
         
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String paramList = "The parameters passed to service:: msisdn(userid):" + sub.getMSISDN() + " newUserID:" + newMsisdn
            + " waitOnTask:" + configBean.getModifyUserWait() + " timeoutInterval:"
            + configBean.getModifyUserTimeOut() + " paramList:" + mvmParamList;
           new DebugLogMsg(this, paramList, null).log(ctx);
        }
        
        try
        {
            returnTask = mpathixVMService.modifyUser(sub.getMSISDN(), mvmParamList, configBean.getModifyUserWait(), configBean
                    .getModifyUserTimeOut());
            ret = handleReturn(ctx, configBean.getModifyUserWait(), returnTask, msg); 
        }
        catch (Throwable thr)
        {
             ret = RESULT_FAIL_UNKNOWN;
             handleFailure(ctx, msg + " failed.", thr);
        }
        
        return new ExternalProvisionResult(ret, returnTask.getErrorCode()); 
    }   
    
    
    public List getAllVMPlans(Context ctx) throws HomeException
    {
        MpathixService vmService = null; 
        
        try {
            vmService = getVMService(); 
        } 
        catch (Exception e )    
        {
            HomeException he = new HomeException(e.getMessage()); 
            he.initCause(e);
            throw he; 
        }

        
        VoicemailServiceConfig vmConfig = VoicemailSupport.getVMConfig(ctx);
        
        MpathixAllPlansTask selAllTask = (MpathixAllPlansTask) vmService.selectAllPlans(true, vmConfig
                .getSelectAllTimeOut());
        
        Set plans = selAllTask.getPlanIDs();
        if (plans != null && plans.size() > 0)
        {
            String planIDString = null;
            VMPlan vmPlanBean = null;
            List planList = new ArrayList();
            Iterator planIdIterator = plans.iterator();
            while (planIdIterator.hasNext())
            {
                planIDString = String.valueOf(planIdIterator.next());
                vmPlanBean = new VMPlan();
                vmPlanBean.setVmPlanId(planIDString);
                vmPlanBean.setDescription(selAllTask.getPlanDesc(planIDString));
                planList.add(vmPlanBean);
            }
            return planList;
        }
        else
        {
            new MajorLogMsg(
                    this,
                    "MpathixService returned no planIDs, please check whether connection to Voicemail server is alive or not",
                    null).log(ctx);
            return null;
        }
    }


    public Object getVMPlan(Context ctx, String planId) throws HomeException
    {
        MpathixService vmService = null; 
        
        try {
            vmService = getVMService(); 
        } 
        catch (Exception e )    
        {
            HomeException he = new HomeException(e.getMessage()); 
            he.initCause(e);
            throw he; 
        }

        VoicemailServiceConfig vmConfig = VoicemailSupport.getVMConfig(ctx);
        MpathixSelTask selTask = null;
        try
        {
            selTask = (MpathixSelTask) vmService.selectPlan(Integer.parseInt(planId), true, vmConfig
                    .getSelectPlanTimeOut());
        }
        catch (NumberFormatException nfe)
        {
            String msg = "The planId stored is not an integer, we can query to Voicemail service only with integer planIds, the planId supplied:"
                    + planId;
            new MajorLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);
        }
        if (selTask == null)
        {
            String msg = "VM returned null task for the planId :" + planId
                    + "Please check if the plan with given planId exists in Voicemail";
            new MajorLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);
        }
        String planDesc = selTask.getValue(MpathixConstants.PLAN_DESC);
        if (planDesc != null)
        {
            VMPlan plan = new VMPlan();
            plan.setVmPlanId(planId);
            plan.setDescription(planDesc);
            return plan;
        }
        else
        {
            new MajorLogMsg(this, "MpathixService returned no planID description for planId" + planId
                    + ", please check whether connection to Voicemail server is alive or not", null).log(ctx);
            return null;
        }
    }
    
    
    
    private int handleReturn(Context ctx, boolean waitFlag,  MpathixTask returnTask, String msg)
    {
        
        int ret = RESULT_SUCCESS; 
        
        if (waitFlag)
        {
            if (!returnTask.isProcessed())
            {
                ret = RESULT_FAIL_TIMEOUT; 
                handleFailure(ctx, msg + ", failed due to time out", null); 
            }
            else
            {
                if (returnTask.getErrorCode() != MpathixTask.SUCCESS)
                {
                    ret = mapResultCode(returnTask.getErrorCode()); 
                    handleFailure(ctx, msg + " failed, return from VM = " + returnTask.getErrorCode(), null); 
                 
                }
                else
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {    
                        new DebugLogMsg(this, msg + " was sucessfull " + returnTask.getErrorCode(), null).log(ctx);
                    }   
                }
            }
        } else {
            if (LogSupport.isDebugEnabled(ctx))
            {    
                new DebugLogMsg(this, msg + ", command is send out, no waiting for reply ", null).log(ctx);
            }   
        }
        
          return ret; 
    }
    
    
    private void handleFailure(Context ctx, String msg, Throwable exception)
    {
        new MajorLogMsg(this,msg,exception).log(ctx);
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
    
    private synchronized MpathixService getVMService() 
    throws AgentException 
    {
         if (vmService == null)
             throw new AgentException("System error: MpathixService is null");
         if (!vmService.isConnected2VM())
         {
             try 
             {
                 vmService.startConnection();
             
             } catch ( Throwable t)
             {
                 AgentException e = new AgentException("Not connected to voicemail service");
                 e.initCause(t); 
                 throw e; 
             }   
          }
         return vmService; 
    }
    
    
    public synchronized void connect()
    throws AgentException 
    {
        if (!vmService.isConnected2VM())
        {
            try 
            {
                vmService.startConnection();
            
            } catch ( Throwable t)
            {
                AgentException e = new AgentException("Failed to connect to voicemail service");
                e.initCause(t); 
                throw e; 
            }   
         }
   
    }
    
    public synchronized void disconnect()
    throws AgentException
    {
        if (vmService.isConnected2VM())
        {
            try 
            {
                vmService.closeConnection();
            
            } catch ( Throwable t)
            {
                AgentException e = new AgentException("fail to close the voicemail connection");
                e.initCause(t); 
                throw e; 
            }   
         }

    }
    
    public synchronized void reconnect()
    throws AgentException
    {
        disconnect(); 
        connect(); 
    }

    public synchronized void acquireNewConnection()
    throws AgentException
    {
        disconnect(); 
        final MpathixConnectionInfo info = VoicemailSupport.getConnInfo(getContext());
        vmService = new MpathixService(getContext(), info);
        connect(); 
        
    }

    public Context getContext() {
        return ctx;
    }


    public void setContext(Context ctx) {
        this.ctx = ctx;
    } 

    public String getName()
    {
        return SERVICE_NAME;
    }


    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }
    

    public String getRemoteInfo()
    {
       MpathixService vmService = null; 
        
        try {
            vmService = getVMService(); 
        } 
        catch (Exception e )    
        {
            return ""; 
        }

        if (vmService != null)
        {    
              return vmService.getConnectionInfo();
        }
        else
        {
              MpathixConnectionInfo connInfo = VoicemailSupport.getConnInfo(getContext());
              if (connInfo == null)
                   return "";
               else
                   return connInfo.getHostname() + ":" + connInfo.getPort();
        }
        

    }

    
    public boolean isAlive()
    {
        if (vmService!=null)
        {
            return vmService.isConnected2VM(); 
        }
        else
        {
            return false;
        }
    }
    
    private MpathixService vmService; 
    Context ctx;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(this.getRemoteInfo(), isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
    
}
