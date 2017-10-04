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
package com.trilogy.app.crm.poller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingComponentPlaceEnum;
import com.trilogy.app.crm.bean.ComponentCharge;
import com.trilogy.app.crm.bean.DayCategoryEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.AppliedSpecialChargeIndicatorEnum;
import com.trilogy.app.crm.bean.calldetail.CGPAPresentationRestrictedEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bean.calldetail.TeleserviceTypeEnum;
import com.trilogy.app.crm.bean.calldetail.VPNCallTypeEnum;
import com.trilogy.app.crm.bean.core.ChargingComponents;
import com.trilogy.app.crm.bean.core.ChargingComponentsConfig;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.home.UsageTypePreventDefaultItemDeleteHome;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.NumberMgmtHistorySupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xhome.support.StringUtil;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Creates a Call detail based on the ER 501
 *
 * @author arturo.medina@redknee.com
 */
public class URSCallDetailCreator implements CallDetailCreator, Constants
{

    public URSCallDetailCreator()
    {
        this(false);
    }

    public URSCallDetailCreator(boolean chargedMsisdnIsImsi)
    {
        super();
        chargedMsisdnIsImsi_ = chargedMsisdnIsImsi;
    }

    public CallDetail createCallDetail(final Context ctx, final ProcessorInfo info, final List params)
        throws ParseException, HomeInternalException, HomeException, AgentException
    {
        try {
            CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
        } catch ( FilterOutException e){
            return null; 
        }

        // drop ER when the billing option is "discard"
        if (DISCARD.equals(CRMProcessorSupport.getField(params, URS_BILLING_OPTION).toLowerCase()))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final String msg = "Billing Option is " + CRMProcessorSupport.getField(params, URS_BILLING_OPTION)
                        + ".  No CDR generation required.";
                new DebugLogMsg(this, msg, null).log(ctx);
            }
            return null;
        }

        if (!("2".equals(CRMProcessorSupport.getField(params, URS_EVENTTYPE_INDEX))))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Event Type is " + CRMProcessorSupport.getField(params, URS_EVENTTYPE_INDEX)
                        + ".  No CDR generation required.", null).log(ctx);
            }
            return null;
        }

        final String chargedMsisdn;
        final Date transDate = CRMProcessorSupport.getDate(CRMProcessorSupport.getField(params, URS_DATE_OF_CALL) + " "
                + CRMProcessorSupport.getField(params, URS_TIME_OF_CALL));

        if( !isChargedMsisdnAnImsi() )
        {
            // Non-roaming scenario.  ER 501 contains the actual charged MSISDN.
            chargedMsisdn = CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, URS_CHARGEDMSISDN_INDEX));
        }
        else
        {
            // HLD OID 38543: When the poller is configured as URS ROAMING poller, then
            // the Charged MSISDN field in the ER501 will be an IMSI.
            String imsi = CRMProcessorSupport.getField(params, URS_CHARGEDMSISDN_INDEX);
            chargedMsisdn = getChargedMsisdnFromImsi(ctx, imsi, transDate);
        }
        
        final CallDetail t = new CallDetail();

        CallDetailConfig config = (CallDetailConfig) ctx.get(CallDetailConfig.class);
        
        // we don't use TransID anymore.

        // prins all the parameters
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, getDebugParams(params), null).log(ctx);
        }
        t.setTranDate(transDate);
        t.setPostedDate(new Date());
        t.setChargedMSISDN(chargedMsisdn);
        t.setOmsc(CRMProcessorSupport.getField(params, URS_ORIGINATING_MSC_ID_INDEX));
        String defaultGlCode = "";
        if( !isChargedMsisdnAnImsi() )
        {
            // Non-roaming scenario.  ER 501 contains the actual originating MSISDN.
            t.setOrigMSISDN(CRMProcessorSupport.getField(params, URS_ORIGMSISDN_INDEX));
        }
        else
        {
            // HLD OID 38543: When polling, the IMSI will get mapped to a Subscriber-ID, 
            // using the IMSI/SubID history table.  That subscriber's current MSISDN will 
            // then become the chargedMSISDN and originatingMSISDN of the calldetail.
            t.setOrigMSISDN(chargedMsisdn);
            
          // default gl code used for roaming records
            defaultGlCode = config.getRoamingGLCode();
        }

        t.setDestMSISDN(CRMProcessorSupport.getField(params, URS_DESTNUMBER_INDEX));
        t.setRedirectedAddress(CRMProcessorSupport.getField(params, URS_REDIRECTED_ADDRESS_INDEX));
        t.setCallingPartyLocation(CRMProcessorSupport.getField(params, URS_ORIGLOCZONEDESC_INDEX));        
        final int seconds = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_DURATION_INDEX), 0);
        t.setDuration(new Time(0, 0, seconds));
        t.setFlatRate(CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, URS_FLATRATE_INDEX), -1));
        t.setVariableRate(CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, URS_VARIABLERATE_INDEX), -1));
        t.setVariableRateUnit(getVariableRateUnit(CRMProcessorSupport.getField(params, URS_VARIABLERATEUNIT_INDEX)));
        t.setCharge(CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, URS_RUNNINGCALLCOST_INDEX), 0));
        t.setBalance(CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, URS_BALANCE_INDEX), 0));

        final String location = CRMProcessorSupport.getField(params, URS_ORIGLOCZONED_INDEX);

        final String[] locationInfo = parseLocationInfo(location);

        t.setLocationType(Integer.parseInt(locationInfo[0]));
        t.setLocation(locationInfo[1]);
        t.setUsageType(UsageTypePreventDefaultItemDeleteHome.DEFAULT);
        t.setUsedMinutes(CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_BUCKETRATINGBALANCE_INDEX), 0));
        
        final CRMSpid crmSpid;
        {
            int spid = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_SPID_INDEX), -1);
            crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            if(null == crmSpid)
            {
                throw new ParseException("SPID [" +   spid +    "] not valid ", URS_SPID_INDEX);
            }
        }
        t.setSpid(crmSpid.getSpid());
        t.setRatePlan(CRMProcessorSupport.getField(params, URS_RATEPLAN_INDEX));
        t.setRatingRule(CRMProcessorSupport.getField(params, URS_RATINGRULE_INDEX));
        t.setTransactionSourceId(CRMProcessorSupport.getField(params, URS_CALL_REFERENCE_ID));
        //as tt6040733019, change from URS_CALL_REFERENCE_ID to URS_CALLSESSIONID_INDEX
        t.setCallID(CRMProcessorSupport.getField(params, URS_CALLSESSIONID_INDEX));

        final String chargedParty = CRMProcessorSupport.getField(params, URS_CHARGEDPARTY_INDEX);


        t.setGLCode(CRMProcessorSupport.getField(params, URS_GLCODE_INDEX, defaultGlCode));
        t.setDisconnectReason(CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_DISCONNECTREASON_INDEX), 0));

        // Bucket Rate ID is optional.
        final String bucketRateIdStr = CRMProcessorSupport.getField(params, URS_BUCKETRATEID_INDEX).trim();
        if (bucketRateIdStr.length() != 0)
        {
            t.setBucketRateID(CRMProcessorSupport.getInt(ctx, bucketRateIdStr,
                    com.redknee.app.crm.bean.calldetail.CallDetail.DEFAULT_BUCKETRATEID));
        }

        //Prasanna: Removed the FF relaed code as from 7 onwards this data comes in List of discounts,
        //parseDiscount method is used now.

        // Restrict the called numbers ID to be shown in the invoice.
        int index = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_CGPA_PRESENTATION_RESTRICTED), -1);
        if (index >= 0 && index <= 1)
        {
            t.setRestricted(CGPAPresentationRestrictedEnum.get((short) index));
        }
        else
        {
            throw new AgentException("Invalid CGPA Presentation Restricted: " + index + ". Cannot continue.");
        }

        // Account type of subscriber payment
        index = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_ACCOUNT_TYPE), -1);
        if (index >= 0 && index <= 1)
        {
            t.setSubscriberType(SubscriberTypeEnum.get((short) index));
        }
        else
        {
            throw new AgentException("Invalid Account Type: " + index + ". Cannot continue.");
        }

        // STring information extract from rate rule
        t.setBillingOption(CRMProcessorSupport.getField(params, URS_BILLING_OPTION));

        t.setTimeBandType(CRMProcessorSupport.getField(params, URS_TIMEBAND_TYPE));

        t.setDayCategory(getDayCategory(CRMProcessorSupport.getField(params, URS_DAY_CATEGORY)));

        t.setTeleserviceType(TeleserviceTypeEnum.get(
                (short) CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_TELESERVICE_TYPE), 0)));
        
        setCallType(ctx, t, chargedParty, seconds);

