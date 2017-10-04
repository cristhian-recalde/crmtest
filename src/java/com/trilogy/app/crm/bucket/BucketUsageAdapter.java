package com.trilogy.app.crm.bucket;

import java.util.Date;

import com.trilogy.app.crm.bean.core.custom.SubscriberCycleUsage;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.bundle.SubscriberBucket;

public class BucketUsageAdapter
   implements Adapter
{
   /**
    * used by create and store methods.
    */
   public Object unAdapt(Context ctx, Object obj)
      throws HomeException
   {
      if (obj instanceof SubscriberBucket)
      {
         SubscriberBucket subBucket = (SubscriberBucket)obj;
         SubscriberCycleUsage usage = null;

         try
         {
            usage = (SubscriberCycleUsage) XBeans.instantiate(SubscriberCycleUsage.class, ctx);
         }
         catch (Throwable t)
         {
            usage = new SubscriberCycleUsage();
         }
         /*TODO: the new BM CORBA interface doesn't expose the last modified attribute
          * lastModified used to be an s-transient which default is new Date() leaving this
          * attribute to new Date() as well
         */
         usage.initializeIdentification(ctx, subBucket.getMsisdn(), new Date());
         if (subBucket.getSpid() != usage.getSpid())
         {
            throw new HomeException(new IllegalStateException(
                     "The service provider in the SubscriberBucket ["
                     + subBucket.getSpid()
                     + "] does not match the service provider of the subscriber ["
                     + usage.getSpid()
                     + "]."));
         }
         // TODO waiting for mapping from Gilles
         usage.setPastMonthlyFCT(0);
         usage.setPastFCTUsed(0);
         usage.setPastRollOverFCTAwarded(0);
         usage.setPastRollOverFCTUsed(0);
         usage.setPastUsedGroupFCT(0);
         usage.setRollOverFCT(0);
         usage.setExpiredFCT(0);

         return usage;
      }
      else if (obj instanceof SubscriberCycleUsage)
      {
         // nothing to do
         return obj;
      }
      throw new HomeException(new IllegalArgumentException("input bean is not supported ["+obj.getClass().getName()+"]"));
   }

   /**
    * used by find and select methods.
    */
   public Object adapt(Context ctx, Object obj)
      throws HomeException
   {
      if (obj instanceof SubscriberCycleUsage)
      {
         SubscriberCycleUsage usage = (SubscriberCycleUsage)obj;
         SubscriberBucket subBucket = null;

         try
         {
            subBucket = (SubscriberBucket) XBeans.instantiate(SubscriberBucket.class, ctx);
         }
         catch (Throwable t)
         {
            subBucket = new SubscriberBucket();
         }
         subBucket.setMsisdn(usage.getMsisdn(ctx));
         // unknown
         //subBucket.setSubscriptionType(???);
         subBucket.setSpid(usage.getSpid());

         // TODO map SubscriberCycleUsage to SubscriberBucket
         return subBucket;
      }
      else if (obj instanceof SubscriberBucket)
      {
         // nothing to do
         return obj;
      }
      throw new HomeException(new IllegalArgumentException("input bean is not supported ["+obj.getClass().getName()+"]"));
   }
}
