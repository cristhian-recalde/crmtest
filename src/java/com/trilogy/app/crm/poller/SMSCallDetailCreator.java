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
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingComponentPlaceEnum;
import com.trilogy.app.crm.bean.ComponentCharge;
import com.trilogy.app.crm.bean.SmsbErIndicesConfig;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bean.calldetail.TeleserviceTypeEnum;
import com.trilogy.app.crm.bean.calldetail.VPNCallTypeEnum;
import com.trilogy.app.crm.bean.core.ChargingComponents;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * Creates a Call detail based on the ER 311 SMSB
 *
 * @author amedina
 */
public class SMSCallDetailCreator implements CallDetailCreator, Constants
{

    public SMSCallDetailCreator(Context ctx)
    {
        super();

        confIndices = (SmsbErIndicesConfig) ctx.get(SmsbErIndicesConfig.class);
        if (confIndices == null)
        {
            new EntryLogMsg(13147, this, "", "", new String[]{confIndices.toString()}, null).log(ctx);
            LogSupport.crit(ctx, this,
                    "SMSB ER Indices are not correctly configured,Polling of ER311 will not be successfull.");
        }
        else
        {
            validIndices = setErIndices(ctx, confIndices);   // Safe to call in the Constructor ??
        }
    }

    public CallDetail createCallDetail(Context ctx, ProcessorInfo info,
                                       List params) throws ParseException, HomeException, AgentException
    {
    	try {
    		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
       	} catch ( FilterOutException e){
			return null; 
		}

        new DebugLogMsg(this, "\n\n\t Params after processing = " + params + "\n\n", null).log(ctx);


        CallDetail t = new CallDetail();

        if (validIndices)
        {
            Date transDate = CRMProcessorSupport.getDate(CRMProcessorSupport.getField(params, SMSB_LOCAL_SUBSCRIBER_DATE) + " "
                    + CRMProcessorSupport.getField(params, SMSB_LOCAL_SUBSCRIBER_TIME));
            String chargedMsisdn = CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX));

            if (SMSB_CHARGED_PARTY_CONFIGURED)
            {
                t.setChargedParty(CRMProcessorSupport.getField(params, SMSB_CHARGED_PARTY));
            }
            else
            {
                t.setChargedParty(confIndices.getChargedPartyDefault());
            }

