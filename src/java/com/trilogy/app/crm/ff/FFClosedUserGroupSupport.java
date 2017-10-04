package com.trilogy.app.crm.ff;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.CUGStateEnum;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrBooleanHolder;
import com.trilogy.app.ff.ecare.rmi.TrBooleanHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrCug;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplate;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;
import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterID;


public class FFClosedUserGroupSupport
{
    
   public static TrPeerMsisdn[] convertToTrPeerMsisdn(Map<String, ClosedSub> msisdns)
    {
        if (msisdns == null)
        {
            return null;
        }
        final TrPeerMsisdn[] ffMsisdnArray = new TrPeerMsisdn[msisdns.size()];
        int index = 0;
        for (Map.Entry<String, ClosedSub> msisdnEntry : msisdns.entrySet())
        {
            ffMsisdnArray[index] = convertToTrPeerMsisdn(msisdnEntry.getValue());
            ++index;
        }
        return ffMsisdnArray;
    }
   
   public static TrPeerMsisdn[] convertToTrPeerMsisdn(Set <ClosedSub> msisdns)
   {
       if (msisdns == null)
       {
           return null;
       }
       final TrPeerMsisdn[] ffMsisdnArray = new TrPeerMsisdn[msisdns.size()];
       int index = 0;
       for (ClosedSub msisdnEntry : msisdns)
       {
           ffMsisdnArray[index] = convertToTrPeerMsisdn(msisdnEntry);
           ++index;
       }
       return ffMsisdnArray;
   }
   
   public static TrPeerMsisdn convertToTrPeerMsisdn(ClosedSub closedGroupSub)
    {
        return new TrPeerMsisdn(closedGroupSub.getPhoneID(), closedGroupSub.getShortCode());
    }
   
   public static HashMap<String, ClosedSub> convertTrPeerMsisdnToStringSet(TrPeerMsisdn[] msisdnArray)
    {
        HashMap<String, ClosedSub> returnSet = new HashMap<String, ClosedSub>();
        if (msisdnArray == null || msisdnArray.length == 0)
        {
            return returnSet;
        }
        for (int i = 0; i < msisdnArray.length; i++)
        {
            ClosedSub sub = new ClosedSub();
            sub.setPhoneID(msisdnArray[i].msisdn);
            for (Parameter parameter : msisdnArray[i].inParam)
            {
                if (ParameterID.PARAM_CUG_SHORTCODE == parameter.parameterID)
                {
                    sub.setShortCode(parameter.value.stringValue());
                }
            }
            returnSet.put(sub.getPhoneID(), sub);
        }
        return returnSet;
    }
   
   /**
    * Convert a CUG structure (used by the remote Friends & Family RMI service)
    * into a ClosedUserGroup bean.
    * 
    * @param cug The given CUG structure.
    * 
    * @return ClosedUserGroup The converted ClosedUserGroup bean.
    */
   public static ClosedUserGroup convertFFCugToCrmCug(Context ctx, final TrCug cug)
   throws HomeException
   {
       ClosedUserGroup bean = new ClosedUserGroup();
       if (cug != null)
       {
           bean.setID(cug.getId());
           bean.setSpid(cug.getSpId());
           
           if (cug.smsNotifyUser == null)
           {
               bean.setSmsNotifyUser("");
               
           }
           else
           {
               bean.setSmsNotifyUser(cug.smsNotifyUser);
           }
           
           // CUG templates contents
           bean.setName(cug.getName());
           bean.setCugTemplateID(cug.cugTemplateId);
           bean.setDeprecated(cug.deprecated);
           bean.setCugState(CUGStateEnum.get((short)cug.state));

           // Convert the MSISDN list.
           Map<String, ClosedSub> subs = convertTrPeerMsisdnToStringSet(cug.otherMsisdns);
           bean.setSubscribers(subs);
           
           PrivateCug pcug = ClosedUserGroupSupport73.findPrivateCug( ctx, bean.getID()); 
           if (pcug != null)
           {
            	bean.setOwnerMSISDN(pcug.getOwnerMSISDN()); 
           }

       }
       return bean;
   }
   
   /**
    * Convert the CRM ClosedUserGroupTemplate bean into a CUG template structure used by
    * the remote Friends & Family RMI service.
    * 
    * @param bean
    *            The given ClosedUserGroupTemplate bean.
    * 
    * @return TrCug The converted CUG structure.
    */
   public static TrCugTemplate convertCrmCugTemplateToFFCugTemplate(final ClosedUserGroupTemplate bean)
   {
       TrCugTemplate cugTemplate = new TrCugTemplate();
       if (bean == null)
       {
           return cugTemplate;
       }
       cugTemplate.setId(bean.getID());
       cugTemplate.setName(bean.getName());
       cugTemplate.setSpId(bean.getSpid());
       cugTemplate.setServiceType(bean.getCugServiceType().getIndex());
       cugTemplate.setShortcodeEnable(bean.getShortCodeEnable());
       cugTemplate.setShortcodePatter(bean.getShortCodePattern());
       if (bean.getVoiceDiscountType() == DiscountTypeEnum.DISCOUNT)
       {
           cugTemplate.setVoiceDiscountType(CUG_DISCOUNT);
       }
       else
       {
           cugTemplate.setVoiceDiscountType(CUG_RATEPLAN);
       }
       cugTemplate.setVoiceMOValue(bean.getVoiceOutgoingValue());
       cugTemplate.setVoiceMTValue(bean.getVoiceIncomingValue());
       
       //SMS discount is not supported in this release
       if (bean.getSmsDiscountType() == DiscountTypeEnum.DISCOUNT)
       {
           cugTemplate.setSmsDiscountType(CUG_DISCOUNT);
       }
       else
       {
           cugTemplate.setSmsDiscountType(CUG_RATEPLAN);
       }
       cugTemplate.setSmsMOValue(bean.getSmsOutgoingValue());
       cugTemplate.setSmsMTValue(bean.getSmsIncomingValue());

       cugTemplate.setStartDateString(CUG_DATE_FORMAT.format(bean.getStartDate()));
       cugTemplate.setStopDateString(CUG_DATE_FORMAT.format(bean.getEndDate()));
       cugTemplate.deprecated = bean.isDeprecated();
       cugTemplate.setOwnershipType(CUGTypeEnum.PublicCUG.equals(bean.getCugType()) ? CUG_PUBLIC : CUG_PRIVATE); 
       
       return cugTemplate;
   }
   
