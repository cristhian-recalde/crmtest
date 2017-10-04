package com.trilogy.app.crm.support;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.BalanceBundleUsageSummary;
import com.trilogy.app.crm.bean.BalanceBundleUsageSummaryHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CallDetailProcessConfig;
import com.trilogy.app.crm.bean.ChargedBundleInfo;
import com.trilogy.app.crm.bean.ChargedBundleInfoHome;
import com.trilogy.app.crm.bean.ChargedBundleInfoXInfo;
import com.trilogy.app.crm.bean.ComponentChargeDetail;
import com.trilogy.app.crm.bean.CurrencyBundleCharge;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bean.priceplan.CalldetailExtension;
import com.trilogy.app.crm.bean.priceplan.CalldetailExtensionHome;
import com.trilogy.app.crm.bean.priceplan.CalldetailExtensionXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.clean.BalanceBundleUsageSummaryLifeCycleAgent;
import com.trilogy.app.crm.clean.visitor.CallDetailBalanceBundleUsageVisitor;
import com.trilogy.app.crm.util.CallCharges;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;

public class BalanceBundleUsageSummarySupport {

	
	/**
	 * this method is to insert/update record in BBUS table for component which is variable type.
	 * @param ctx
	 * @param callDetail
	 * @param chargedComponent
	 * @param glCode
	 * @param componentCharge
	 */
	public  void prepareBalanceBundleUsageSummary(Context ctx,CallDetail callDetail,CRMSpid crmSpid) throws HomeException , Exception
	{
		
		
		final Home balanceBundleUsageSummaryHome	=	(Home)ctx.get(BalanceBundleUsageSummaryHome.class);
		updateCallDetailProcessConfigData(ctx,callDetail,true);
		
		try
		{
			CalldetailExtension cdpp = getCalldetailExtension(ctx, callDetail);
			CallTypeEnum calltype 	=	callDetail.getCallType();
			Collection<ChargedBundleInfo> chargedBundleInfo		= getChargedBundleInfo(ctx, callDetail);
			CallCharges callChargesDetail = new CallCharges();
			callChargesDetail.build(ctx, callDetail, chargedBundleInfo, crmSpid);
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this , "CallChargesDetail populated for callId " +callDetail.getId()+ " is --->> " +callChargesDetail.toString());
			}
			if(cdpp != null)
			{
				if(cdpp.getBillableduration() != -1)
				{
					callChargesDetail.setTotalUnitsCharged(cdpp.getBillableduration());
				}
			}
			else
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this , "Add CalldetailExtension for failed calldetail record. CallDetailId :" + callDetail.getId()+ " and SessionId: " +callDetail.getCallID());
				}
				CalldetailExtension calldetailExtension = new CalldetailExtension();
			      
				calldetailExtension.setCalldetailId(callDetail.getId());      
				calldetailExtension.setRateRuleId(callDetail.getRatingRule());      
				calldetailExtension.setPricePlanId(BalanceBundleUsageSummaryLifeCycleAgent.CONSTANT_PRICEPLAN);      
				calldetailExtension.setSpid(callDetail.getSpid());
				calldetailExtension.setBillableduration(callDetail.getDuration().getSeconds());
		      
				HomeSupportHelper.get(ctx).createBean(ctx, calldetailExtension);
				logEr(ctx,callDetail.getSpid(),callDetail.getId(),"Add CalldetailExtension calldetail successfully processed." , CallDetailBalanceBundleUsageVisitor.SUCCESS);
			}
			
			callChargesDetail.segrgateCharges(ctx);
			String bunldeGlcode = callChargesDetail.getBundleGLCode();
			int bundleComponetId = callChargesDetail.getBundleComponentId();
			
			if(callChargesDetail.isUnitBundleCharged() && !bunldeGlcode.trim().isEmpty())
			{
				List<ChargedBundleInfo> unitBundlesCharged = callChargesDetail.getUnitBundleCharged();
				for(ChargedBundleInfo unitBundle :unitBundlesCharged ){
					BalanceBundleUsageSummary summaryPKObj= createBalanceBundleUsageSummaryPKObject(ctx,callDetail,unitBundle,bundleComponetId,bunldeGlcode);
					if(balanceBundleUsageSummaryHome != null)
					{
						BalanceBundleUsageSummary balanceBundleSummary	=	(BalanceBundleUsageSummary) balanceBundleUsageSummaryHome.find(ctx, summaryPKObj);
						if(balanceBundleSummary != null)
						{
							aggregateBalanceBundleUsageSummaryDataforUnitBundle( balanceBundleSummary,unitBundle);
							balanceBundleUsageSummaryHome.store(ctx, balanceBundleSummary);
						}
						else
						{
							BalanceBundleUsageSummary summary 	=	populateBalanceBundleUsageSummaryforUnitBundle(ctx,callDetail, unitBundle, bunldeGlcode ,bundleComponetId,cdpp);
							balanceBundleUsageSummaryHome.create(ctx, summary);
						}
					}
				}
			}
			if(!callChargesDetail.isOnlyUnitBundleCharged())
			{
				if(!bunldeGlcode.trim().isEmpty()){
					List<CurrencyBundleCharge> currencyBundleCharged =	callChargesDetail.getCurrencyBundleCharged();
					
					for (CurrencyBundleCharge currencyBundleCharge : currencyBundleCharged)
					{
						ChargedBundleInfo bundleInfo = currencyBundleCharge.getBundleChargeInfo();
						BalanceBundleUsageSummary summaryPKObj= createBalanceBundleUsageSummaryPKObject(ctx,callDetail,bundleInfo,bundleComponetId,bunldeGlcode);
						BalanceBundleUsageSummary balanceBundleSummary	=	(BalanceBundleUsageSummary) balanceBundleUsageSummaryHome.find(ctx, summaryPKObj);
						if(balanceBundleSummary != null)
						{
							aggregateBalanceBundleUsageSummaryforCurrencyBundle(balanceBundleSummary, callDetail, bundleInfo.getChargedAmount(), currencyBundleCharge.getEquivalentUnits()); 
							balanceBundleUsageSummaryHome.store(ctx, balanceBundleSummary);
						}
						else
						{
							BalanceBundleUsageSummary summary 	=	populatBalanceBundleUsageSummaryforCurrencyBundle(ctx,callDetail, currencyBundleCharge,  bunldeGlcode ,bundleComponetId,cdpp);
							balanceBundleUsageSummaryHome.create(ctx, summary);
						}
					}
				}
				List<ComponentChargeDetail>	variableComponents = callChargesDetail.getVariableComponent();
				for(ComponentChargeDetail variableComponent:variableComponents)
				{
					if(!variableComponent.getGlCode().trim().isEmpty() && variableComponent.getEquivalentUnits() > 0){

						BalanceBundleUsageSummary summaryPKObj= createBalanceBundleUsageSummaryPKObject(ctx,callDetail,null,variableComponent.getComponentId(),variableComponent.getGlCode());
						createAggregateBalanceBundleUsageSummaryForComponent(ctx,  callDetail, null, variableComponent, cdpp);
					}
				}
			}
			if(callChargesDetail.getFlatComponent() != null)
			{
				ComponentChargeDetail flatComponent = callChargesDetail.getFlatComponent();
				if(flatComponent.getCharge() > 0 && !flatComponent.getGlCode().trim().isEmpty())
				{
					createAggregateBalanceBundleUsageSummaryForComponent(ctx, callDetail, null, flatComponent, cdpp);
				}
			}
			
			updateCallDetailProcessConfigData(ctx,callDetail,false);
			
		}catch (HomeException he)
		{
			new MajorLogMsg(this, "Exception occurred while preparing BBUS Data--->> " + he.getMessage(), he).log(ctx);
			throw new HomeException(he);
		}
		catch (UnsupportedOperationException unsupprtedEx) 
		{
			new MajorLogMsg(this, "Exception occurred while preparing BBUS Data--->> " + unsupprtedEx.getMessage(), unsupprtedEx).log(ctx);
			throw new UnsupportedOperationException(unsupprtedEx);
		}
		catch (Exception e) 
		{
			new MajorLogMsg(this,"Exception occurred while preparing BBUS Data--->> "  + e.getMessage(), e).log(ctx);
			throw new Exception(e);
		}
			
		
	}
	public void logEr(Context ctx,int spid,long callId,String msg, int result)
	{
		final String[] fields = new String[3];
		fields[0] = String.valueOf(callId);
		fields[1] = msg;
		fields[2] = String.valueOf(result);
		new ERLogMsg(CallDetailBalanceBundleUsageVisitor.BALANCE_BUNDLE_USAGE_SUMMARY_ERID, 700, "Balance Bundle Usage Summary Task ER", spid, fields)
        .log(ctx);
		
	}
	public BalanceBundleUsageSummary populateBalanceBundleUsageSummaryforUnitBundle(Context ctx,CallDetail callDetail, ChargedBundleInfo chargedBundleInfo, String glCode, int componentId, CalldetailExtension cdpp)
	{
		BalanceBundleUsageSummary summary 	=	new BalanceBundleUsageSummary();
		summary.setSpid(callDetail.getSpid());
		summary.setRateRuleId(callDetail.getRatingRule());
		summary.setPricePlanId(cdpp.getPricePlanId());
        summary.setTransDate(getCallDetailtransDate(callDetail, ctx).getTime());
		summary.setGlCode(glCode);
		summary.setCallType(callDetail.getCallType());
		summary.setCallTypeName(callDetail.getCallType().getDescription());
		summary.setComponentId(componentId);
		if(chargedBundleInfo != null)
		{
			summary.setBundleProfileId(String.valueOf(chargedBundleInfo.getBundleId()));
			
			if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_BYTES) )
			{
				summary.setTotalDataVolume( chargedBundleInfo.getChargedAmount());
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_KILOBYTES))
			{
				summary.setTotalDataVolume(chargedBundleInfo.getChargedAmount()*1000);
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_SECONDS))
			{
				summary.setTotalCallDuration( chargedBundleInfo.getChargedAmount());
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.EVENT_SMS_MMS) || chargedBundleInfo.getUnitType().equals(UnitTypeEnum.EVENT_GENERIC))
			{
				summary.setTotalEvents(chargedBundleInfo.getChargedAmount());
			}
		}
		
		return summary;
	}
	
	public BalanceBundleUsageSummary createBalanceBundleUsageSummaryPKObject(Context ctx, CallDetail callDetail, ChargedBundleInfo chargedBundleInfo,long componentId,String glCode) throws HomeException
	{
		CalldetailExtension calldetailExtension = getCalldetailExtension(ctx, callDetail);
		BalanceBundleUsageSummary summary = new BalanceBundleUsageSummary();
		if (chargedBundleInfo != null)
		{
			summary.setBundleProfileId(String.valueOf(chargedBundleInfo.getBundleId()));
		}
		summary.setSpid(callDetail.getSpid());
		if(calldetailExtension != null)
		{
			summary.setPricePlanId(calldetailExtension.getPricePlanId());
		}
		else
		{
			throw new HomeException("Price Plan not available for call deial Id : " + callDetail.getId() + " . Exiting further processing as PricePlan is mandatory." );
		}
		summary.setRateRuleId(callDetail.getRatingRule());
		summary.setCallType(callDetail.getCallType());
		summary.setTransDate(getCallDetailtransDate(callDetail, ctx).getTime());
		summary.setComponentId(componentId);
		summary.setGlCode(glCode);
		return summary;
	}
	

	public Date getCallDetailtransDate(CallDetail callDetail, Context ctx)
	{
		final CalendarSupport calendar = CalendarSupportHelper.get(ctx);
		return calendar.getDateWithNoTimeOfDay(callDetail.getTranDate());
	}
	
	
	public void aggregateBalanceBundleUsageSummaryDataforUnitBundle(BalanceBundleUsageSummary bbus,ChargedBundleInfo chargedBundleInfo)
	{
		
		if(chargedBundleInfo !=null)
		{
			if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_BYTES) )
			{
				bbus.setTotalDataVolume(bbus.getTotalDataVolume() + chargedBundleInfo.getChargedAmount());
				
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_KILOBYTES))
			{
				bbus.setTotalDataVolume(bbus.getTotalDataVolume() + chargedBundleInfo.getChargedAmount()*1000);
				
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.VOLUME_SECONDS))
			{
				bbus.setTotalCallDuration(bbus.getTotalCallDuration() + chargedBundleInfo.getChargedAmount());
				
			}
			else if(chargedBundleInfo.getUnitType().equals(UnitTypeEnum.EVENT_SMS_MMS) || chargedBundleInfo.getUnitType().equals(UnitTypeEnum.EVENT_GENERIC))
			{
				bbus.setTotalEvents(bbus.getTotalEvents() + chargedBundleInfo.getChargedAmount());
			}
		}
		
	}
	
	public void aggregateBalanceBundleUsageSummaryforCurrencyBundle(BalanceBundleUsageSummary bbus,CallDetail callDetail,long currencyCharge,long equivalentUnitWeightage)
	{
		setTotalUnitInBBUS(bbus,callDetail,equivalentUnitWeightage);		
		bbus.setCurrencyBundleUsage(bbus.getCurrencyBundleUsage()+currencyCharge);
	}
	
	public void aggregateBBUSDataforComponents(BalanceBundleUsageSummary bbus,CallDetail callDetail,ComponentChargeDetail component)
	{
		setTotalUnitInBBUS(bbus,callDetail,component.getEquivalentUnits());		
		bbus.setTotalBalanceCharged(bbus.getTotalBalanceCharged() + component.getCharge());
	}
	
	private void setTotalUnitInBBUS(BalanceBundleUsageSummary bbus,CallDetail callDetail ,long equivalentUnitWeightage)
	{
		if(callDetail.getVariableRateUnit() == RateUnitEnum.EVENT || callDetail.getVariableRateUnit() == RateUnitEnum.MSG)
		{
			bbus.setTotalEvents(bbus.getTotalEvents()+equivalentUnitWeightage);
		}
		else if(callDetail.getVariableRateUnit() == RateUnitEnum.MIN || callDetail.getVariableRateUnit() == RateUnitEnum.SEC)
		{
			bbus.setTotalCallDuration(bbus.getTotalCallDuration()+equivalentUnitWeightage);
		}
		else if(callDetail.getVariableRateUnit() == RateUnitEnum.KBYTES )
		{
			bbus.setTotalDataVolume(bbus.getTotalDataVolume()+equivalentUnitWeightage);
		}
	}
	
	public BalanceBundleUsageSummary populatBalanceBundleUsageSummaryforCurrencyBundle(Context ctx,CallDetail callDetail, CurrencyBundleCharge currencyBundleInfo, String glCode, int componentId, CalldetailExtension cdpp)
	{
		BalanceBundleUsageSummary summary 	=	new BalanceBundleUsageSummary();
		fillBBUSInitailData(ctx,summary, callDetail, glCode, componentId,cdpp);
		summary.setBundleProfileId(String.valueOf(currencyBundleInfo.getBundleChargeInfo().getBundleId()));
		setTotalUnitInBBUS(summary,callDetail,currencyBundleInfo.getEquivalentUnits());
		summary.setCurrencyBundleUsage(currencyBundleInfo.getBundleChargeInfo().getChargedAmount());
		return summary;
	}
	
	public BalanceBundleUsageSummary populatBBUSObjectforComponent(Context ctx,CallDetail callDetail,  ComponentChargeDetail component, CalldetailExtension cdpp)
	{
		BalanceBundleUsageSummary summary 	=	new BalanceBundleUsageSummary();
		fillBBUSInitailData(ctx,summary, callDetail, component.getGlCode(), component.getComponentId(),cdpp);
		setTotalUnitInBBUS(summary,callDetail,component.getEquivalentUnits());
		summary.setTotalBalanceCharged(component.getCharge());
		return summary;
	}
	
	private void fillBBUSInitailData(Context ctx,BalanceBundleUsageSummary summary, CallDetail callDetail, String glCode, long componentId,CalldetailExtension callDetailExt)
	{
		summary.setSpid(callDetail.getSpid());
		summary.setRateRuleId(callDetail.getRatingRule());//this is hardcoded for now as it is not coming in CDR.
		summary.setPricePlanId(callDetailExt.getPricePlanId());//will populate from temp table
        summary.setTransDate(getCallDetailtransDate(callDetail,ctx).getTime());
		summary.setGlCode(glCode);
		summary.setCallType(callDetail.getCallType());
		summary.setCallTypeName(callDetail.getCallType().getDescription());
		summary.setComponentId(componentId);
	}
	
	
	/**
	 * Method to fetch CalldetailPricePlan object for particular calldetail.
	 * @param ctx
	 * @param callDetail
	 * @return CalldetailPricePlan
	 */
	public CalldetailExtension getCalldetailExtension(Context ctx,CallDetail callDetail) throws HomeException
	{
		Home calldetailExtensionHome	=	(Home)ctx.get(CalldetailExtensionHome.class);
		And and = new And();
		CalldetailExtension cdpp = null;
		and.add(new EQ(CalldetailExtensionXInfo.CALLDETAIL_ID, callDetail.getId()));
		
		if(calldetailExtensionHome != null)
		{
			cdpp= (CalldetailExtension) calldetailExtensionHome.find(ctx, and);
		}
		else
		{
			throw new HomeException("No Home found for calldetailExtension, can not proceed further as some mandatory fields are expected to be fetched from this home");
		}
		
		return cdpp;
	}
	
	private void createAggregateBalanceBundleUsageSummaryForComponent(Context ctx, CallDetail callDetail, ChargedBundleInfo bundleInfo  , ComponentChargeDetail component, CalldetailExtension callDetailExt) throws HomeException
	{
		BalanceBundleUsageSummary summaryPKObj= createBalanceBundleUsageSummaryPKObject(ctx,callDetail,null,component.getComponentId(),component.getGlCode());
		
		Home balanceBundleUsageSummaryHome = (Home)ctx.get(BalanceBundleUsageSummaryHome.class);
		if(balanceBundleUsageSummaryHome != null)
		{
			BalanceBundleUsageSummary balanceBundleSummary = (BalanceBundleUsageSummary) balanceBundleUsageSummaryHome.find(ctx, summaryPKObj);
			if(balanceBundleSummary != null)
			{
				aggregateBBUSDataforComponents(balanceBundleSummary, callDetail, component); 							 
				balanceBundleUsageSummaryHome.store(ctx, balanceBundleSummary);
			}
			else
			{
				BalanceBundleUsageSummary summary 	=	populatBBUSObjectforComponent(ctx,callDetail,component, callDetailExt);
				balanceBundleUsageSummaryHome.create(ctx, summary);
			}
		}
		else
		{
			throw new HomeException("No Home found for balanceBundleUsageSummaryHome, can not proceed further as some mandatory fields are expected to be fetched from this home");
		}
	}
	
	public void updateCallDetailProcessConfigData(Context ctx,CallDetail callDetail, boolean isUnderProcess)
	{
		
		CallDetailProcessConfig processConfig = (CallDetailProcessConfig)ctx.get(CallDetailProcessConfig.class);
		if(processConfig == null)
		{
			processConfig	=	new CallDetailProcessConfig();
		}
		if(isUnderProcess)
		{
			processConfig.setCallDetailId(callDetail.getId());
			processConfig.setPostedDate(callDetail.getPostedDate());
			processConfig.setInProcess(isUnderProcess);
		}
		else
		{
			processConfig.setInProcess(isUnderProcess);
		}
		ctx.put(CallDetailProcessConfig.class, processConfig);
	}
	
	private Collection  getChargedBundleInfo(Context ctx, CallDetail callDetail) 
	{
		Collection<ChargedBundleInfo> chargedBundleInfo = Collections.EMPTY_LIST;
		final Home chargedBundleInfoHome = (Home)ctx.get(ChargedBundleInfoHome.class);
		And chargedBundleInfoPredicate	=	new And();
		chargedBundleInfoPredicate.add(new EQ(ChargedBundleInfoXInfo.TRANS_DATE, callDetail.getTranDate()));
		chargedBundleInfoPredicate.add(new EQ(ChargedBundleInfoXInfo.CALL_DETAIL_ID,callDetail.getId()));
		if(chargedBundleInfoHome != null)
		{
			try {
				chargedBundleInfo		=	(Collection<ChargedBundleInfo>)chargedBundleInfoHome.select(ctx, chargedBundleInfoPredicate);
			} catch (HomeInternalException e) {
				new MajorLogMsg(this, e.getMessage(), e).log(ctx);
			} catch (UnsupportedOperationException e) {
				new MajorLogMsg(this, e.getMessage(), e).log(ctx);
			} catch (HomeException e) {
				new MajorLogMsg(this, e.getMessage(), e).log(ctx);
			}
		}
		
		return chargedBundleInfo;
	}
}
