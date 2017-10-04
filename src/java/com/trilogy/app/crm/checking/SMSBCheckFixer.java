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
package com.trilogy.app.crm.checking;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.config.AppSmsbClientConfig;
import com.trilogy.app.crm.provision.SmsProvisionAgent;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.util.SubscriberProcessingInterruptionException;
import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7;

/**
 * For use by the DIVA tool.
 * 
 * @author lxia
 * @author skushwaha
 */
public class SMSBCheckFixer extends AbstractIntegrityValidation {

    static public final String SERVICE_SMS = "Sms"; 

    /**
     * {@inheritDoc}
     */
    public void printResults()
    {
        print("TOTAL: " + totalCount_); 
        if ( isRepairEnabled()){
            print("Error: " + errorCount_); 
            print("fixed: " + changeCount_); 
            print("failed: " + failCount_); 
            print("no smsb service or service stopped: " + noServiceCount_); 
            print("No need fix:" + noChangeCount_); 
        } else {
            print("Error: " + errorCount_); 
            print("no smsb service or service stopped: " + noServiceCount_); 
            //print("No Error: " + noChangeCount_); 
        }
        if ( notExistCount_ > 0 ){
            print("Subscriber not exist on SMSB: " + notExistCount_); 
        }
        if ( ghostCount_ > 0 ){
            print("Ghost profile on SMSB: " + ghostCount_); 

        }
    }

    
    /**
     * {@inheritDoc}
     */
    public void process(final Context ctx, final Subscriber sub)
        throws SubscriberProcessingInterruptionException
    {
        Home acctHome = (Home) ctx.get(AccountHome.class); 
        
        AppSmsbClient smsbClient = (AppSmsbClient) ctx.get(AppSmsbClient.class); 

        ++totalCount_; 
                
        boolean ownsMsisdn = true; 
        try {
            ownsMsisdn = SubscriberSupport.ownsMSISDN(ctx, sub); 
        } catch (HomeException e){
            ownsMsisdn = false; 
        }
                
        if ( ownsMsisdn ){

            // get account
            Account account = null; 
            if ( sub != null && !SubscriberStateEnum.INACTIVE.equals( sub.getState()) ){
                try {
                    account = (Account) acctHome.find(ctx,sub.getBAN()); 
                } catch (final HomeException e)
                {
                    print(" Account " + sub.getBAN()  + "not found");
                                     
                } 
            }
        
            if ( hasEcpProfile(sub) && hasSms(ctx, sub)) {

                boolean result = false;
                subsProfile7 smsbSub = smsbClient.getSubsProfile(sub.getMSISDN()); 
                    
                if ( smsbSub != null) {
                    result = subCompareWithSmsb( ctx, sub, smsbSub,  account); 
                    if ( !result ){
                        ++errorCount_; 
                    } else {
                        ++noChangeCount_; 
                    }
                    
                } else {
                    print("Subscriber " + sub.getMSISDN() + " doesn't exist on SMSB" + 
                          " subscriber state is " + sub.getState().getDescription() 
                          + " account state is " + account.getState().getDescription()); 
                    ++notExistCount_; 
                }
                    
                if ( !result ){
                    if ( isRepairEnabled()){
                        if ( subSynchWithSmsb( ctx, sub, smsbSub, account, smsbClient)){
                            ++changeCount_; 
                        }else {
                            ++failCount_; 
                        }
                    } 
                }
                
            } else {
                subsProfile7 smsbSub = smsbClient.getSubsProfile(sub.getMSISDN());
                if ( smsbSub != null  ){
                    ++ghostCount_; 
                        
                    print("Subscriber " + sub.getMSISDN() + " is a ghost profile on SMSB" + 
                          " subscriber state is " + sub.getState().getDescription() 
                          + " account state is " + account.getState().getDescription() 
                          + "subscriver type is " + sub.getSubscriberType().getDescription() 
                          + " SMS service " + hasSms(ctx, sub)); 
                } else {
                        
                    if (account == null){
                        account = new Account(); 
                        account.setState( AccountStateEnum.INACTIVE); 
                    }
                        
                    print("Subscriber " + sub.getMSISDN() + " doesn't exist on SMSB" + 
                          " subscriber state is " + sub.getState().getDescription() 
                          + " account state is " + account.getState().getDescription() 
                          + "subscriver type is " + sub.getSubscriberType().getDescription() 
                          + " SMS service " + hasSms(ctx, sub)); 
                    ++noServiceCount_; 
                }
            } 
                
        } else {
            print("Subscriber " + sub.getMSISDN() + "no longer owns MSISDN" + sub.getMSISDN()); 
            ++noServiceCount_; 
            // is msisdn is not belong to this sub.
        }
    }
    
