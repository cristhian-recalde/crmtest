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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.ff;

import java.rmi.RemoteException;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.support.ExternalAppSupport;
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
import com.trilogy.app.ff.ecare.rmi.TrFnFProfileHolder;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.app.ff.ecare.rmi.TrPlp;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2Holder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileHolder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileViewHolder;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterSetHolder;

/**
 * FFECareRmiService provisioning client.
 *
 * @author Marcio Marques
 * @author Mangaraj Sahoo
 */
public class FFECareRmiServiceClient extends AbstractCrmClient<FFECareRmiService> implements FFECareRmiService
{

    private static final String SERVICE_NAME = "FFECareServiceClient";
    private static final String SERVICE_DESCRIPTION = "RMI client for Friends & Family E-Care services";
    private static final Class<FFECareRmiService> RMI_CLIENT_KEY = FFECareRmiService.class;


    public FFECareRmiServiceClient(Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, RMI_CLIENT_KEY);
    }


    public FFECareRmiServiceClient(Context ctx, final FFECareRmiService delegate)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESCRIPTION, RMI_CLIENT_KEY);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#createCUG(com.redknee.app.ff.ecare.rmi.TrCug, com.redknee.app.ff.ecare.rmi.TrCugHolder)
     */
    public int createCUG(TrCug cug, TrCugHolder newCug) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.createCUG(cug, newCug);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#createPLP(com.redknee.app.ff.ecare.rmi.TrPlp, com.redknee.app.ff.ecare.rmi.TrPlpHolder)
     */
    public int createPLP(TrPlp plp, TrPlpHolder newPlp) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.createPLP(plp, newPlp);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int updateCUGState(long cugId, int newState) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGState(cugId, newState);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#cugAddSub(long, java.lang.String)
     */
    public int cugAddSub(long cugId, String msisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.cugAddSub(cugId, msisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#cugRemoveSub(long, java.lang.String)
     */
    public int cugRemoveSub(long cugId, String msisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.cugRemoveSub(cugId, msisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deleteCUG(long)
     */
    @Deprecated
    public int deleteCUG(long cugId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteCUG(cugId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLP(long)
     */
    @Deprecated
    public int deletePLP(long plpId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deletePLP(plpId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLPForSub(java.lang.String)
     */
    @Deprecated
    public int deletePLPForSub(String ownerMsisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deletePLPForSub(ownerMsisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLPForSub(java.lang.String, long)
     */
    @Deprecated
    public int deletePLPForSub(String ownerMsisdn, long plpId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deletePLPForSub(ownerMsisdn, plpId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUG(long, com.redknee.app.ff.ecare.rmi.TrCugHolder)
     */
    @Deprecated
    public int getCUG(long cugId, TrCugHolder cug) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUG(cugId, cug);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGByName(java.lang.String, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    @Deprecated
    public int getCUGByName(String name, TrCugListHolder cugList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGByName(name, cugList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGBySPID(int, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getCUGBySPID(int spid, TrCugListHolder cugList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGBySPID(spid, cugList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLP(long, com.redknee.app.ff.ecare.rmi.TrPlpHolder)
     */
    @Deprecated
    public int getPLP(long plpId, TrPlpHolder plp) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getPLP(plpId, plp);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLPByName(java.lang.String, com.redknee.app.ff.ecare.rmi.TrPlpListHolder)
     */
    @Deprecated
    public int getPLPByName(String name, TrPlpListHolder plpList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getPLPByName(name, plpList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLPBySPID(int, com.redknee.app.ff.ecare.rmi.TrPlpListHolder)
     */
    public int getPLPBySPID(int spid, TrPlpListHolder plpList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getPLPBySPID(spid, plpList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSub(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfileHolder)
     */
    public int getSub(String ownerMsisdn, TrSubscriberProfileHolder subProfile) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getSub(ownerMsisdn, subProfile);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSub(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfile2Holder)
     */
    public int getSub(String ownerMsisdn, TrSubscriberProfile2Holder subProfile) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getSub(ownerMsisdn, subProfile);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSubWithDetails(java.lang.String,
     * com.redknee.app.ff.ecare.rmi.TrSubscriberProfileHolder,
     * com.redknee.app.ff.ecare.rmi.TrPlpListHolder,
     * com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getSubWithDetails(String ownerMsisdn, TrSubscriberProfileHolder subProfile, TrPlpListHolder plpList,
            TrCugListHolder cugList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getSubWithDetails(ownerMsisdn, subProfile, plpList, cugList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSubWithDetails(java.lang.String,
     * com.redknee.app.ff.ecare.rmi.TrSubscriberProfile2Holder,
     * com.redknee.app.ff.ecare.rmi.TrPlpListHolder,
     * com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getSubWithDetails(String ownerMsisdn, TrSubscriberProfile2Holder subProfile, TrPlpListHolder plpList,
            TrCugListHolder cugList) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getSubWithDetails(ownerMsisdn, subProfile, plpList, cugList);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#updateCUG(com.redknee.app.ff.ecare.rmi.TrCug)
     */
    @Deprecated
    public int updateCUG(TrCug cug) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUG(cug);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#updatePLP(com.redknee.app.ff.ecare.rmi.TrPlp)
     */
    public int updatePLP(TrPlp plp) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updatePLP(plp);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.ff.ecare.rmi.FFECareRmiService#updatePLPForSub(java.lang.String, long, com.redknee.app.ff.ecare.rmi.TrPeerMsisdn[])
     */
    @Deprecated
    public int updatePLPForSub(String ownerMsisdn, long plpId, TrPeerMsisdn[] peerMsisdnArray) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updatePLPForSub(ownerMsisdn, plpId, peerMsisdnArray);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int createBirthdayPlan(final TrBirthdayPlan bdayPlan, final TrBirthdayPlanHolder newBdayPlan)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.createBirthdayPlan(bdayPlan, newBdayPlan);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int updateBirthdayPlan(final TrBirthdayPlan bdayPlan) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateBirthdayPlan(bdayPlan);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int deleteBirthdayPlan(final long bDayId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteBirthdayPlan(bDayId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int getBirthdayPlanByName(final String bdayPlanName, final TrBirthdayPlanListHolder birthdayPlanListHolder)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlanByName(bdayPlanName, birthdayPlanListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int getBirthdayPlanBySPID(final int spid, final TrBirthdayPlanListHolder birthdayPlanListHolder)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlanBySPID(spid, birthdayPlanListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int updateBirthdayPlanForSub(final String ownerMsisdn, final long birthdayPlanId, final int dayOfMonth,
            final int month, final String subTimeZone) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateBirthdayPlanForSub(ownerMsisdn, birthdayPlanId, dayOfMonth, month, subTimeZone);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int updateBirthdayPlanForSub(String ownerMsisdn, long birthdayPlanId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateBirthdayPlanForSub(ownerMsisdn, birthdayPlanId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int deleteBirthdayPlanForSub(final String ownerMsisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteBirthdayPlanForSub(ownerMsisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int getBirthdayPlan(final long bDayPlanId, final TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlan(bDayPlanId, birthdayPlan);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int getBirthdayPlanForSub(final String ownerMsidn,
            final TrSubscriberProfileViewHolder trSubscriberProfileViewHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlanForSub(ownerMsidn, trSubscriberProfileViewHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int createCUGTemplate(int spid, TrCugTemplate cugTemplate, TrCugTemplateHolder newCugTemplate)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.createCUGTemplate(spid, cugTemplate, newCugTemplate);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int updateCUGTemplate(int spid, TrCugTemplate cugTemplate) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGTemplate(spid, cugTemplate);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int deleteCUGTemplate(int spid, long cugTemplateId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteCUGTemplate(spid, cugTemplateId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int getCUGTemplate(int spid, long cugTemplateId, TrCugTemplateHolder cugTemplate) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGTemplate(spid, cugTemplateId, cugTemplate);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }
    

    @Deprecated
    public int getCUGTemplateByName(String cugTemplateName, TrCugTemplateListHolder cugTemplateListHolder)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGTemplateByName(cugTemplateName, cugTemplateListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int getCUGTemplateBySPID(int spid, TrCugTemplateListHolder cugTemplateListHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGTemplateBySPID(spid, cugTemplateListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int createCUGAssociation(int spid, long cugTemplateId, TrPeerMsisdn notifyMsisdn, TrCugIdHolder createdId)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.createCUGAssociation(spid, cugTemplateId, notifyMsisdn, createdId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int updateCUGAssociation(int spid, long cugId, long cugTemplateId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGAssociation(spid, cugId, cugTemplateId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int deleteCUGAssociation(int spid, long cugId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteCUGAssociation(spid, cugId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }

    @Deprecated
    public int addSubsToCUG(long cugId, TrPeerMsisdn[] msisdns) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.addSubsToCUG(cugId, msisdns);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }

    @Deprecated
    public int removeSubsFromCUG(long cugId, TrPeerMsisdn[] msisdns) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.removeSubsFromCUG(cugId, msisdns);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }

    @Deprecated
    public int isCugTemplateInUse(long cugTemplateId, TrBooleanHolder resultHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.isCugTemplateInUse(cugTemplateId, resultHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    public int updateCUGNotifyMsisdn(long cugInstanceId, String notifyMsisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGNotifyMsisdn(cugInstanceId, notifyMsisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int updatePLPForSub(String ownerMsisdn, long plpId, TrPeerMsisdn[] otherMsisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updatePLPForSub(ownerMsisdn, plpId, otherMsisdns, inParams,outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    @Deprecated
    public int updateSubForCug(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inputParam) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateSubForCug(cugId, msisdns, inputParam);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    @Deprecated
    public int updateCUGInstance(long cugId, Parameter[] inputParam) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGInstance(cugId,  inputParam);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int deletePLPForSub(String ownerMsisdn, long plpId, Parameter[] inParams, ParameterSetHolder outParams)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deletePLPForSub(ownerMsisdn, plpId, inParams, outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    @Deprecated
    public int addSubsToCUG(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams, ParameterSetHolder outParams)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.addSubsToCUG(cugId, msisdns, inParams, outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    @Deprecated
    public int removeSubsFromCUG(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams, ParameterSetHolder outParams)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.removeSubsFromCUG(cugId, msisdns, inParams, outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }

    
    @Override
    public int deletePLP(int spId, long plpId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deletePLP(spId, plpId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getPLP(int spId, long plpId, TrPlpHolder plp) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getPLP(spId, plpId, plp);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getPLPByName(int spId, String plpName, TrPlpListHolder plpListHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getPLPByName(spId, plpName, plpListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int deleteBirthdayPlan(int spId, long bDayId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteBirthdayPlan(spId, bDayId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getBirthdayPlanByName(int spId, String bdayPlanName, TrBirthdayPlanListHolder birthdayPlanListHolder)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlanByName(spId, bdayPlanName, birthdayPlanListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int deleteBirthdayPlanForSub(int spId, String ownerMsisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteBirthdayPlanForSub(spId, ownerMsisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getBirthdayPlan(int spId, long bDayPlanId, TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getBirthdayPlan(spId, bDayPlanId, birthdayPlan);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int deleteCUG(int spId, long cugId) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.deleteCUG(spId, cugId);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getCUG(int spId, long cugId, TrCugHolder cug) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUG(spId, cugId, cug);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getCUGByName(int spId, String cugName, TrCugListHolder cugListHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGByName(spId, cugName, cugListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int getCUGTemplateByName(int spId, String cugTemplateName, TrCugTemplateListHolder cugTemplateListHolder)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.getCUGTemplateByName(spId, cugTemplateName, cugTemplateListHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int addSubsToCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.addSubsToCUG(spId, cugId, msisdns, inParams, outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int removeSubsFromCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.removeSubsFromCUG(spId, cugId, msisdns, inParams, outParams);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int isCugTemplateInUse(int spId, long cugTemplateId, TrBooleanHolder resultHolder) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.isCugTemplateInUse(spId, cugTemplateId, resultHolder);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int updateSubForCug(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inputParam)
            throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateSubForCug(spId, cugId, msisdns, inputParam);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


    @Override
    public int updateCUGInstance(int spId, long cugId, Parameter[] inputParam) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGInstance(spId, cugId, inputParam);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }  
    

    public int updateCUGNotifyMsisdnWithSpid(int spid, long cugInstanceId, String notifyMsisdn) throws RemoteException
    {
        FFECareRmiService service = getService();
        if (service != null)
        {
            return service.updateCUGNotifyMsisdnWithSpid(spid, cugInstanceId, notifyMsisdn);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
    }


	@Override
	public int getFnFProfile(String paramString, TrFnFProfileHolder paramTrFnFProfileHolder) throws RemoteException {
		 FFECareRmiService service = getService();
	        if (service != null)
	        {
	            return service.getFnFProfile(paramString, paramTrFnFProfileHolder);
	        }
	        else
	        {
	            return ExternalAppSupport.NO_CONNECTION;
	        }
	}


	@Override
	public int removePlpUserListByPeerMsisdn(String paramString) throws RemoteException {

		FFECareRmiService service = getService();
        if (service != null)
        {
            return service.removePlpUserListByPeerMsisdn(paramString);
        }
        else
        {
            return ExternalAppSupport.NO_CONNECTION;
        }
	}
}