            if (SMSB_ORIG_SVC_GRADE_CONFIGURED)
            {
                t.setOrigSvcGrade(
                        CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, SMSB_ORIG_SVC_GRADE), ER_POSTPAID));
            }
            else
            {
                t.setOrigSvcGrade(CRMProcessorSupport.getInt(ctx, confIndices.getOrigSvcGradeDefault(), ER_POSTPAID));
            }

            if (SMSB_TERM_SVC_GRADE_CONFIGURED)
            {
                t.setTermSvcGrade(
                        CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, SMSB_TERM_SVC_GRADE), ER_POSTPAID));
            }
            else
            {
                t.setTermSvcGrade(CRMProcessorSupport.getInt(ctx, confIndices.getTermSvcGradeDefault(), ER_POSTPAID));
            }

            t.setTranDate(transDate);
            t.setPostedDate(new Date());
            t.setChargedMSISDN(chargedMsisdn);
            //t.setOrigMSISDN(CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX)));
            //t.setDestMSISDN(CRMProcessorSupport.getField(params, SMSB_OTHERMSISDN_INDEX));
            //addded for alphanumeric Support
            String chargedParty = "";
            if (SMSB_CHARGED_PARTY_CONFIGURED)
            {
                chargedParty = CRMProcessorSupport.getField(params, SMSB_CHARGED_PARTY);
            }
            else
            {
                chargedParty = confIndices.getChargedPartyDefault();
            }

            if (chargedParty.equals(ER_MO_SMS))
            {
                t.setOrigMSISDN(CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX)));
                t.setChargedMSISDN(CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX)));
                t.setDestMSISDN(CRMProcessorSupport.getField(params, SMSB_OTHERMSISDN_INDEX));
            }
            else if (chargedParty.equals(ER_MT_SMS))
            {
                t.setOrigMSISDN(CRMProcessorSupport.getField(params, SMSB_OTHERMSISDN_INDEX));
                t.setChargedMSISDN(CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX)));
                t.setDestMSISDN(CRMProcessorSupport.getMsisdn(CRMProcessorSupport.getField(params, SMSB_CHARGEDMSISDN_INDEX)));
            }

            if (SMSB_ORIGMSCID_INDEX_CONFIGURED)
            {
                t.setCallingPartyLocation(CRMProcessorSupport.getField(params, SMSB_ORIGMSCID_INDEX));
            }
            else
            {
                t.setCallingPartyLocation(confIndices.getOrigMscIdDefault());
            }

            long rate;
            if (SMSB_RATE_INDEX_CONFIGURED)
            {
                rate = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, SMSB_RATE_INDEX), 0);
            }
            else
            {
                rate = Long.parseLong(confIndices.getRateDefault());
            }
            t.setVariableRate(rate);
            t.setVariableRateUnit(RateUnitEnum.MSG);
            t.setCharge(rate);

            if (SMSB_BALANCE_INDEX_CONFIGURED)
            {
                t.setBalance(CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, SMSB_BALANCE_INDEX), 0));
            }
            else
            {
                t.setBalance(Long.parseLong(confIndices.getBalanceDefault()));
            }

            final int spid;
            if (SMSB_SPID_INDEX_CONFIGURED)
            {
                spid = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, SMSB_SPID_INDEX), -1);
                
            }
            else
            {
                spid = Integer.parseInt(confIndices.getSpidDefault());
            }
            t.setSpid(spid);
            final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            if (null == crmSpid)
            {
                throw new ParseException("No CRM-SPID could be found for spid value [" + spid + "]", SMSB_SPID_INDEX);
            }
            if(crmSpid.isEnableChargingComponents())
            {
                String allComponentString = CRMProcessorSupport.getField(params, SMSB_CHARGING_COMPONENTS_ER_INDEX, "");
                if (allComponentString.length() == 0)
                {
                    throw new ParseException("Could not parse Charging Component Field [" + allComponentString + "]",
                            SMSB_CHARGING_COMPONENTS_ER_INDEX);
                }
                final ChargingComponents chargingComponents = new ChargingComponents().setAllComponentsFromString(
                        allComponentString, crmSpid.getChargingComponentsConfig(ctx));
                setChargingComponents(t, chargingComponents);
            }
            if (SMSB_RATEPLANID_INDEX_CONFIGURED)
            {
                t.setRatePlan(CRMProcessorSupport.getField(params, SMSB_RATEPLANID_INDEX));
            }
            else
            {
                t.setRatePlan(confIndices.getRateplanIdDefault());
            }

            if (SMSB_RATINGRULE_INDEX_CONFIGURED)
            {
                t.setRatingRule(CRMProcessorSupport.getField(params, SMSB_RATINGRULE_INDEX));
            }
            else
            {
                t.setRatingRule(confIndices.getRatingRuleDefault());
            }

            t.setCallType(CallTypeEnum.SMS);

            if (SMSB_SEQUENCE_NUM_CONFIGURED)
            {
                t.setCallID(CRMProcessorSupport.getField(params, SMSB_SEQUENCE_NUM));
            }
            else
            {
                t.setCallID(confIndices.getSequenceNumDefault());
            }

            if (SMSB_BAN_CONFIGURED)
            {
                t.setBAN(CRMProcessorSupport.getField(params, SMSB_BAN));
            }
            else
            {
                t.setBAN(confIndices.getBanDefault());
            }

            t.setTeleserviceType(TeleserviceTypeEnum.SMS);

            CallDetailConfig tconfig = (CallDetailConfig) ctx.get(CallDetailConfig.class);

            if (tconfig == null)
            {
                new MajorLogMsg(this, "CallDetailConfig bean not found in context", null).log(ctx);
                return null;
            }

            String glCode = tconfig.getSmsbGLCode();
            if (glCode == null || glCode.trim().length() == 0)
            {
                new MajorLogMsg(this, "GLCode from CallDetailconfiguration is null or invalid. Cannot continue.", null)
                        .log(ctx);
                return null;
            }

            t.setGLCode(tconfig.getSmsbGLCode());

            String origLargeAcctId = "";
            String destLargeAcctId = "";
            if (SMSB_ORIG_LARGE_ACCT_ID_CONFIGURED)
            {
                origLargeAcctId = CRMProcessorSupport.getField(params, SMSB_ORIG_LARGE_ACCT_ID);
            }
            else
            {
                origLargeAcctId = confIndices.getOrigLargeActIdDefault();
            }

            if (SMSB_DEST_LARGE_ACCT_ID_CONFIGURED)
            {
                destLargeAcctId = CRMProcessorSupport.getField(params, SMSB_DEST_LARGE_ACCT_ID);
            }
            else
            {
                destLargeAcctId = confIndices.getDestLargeActIdDefault();
            }

            if (origLargeAcctId != null && origLargeAcctId.trim().length() > 0)
            {
                t.setBillingOption(origLargeAcctId);
            }
            else if (destLargeAcctId != null && destLargeAcctId.trim().length() > 0)
            {
                t.setBillingOption(destLargeAcctId);
            }

            setBucketDecrement(ctx, t, params, confIndices);

            /*
            * Adding the VPN  fields
            * VPN_Called,vpn_call_type, vpn_discount, vpn_ban, vpn_total_saving,
            * Not adding vpn_billindn which comes from the ER.
            * * All the VPN fields/columns are retrieved only if the vpn_called field
            * has a value of 1.
            * Reference : Refer to section 9.1.3, object 814 of the SMSB HLD for details
            */
            String vpnCalled = "";

            if (SMSB_VPN_CALLED_CONFIGURED)
            {
                vpnCalled = CRMProcessorSupport.getField(params, SMSB_VPN_CALLED);
            }
            else
            {
                vpnCalled = confIndices.getVpnCalledDefault();
            }

            if ((vpnCalled != null && !vpnCalled.equals(""))
                    && (vpnCalled.equals(SMSB_VPN_CALLED_VALID)))
            {
                if (SMSB_VPN_CALL_TYPE_CONFIGURED)
                {
                    t.setVpn_Call_type(VPNCallTypeEnum.get((short) CRMProcessorSupport.getInt(ctx,
                            CRMProcessorSupport.getField(params, SMSB_VPN_CALL_TYPE), 0)));
                }
                else
                {
                    t.setVpn_Call_type(VPNCallTypeEnum.get((short) CRMProcessorSupport.getInt(ctx,
                            confIndices.getVpnCallTypeDefault(), 0)));
                }

                if (SMSB_VPN_DISCOUNT_CONFIGURED)
                {
                    t.setVpn_Discount(CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, SMSB_VPN_DISCOUNT), -1));
                }
                else
                {
                    t.setVpn_Discount(CRMProcessorSupport.getInt(ctx, confIndices.getVpnDiscountDefault(), -1));
                }

                if (t.getVpn_Discount() > 0)
                {
                    t.setVpn_Discount_On(1);
                }
                else
                {
                    t.setVpn_Discount_On(0);
                }


                long vpnSavings = (t.getCharge() * t.getVpn_Discount()) / (100 - t.getVpn_Discount());
                t.setVpn_Total_Saving(vpnSavings);

                String vpnBillingDn = "";
                if (SMSB_VPN_BILLINGDN_CONFIGURED)
                {
                    vpnBillingDn = CRMProcessorSupport.getField(params, SMSB_VPN_BILLINGDN);
                }
                else
                {
                    vpnBillingDn = confIndices.getVpnBillingDnDefault();
                }

                if (vpnBillingDn != null & vpnBillingDn.length() > 0)
                {
                    t.setVpn_BAN(vpnBillingDn);
                }
                else
                {
                    t.setVpn_BAN(t.getBAN());
                }
            }

        }
        else
        {
            LogSupport.minor(ctx, this, "Configured ER Indices required to parse SMS ER311 are invalid.");
            return null;
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

    // Should it be synchronized ?
    private boolean setErIndices(final Context ctx, final SmsbErIndicesConfig confIndices)
    {
        boolean isSuccess = false;
        try
        {
            if (confIndices.getLocalSubDate() != "" && confIndices.getLocalSubDate().length() > 0)
            {
                SMSB_LOCAL_SUBSCRIBER_DATE = Integer.parseInt(confIndices.getLocalSubDate());
                SMSB_LOCAL_SUBSCRIBER_DATE_CONFIGURED = true;
            }
            else
            {
                SMSB_LOCAL_SUBSCRIBER_DATE_CONFIGURED = false;
            }

            if (confIndices.getLocalSubTime() != "" && confIndices.getLocalSubTime().length() > 0)
            {
                SMSB_LOCAL_SUBSCRIBER_TIME = Integer.parseInt(confIndices.getLocalSubTime());
                SMSB_LOCAL_SUBSCRIBER_TIME_CONFIGURED = true;
            }
            else
            {
                SMSB_LOCAL_SUBSCRIBER_TIME_CONFIGURED = false;
            }

            if (confIndices.getChargedMsisdn() != "" && confIndices.getChargedMsisdn().length() > 0)
            {
                SMSB_CHARGEDMSISDN_INDEX = Integer.parseInt(confIndices.getChargedMsisdn());
                SMSB_CHARGEDMSISDN_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_CHARGEDMSISDN_INDEX_CONFIGURED = false;
            }

            if (confIndices.getChargedParty() != "" && confIndices.getChargedParty().length() > 0)
            {
                SMSB_CHARGED_PARTY = Integer.parseInt(confIndices.getChargedParty());
                SMSB_CHARGED_PARTY_CONFIGURED = true;
            }
            else
            {
                SMSB_CHARGED_PARTY_CONFIGURED = false;
            }

            if (confIndices.getOrigSvcGrade() != "" && confIndices.getOrigSvcGrade().length() > 0)
            {
                SMSB_ORIG_SVC_GRADE = Integer.parseInt(confIndices.getOrigSvcGrade());
                SMSB_ORIG_SVC_GRADE_CONFIGURED = true;
            }
            else
            {
                SMSB_ORIG_SVC_GRADE_CONFIGURED = false;
            }

            if (confIndices.getTermSvcGrade() != "" && confIndices.getTermSvcGrade().length() > 0)
            {
                SMSB_TERM_SVC_GRADE = Integer.parseInt(confIndices.getTermSvcGrade());
                SMSB_TERM_SVC_GRADE_CONFIGURED = true;
            }
            else
            {
                SMSB_TERM_SVC_GRADE_CONFIGURED = false;
            }

            if (confIndices.getOtherMsisdn() != "" && confIndices.getOtherMsisdn().length() > 0)
            {
                SMSB_OTHERMSISDN_INDEX = Integer.parseInt(confIndices.getOtherMsisdn());
                SMSB_OTHERMSISDN_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_OTHERMSISDN_INDEX_CONFIGURED = false;
            }

            if (confIndices.getOrigMscId() != "" && confIndices.getOrigMscId().length() > 0)
            {
                SMSB_ORIGMSCID_INDEX = Integer.parseInt(confIndices.getOrigMscId());
                SMSB_ORIGMSCID_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_ORIGMSCID_INDEX_CONFIGURED = false;
            }

            if (confIndices.getRate() != "" && confIndices.getRate().length() > 0)
            {
                SMSB_RATE_INDEX = Integer.parseInt(confIndices.getRate());
                SMSB_RATE_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_RATE_INDEX_CONFIGURED = false;
            }

            if (confIndices.getBalance() != "" && confIndices.getBalance().length() > 0)
            {
                SMSB_BALANCE_INDEX = Integer.parseInt(confIndices.getBalance());
                SMSB_BALANCE_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_BALANCE_INDEX_CONFIGURED = false;
            }

            if (confIndices.getSpid() != "" && confIndices.getSpid().length() > 0)
            {
                SMSB_SPID_INDEX = Integer.parseInt(confIndices.getSpid());
                SMSB_SPID_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_SPID_INDEX_CONFIGURED = false;
            }

            if (confIndices.getRateplanId() != "" && confIndices.getRateplanId().length() > 0)
            {
                SMSB_RATEPLANID_INDEX = Integer.parseInt(confIndices.getRateplanId());
                SMSB_RATEPLANID_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_RATEPLANID_INDEX_CONFIGURED = false;
            }

            if (confIndices.getRatingRule() != "" && confIndices.getRatingRule().length() > 0)
            {
                SMSB_RATINGRULE_INDEX = Integer.parseInt(confIndices.getRatingRule());
                SMSB_RATINGRULE_INDEX_CONFIGURED = true;
            }
            else
            {
                SMSB_RATINGRULE_INDEX_CONFIGURED = false;
            }

            if (confIndices.getSequenceNum() != "" && confIndices.getSequenceNum().length() > 0)
            {
                SMSB_SEQUENCE_NUM = Integer.parseInt(confIndices.getSequenceNum());
                SMSB_SEQUENCE_NUM_CONFIGURED = true;
            }
            else
            {
                SMSB_SEQUENCE_NUM_CONFIGURED = false;
            }

            if (confIndices.getBan() != "" && confIndices.getBan().length() > 0)
            {
                SMSB_BAN = Integer.parseInt(confIndices.getBan());
                SMSB_BAN_CONFIGURED = true;
            }
            else
            {
                SMSB_BAN_CONFIGURED = false;
            }

            if (confIndices.getOrigLargeActId() != "" && confIndices.getOrigLargeActId().length() > 0)
            {
                SMSB_ORIG_LARGE_ACCT_ID = Integer.parseInt(confIndices.getOrigLargeActId());
                SMSB_ORIG_LARGE_ACCT_ID_CONFIGURED = true;
            }
            else
            {
                SMSB_ORIG_LARGE_ACCT_ID_CONFIGURED = false;
            }

            if (confIndices.getDestLargeActId() != "" && confIndices.getDestLargeActId().length() > 0)
            {
                SMSB_DEST_LARGE_ACCT_ID = Integer.parseInt(confIndices.getDestLargeActId());
                SMSB_DEST_LARGE_ACCT_ID_CONFIGURED = true;
            }
            else
            {
                SMSB_DEST_LARGE_ACCT_ID_CONFIGURED = false;
            }

            if (confIndices.getVpnCalled() != "" && confIndices.getVpnCalled().length() > 0)
            {
                SMSB_VPN_CALLED = Integer.parseInt(confIndices.getVpnCalled());
                SMSB_VPN_CALLED_CONFIGURED = true;
            }
            else
            {
                SMSB_VPN_CALLED_CONFIGURED = false;
            }

            if (confIndices.getVpnCallType() != "" && confIndices.getVpnCallType().length() > 0)
            {
                SMSB_VPN_CALL_TYPE = Integer.parseInt(confIndices.getVpnCallType());
                SMSB_VPN_CALL_TYPE_CONFIGURED = true;
            }
            else
            {
                SMSB_VPN_CALL_TYPE_CONFIGURED = false;
            }

            if (confIndices.getVpnDiscount() != "" && confIndices.getVpnDiscount().length() > 0)
            {
                SMSB_VPN_DISCOUNT = Integer.parseInt(confIndices.getVpnDiscount());
                SMSB_VPN_DISCOUNT_CONFIGURED = true;
            }
            else
            {
                SMSB_VPN_DISCOUNT_CONFIGURED = false;
            }

            if (confIndices.getVpnBillingDn() != "" && confIndices.getVpnBillingDn().length() > 0)
            {
                SMSB_VPN_BILLINGDN = Integer.parseInt(confIndices.getVpnBillingDn());
                SMSB_VPN_BILLINGDN_CONFIGURED = true;
            }
            else
            {
                SMSB_VPN_BILLINGDN_CONFIGURED = false;
            }

            if (confIndices.getBmActionId() != "" && confIndices.getBmActionId().length() > 0)
            {
                SMSB_BM_ACTION_ID = Integer.parseInt(confIndices.getBmActionId());
                SMSB_BM_ACTION_ID_CONFIGURED = true;
            }
            else
            {
                SMSB_BM_ACTION_ID_CONFIGURED = false;
            }

            isSuccess = true;

        }
        catch (final NumberFormatException nfe)
        {
            LogSupport.minor(ctx, this,
                    "Error Parsing the configured ER Indices. ", nfe);
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * Depending on the logic to follow (Either SMSB Bucket decrement or Bundle decrement) this method will
     * set the bucket decrement for this particular call detail
     *
     * @throws HomeException
     */
    private void setBucketDecrement(Context ctx, CallDetail t, List params, final SmsbErIndicesConfig confIndices) throws HomeException
    {
        /*
           * Remove because of Bundle manager integration with SMSB
           */
        //t.setBucketDecrement(CRMProcessorSupport.getBoolean(CRMProcessorSupport.getField(params, SMSB_BUCKET_DECREMENT), false));
        int bmAction = 0;
        try
        {
            if (SMSB_BM_ACTION_ID_CONFIGURED)
            {
                bmAction = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, SMSB_BM_ACTION_ID), 0);
            }
            else
            {
                bmAction = Integer.parseInt(confIndices.getBmActionIdDefault());
            }

            if (bmAction == 1)
            {
                t.setBucketDecrement(true);
            }
            else
            {
                t.setBucketDecrement(false);
            }
        }
        catch (NumberFormatException ne)
        {
            throw new HomeException("Error while Parsing the bucket Decrement field of the ER", ne);
        }
    }

    public List<CallDetail> createCallDetails(Context ctx, ProcessorInfo info, List params) throws ParseException, HomeException, AgentException
    {
        List<CallDetail> list = new ArrayList<CallDetail>();
        CallDetail detail = createCallDetail(ctx, info, params);
        if (detail != null)
        {
            list.add(detail);
        }
        return list;
    }

    public int getSMSBChargedMSISDNIndex()
    {
        return SMSB_CHARGEDMSISDN_INDEX;
    }

    ////////////////////////////
    // SMSB
    // index 0 is the ER class

    /**
     * ER field index. service provider id of subscriber.
     */
    protected int SMSB_SPID_INDEX = 2;
    /**
     * ER field index. the msisdn of the subscriber (the one that pays)
     */
    protected int SMSB_CHARGEDMSISDN_INDEX = 3;
    /**
     * ER field index. originating MSCID that originator was on or 0 if
     * SMPPoroginated message.
     */
    protected int SMSB_ORIGMSCID_INDEX = 4;
    /**
     * ER field index. the other party msisdn or a combination of the source sme
     * and source account billing.
     */
    protected int SMSB_OTHERMSISDN_INDEX = 5;
    /**
     * ER field index. charged rate in sub-local currency to subscriber. If
     * negative implies a credit.
     */
    protected int SMSB_RATE_INDEX = 9;

    /**
     * ER field index. charged party
     */
    protected int SMSB_CHARGED_PARTY = 10;

    /**
     * ER field index. The rule# that was applied to determine the rate.
     */
    protected int SMSB_RATINGRULE_INDEX = 12;
    /**
     * ER field index. Internal transaction ID that can be used to match to
     * resultant transactions and used as a reference in the corresponding OIS
     * transaction.
     */
    //public static final int SMSB_TRANSACTIONID_INDEX = 13;
    /**
     * ER field index.  The remaining balance of the charged MSISDN, IF the
     * MSISDN was charged. Otherwise null. If the message was blocked, the
     * original balance of the subscriber.
     */
    protected int SMSB_BALANCE_INDEX = 15;
    /**
     * ER field index. The rate plan of of the subscriber who was charged with
     * this transaction. Not valid in Ensure Delivery mode.
     */
    protected int SMSB_RATEPLANID_INDEX = 16;

    /**
     * The messag is bundle/bucket charged and decremented from the bundle/bucket. Set to True i the message matches
     * a bucket/bundle rule and the SMS count increses.
     */
    //public static final int SMSB_BUCKET_DECREMENT = 18;

    protected int SMSB_ORIG_SVC_GRADE = 19;
    protected int SMSB_TERM_SVC_GRADE = 20;
    protected int SMSB_SEQUENCE_NUM = 21;
    /**
     * ER field index. The local date of the subscriber in his/her timezone.
     */
    protected int SMSB_LOCAL_SUBSCRIBER_DATE = 23;
    /**
     * ER field index. The local time of the subscriber in his/her timezone.
     */
    protected int SMSB_LOCAL_SUBSCRIBER_TIME = 24;

    protected int SMSB_BAN = 26;

    //public static final int SMSB_ORIG_LARGE_ACCT_ID=35;
    //Manda - Modified the index as per the new ER modified fields
    protected int SMSB_ORIG_LARGE_ACCT_ID = 34;
    //public static final int SMSB_DEST_LARGE_ACCT_ID=36;
    //Manda - Modified the index as per the new ER modified fields
    protected int SMSB_DEST_LARGE_ACCT_ID = 35;

    //ER Field Index for VPN Call Type
    protected int SMSB_VPN_CALLED = 42;
    //public static final int SMSB_VPN_SESSION_ID = 43;
    //public static final int SMSB_VPN_BAN = 43;
    //public static final int SMSB_VPN_TRANS_DEST_ADDR = 44;
    //public static final int SMSB_VPN_TRANS_DEST_TON = 45;
    //public static final int SMSB_VPN_TRANS_DEST_NPI = 46;

    protected int SMSB_VPN_CALL_TYPE = 47;
    protected int SMSB_VPN_DISCOUNT = 48;
    protected int SMSB_VPN_BILLINGDN = 49;
    protected String SMSB_VPN_CALLED_VALID = "1";


    protected int SMSB_BM_ACTION_ID = 53;
    
    protected int SMSB_CHARGING_COMPONENTS_ER_INDEX = 76;


    private boolean SMSB_SPID_INDEX_CONFIGURED = false;
    private boolean SMSB_CHARGEDMSISDN_INDEX_CONFIGURED = false;
    private boolean SMSB_ORIGMSCID_INDEX_CONFIGURED = false;
    private boolean SMSB_OTHERMSISDN_INDEX_CONFIGURED = false;
    private boolean SMSB_RATE_INDEX_CONFIGURED = false;
    private boolean SMSB_CHARGED_PARTY_CONFIGURED = false;
    private boolean SMSB_RATINGRULE_INDEX_CONFIGURED = false;
    private boolean SMSB_BALANCE_INDEX_CONFIGURED = false;
    private boolean SMSB_RATEPLANID_INDEX_CONFIGURED = false;
    private boolean SMSB_ORIG_SVC_GRADE_CONFIGURED = false;
    private boolean SMSB_TERM_SVC_GRADE_CONFIGURED = false;
    private boolean SMSB_SEQUENCE_NUM_CONFIGURED = false;
    private boolean SMSB_LOCAL_SUBSCRIBER_DATE_CONFIGURED = false;
    private boolean SMSB_LOCAL_SUBSCRIBER_TIME_CONFIGURED = false;
    private boolean SMSB_BAN_CONFIGURED = false;
    private boolean SMSB_ORIG_LARGE_ACCT_ID_CONFIGURED = false;
    private boolean SMSB_DEST_LARGE_ACCT_ID_CONFIGURED = false;
    private boolean SMSB_VPN_CALLED_CONFIGURED = false;
    private boolean SMSB_VPN_CALL_TYPE_CONFIGURED = false;
    private boolean SMSB_VPN_DISCOUNT_CONFIGURED = false;
    private boolean SMSB_VPN_BILLINGDN_CONFIGURED = false;
    private boolean SMSB_BM_ACTION_ID_CONFIGURED = false;
    private boolean validIndices = false;
    private SmsbErIndicesConfig confIndices = null;
}