    private short getSubSvcId(PricePlanVersion pricePlan,Context ctx) {
        
        short svcid = -1;
        Map fees = pricePlan.getServiceFees(ctx);
        if (fees != null)
        {
            for (Iterator i = fees.values().iterator(); i.hasNext();)
            {
                ServiceFee2 fee = (ServiceFee2) i.next();
                
                Service service = null;
                try 
                {
                    service = ServiceSupport.getService(ctx, fee.getServiceId());
                } 
                catch (HomeException e) {
                    print("Error retreving sms service");
                    e.printStackTrace();
                }
                if (service != null && service.getType().equals(ServiceTypeEnum.SMS))
                {
                    svcid = (short)fee.getServiceId();
                    break;
                }
            }
        }
        return svcid;
    }
    
    
    public boolean subCompareWithSmsb(Context ctx, 
            Subscriber crmSub, 
            subsProfile7 smsbSub,
            Account account){
        
        String default_msg  = "subscriber MSISDN = " + crmSub.getMSISDN(); 
        String msg =  default_msg;
        boolean  ret = true; 
        
        PricePlanVersion pricePlan;
        AppSmsbClientConfig config;
    

        try
        {
            // TODO 2007-04-16 Improve performance use getRawPricePlanVersion() and remove non subscribed services
            pricePlan = crmSub.getPricePlan(ctx);
            //As of CRM 8.2, Rate Plan information is in Price Plan and doesn't need to be checked per subcsriber
        }
        catch (HomeException e)
        {
            pricePlan = null; 
        }
        if (pricePlan == null)
        {
             print("System error: No price plan associated with subscriber ");
            return false; 
        }

 
        config = (AppSmsbClientConfig)ctx.get(AppSmsbClientConfig.class);
        
        if (config == null)
        {
             print("System error: AppSMSBClientConfig not found in context");
            return false; 
        }
  
        final short svcId = getSubSvcId(pricePlan,ctx);
        final short subSvcId = (svcId != -1) ? svcId : config.getSvcId();
        
        if (!crmSub.getIMSI().equals( smsbSub.imsi)){
            msg = msg.concat( " IMSI (CRM) " + crmSub.getIMSI()  + " (SMSB) " + smsbSub.imsi);             
            ret = false;             
        }
   
           if ( account!=null && account.getSpid() != smsbSub.spid) {
            msg = msg.concat( " spid (CRM) " + account.getSpid() + " (SMSB) " + smsbSub.spid);             
            ret = false; 
        }
        if ( subSvcId != smsbSub.svcid ) {
            msg = msg.concat( " SVCID (CRM) " + subSvcId + "  (SMSB) " + smsbSub.svcid);             
            ret = false; 
        }
        if (config.getSvcGrade(crmSub.getSubscriberType())!= smsbSub.svcGrade ) {
            msg = msg.concat( " SVCGRADE (CRM) " + config.getSvcGrade(crmSub.getSubscriberType()) + "  (SMSB) " + smsbSub.svcGrade);             
            ret = false; 
        }
        if ( !crmSub.getBAN().equals( smsbSub.ban )) {
            msg = msg.concat( " BAN (CRM) " + crmSub.getBAN()  + " (SMSB) " + smsbSub.ban);             
            ret = false;             
        }
        // Skip checking birthdate, gender, language, location, billcycledate and eqtype because they are all empty
        if (config.getTzOffset()  != smsbSub.TzOffset  ) {
            msg = msg.concat( " TimeZoneOffset (CRM) " + config.getTzOffset() + "  (SMSB) " + smsbSub.TzOffset);             
            ret = false; 
        }
        if (config.getRecurDate() !=  smsbSub.recurDate ) {
            msg = msg.concat( " RecurDate (CRM) " + config.getRecurDate() + " (SMSB) " + smsbSub.recurDate );             
            ret = false; 
        }
        if (config.getScpId()  != smsbSub.scpid  ) {
            msg = msg.concat( " SCPID (CRM) " + config.getScpId()  + " (SMSB) " + smsbSub.scpid );             
            ret = false; 
        }
         if ( crmSub.getHlrId() != smsbSub.hlrid ){
            msg = msg.concat( " HLRID (CRM) " + crmSub.getHlrId() + " (SMSB) " + smsbSub.hlrid );             
            ret = false; 
         }
        if ( smsbSub.enable != true ){
            msg = msg.concat( " Enabled (CRM) true " + "(SMSB) " + smsbSub.enable  );             
            ret = false; 
        }

        if ( config.getBarringPlan() != smsbSub.barringplan  ){
            msg = msg.concat( " Barring Plan (CRM) " + config.getBarringPlan()  + " (SMSB) " + smsbSub.barringplan );             
            ret = false; 
         }
        
        if ( msg.equals( default_msg)){
            msg = msg.concat( " SMSB profile is OK"); 
        }
        print( msg); 
          return ret; 
    }
    
