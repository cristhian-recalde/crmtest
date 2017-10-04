/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.support.MultiDbSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.BeanNotFoundHomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;



/**
 * @author simar.singh@redknee.com
 */
public class BucketEventFunctions
{




    /**
     * Get BundleFee corresponding to BundleProfile
     * @param ctx
     * @param bundle
     * @param startDate
     * @param endDate
     * @return
     */
    protected BundleFee getBundleFee(Context ctx, BundleProfile bundle, Date startDate, Date endDate)
    {
        final BundleFee fee = new BundleFee();
        {
            fee.setFee(bundle.getAuxiliaryServiceCharge());
            fee.setId(bundle.getBundleId());
            fee.setSource(BundleFee.AUXILIARY);
            fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
            if (RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE == bundle.getRecurrenceScheme()
                    || RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL == bundle.getRecurrenceScheme())
            {
                fee.setServicePeriod(ServicePeriodEnum.ONE_TIME);
            }
            else
            {
                // we just support monthly serice period for Recurring Aux Bundles
                fee.setServicePeriod(ServicePeriodEnum.MONTHLY);
            }
            if (RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE == bundle.getRecurrenceScheme())
            {
                fee.setStartDate(bundle.getStartDate());
                fee.setEndDate(bundle.getEndDate());
            }
            else
            {
                fee.setStartDate(startDate);
                fee.setEndDate(endDate);
            }
        }
        return fee;
    }

   /**
    * Get Bundle Profile corresponding to bundle-id
    * @param ctx
    * @param bundleId
    * @return
    */
    protected BundleProfile getBundle(Context ctx, long bundleId, int spid)
    {
        CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
        BundleProfile bundle = null;
        if (service != null)
        {
            try
            {
                bundle = service.getBundleProfile(ctx,spid, bundleId);
            }
            catch (Exception e)
            {
                LogSupport.major(ctx, this, "getBundleName: HomeException : " + e.getMessage(), e);
            }
        }
        return bundle;
    }

    /**
     * Associates a bundle with a Subscriber. Add it if not present, update it with end-date if present
     * This operation operates at the Store level directly.
     * Do not use this method if you want to un-provision end to end; instead use subscriberHome.store(sub)
     * @param ctx
     * @param subscriber
     * @param bundleFee
     * @throws HomeException
     */
    protected void addOrUpdateSubscriberBundle(Context ctx, Subscriber subscriber, BundleFee bundleFee)
            throws HomeException
    {
        final BundleFee originalBundleFee = (BundleFee) subscriber.getBundles().get(bundleFee.getId());
        if (null == originalBundleFee || !originalBundleFee.getEndDate().after(bundleFee.getEndDate()))
        {
            subscriber.getBundles().put(bundleFee.getId(), bundleFee);
            subscriber.bundleProvisioned(bundleFee);
            updateSubscriberBundles(ctx, subscriber);
            updateSubscriberBundleAuxillaryServiceRecord(ctx, subscriber, bundleFee);
            if (originalBundleFee == null)
            {
            	addSubscriberNote(ctx, subscriber, bundleFee,true);
            }
        }
        else
        {
            new InfoLogMsg(this, "Ignoring Bucket Create Event Bundle-ID [" + bundleFee.getId() + "] as the end-date["
                    + bundleFee.getEndDate() + "] in event record is before  of that Subsriber-Bundle's end-date["
                    + originalBundleFee.getEndDate() + "] in CRM; the event might be statle", null).log(ctx);
        }
    }
    
    
    public void addSubscriberNote(Context ctx, Subscriber subscriber, BundleFee bundleFee,boolean isBundleCreationCall)
    {
    	String bundleName = "";	
    	String msg = "";
    			try
                      {
                      bundleName = bundleFee.getBundleProfile(ctx).getName();
                      }catch(Exception e)
                      {
                      	LogSupport.debug(ctx, this, "Bundle Profile not Found"+e);
                      }
    			      
    			      if(isBundleCreationCall)
    			      {
    			    	  msg = MessageFormat.format("Subscriber Bundle: {0} : {1} Addedd successfully.",
                              new Object[]{Long.valueOf(bundleFee.getId()), bundleName});
    			      }else
    			      {
    			    	  msg = MessageFormat.format("Subscriber Bundle: {0} : {1} remove successfully.",
                                  new Object[]{Long.valueOf(bundleFee.getId()), bundleName}); 
    			      }
    			      
                      StringBuilder noteBuff = new StringBuilder();
                      noteBuff.append(msg);
                      
                      SubscriberNoteSupport.createSubscriberNote(ctx, BucketEventFunctions.class.getName(), 
                              SubscriberNoteSupport.getCsrAgent(ctx, subscriber), 
                                  subscriber.getId(), SystemNoteTypeEnum.EVENTS, 
                                    SystemNoteSubTypeEnum.SUBUPDATE, noteBuff, false);  


    }

