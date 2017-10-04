/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.ff;

import java.rmi.RemoteException;

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
import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterSetHolder;

/**
 * Regular proxy implementation for the FF RMI Service
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class FFECareRmiServiceProxy implements FFECareRmiService
{
    private FFECareRmiService delegate_ = null;
    
    public FFECareRmiServiceProxy()
    {        
    }
    

    public FFECareRmiServiceProxy(final FFECareRmiService delegate)
    {
        setDelegate(delegate);
    }

    
    public FFECareRmiService getDelegate()
    {
        return delegate_;
    }
    
    public void setDelegate(FFECareRmiService delegate)
    {
        delegate_ = delegate;
    }    
    
    
    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#createCUG(com.redknee.app.ff.ecare.rmi.TrCug, com.redknee.app.ff.ecare.rmi.TrCugHolder)
     */
    public int createCUG(TrCug cug, TrCugHolder newCug) throws RemoteException
    {
        return getDelegate().createCUG(cug, newCug);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#createPLP(com.redknee.app.ff.ecare.rmi.TrPlp, com.redknee.app.ff.ecare.rmi.TrPlpHolder)
     */
    public int createPLP(TrPlp plp, TrPlpHolder newPlp) throws RemoteException
    {
        return getDelegate().createPLP(plp, newPlp);
    }

    public int updateCUGState(long cugId, int newState)throws RemoteException
    {
        return getDelegate().updateCUGState(cugId, newState);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#cugAddSub(long, java.lang.String)
     */
    public int cugAddSub(long cugId, String msisdn) throws RemoteException
    {
        return getDelegate().cugAddSub(cugId, msisdn);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#cugRemoveSub(long, java.lang.String)
     */
    public int cugRemoveSub(long cugId, String msisdn) throws RemoteException
    {
        return getDelegate().cugRemoveSub(cugId, msisdn);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deleteCUG(long)
     */
    @Deprecated
    public int deleteCUG(long cugId) throws RemoteException
    {
        return getDelegate().deleteCUG(cugId);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLP(long)
     */
    @Deprecated
    public int deletePLP(long plpId) throws RemoteException
    {
        return getDelegate().deletePLP(plpId);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLPForSub(java.lang.String)
     */
    public int deletePLPForSub(String ownerMsisdn) throws RemoteException
    {
        return getDelegate().deletePLPForSub(ownerMsisdn);
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLPForSub(java.lang.String, long)
     */
    public int deletePLPForSub (String ownerMsisdn, long plpId) throws RemoteException
    {
        return getDelegate().deletePLPForSub(ownerMsisdn, plpId);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUG(long, com.redknee.app.ff.ecare.rmi.TrCugHolder)
     */
    @Deprecated
    public int getCUG(long cugId, TrCugHolder cug) throws RemoteException
    {
        return getDelegate().getCUG(cugId, cug);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGByName(java.lang.String, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    @Deprecated
    public int getCUGByName(String name, TrCugListHolder cugList) throws RemoteException
    {
        return getDelegate().getCUGByName(name, cugList);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGBySPID(int, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getCUGBySPID(int spid, TrCugListHolder cugList) throws RemoteException
    {
        return getDelegate().getCUGBySPID(spid, cugList);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLP(long, com.redknee.app.ff.ecare.rmi.TrPlpHolder)
     */
    @Deprecated
    public int getPLP(long plpId, TrPlpHolder plp) throws RemoteException
    {
        return getDelegate().getPLP(plpId, plp);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLPByName(java.lang.String, com.redknee.app.ff.ecare.rmi.TrPlpListHolder)
     */
    @Deprecated
    public int getPLPByName(String name, TrPlpListHolder plpList) throws RemoteException
    {
        return getDelegate().getPLPByName(name, plpList);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLPBySPID(int, com.redknee.app.ff.ecare.rmi.TrPlpListHolder)
     */
    public int getPLPBySPID(int spid, TrPlpListHolder plpList) throws RemoteException
    {
        return getDelegate().getPLPBySPID(spid, plpList);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSub(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfileHolder)
     */
    public int getSub(String ownerMsisdn, TrSubscriberProfileHolder subProfile) throws RemoteException
    {
        return getDelegate().getSub(ownerMsisdn, subProfile);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSub(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfile2Holder)
     */
    public int getSub (String ownerMsisdn, TrSubscriberProfile2Holder subProfile) throws RemoteException
    {
        return getDelegate().getSub(ownerMsisdn, subProfile);
        
    }

    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSubWithDetails(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfileHolder, com.redknee.app.ff.ecare.rmi.TrPlpListHolder, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getSubWithDetails(String ownerMsisdn, TrSubscriberProfileHolder subProfile, TrPlpListHolder plpList, TrCugListHolder cugList)
            throws RemoteException
    {
        return getDelegate().getSubWithDetails(ownerMsisdn, subProfile, plpList, cugList);
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getSubWithDetails(java.lang.String, com.redknee.app.ff.ecare.rmi.TrSubscriberProfile2Holder, com.redknee.app.ff.ecare.rmi.TrPlpListHolder, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    public int getSubWithDetails (String ownerMsisdn, TrSubscriberProfile2Holder subProfile, TrPlpListHolder plpList, TrCugListHolder cugList) throws RemoteException
    {
        return getDelegate().getSubWithDetails(ownerMsisdn, subProfile, plpList, cugList);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#updateCUG(com.redknee.app.ff.ecare.rmi.TrCug)
     */
    @Deprecated
    public int updateCUG(TrCug cug) throws RemoteException
    {
        return getDelegate().updateCUG(cug);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#updatePLP(com.redknee.app.ff.ecare.rmi.TrPlp)
     */
    public int updatePLP(TrPlp plp) throws RemoteException
    {
        return getDelegate().updatePLP(plp);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#updatePLPForSub(java.lang.String, long, com.redknee.app.ff.ecare.rmi.TrPeerMsisdn[])
     */
    public int updatePLPForSub(String ownerMsisdn, long plpId, TrPeerMsisdn[] peerMsisdnArray) throws RemoteException
    {
        return getDelegate().updatePLPForSub(ownerMsisdn, plpId, peerMsisdnArray);
    }


    /**
     * {@inheritDoc}
     */
    public int createBirthdayPlan(final TrBirthdayPlan bdayPlan, final TrBirthdayPlanHolder newBdayPlan)
        throws RemoteException
    {
        return getDelegate().createBirthdayPlan(bdayPlan, newBdayPlan);
    }

    /**
     * {@inheritDoc}
     */
    public int updateBirthdayPlan(final TrBirthdayPlan bdayPlan) throws RemoteException
    {
        return getDelegate().updateBirthdayPlan(bdayPlan);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int deleteBirthdayPlan(final long bDayId) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlan(bDayId);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public int getBirthdayPlanByName(final String bdayPlanName, final TrBirthdayPlanListHolder birthdayPlanListHolder)
        throws RemoteException
    {
        return getDelegate().getBirthdayPlanByName(bdayPlanName, birthdayPlanListHolder);
    }

    /**
     * {@inheritDoc}
     */
    public int getBirthdayPlanBySPID(final int spid, final TrBirthdayPlanListHolder birthdayPlanListHolder)
        throws RemoteException
    {
        return getDelegate().getBirthdayPlanBySPID(spid, birthdayPlanListHolder);
    }

    @Deprecated
    public int updateBirthdayPlanForSub(final String ownerMsisdn, final long birthdayPlanId, final int dayOfMonth,
            final int month, final String subTimeZone) throws RemoteException
    {
        return getDelegate().updateBirthdayPlanForSub(ownerMsisdn, birthdayPlanId, dayOfMonth, month, subTimeZone);
    }
    
    public int updateBirthdayPlanForSub (String ownerMsisdn, long birthdayPlanId) throws RemoteException
    {
        return getDelegate().updateBirthdayPlanForSub(ownerMsisdn, birthdayPlanId);
    }
    

    @Deprecated
    public int deleteBirthdayPlanForSub(final String ownerMsisdn) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlanForSub(ownerMsisdn);    
    }

    @Deprecated
    public int getBirthdayPlan(final long bDayPlanId, final TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        return getDelegate().getBirthdayPlan(bDayPlanId, birthdayPlan);
    }

    /**
     * {@inheritDoc}
     */
    public int getBirthdayPlanForSub(final String ownerMsidn,
            final TrSubscriberProfileViewHolder trSubscriberProfileViewHolder) throws RemoteException
    {
        return getDelegate().getBirthdayPlanForSub(ownerMsidn,trSubscriberProfileViewHolder );
    }
    
    
    public int createCUGTemplate(int spid, TrCugTemplate cugTemplate, TrCugTemplateHolder newCugTemplate) throws RemoteException
    {
        return getDelegate().createCUGTemplate(spid, cugTemplate, newCugTemplate);
    }
    
 
    public int updateCUGTemplate(int spid, TrCugTemplate cugTemplate) throws RemoteException
    {
        return getDelegate().updateCUGTemplate(spid, cugTemplate);
    }
    
    public int deleteCUGTemplate(int spid, long cugTemplateId) throws RemoteException
    {
        return getDelegate().deleteCUGTemplate(spid, cugTemplateId);
    }
    
    public int getCUGTemplate(int spid, long cugTemplateId, TrCugTemplateHolder cugTemplate) throws RemoteException
    {
        return getDelegate().getCUGTemplate(spid, cugTemplateId, cugTemplate);
    }
    
    @Deprecated
    public int getCUGTemplateByName(String cugTemplateName, TrCugTemplateListHolder cugTemplateListHolder) throws RemoteException
    {
        return getDelegate().getCUGTemplateByName(cugTemplateName, cugTemplateListHolder);
    }
    
    public int getCUGTemplateBySPID(int spid, TrCugTemplateListHolder cugTemplateListHolder) throws RemoteException
    {
        return getDelegate().getCUGTemplateBySPID(spid, cugTemplateListHolder);
    }

    public int createCUGAssociation(int spid, long cugTemplateId, TrPeerMsisdn notifyMsisdn, TrCugIdHolder createdId) throws RemoteException
    {
        return getDelegate().createCUGAssociation(spid, cugTemplateId, notifyMsisdn, createdId);
    }

    public int updateCUGAssociation(int spid, long cugId, long cugTemplateId) throws RemoteException
    {
        return getDelegate().updateCUGAssociation(spid, cugId, cugTemplateId);
    }
    
    public int deleteCUGAssociation(int spid, long cugId) throws RemoteException
    {
        return getDelegate().deleteCUGAssociation(spid, cugId);
    }
    
    @Override
    @Deprecated
    public int addSubsToCUG(long cugId, TrPeerMsisdn[] msisdns) throws RemoteException
    {
        return getDelegate().addSubsToCUG(cugId, msisdns);
    }
    
    @Override
    @Deprecated
    public int removeSubsFromCUG(long cugId, TrPeerMsisdn[] msisdns) throws RemoteException
    {
        return getDelegate().removeSubsFromCUG(cugId, msisdns);
    }
    
    @Deprecated
    public int isCugTemplateInUse(long cugTemplateId, TrBooleanHolder resultHolder) throws RemoteException
    {
        return getDelegate().isCugTemplateInUse(cugTemplateId, resultHolder);
    }
    
    public int updateCUGNotifyMsisdn(long cugInstanceId, String notifyMsisdn) throws RemoteException
    {
        return getDelegate().updateCUGNotifyMsisdn(cugInstanceId, notifyMsisdn);
    }

    @Override
    public int updatePLPForSub(String ownerMsisdn, long plpId, TrPeerMsisdn[] otherMsisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().updatePLPForSub(ownerMsisdn, plpId, otherMsisdns, inParams, outParams);
    }

    @Override
    @Deprecated
    public int updateSubForCug(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inputParam) throws RemoteException
    {
        return getDelegate().updateSubForCug(cugId,msisdns,inputParam);
    }

    @Override
    @Deprecated
    public int updateCUGInstance(long cugId, Parameter[] inputParam) throws RemoteException
    {
        return getDelegate().updateCUGInstance(cugId,inputParam);
    }

    @Override
    public int deletePLPForSub(String ownerMsisdn, long plpId, Parameter[] inParams, ParameterSetHolder outParams)
            throws RemoteException
    {
        return getDelegate().deletePLPForSub(ownerMsisdn, plpId, inParams, outParams);
    }

    @Override
    @Deprecated
    public int addSubsToCUG(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams, ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().addSubsToCUG(cugId, msisdns, inParams, outParams);
    }

    @Override
    @Deprecated
    public int removeSubsFromCUG(long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams, ParameterSetHolder outParams)
            throws RemoteException
    {
        return getDelegate().removeSubsFromCUG(cugId, msisdns, inParams, outParams);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deletePLP(int, long)
     */
    @Override
    public int deletePLP(int spId, long plpId) throws RemoteException
    {
        return getDelegate().deletePLP(spId, plpId);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLP(int, long, com.redknee.app.ff.ecare.rmi.TrPlpHolder)
     */
    @Override
    public int getPLP(int spId, long plpId, TrPlpHolder plp) throws RemoteException
    {
        return getDelegate().getPLP(spId, plpId, plp);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getPLPByName(int, java.lang.String, com.redknee.app.ff.ecare.rmi.TrPlpListHolder)
     */
    @Override
    public int getPLPByName(int spId, String plpName, TrPlpListHolder plpListHolder) throws RemoteException
    {
        return getDelegate().getPLPByName(spId, plpName, plpListHolder);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deleteBirthdayPlan(int, long)
     */
    @Override
    public int deleteBirthdayPlan(int spId, long bDayId) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlan(spId, bDayId);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getBirthdayPlanByName(int, java.lang.String, com.redknee.app.ff.ecare.rmi.TrBirthdayPlanListHolder)
     */
    @Override
    public int getBirthdayPlanByName(int spId, String bdayPlanName, TrBirthdayPlanListHolder birthdayPlanListHolder)
            throws RemoteException
    {
        return getDelegate().getBirthdayPlanByName(spId, bdayPlanName, birthdayPlanListHolder);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deleteBirthdayPlanForSub(int, java.lang.String)
     */
    @Override
    public int deleteBirthdayPlanForSub(int spId, String ownerMsisdn) throws RemoteException
    {
        return getDelegate().deleteBirthdayPlanForSub(spId, ownerMsisdn);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getBirthdayPlan(int, long, com.redknee.app.ff.ecare.rmi.TrBirthdayPlanHolder)
     */
    @Override
    public int getBirthdayPlan(int spId, long bDayPlanId, TrBirthdayPlanHolder birthdayPlan) throws RemoteException
    {
        return getDelegate().getBirthdayPlan(spId, bDayPlanId, birthdayPlan);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#deleteCUG(int, long)
     */
    @Override
    public int deleteCUG(int spId, long cugId) throws RemoteException
    {
        return getDelegate().deleteCUG(spId, cugId);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUG(int, long, com.redknee.app.ff.ecare.rmi.TrCugHolder)
     */
    @Override
    public int getCUG(int spId, long cugId, TrCugHolder cug) throws RemoteException
    {
        return getDelegate().getCUG(spId, cugId, cug);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGByName(int, java.lang.String, com.redknee.app.ff.ecare.rmi.TrCugListHolder)
     */
    @Override
    public int getCUGByName(int spId, String cugName, TrCugListHolder cugListHolder) throws RemoteException
    {
        return getDelegate().getCUGByName(spId, cugName, cugListHolder);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#getCUGTemplateByName(int, java.lang.String, com.redknee.app.ff.ecare.rmi.TrCugTemplateListHolder)
     */
    @Override
    public int getCUGTemplateByName(int spId, String cugTemplateName, TrCugTemplateListHolder cugTemplateListHolder)
            throws RemoteException
    {
        return getDelegate().getCUGTemplateByName(spId, cugTemplateName, cugTemplateListHolder);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#addSubsToCUG(int, long, com.redknee.app.ff.ecare.rmi.TrPeerMsisdn[], com.redknee.model.app.ff.param.Parameter[], com.redknee.model.app.ff.param.ParameterSetHolder)
     */
    @Override
    public int addSubsToCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().addSubsToCUG(spId, cugId, msisdns, inParams, outParams);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#removeSubsFromCUG(int, long, com.redknee.app.ff.ecare.rmi.TrPeerMsisdn[], com.redknee.model.app.ff.param.Parameter[], com.redknee.model.app.ff.param.ParameterSetHolder)
     */
    @Override
    public int removeSubsFromCUG(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inParams,
            ParameterSetHolder outParams) throws RemoteException
    {
        return getDelegate().removeSubsFromCUG(spId, cugId, msisdns, inParams, outParams);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#isCugTemplateInUse(int, long, com.redknee.app.ff.ecare.rmi.TrBooleanHolder)
     */
    @Override
    public int isCugTemplateInUse(int spId, long cugTemplateId, TrBooleanHolder resultHolder) throws RemoteException
    {
        return getDelegate().isCugTemplateInUse(spId, cugTemplateId, resultHolder);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#updateSubForCug(int, long, com.redknee.app.ff.ecare.rmi.TrPeerMsisdn[], com.redknee.model.app.ff.param.Parameter[])
     */
    @Override
    public int updateSubForCug(int spId, long cugId, TrPeerMsisdn[] msisdns, Parameter[] inputParam)
            throws RemoteException
    {
        return getDelegate().updateSubForCug(spId, cugId, msisdns, inputParam);
    }


    /* (non-Javadoc)
     * @see com.redknee.app.ff.ecare.rmi.FFECareRmiService#updateCUGInstance(int, long, com.redknee.model.app.ff.param.Parameter[])
     */
    @Override
    public int updateCUGInstance(int spId, long cugId, Parameter[] inputParam) throws RemoteException
    {
        return getDelegate().updateCUGInstance(spId, cugId, inputParam);
    }


    public int updateCUGNotifyMsisdnWithSpid(int spid, long cugInstanceId, String notifyMsisdn) throws RemoteException
    {
        return getDelegate().updateCUGNotifyMsisdnWithSpid(spid, cugInstanceId, notifyMsisdn);
    }


	@Override
	public int getFnFProfile(String paramString, TrFnFProfileHolder paramTrFnFProfileHolder) throws RemoteException {
		// TODO Auto-generated method stub
		return getDelegate().getFnFProfile(paramString, paramTrFnFProfileHolder);
	}


	@Override
	public int removePlpUserListByPeerMsisdn(String paramString) throws RemoteException {
		return getDelegate().removePlpUserListByPeerMsisdn(paramString);
	}
}