//***** HLD ObjID 5970,TT6050233855
        final int bucketCounter = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, URS_BUCKET_COUNTER), 0);
        t.setBucketCounter(bucketCounter);
        t.setBucketDecrement(setBucketDecrementField(ctx, bucketCounter));
//*****

        String listOfAmounts = CRMProcessorSupport.getField(params, URS_LIST_OF_AMOUNTS);
        if (listOfAmounts == null)
        {
            listOfAmounts = "";
        }
        final Map amounts = parseAmounts(listOfAmounts);

        final String strAirMsg = (String) amounts.get(config.getAirMsg());
        if (StringUtil.validString(strAirMsg))
        {
            t.setAir(CRMProcessorSupport.getLong(ctx, strAirMsg, 0));
        }

        final String strTollMsg = (String) amounts.get(config.getTollMsg());
        if (StringUtil.validString(strTollMsg))
        {
            t.setToll(CRMProcessorSupport.getLong(ctx, strTollMsg, 0));
        }

        final String strTaxMsg = (String) amounts.get(config.getTaxMsg());
        if (StringUtil.validString(strTaxMsg))
        {
            t.setTax(CRMProcessorSupport.getLong(ctx, strTaxMsg, 0));
        }

        String listOfDiscounts = CRMProcessorSupport.getField(params, URS_LIST_OF_DISCOUNTS);
        if (listOfDiscounts == null)
        {
            listOfDiscounts = "";
        }
        parseDiscounts(ctx, t, listOfDiscounts);

        // Check if its a rerated ER
        if ("1".equals(CRMProcessorSupport.getField(params, URS_RERATE_FLAG)))
        {
            // Call detail was rerated - set rerated flag to true
            t.setRerated(true);
        }
        
        String BAN = CRMProcessorSupport.getField(params, URS_BAN_ID, "");
        t.setBAN(BAN);
        
        String subscriptionType = CRMProcessorSupport.getField(params, URS_SUBSCRIPTION_TYPE, "");
        if (! "".equals(subscriptionType))
        {
            t.setSubscriptionType(StringUtil.getInt(subscriptionType));
        }
        
        // set charging components only if they are available for the SPID
        if(crmSpid.isEnableChargingComponents())
        {
            String allComponentString = CRMProcessorSupport.getField(params, URS_CHARGING_COMPONENTS, "");
            if (allComponentString.length() == 0)
            {
                throw new ParseException("Could not parse Charging Component Field [" + allComponentString + "]",
                        URS_CHARGING_COMPONENTS);
            }
            final ChargingComponents chargingComponents = new ChargingComponents().setAllComponentsFromString(
                    allComponentString, crmSpid.getChargingComponentsConfig(ctx));
            setChargingComponents(t, chargingComponents);
        }

        
        
        return t;
    }
    
    private CallDetail setChargingComponents(CallDetail callDetail, ChargingComponents components)
    {
        setFirstComponent(callDetail, ChargingComponentPlaceEnum.FIRST.getComponent(components));
        setSecondComponent(callDetail, ChargingComponentPlaceEnum.SECOND.getComponent(components));
        setThirdComponent(callDetail, ChargingComponentPlaceEnum.THIRD.getComponent(components));
        return callDetail;
    }
    
    private CallDetail setFirstComponent(CallDetail callDetail, ComponentCharge charge)
    {
        callDetail.setComponentCharge1(charge.getCharge());
        callDetail.setComponentGLCode1(charge.getGlCode());
        callDetail.setComponentRate1(charge.getRate());
        return callDetail;
    }

    private CallDetail setSecondComponent(CallDetail callDetail, ComponentCharge charge)
    {
        callDetail.setComponentCharge2(charge.getCharge());
        callDetail.setComponentGLCode2(charge.getGlCode());
        callDetail.setComponentRate2(charge.getRate());
        return callDetail;
    }
    
    private CallDetail setThirdComponent(CallDetail callDetail, ComponentCharge charge)
    {
        callDetail.setComponentCharge3(charge.getCharge());
        callDetail.setComponentGLCode3(charge.getGlCode());
        callDetail.setComponentRate3(charge.getRate());
        return callDetail;
    }
    /**
     * Translates an IMSI to the correct MSISDN for the given transaction date.
     * 
     * HLD OID 38543: When the poller is configured as URS ROAMING poller, then
     * the Charged MSISDN field in the ER501 will be an IMSI.  When polling, that 
     * IMSI will get mapped to a Subscriber-ID, using the IMSI/SubID history table.  
     * That subscriber's current MSISDN will then become the chargedMSISDN and 
     * originatingMSISDN of the calldetail.
     * 
     * @param ctx FW Context
     * @param transDate Transaction date
     * @param imsi IMSI
     * @return
     * @throws HomeException
     * @throws AgentException
     * @throws ParseException
     */
    private String getChargedMsisdnFromImsi(final Context ctx, final String imsi, final Date transDate) throws HomeException,
            AgentException, ParseException
    {
        // Get the subscriber ID for the IMSI on the transaction date.
        String subId = NumberMgmtHistorySupportHelper.get(ctx).lookupBestSubscriberIDFromImsiHistory(ctx, imsi, transDate);
        if( subId == null )
        {
            throw new AgentException("No corresponding subscriber found for IMSI " + imsi + " for transaction date " + transDate + ". Cannot continue.");
        }
        
        // Use the subscriber's current MSISDN.
        Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);
        if( subId == null )
        {
            throw new AgentException("No subscriber found with Subscriber ID " + subId + " for IMSI " + imsi + " for transaction date " + transDate + ". Cannot continue.");
        }
        return CRMProcessorSupport.getMsisdn(subscriber.getMSISDN());
    }


    /**
     * Sets the call detail type depending on the different situations:
     * The default type is ORIG
     * if charged party == T the call detail is TERM
     * if teleservice type == SMS the call type is ROAMING_SMS
     * if teleservice type == DATA the call type is WEB and the duration is the same as the duration in the call detail
     * @param ctx
     * @param t
     * @param chargedParty
     */
    private void setCallType(Context ctx, CallDetail t, String chargedParty, int seconds)
    {
        t.setCallType(CallTypeEnum.ORIG);

        if (chargedParty != null)
        {
            if ("T".equals(chargedParty))
            {
                t.setCallType(CallTypeEnum.TERM);
                return;
            }
        }
        
        switch (t.getTeleserviceType().getIndex())
        {
            case (TeleserviceTypeEnum.SMS_INDEX):
            {
                t.setCallType(CallTypeEnum.ROAMING_SMS);
                break;
            }
            case (TeleserviceTypeEnum.DATA_INDEX):
            {
                t.setCallType(CallTypeEnum.WEB);
                t.setDataUsage(seconds);
                break;
            }
            default:
        }
    }


    private boolean setBucketDecrementField(final Context ctx, final int bucketCounter)
    {
        if (bucketCounter > 0)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,
                        "Setting the CD.BucketDecrement field to true [ER.BucketCounter: " + bucketCounter + " > 0]",
                        null).log(ctx);
            }
            return true;
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,
                        "Setting the CD.BucketDecrement field to false [ER.BucketCounter: " + bucketCounter + "]",
                        null).log(ctx);
            }
            return false;
        }
    }


    /**
     * Parses a string of the forma n1=v1|n2=v2|n3=v3 into a map, mapping each nk to vk.
     *
     * @param listOfAmounts
     * @return a map of nk to vk
     */
    protected Map parseAmounts(final String listOfAmounts)
    {
        final Map ret = new HashMap();
        final StringSeperator sep = new StringSeperator(listOfAmounts, '|');

        while (sep.hasNext())
        {
            final String amount = sep.next();

            final int pos = amount.indexOf('=');
            if (pos != -1)
            {
                final String name = amount.substring(0, pos);
                final String value = amount.substring(pos + 1);

                ret.put(name, value);
            }
        }

        return ret;
    }

    /**
     * Parses a string of the Discounts in the format
     * "VPN"|<VPN_Indicator>|<VPN_BAN>         |<VPN_CallType>|<VPN_Discount>         |<VPN_Total_Saving>|
     * "FF" |<FF_Indicator> |<FF CUG or PLP Id>|<FF_Discount> |<FF_Discount_Rate_Plan>|<FF_Total_Saving> |
     * "HZ" |<HZ_Indicator> |<HZ_ZoneID>       |<HZ_Discount> |<HZ_Total_Saving>
     *
     * @param listOfDiscounts
     */
    protected void parseDiscounts(final Context ctx, final CallDetail t, final String listOfDiscounts)
        throws AgentException
    {
        final StringSeperator sep = new StringSeperator(listOfDiscounts, '|');

        if (sep.hasNext())
        {
            //VPN FIELDS
            //Ignore first element as it is "VPN"
            sep.next();
            //All the VPN  Fields
            t.setVpn_Discount_On(CRMProcessorSupport.getInt(ctx, sep.next(), -1));
            t.setVpn_BAN(sep.next());
            t.setVpn_Call_type(VPNCallTypeEnum.get((short) CRMProcessorSupport.getInt(ctx, sep.next(), 0)));
            t.setVpn_Discount(CRMProcessorSupport.getInt(ctx, sep.next(), -1));
            t.setVpn_Total_Saving(CRMProcessorSupport.getLong(ctx, sep.next(), -1));

            //FF FIELDS
            //Ignore first element as it is "FF"
            sep.next();
            //All the FF Fields
            final int index = CRMProcessorSupport.getInt(ctx, sep.next(), -1);
            if (index >= 0 && index <= 6)
            {
                t.setApplSpecialChargeInd(AppliedSpecialChargeIndicatorEnum.get((short) index));
            }
            else
            {
                throw new AgentException("Invalid Applied Special Charge Indicator: " + index + ". Cannot continue.");
            }
            t.setPlpCugID(CRMProcessorSupport.getInt(ctx, sep.next(), 0));
            t.setApplPercentageDiscount(CRMProcessorSupport.getInt(ctx, sep.next(), 0));
            //following 2 fields are only stored in the db now...not used anywhere.
            t.setFf_Disc_Rate_Plan(sep.next());
            t.setFf_Total_Saving(CRMProcessorSupport.getLong(ctx, sep.next(), -1));

            //HOMEZONE FIELDS
            //Ignore first element as it is "HZ"
            sep.next();
            //All the homezone fields
            t.setHz_Discount_On(CRMProcessorSupport.getInt(ctx, sep.next(), -1));
            t.setHz_Zone_ID(CRMProcessorSupport.getInt(ctx, sep.next(), -1));
            t.setHz_Discount(CRMProcessorSupport.getInt(ctx, sep.next(), -1));
            t.setHz_Total_Saving(CRMProcessorSupport.getLong(ctx, sep.next(), -1));
        }
    }

    /**
     * Parses out the location info into two fields.
     * 1st field: Location Type
     * 2nd field: Location
     *
     * ex: 2|338050211057162 is parsed into Location 338050211057162 and Location Type 2
     *
     * @param locationInfo
     * @return Array containing in order: location type, location
     * @throws AgentException
     */
    protected String[] parseLocationInfo(final String locationInfo) throws AgentException
    {
        final StringSeperator sep = new StringSeperator(locationInfo, '|');
        if (!sep.hasNext())
        {
            throw new AgentException("Did not find pipe character in location info.");
        }

        final String[] returnList = new String[2];

        returnList[0] = sep.next();
        returnList[1] = sep.next();

        // We wont check if it has actually more than 2 fields
        // in case someone decides to add more location info

        return returnList;
    }

    /**
     * Maps the result on the ER to a DayCategory Enum
     *
     * @param field
     * @return
     */
    private DayCategoryEnum getDayCategory(final String field)
    {
        if (field.equalsIgnoreCase("N"))
        {
            return DayCategoryEnum.NORMAL;
        }

        if (field.equalsIgnoreCase("I"))
        {
            return DayCategoryEnum.INDEPENDENT;
        }

        if (field.equalsIgnoreCase("P"))
        {
            return DayCategoryEnum.PUBLIC_HOLIDAY;
        }

        if (field.equalsIgnoreCase("R"))
        {
            return DayCategoryEnum.REDUCED;
        }

        return DayCategoryEnum.ANY;
    }


    protected String getDebugParams(final List _params)
    {
        final Iterator iParams = _params.iterator();
        int index = 0;

        final StringBuilder buf = new StringBuilder();
        while (iParams.hasNext())
        {
            buf.append(index);
            buf.append("[");
            buf.append(CRMProcessorSupport.getField(_params, index));
            buf.append("] ");

            iParams.next();
            index++;
        }

        return buf.toString();
    }

    /**
     * Converts from a String to a RateUnitEnum object
     *
     * @param unit the name of the unit
     * @return the enum coresponding to the name
     */
    private RateUnitEnum getVariableRateUnit(final String unit)
    {
        if (unit == null)
        {
            return RateUnitEnum.MIN;
        }

        if ("minute".equalsIgnoreCase(unit))
        {
            return RateUnitEnum.MIN;
        }

        return RateUnitEnum.SEC;
    }

    public List<CallDetail> createCallDetails(final Context ctx, final ProcessorInfo info, final List params)
        throws ParseException, HomeException, AgentException
    {
        final List<CallDetail> list = new ArrayList<CallDetail>();
        final CallDetail detail = createCallDetail(ctx, info, params);
        if (detail != null)
        {
            list.add(detail);
        }
        return list;
    }

    
    public boolean isChargedMsisdnAnImsi()
    {
        return chargedMsisdnIsImsi_;
    }
    
    private CallDetail fillChargingComponents(Context ctx, CallDetail callDetail, final List params) throws HomeException
    {
        final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, callDetail.getSpid());
        if (null == crmSpid)
        {
            throw new HomeException("CRM SPID with ID [" + crmSpid + "] not found.");
        }
        if (crmSpid.isEnableChargingComponents())
        {
            final ChargingComponentsConfig config = crmSpid.getChargingComponentsConfig(ctx);
            //CRMProcessorSupport.getField(params, index, defaultValue)
        }
        
        return callDetail;
    }
    
    private boolean chargedMsisdnIsImsi_ = false;

    private static final String DISCARD = "discard";

}