   /**
    * Convert a CUG template structure (used by the remote Friends & Family RMI service)
    * into a ClosedUserGroupTemplate bean.
    * 
    * @param cug
    *            The given CUG structure.
    * 
    * @return ClosedUserGroupTemplate The converted ClosedUserGroupTemplate bean.
    */
    public static ClosedUserGroupTemplate convertFFCugTemplateToCRMCugTemplate(Context ctx,
            final TrCugTemplate cugTemplate)
   {
       ClosedUserGroupTemplate bean = new ClosedUserGroupTemplate();
       if (cugTemplate != null)
       {
           bean.setID(cugTemplate.getId());
           bean.setName(cugTemplate.getName());
           bean.setSpid(cugTemplate.getSpId());
           bean.setCugServiceType(CallingGroupServiceTypeEnum.get((short) cugTemplate.getServiceType()));
           bean.setVoiceDiscountType(cugTemplate.getVoiceDiscountType() == CUG_DISCOUNT
                   ? DiscountTypeEnum.DISCOUNT
                   : DiscountTypeEnum.RATE_PLAN);
           bean.setSmsDiscountType(cugTemplate.getSmsDiscountType() == CUG_DISCOUNT
                   ? DiscountTypeEnum.DISCOUNT
                   : DiscountTypeEnum.RATE_PLAN);
           bean.setVoiceOutgoingValue(cugTemplate.getVoiceMOValue());
           bean.setVoiceIncomingValue(cugTemplate.getVoiceMTValue());
           bean.setSmsOutgoingValue(cugTemplate.getSmsMOValue());
           bean.setSmsIncomingValue(cugTemplate.getSmsMTValue());
           bean.setDeprecated(cugTemplate.deprecated);
            bean.setCugType(cugTemplate.getOwnershipType() == CUG_PUBLIC
                    ? CUGTypeEnum.PublicCUG
                    : CUGTypeEnum.PrivateCUG);
           bean.setShortCodePattern(cugTemplate.getShortcodePattern());
           bean.setShortCodeEnable(cugTemplate.getShortcodeEnable());
           try
           {
               bean.setStartDate(CUG_DATE_FORMAT.parse(cugTemplate.getStartDateString()));
           }
           catch (ParseException e)
           {
               String msg = "Unable to parse StartDate for CUG " + cugTemplate.getId();
               LogSupport.minor(ctx, FFClosedUserGroupSupport.class, msg, e);
               throw new IllegalArgumentException(msg);
           }
           try
           {
               bean.setEndDate(CUG_DATE_FORMAT.parse(cugTemplate.getStopDateString()));
           }
           catch (ParseException e)
           {
               String msg = "Unable to parse EndDate for CUG " + cugTemplate.getId();
                LogSupport.minor(ctx, FFClosedUserGroupSupport.class,
                        "Unable to parse EndDate for CUG " + cugTemplate.getId(), e);
               throw new IllegalArgumentException(msg);
           }
       }
       return bean;
   }
   
   public static boolean isCUGTemplateInUse(Context ctx, ClosedUserGroupTemplate cugTemplate) throws HomeException
   {
       try
       {
           TrBooleanHolder booleanHolder = new TrBooleanHolderImpl();
            int result = getFFRmiService(ctx, cugTemplate.getClass()).isCugTemplateInUse(cugTemplate.getSpid(),
                    cugTemplate.getID(), booleanHolder);
           
           if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
           {
               String msg = "Failed to check if CUG Template is in Use: " + cugTemplate.getID() + " [" + 
                       ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.FF, result) + "]";
               throw new HomeException(msg);
           }
           
           return booleanHolder.getValue().booleanValue();
       } 
       catch (FFEcareException e)
       {
           throw new HomeException(e);
       }
       catch (RemoteException e)
       {
           String msg = "Failed to check if CUG Template is in use: " + e.getMessage();
           throw new HomeException(new FFEcareException(msg, ExternalAppSupport.REMOTE_EXCEPTION));
       }
   }

   /**
    * Return the remote Friends & Family RMI service.
    * 
    * @return FFECareRmiService The Friends & Family RMI service.
    */
   public static FFECareRmiService getFFRmiService(Context ctx, Class objClass) throws FFEcareException
   {
       FFECareRmiService service = (FFECareRmiService) ctx.get(FFECareRmiServiceClient.class);
       if (service == null)
       {
           String msg = "No F&F RMI Service found in context.";
           new SeverityLogMsg(SeverityEnum.MAJOR, objClass.getName(), msg, null).log(ctx);
           throw new FFEcareException(msg, ExternalAppSupport.NO_CONNECTION);
       }
       return service;   
   }


   private static final SimpleDateFormat CUG_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

   private static final int CUG_RATEPLAN = 3;

   private static final int CUG_DISCOUNT = 4;

   private static final int CUG_PUBLIC = 0;
   
   private static final int CUG_PRIVATE = 1;
}
