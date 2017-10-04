/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.CrmVmPlanHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.VoicemailAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension;
import com.trilogy.driver.voicemail.mpathix.xgen.MpathixConnectionInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 17, 2005
 */
public class VoicemailSupport
{

    public static VoicemailServiceConfig getVMConfig(Context ctx)
    {
        VoicemailServiceConfig configBean = (VoicemailServiceConfig) ctx.get(VoicemailServiceConfig.class);
        if (configBean != null)
            return configBean;
        configBean = new VoicemailServiceConfig();
        configBean.setAddUserWait(DEFAULT_WAIT);
        configBean.setAddUserTimeOut(DEFAULT_TIMEOUT);
        configBean.setModifyUserWait(DEFAULT_WAIT);
        configBean.setModifyUserTimeOut(DEFAULT_TIMEOUT);
        configBean.setDeleteUserWait(DEFAULT_WAIT);
        configBean.setDeleteUserTimeOut(DEFAULT_TIMEOUT);
        configBean.setSelectAllTimeOut(DEFAULT_TIMEOUT);
        configBean.setSelectPlanTimeOut(DEFAULT_TIMEOUT);
        return configBean;
    }


 
    public static int getVMPlanId(Context ctx, long auxServiceId) throws HomeException
    {
        VoicemailAuxSvcExtension extension = getRelatedVoicemail(ctx, auxServiceId);
        if (extension == null)
        {
            final String message = "Voicemail info with ID [" + auxServiceId + "] couldn't be found";
            new MajorLogMsg("com.redknee.app.crm.support.VoicemailSupport", message, null).log(ctx);
            throw new HomeException(message);
        }
        try
        {
            return Integer.parseInt(extension.getVmPlanId());
        }
        catch (NumberFormatException nfe)
        {
            final String message = "The vmPlanId is:" + extension.getVmPlanId()
                    + " It should be integer, can not add to voicemail service";
            new MajorLogMsg("com.redknee.app.crm.support.VoicemailSupport", message, nfe).log(ctx);
            throw new HomeException(message);
        }
        catch (Exception e)
        {
            final String message = "Generic exception occured while retrieving vmPlanId for auxiliary service:"
                    + auxServiceId;
            new MajorLogMsg("com.redknee.app.crm.support.VoicemailSupport", message, e).log(ctx);
            throw new HomeException(message);
        }
    }


    public static VoicemailAuxSvcExtension getRelatedVoicemail(Context ctx, long auxServiceId) throws HomeException
    {
        try
        {
            VoicemailAuxSvcExtension extension = HomeSupportHelper.get(ctx).findBean(ctx, VoicemailAuxSvcExtension.class, new EQ(VoicemailAuxSvcExtensionXInfo.AUXILIARY_SERVICE_ID, Long.valueOf(auxServiceId))); 
            return extension;
        }
        catch (final HomeException exception)
        {
            final String message = "Voicemail info with ID [" + auxServiceId + "] couldn't be found";
            throw new HomeException(message);
        }
        catch (Exception e)
        {
            final String message = "Voicemail info with ID [" + auxServiceId
                    + "] couldn't be found; Generic exception occured";
            new MajorLogMsg("com.redknee.app.crm.support.VoicemailSupport", message, e).log(ctx);
            throw new HomeException(e);
        }
    }


    public static MpathixConnectionInfo getConnInfo(Context ctx)
    {
        MpathixConnectionInfo vmConnConfig = (MpathixConnectionInfo)ctx.get(MpathixConnectionInfo.class);
        MpathixConnectionInfo connInfo = new MpathixConnectionInfo();
        connInfo.setHostname(vmConnConfig.getHostname());
        connInfo.setPort(vmConnConfig.getPort());
        connInfo.setConnectionTimeout(vmConnConfig.getConnectionTimeout());
        connInfo.setRetryInterval(vmConnConfig.getRetryInterval());
        connInfo.setLogin(vmConnConfig.getLogin());
        connInfo.setPassword(vmConnConfig.getPassword());
        connInfo.setKeepAlive(vmConnConfig.getKeepAlive());
        connInfo.setKeepAliveInterval(vmConnConfig.getKeepAliveInterval());
        connInfo.setKeepAliveTimeout(vmConnConfig.getKeepAliveTimeout());
        connInfo.setKeepAliveString(vmConnConfig.getKeepAliveString());
        connInfo.setKeepAliveResponse(vmConnConfig.getKeepAliveResponse());
        return connInfo;
    }

    
    
    public static CrmVmPlan getCrmVmPlan(Context ctx, Subscriber sub)
    {
        try {
            Service vmService = SubscriberSupport.getService(ctx, sub, ServiceTypeEnum.VOICEMAIL, VoicemailSupport.class);
            if (vmService != null )
            {
                return getCrmVmPlan(ctx, vmService); 
            }
            else  
            {
                AuxiliaryService auxSrv= SubscriberAuxiliaryServiceSupport.findSubscriberProvisionedAuxiliaryServicesByType(
                    ctx, sub, AuxiliaryServiceTypeEnum.Voicemail); 
            
                if (auxSrv != null )
                {
                    return getCrmVmPlan(ctx, auxSrv); 
                }
            
            }
        } catch (Throwable t)
        {
            new MajorLogMsg(VoicemailSupport.class, "faild to find vm plan for subscriber = " + sub.getId(), t).log(ctx); 
        }
          return null; 
    }
    
    public static String getVoicemailPlanID(Context ctx, ServiceBase service)
    {
        String planID = null;
        
        if (service instanceof com.redknee.app.crm.bean.Service)
        {
            planID = ((com.redknee.app.crm.bean.Service) service).getVmPlanId();
        }
        else if (service instanceof com.redknee.app.crm.bean.ui.Service)
        {
            planID = ((com.redknee.app.crm.bean.ui.Service) service).getVmPlanId();
        }
        else
        {
            long identifier = service.getID();
            try
            {
                VoicemailAuxSvcExtension extension = VoicemailAuxSvcExtension.getVoiceMailAuxSvcExtension(ctx, identifier);
                if (extension!=null)
                {
                    planID = extension.getVmPlanId();
                }
            }
            catch (HomeException e)
            {
                LogSupport
                        .minor(ctx,
                                VoicemailSupport.class,
                                "Unable to retrieve Voicemail Plan ID for auxiliary service " + service + ": "
                                        + e.getMessage(), e);
            }
        }
        return planID;
    }
    
    public static CrmVmPlan getCrmVmPlan(Context ctx, ServiceBase service )
    {
        CrmVmPlan result = null;
        String planID = getVoicemailPlanID(ctx, service);
        
        if (planID!=null)
        {
            Home home = (Home) ctx.get(CrmVmPlanHome.class); 
            
            try 
            {
                result =  (CrmVmPlan) home.find(ctx, Long.valueOf(planID));
                if (result==null)
                {
                    LogSupport.minor(ctx, VoicemailSupport.class, "Unable to find Voicemail Plan with ID = " + planID);
                }
            }catch ( Throwable t)
            {
                new MajorLogMsg(VoicemailSupport.class, "faild to find vm plan for vmplan  = " + planID, t).log(ctx); 
            }
        }
        else
        {
            LogSupport.minor(ctx, VoicemailSupport.class, "Unable to find Voicemail Plan ID for service " + service);
        }
        return result; 
    }
    
    public static long DEFAULT_TIMEOUT = 60000;
    public static boolean DEFAULT_WAIT = true;
}