    /**
     * Disassociates a bundle with a Subscriber if present
     * This operation operates at the Store level directly.
     * Do not use this method if you want to un-provision end to end; instead use subscriberHome.store(sub)
     * @param ctx
     * @param subscriber
     * @param bundleFee
     * @throws HomeException
     */
    protected void removeSubscriberBundle(Context ctx, Subscriber subscriber, BundleFee bundleFee)
            throws HomeException
    {
        final BundleFee originalBundleFee = (BundleFee) subscriber.getBundles().get(bundleFee.getId());
        if (null == originalBundleFee)
        {
            // bundle not associated with subscriber
            return;
        }
        else if (!originalBundleFee.getEndDate().after(bundleFee.getEndDate()))
        {
            subscriber.bundleUnProvisioned(bundleFee);
            subscriber.getBundles().remove(bundleFee.getId());
           	addSubscriberNote(ctx, subscriber, bundleFee,false);
            updateSubscriberBundles(ctx, subscriber);
            final Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
            home.where(
                    ctx,
                    new And().add(new EQ(BundleAuxiliaryServiceXInfo.ID, new Long(bundleFee.getId()))).add(
                            new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID, subscriber.getId()))).removeAll();
        }
        else
        {
            new InfoLogMsg(this, "Ignoring Bucket Create Event Bundle-ID [" + bundleFee.getId() + "] as the end-date["
                    + bundleFee.getEndDate() + "] in event record is before  of that Subsriber-Bundle's end-date["
                    + originalBundleFee.getEndDate() + "] in CRM; the event might be statle", null).log(ctx);
        }
    }


    /**
     * Does a field level update on Subscriber store for Bundles and Provisioned bundles
     * @param ctx
     * @param subscriber
     * @throws HomeException
     */
    private void updateSubscriberBundles(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        // this is dirty way of updating subscriber bundles
        // we do it because to minimize the chance of race condition
        // with this field update mechanism, the race is only limited to bundles
        // with home.store(sub), the race would occur for only 2 simultaneous update
        // modifying any field(s).
        XDB xdb = (XDB) ctx.get(XDB.class);
        int count;
        
        
        count = xdb.execute(ctx, new XStatement()
        {
        	
            private static final long serialVersionUID = 1L;
            private final String tableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,SubscriberHome.class, "SUBSCRIBER");
            private final String preparedStatementString_ = "UPDATE "
                    + tableName + " SET "
                    + SubscriberXInfo.BUNDLES.getSQLName() + " = ? ,"
                    + SubscriberXInfo.PROVISIONED_BUNDLES.getSQLName() + " = ?  WHERE "
                    + SubscriberXInfo.ID.getSQLName() + " = ? ";


            public String createStatement(Context ctx)
            {
                return preparedStatementString_;
            }


            public void set(Context ctx, XPreparedStatement ps) throws SQLException
            {
                ps.setMap(subscriber.getBundles(), BundleFee.class);
                ps.setSet(subscriber.getProvisionedBundles(), BundleFee.class);
                ps.setString(subscriber.getId());
            }
        });
        if (count < 1)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                String msg = " Unable to update SUBSCRIBER BUNDLE-FEE MAP = " + subscriber.getBundles()
                        + " where id = " + subscriber.getId();
                LogSupport.info(ctx, this, msg, null);
                LogSupport.debug(ctx, this, msg);
            }
            throw new BeanNotFoundHomeException("SUBSCRIBER - ID[" + subscriber.getId() + "] NOT FOUND");
        }
        else
        {
            String msg = " Update SUBSCRIBER BUNDLE-FEE MAP = " + subscriber.getBundles() + " where id = "
                    + subscriber.getId();
            LogSupport.info(ctx, this, msg, null);
        }
    }


    private void updateSubscriberBundleAuxillaryServiceRecord(Context ctx, Subscriber subscriber, BundleFee bundleFee)
    {
        final Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
        BundleAuxiliaryService bundleAuxService;
        try
        {
            bundleAuxService = (BundleAuxiliaryService) home.find(ctx, new And().add(
                    new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID, subscriber.getId())).add(
                    new EQ(BundleAuxiliaryServiceXInfo.ID, bundleFee.getId())));
            if (null == bundleAuxService)
            {
                try
                {
                    bundleAuxService = (BundleAuxiliaryService)XBeans.instantiate(BundleAuxiliaryService.class, ctx);
                }
                catch (Exception e)
                {
                    new InfoLogMsg(this, "Unable to instantiate BundleAuxiliaryService.class bean through XBeans", null).log(ctx);
                    bundleAuxService = new BundleAuxiliaryService();

                }

                bundleAuxService.setId(bundleFee.getId());
                bundleAuxService.setFee(bundleFee.getFee());
                bundleAuxService.setPaymentNum(bundleFee.getPaymentNum());
                bundleAuxService.setProvisioned(true);
                bundleAuxService.setServicePreference(bundleFee.getServicePreference());
                bundleAuxService.setSource(bundleFee.getSource());
                bundleAuxService.setSubscriberId(subscriber.getId());
                if (ServicePeriodEnum.ONE_TIME == bundleFee.getServicePeriod())
                {
                    // BundleAuxillaryServiceHome is used to find bundles that
                    // need to expire. Setting end date to lifetime because we do
                    // not want CRM to un-provision it
                    bundleAuxService.setStartDate(new Date(0));
                    bundleAuxService.setEndDate(new Date(Long.MAX_VALUE));
                }
                else
                {
                    bundleAuxService.setStartDate(bundleFee.getStartDate());
                    bundleAuxService.setEndDate(bundleFee.getEndDate());
                }
                home.create(ctx, bundleAuxService);
                
               
            }
            else
            {
                if (ServicePeriodEnum.ONE_TIME == bundleFee.getServicePeriod())
                {
                    bundleAuxService.setStartDate(new Date(0));
                    bundleAuxService.setEndDate(new Date(Long.MAX_VALUE));
                }
                else
                {
                    bundleAuxService.setStartDate(bundleFee.getStartDate());
                    bundleAuxService.setEndDate(bundleFee.getEndDate());
                }
            }
            home.store(ctx, bundleAuxService);
        }
        catch (HomeException e)
        {
            // TODO Auto-generated catch block
            new MinorLogMsg(this, "Could not record bundle-ID [" + bundleFee.getId() + "] for Subscriber-ID ["
                    + subscriber.getId() + "] in BundleAuxiliaryServiceHome", e);
        }
    }
}