    public boolean subSynchWithSmsb(Context ctx, 
            Subscriber crmSub, 
            subsProfile7 smsbSub, 
            Account account,
            AppSmsbClient smsbClient){
        
        PricePlanVersion pricePlan = null;
        AppSmsbClientConfig config;
        int result;


        try
        {
            // TODO 2007-04-13 use getRawPricePlanVersion()
            pricePlan = crmSub.getPricePlan(ctx);
            //As of CRM 8.2, Rate Plan information is in Price Plan and doesn't need to be checked per subcsriber
        }
        catch (HomeException e)
        {
            //throw new AgentException(e.getMessage());
            return false; 
        }
        if (pricePlan == null)
        {
            print("System error: No price plan associated with subscriber");
            return false; 
        }

 
        config = (AppSmsbClientConfig)ctx.get(AppSmsbClientConfig.class);

        if (config == null)
        {
            print("System error: AppSmsbClientConfig not found in context");
            return false; 
        }
        
        final short svcId = getSubSvcId(pricePlan,ctx);
        final short subSvcId = (svcId != -1) ? svcId : config.getSvcId();
        
        // Set BillCycleDate
        short billCycleDate = SmsProvisionAgent.DEFAULT_BILLCYCLEDATE;
        {
            Home billCycleHome = (Home) ctx.get(BillCycleHome.class);
            
            try 
            {
                BillCycle billCycle = (BillCycle) billCycleHome.find(ctx, Integer.valueOf(account.getBillCycleID()));
                
                if (billCycle != null)
                {
                    billCycleDate = (short) billCycle.getDayOfMonth();
                }
            }
            catch (HomeException e)
            {
                // ignore, just use default billcycledate then
            }
        }
        
        // Set Equiment Type
        String eqType = SmsProvisionAgent.DEFAULT_EQTYPE;
        {
            if (TechnologyEnum.GSM == crmSub.getTechnology())
                  {
                    eqType = "G";
                }
                else if (TechnologyEnum.CDMA == crmSub.getTechnology())
                {
                    eqType = "C";
                }
                else
                {
                    eqType = "T";
                }
        }
        Boolean smsbState = AppSmsbClientSupport.mapSmsbState(ctx, crmSub); 
        String groupMSISDN = crmSub.getGroupMSISDN(ctx);
        if( "".equals(groupMSISDN) )
        {
            groupMSISDN = crmSub.getMSISDN();
        }
        if ( smsbSub == null ){
            result = smsbClient.addSubscriber(
                crmSub.getMSISDN(),
                groupMSISDN,
                crmSub.getIMSI(),
                (short)account.getSpid(),
                subSvcId,
                //config.getSvcGrade(crmSub.getSubscriberType()),
                config.getSvcGradeWithVpnCheck(ctx,account,crmSub), //Manda - added this new method call to check for vpn
                crmSub.getBAN(),
                // TODO 2008-08-21 date of birth no longer part of subscriber
                //(crmSub.getDateOfBirth() != null)? new SimpleDateFormat(AppSmsbClient.SIMPLE_DATE_FORMAT_STRING).format(crmSub.getDateOfBirth()):"", // birthdate
                "", // birthdate
                "", // gender
                SmsProvisionAgent.DEFAULT_LANGUAGE, // language
                "", // location
                billCycleDate,
                eqType, // eqtype
                config.getTzOffset(),
                (short) 0,  //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface
                config.getRecurDate(),
                config.getScpId(),
                crmSub.getHlrId(),
                smsbState == null? true: smsbState.booleanValue(), // state
                config.getBarringPlan(),
                "0",
                ""); // incomingSmsCount
        } else {
            result = smsbClient.updateSubscriber(
                    crmSub.getMSISDN(),
                    groupMSISDN,
                    crmSub.getIMSI(),
                    (short)account.getSpid(),
                    subSvcId,
                    //config.getSvcGrade(crmSub.getSubscriberType()),
                    config.getSvcGradeWithVpnCheck(ctx,account,crmSub), //Manda - added this new method call to check for vpn
                    crmSub.getBAN(),
                    "", // birthdate
                    "", // gender
                    "", // language
                    "", // location
                    (short) 1, // billcycledate
                    "", // eqtype
                    config.getTzOffset(),
                    (short) 0, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface
                    config.getRecurDate(),
                    config.getScpId(),
                    crmSub.getHlrId(),
                    smsbState == null? true: smsbState.booleanValue(), // state
                    config.getBarringPlan()
                    );
            
            /*
             * result = smsbClient.updateSubscriber(
                    smsbSub.msisdn,
                    smsbSub.groupMsisdn,
                    smsbSub.imsi,
                    smsbSub.spid,
                    smsbSub.svcid,
                    smsbSub.svcGrade,
                    smsbSub.ban,
                    smsbSub.birthdate,
                    smsbSub.gender,
                    smsbSub.language,
                    smsbSub.location,
                    smsbSub.billcycledate,
                    smsbSub.eqtype,
                    smsbSub.TzOffset,
                    smsbSub.ratePlan,
                    smsbSub.recurDate,
                    smsbSub.scpid,
                    smsbSub.hlrid,
                    true, // state
                    smsbSub.barringplan,
                    Integer.valueOf(smsbSub.outgoingSmsCount).intValue(),
                    Integer.valueOf(smsbSub.incomingSmsCount).intValue()); // incomingSmsCount
                */
        }
 
        if (result != 0)
        {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SMSB_ERROR).log(ctx);
                print("SMSB synchronization failed. " +  result);
                return false; 
        }

        return true; 
    }
    
    
    public boolean hasSms(Context ctx, Subscriber sub) {

        Home serviceHome = (Home) ctx.get(ServiceHome.class); 
        for ( Iterator i = sub.getServices().iterator(); i.hasNext();){
            Long id = (Long) i.next();
            try {
                if (id != null)
                {
                    Service service = (Service) serviceHome.find(ctx,id); 
                    if ( service != null && service.getHandler().equals( SERVICE_SMS)){
                        return true; 
                    }
                }
            } catch (HomeException e){
                
            }
            
        }
        
        return false; 
    }
    
    // if sub is prepaid, then smsb should keep a sub profile. 
    public boolean hasEcpProfile(Subscriber sub) {

        if (sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)){
             return true;             
         } 

         if ( sub.getState().equals( SubscriberStateEnum.INACTIVE)){
             return false; 
         }
         
        return true; 
    }


    private int totalCount_ = 0; 
    private int notExistCount_ =0;
    private int errorCount_ = 0; 
    private int changeCount_ = 0; 
    private int noChangeCount_ = 0; 
    private int ghostCount_ = 0; 
    private int failCount_ = 0; 
    private int noServiceCount_ = 0; 

}
