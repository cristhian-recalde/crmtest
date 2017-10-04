/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.ff;

import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterSetHolder;

import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlan;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanHolder;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanListHolder;
import com.trilogy.app.ff.ecare.rmi.TrBooleanHolder;
import com.trilogy.app.ff.ecare.rmi.TrCug;
import com.trilogy.app.ff.ecare.rmi.TrCugHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugIdHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplate;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateListHolder;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.app.ff.ecare.rmi.TrPlp;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2Holder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileHolder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileViewHolder;

/**
 * @author margarita.alp@redknee.com
 */
public class FFECarePMDecorator extends FFECareRmiServiceProxy implements ContextAware
{
    public FFECarePMDecorator(final Context ctx, final FFECareRmiService delegate)
    {
        setContext(ctx);
        setDelegate(delegate);
    }

    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(final Context ctx)
    {
        ctx_ = ctx;
    }
    

    @Override
    public int updatePLPForSub(final String s, final long l, final TrPeerMsisdn[] atrpeermsisdn) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updatePLPForSub");
        try
        {
            return super.updatePLPForSub(s, l, atrpeermsisdn);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int deletePLPForSub(final String s) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deletePLPForSub");
        try
        {
            return super.deletePLPForSub(s);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int getSub(final String s, final TrSubscriberProfileHolder profileHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSub");
        try
        {
            return super.getSub(s, profileHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int getSubWithDetails(final String s, final TrSubscriberProfileHolder profileHolder,
            final TrPlpListHolder plpListHolder, final TrCugListHolder cugListHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSubWithDetails");
        try
        {
            return super.getSubWithDetails(s, profileHolder, plpListHolder, cugListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int createPLP(final TrPlp plp, final TrPlpHolder plpHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "createPLP");
        try
        {
            return super.createPLP(plp, plpHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int updatePLP(final TrPlp plp) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updatePLP");
        try
        {
            return super.updatePLP(plp);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int deletePLP(final long id) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deletePLP");
        try
        {
            return super.deletePLP(id);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int getPLP(final long id, final TrPlpHolder plpHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getPLP");
        try
        {
            return super.getPLP(id, plpHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int getPLPByName(final String name, final TrPlpListHolder plpListHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getPLPByName");
        try
        {
            return super.getPLPByName(name, plpListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int getPLPBySPID(final int spid, final TrPlpListHolder plpListHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getPLPBySPID");
        try
        {
            return super.getPLPBySPID(spid, plpListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int createCUG(final TrCug cug, final TrCugHolder cugHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "createCUG");
        try
        {
            return super.createCUG(cug, cugHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int updateCUG(final TrCug cug) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateCUG");
        try
        {
            return super.updateCUG(cug);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int cugRemoveSub(final long l, final String s) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "cugRemoveSub");
        try
        {
            return super.cugRemoveSub(l, s);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int cugAddSub(final long l, final String s) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "cugAddSub");
        try
        {
            return super.cugAddSub(l, s);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int deleteCUG(final long l) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteCUG");
        try
        {
            return super.deleteCUG(l);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    @Deprecated
    public int getCUG(final long l, final TrCugHolder cugHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getCUG");
        try
        {
            return super.getCUG(l, cugHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }

    @Override
    @Deprecated
    public int getCUGByName(final String name, final TrCugListHolder cugListHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getCUGByName");
        try
        {
            return super.getCUGByName(name, cugListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    public int getCUGBySPID(final int spid, final TrCugListHolder cugListHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getCUGBySPID");
        try
        {
            return super.getCUGBySPID(spid, cugListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    public int createBirthdayPlan(final TrBirthdayPlan bdayPlan, final TrBirthdayPlanHolder newBdayPlan)
        throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "createBirthdayPlanWebControl");
        try
        {
            return getDelegate().createBirthdayPlan(bdayPlan, newBdayPlan);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    public int updateBirthdayPlan(final TrBirthdayPlan bdayPlan) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateBirthdayPlan");
        try
        {
            return getDelegate().updateBirthdayPlan(bdayPlan);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    @Deprecated
    public int deleteBirthdayPlan(final long bDayId) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteBirthdayPlan");
        try
        {
            return getDelegate().deleteBirthdayPlan(bDayId);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    @Deprecated
    public int getBirthdayPlanByName(final String bdayPlanName, final TrBirthdayPlanListHolder birthdayPlanListHolder)
        throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getBirthdayPlanByName");
        try
        {
            return getDelegate().getBirthdayPlanByName(bdayPlanName, birthdayPlanListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    public int getBirthdayPlanBySPID(final int spid, final TrBirthdayPlanListHolder birthdayPlanListHolder)
        throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getBirthdayPlanBySPID");
        try
        {
            return getDelegate().getBirthdayPlanBySPID(spid, birthdayPlanListHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    @Deprecated
    public int updateBirthdayPlanForSub(final String ownerMsisdn, final long birthdayPlanId, final int dayOfMonth,
            final int month, final String subTimeZone) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateBirthdayPlanForSub");
        try
        {
            return getDelegate().updateBirthdayPlanForSub(ownerMsisdn, birthdayPlanId, dayOfMonth, month, subTimeZone);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    @Deprecated
    public int deleteBirthdayPlanForSub(final String ownerMsisdn) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteBirthdayPlanForSub");
        try
        {
            return getDelegate().deleteBirthdayPlanForSub(ownerMsisdn);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    @Deprecated
    public int getBirthdayPlan(final long bDayPlanId, final TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getBirthdayPlan");
        try
        {
            return getDelegate().getBirthdayPlan(bDayPlanId, birthdayPlan);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    
    
    @Override
    public int getBirthdayPlanForSub(final String ownerMsidn,
            final TrSubscriberProfileViewHolder trSubscriberProfileViewHolder) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getBirthdayPlanForSub");
        try
        {
            return getDelegate().getBirthdayPlanForSub(ownerMsidn, trSubscriberProfileViewHolder);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int addSubsToCUG(long arg0, TrPeerMsisdn[] arg1) throws RemoteException
    {
        return getDelegate().addSubsToCUG(arg0, arg1);
    }
    

    @Override
    public int createCUGAssociation(int arg0, long arg1, TrPeerMsisdn arg2, TrCugIdHolder arg3) throws RemoteException
    {
        return getDelegate().createCUGAssociation(arg0, arg1, arg2, arg3);
    }

    
    @Override
    public int createCUGTemplate(int arg0, TrCugTemplate arg1, TrCugTemplateHolder arg2) throws RemoteException
    {
        return getDelegate().createCUGTemplate(arg0, arg1, arg2);
    }
    

    @Override
    public int deleteCUGAssociation(int arg0, long arg1) throws RemoteException
    {
        return getDelegate().deleteCUGAssociation(arg0, arg1);
    }
    

    @Override
    public int deleteCUGTemplate(int arg0, long arg1) throws RemoteException
    {
        return getDelegate().deleteCUGTemplate(arg0, arg1);
    }
    

    @Override
    public int deletePLPForSub(String arg0, long arg1) throws RemoteException
    {

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deletePLPForSub");
        try
        {
            return getDelegate().deletePLPForSub(arg0, arg1);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int getCUGTemplate(int arg0, long arg1, TrCugTemplateHolder arg2) throws RemoteException
    {
        return getDelegate().getCUGTemplate(arg0, arg1, arg2);
    }
    
    
    @Override
    @Deprecated
    public int getCUGTemplateByName(String arg0, TrCugTemplateListHolder arg1) throws RemoteException
    {
        return getDelegate().getCUGTemplateByName(arg0, arg1);
    }
    

    @Override
    public int getCUGTemplateBySPID(int arg0, TrCugTemplateListHolder arg1) throws RemoteException
    {
        return getDelegate().getCUGTemplateBySPID(arg0, arg1);
    }
    

    @Override
    public int getSub(String arg0, TrSubscriberProfile2Holder arg1) throws RemoteException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSub");
        try
        {
            return getDelegate().getSub(arg0, arg1);
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }
    

    @Override
    public int getSubWithDetails(String arg0, TrSubscriberProfile2Holder arg1, TrPlpListHolder arg2,
            TrCugListHolder arg3) throws RemoteException
    {
        return getDelegate().getSubWithDetails(arg0, arg1, arg2, arg3);
    }
    
    
    @Override
    @Deprecated
    public int isCugTemplateInUse(long arg0, TrBooleanHolder arg1) throws RemoteException
    {
        return getDelegate().isCugTemplateInUse(arg0, arg1);
    }
    

    @Override
    public int removeSubsFromCUG(long arg0, TrPeerMsisdn[] arg1) throws RemoteException
    {
        return getDelegate().removeSubsFromCUG(arg0, arg1);
    }
    

    @Override
    public int updateCUGAssociation(int arg0, long arg1, long arg2) throws RemoteException
    {
        return getDelegate().updateCUGAssociation(arg0, arg1, arg2);
    }
    

    @Override
    public int updateCUGNotifyMsisdn(long arg0, String arg1) throws RemoteException
    {
        return getDelegate().updateCUGNotifyMsisdn(arg0, arg1);
    }
    
    @Override
    public int updateCUGNotifyMsisdnWithSpid(int arg0, long arg1, String arg2) throws RemoteException
    {
        return getDelegate().updateCUGNotifyMsisdnWithSpid(arg0, arg1, arg2);
    }    

    @Override
    public int updateCUGTemplate(int arg0, TrCugTemplate arg1) throws RemoteException
    {
        return getDelegate().updateCUGTemplate(arg0, arg1);
    }


    @Override
    public int deletePLP(int spId, long plpId) throws RemoteException
    {
        return getDelegate().deletePLP(spId, plpId);
    }


    @Override
    public int getPLP(int spId, long plpId, TrPlpHolder plp) throws RemoteException
    {
        return getDelegate().getPLP(spId, plpId, plp);
    }


    @Override
    public int getPLPByName(int spId, String plpName, TrPlpListHolder plpListHolder) throws RemoteException
    {
        return getDelegate().getPLPByName(spId, plpName, plpListHolder);
    }


    @Override
    public int deleteBirthdayPlan(int spId, long bDayId) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlan(spId, bDayId);
    }


    @Override
    public int getBirthdayPlanByName(int spId, String bdayPlanName, TrBirthdayPlanListHolder birthdayPlanListHolder)
            throws RemoteException
    {
        return getDelegate().getBirthdayPlanByName(spId, bdayPlanName, birthdayPlanListHolder);
    }


    @Override
    public int deleteBirthdayPlanForSub(int spId, String ownerMsisdn) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlanForSub(spId, ownerMsisdn);
    }


    @Override
    public int getBirthdayPlan(int spId, long bDayPlanId, TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        return getDelegate().getBirthdayPlan(spId, bDayPlanId, birthdayPlan);
    }


    @Override
    public int deleteCUG(int spId, long cugId) throws RemoteException
    {
        return getDelegate().deleteCUG(spId, cugId);
    }


    @Override
    public int getCUG(int spId, long cugId, TrCugHolder cug) throws RemoteException
    {
        return getDelegate().getCUG(spId, cugId, cug);
    }


    @Override
    public int getCUGByName(int spId, String cugName, TrCugListHolder cugListHolder) throws RemoteException
    {
        return getDelegate().getCUGByName(spId, cugName, cugListHolder);
    }


    @Override
    public int getCUGTemplateByName(int spId, String cugTemplateName, TrCugTemplateListHolder cugTemplateListHolder)
            throws RemoteException
    {
        return getDelegate().getCUGTemplateByName(spId, cugTemplateName, cugTemplateListHolder);
    }


    @Override
    public int addSubsToCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().addSubsToCUG(spId, cugId, msisdns, inParams, outParams);
    }


    @Override
    public int removeSubsFromCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().removeSubsFromCUG(spId, cugId, msisdns, inParams, outParams);
    }


    @Override
    public int isCugTemplateInUse(int spId, long cugTemplateId, TrBooleanHolder resultHolder) throws RemoteException
    {
        return getDelegate().isCugTemplateInUse(spId, cugTemplateId, resultHolder);
    }


    @Override
    public int updateSubForCug(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inputParam)
            throws RemoteException
    {
        return getDelegate().updateSubForCug(spId, cugId, msisdns, inputParam);
    }


    @Override
    public int updateCUGInstance(int spId, long cugId, Parameter[] inputParam) throws RemoteException
    {
        return getDelegate().updateCUGInstance(spId, cugId, inputParam);
    }  
    
    private static final String PM_MODULE = FFECarePMDecorator.class.getName();
    
    private Context ctx_;
}
