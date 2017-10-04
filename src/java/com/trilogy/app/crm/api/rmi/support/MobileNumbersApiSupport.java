package com.trilogy.app.crm.api.rmi.support;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.LnpReqirementEnum;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

public final class MobileNumbersApiSupport
{
    /**
     * Not published in the CRM API
     */
    public static Msisdn createMobileNumber(final Context ctx, final String number, boolean ported , final SubscriberTypeEnum type,
            final CRMSpid sp, final TechnologyEnum technology, final Object caller)
            throws CRMExceptionFault
    {
        Msisdn resultMsisdn = null;
        try
            {
                final Home home = getMsisdnHome(ctx);
                Msisdn msisdn;
                try
                {
                    msisdn = (Msisdn) XBeans.instantiate(Msisdn.class, ctx);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(MobileNumbersApiSupport.class, "Error instantiating new MSISDN.  Using default constructor.", e).log(ctx);
                    msisdn = new Msisdn();
                }
                
                msisdn.setMsisdn(number);
                msisdn.setSpid(sp.getId());
                msisdn.setSubscriberType(type);
                msisdn.setGroup(sp.getAutoCreateMSISDNGroup());
                msisdn.setTechnology(technology);
                msisdn.setLnpRequired(LnpReqirementEnum.NOT_REQUIRED);
                if(number.equals(ctx.get(MsisdnPortHandlingHome.MSISDN_PORT_KEY)) || ported)
                {
                    msisdn.setPortingType(PortingTypeEnum.IN);
                    ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, number);
                }
                if(sp.getAutoCreateMSISDNAsExternal())
                {
                	msisdn.setExternal(true);
                }
                
                TechnologyEnum technologyEnum = msisdn.getTechnology();
                
                if(sp.getAutoCreateMSISDN() && sp.getAutoCreateMSISDNAsExternal())
                {
                    Home msisdnGroupHome = (Home) ctx.get(MsisdnGroupHome.class);
                    MsisdnGroup msisdnGroup = (MsisdnGroup)msisdnGroupHome.find(sp.getExternalMSISDNGroup());
                    if(technologyEnum.getIndex() != msisdnGroup.getTechnology().getIndex())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("profile.msisdn",
                                "MSISDN and subscriber must be on the same technology.");
                    }
                }
                
                if(sp.getAutoCreateMSISDN() && !sp.getAutoCreateMSISDNAsExternal())
                {
                    Home msisdnGroupHome = (Home) ctx.get(MsisdnGroupHome.class);
                    MsisdnGroup msisdnGroup = (MsisdnGroup)msisdnGroupHome.find(sp.getAutoCreateMSISDNGroup());
                    if(technologyEnum.getIndex() != msisdnGroup.getTechnology().getIndex())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("profile.msisdn",
                                "MSISDN and subscriber must be on the same technology.");
                    }
                }
                
                resultMsisdn = (Msisdn) home.create(ctx, msisdn);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to create Mobile Number " + number;
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, Msisdn.class, number, caller);
            }

            return resultMsisdn;
            }


    /**
     * Not published in the CRM API
     */
    public static void removeCrmMsisdn(final Context ctx, final Msisdn msisdn, final Object caller)
            throws CRMExceptionFault
    {
        final Home home = getMsisdnHome(ctx);
        try
        {
            home.remove(ctx, msisdn);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to remove Mobile Number " + msisdn.getMsisdn();
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, caller);
        }
    }


    /**
     * Not published in the CRM API
     */
    public static com.redknee.app.crm.bean.core.Msisdn getCrmMsisdn(Context ctx, final String number,
            final Object caller) throws CRMExceptionFault
    {
        com.redknee.app.crm.bean.core.Msisdn msisdn = null;
        try
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                Home home = HomeSupportHelper.get(ctx).getHome(ctx, com.redknee.app.crm.bean.core.Msisdn.class);
                LogSupport.debug(ctx, caller, "Fetching msisdn from Home: "+ home);
            }
                
            
            msisdn = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.core.Msisdn.class, number);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve MSISDN " + number;
            RmiApiErrorHandlingSupport.generalException(ctx, e, msg, caller);
        }
        return msisdn;
    }

    /**
     * Not published in the CRM API
     */
    public static Home getMsisdnHome(final Context ctx) throws CRMExceptionFault
    {
        return RmiApiSupport.getCrmHome(ctx, MsisdnHome.class, MobileNumbersApiSupport.class);
    }
}
