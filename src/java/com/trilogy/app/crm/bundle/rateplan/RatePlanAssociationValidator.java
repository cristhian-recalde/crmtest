package com.trilogy.app.crm.bundle.rateplan;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;

/**
 * Ensure the selected bundles corresponds to the rate plans
 * defined for the Price Plan Version
 * @author Candy Wong
 */
public class RatePlanAssociationValidator
implements Validator
{
    public void validate(Context ctx, Object obj)
    throws IllegalStateException
    {
        PricePlanVersion ppv = (PricePlanVersion)obj;
        PricePlan pricePlan = null;
        try
        {
            pricePlan = PricePlanSupport.getPlan(ctx, ppv.getId());
        }
        catch (HomeException e)
        {
            String msg = "Exception occured when trying to retrieve the Price Plan for Price Plan Version: " + ppv.getId() + ". " + e.getMessage();
            LogSupport.minor(ctx, this, msg, e);
            throw new IllegalStateException (msg);
        }

        CompoundIllegalStateException el = new CompoundIllegalStateException();

        com.redknee.app.crm.bean.core.ServicePackageVersion coreVersion = null;
        ServicePackageVersion servicePackageVersion = ppv.getServicePackageVersion();
        if (servicePackageVersion instanceof com.redknee.app.crm.bean.core.ServicePackageVersion)
        {
            coreVersion = (com.redknee.app.crm.bean.core.ServicePackageVersion) servicePackageVersion;
        }
        else
        {
            Adapter beanAdapter = new ExtendedBeanAdapter<ServicePackageVersion, com.redknee.app.crm.bean.core.ServicePackageVersion>(ServicePackageVersion.class, com.redknee.app.crm.bean.core.ServicePackageVersion.class);
            try
            {
                coreVersion = (com.redknee.app.crm.bean.core.ServicePackageVersion) beanAdapter.adapt(ctx, servicePackageVersion);
            }
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + RatePlanAssociationValidator.class.getSimpleName() + ".validate(): " + e.getMessage(), e).log(ctx);
                }
            }
        }
        
        if (coreVersion != null)
        {
            Context subCtx = ctx.createSubContext();
            
            com.redknee.framework.xhome.msp.MSP.setBeanSpid(subCtx, pricePlan.getSpid());
            
            Collection bundleFees = coreVersion.getBundleFees(subCtx).values();

            // looping through all the selected bundles to make sure they are
            // associated with the rate plans declared in the Price Plan Version
            for ( Iterator iter = bundleFees.iterator() ; iter.hasNext() ; )
            {
                BundleFee bundleFee = (BundleFee)iter.next();

                try
                {
                    BundleProfile bundle = BundleSupportHelper.get(subCtx).getBundleProfile(subCtx, bundleFee.getId());

                    if ( bundle == null )
                    {
                        throw new IllegalStateException("Missing bundle profile for id '" + bundleFee.getId() + "'.");
                    }
                    else
                    {
                       // REVIEW: It would be better to add a validate method to the enums and then just call enum.validate(), KGR
                        CompoundIllegalStateException crossBundleEl = new CompoundIllegalStateException();
                        Iterator<Map.Entry<?, BundleCategoryAssociation>> associationIter = bundle.getBundleCategoryIds()
                                    .entrySet().iterator();
                        while (associationIter.hasNext())
                        {
                            BundleCategoryAssociation association = associationIter.next().getValue();
                            int associationType = association.getType();
                            if (validateBundle(subCtx, bundle, associationType, pricePlan, crossBundleEl))
                            {
                                // If any association is validated, no need to validate anymore. Discard cross bundle exceptions since it's valid.
                                crossBundleEl = new CompoundIllegalStateException();
                                break;
                            }
                        }
                        // Check if it's necessary to throw exceptions for cross service or currency bundle.
                        if (crossBundleEl.getSize() > 0)
                        {
                            el.rethrow(el);
                        }

                    }

                }
                catch (Exception hEx)
                {
                    LogSupport.major(ctx, this, "Exception occured during Rate plan association validation: " + hEx.getMessage(), hEx);
                }
            }
        }

        el.throwAll();
    }

    /**
     * validates voice bundles against the voice rate plan
     */
    protected void validateVoice(Context ctx, ExceptionListener el, 
            BundleProfile bundle, final String ratePlan)
    {
        validateRatePlan(
                ctx, 
                el, 
                ratePlan, 
                bundle,
                new EQ(RatePlanAssociationXInfo.VOICE_RATE_PLAN, ratePlan));
    }

   private boolean validateBundle(Context ctx, BundleProfile bundle, int type, PricePlan pp, CompoundIllegalStateException el)
   {
       // REVIEW: It would be better to add a validate method to the enums and then just call enum.validate(), KGR
       
       switch (type)
       {
          case BundleTypeEnum.VOICE_INDEX:
             if (pp.getVoiceRatePlan().length() > 0)
             {
                return validateVoice(ctx, el, pp, bundle);
             }
             else
             {
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.VOICE_RATE_PLAN, "supply a valid voice rate plan for the voice bundle "+bundle.getBundleId()));
             }
             break;
          case BundleTypeEnum.SMS_INDEX:
             if (pp.getSMSRatePlan().length() > 0)
             {
                return validateSMS(ctx, el, pp, bundle);
             }
             else
             {
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.SMSRATE_PLAN, "supply a valid SMS rate plan for the SMS bundle "+bundle.getBundleId()));
             }
             break;
          case BundleTypeEnum.DATA_INDEX:
             if (pp.getDataRatePlan().length() > 0)
             {
                return validateData(ctx, el, pp, bundle);
             }
             else
             {
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.DATA_RATE_PLAN, "supply a valid data rate plan for the data bundle "+bundle.getBundleId()));
             }
             break;
          default:
             //TODO
       }
       return true;
       
   }   
   /**
    * validates voice bundles against the voice rate plan
    */
   protected boolean validateVoice(Context ctx, ExceptionListener el, PricePlan pp, BundleProfile bundle)
   {
      return validateRatePlan(
            ctx, 
            el, 
            pp.getVoiceRatePlan(), 
            bundle,
            new EQ(RatePlanAssociationXInfo.VOICE_RATE_PLAN, pp.getVoiceRatePlan()));
   }

   /**
    * validates SMS bundles against the SMS rate plan
    */
   protected boolean validateSMS(Context ctx, ExceptionListener el, PricePlan pp, BundleProfile bundle)
   {
      return validateRatePlan(
            ctx, 
            el, 
            pp.getSMSRatePlan(), 
            bundle,
            new EQ(RatePlanAssociationXInfo.SMS_RATE_PLAN, pp.getSMSRatePlan()));
   }
   /**
    * validates data bundles against the data rate plan
    */
   protected boolean validateData(Context ctx, ExceptionListener el, PricePlan pp, BundleProfile bundle)
   {
      return validateRatePlan(
            ctx, 
            el, 
            String.valueOf(pp.getDataRatePlan()), 
            bundle,
            new EQ(RatePlanAssociationXInfo.DATA_RATE_PLAN, Integer.valueOf(pp.getDataRatePlan())));
   }

   /**
    * ensures a bundle is associated with the input rate plan in the 
    * RatePlanAssociationHome
    */
   protected boolean validateRatePlan(
         Context ctx, 
         ExceptionListener el, 
         String ratePlan,
         BundleProfile bundle,
         Object ratePlanFilter)
   {
      Home assocHome = (Home)ctx.get(RatePlanAssociationHome.class);
        try
        {
            RatePlanAssociation assoc = (RatePlanAssociation)assocHome.find(ctx,
                    new And()
            .add(ratePlanFilter)
            .add(new EQ(RatePlanAssociationXInfo.BUNDLE_ID, Long.valueOf(bundle.getBundleId()))));

         if (assoc == null)
         {
            el.thrown(new IllegalPropertyArgumentException(BundleFeeXInfo.ID, "bundle "+bundle.getBundleId()+" is not associated with rate plan "+ratePlan));
            return false;
         }
      }
      catch (HomeException hEx)
      {
         el.thrown(new IllegalPropertyArgumentException(BundleFeeXInfo.ID, hEx));
         return false;
      }
      return true;
   }
}
