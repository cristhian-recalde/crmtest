package com.trilogy.app.crm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ChargedBundleInfo;
import com.trilogy.app.crm.bean.ChargingComponentMetaData;
import com.trilogy.app.crm.bean.ChargingComponentsConfig;
import com.trilogy.app.crm.bean.ComponentChargeDetail;
import com.trilogy.app.crm.bean.CurrencyBundleCharge;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bundle.BundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.CategoryAssociationTypeEnum;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * 
 *
 * @author odeshpande
 * @since 9.9.1
 */
public class CallCharges extends com.redknee.app.crm.bean.CallCharges
{
	public CallCharges()
	{
		
	}
	
	public CallCharges build(Context ctx,CallDetail calldetail , Collection<ChargedBundleInfo>  allChargedBundleInfo,CRMSpid crmSpid)
	{
		populateComponentCharges(ctx,calldetail,crmSpid);
		populateBundleCharges(ctx,allChargedBundleInfo);
		setTotalUnitsCharged(calldetail);
		setTotalSubscriptionCharge(calldetail.getCharge());
		setBundleComponentInfo();
		return this;
	}
	
	public void setTotalUnitsCharged(CallDetail calldetail)
	{
		
		if(calldetail.getVariableRateUnit() == RateUnitEnum.MSG)
		{
			setTotalUnitsCharged(1);
		}else
		{
			setTotalUnitsCharged(calldetail.getDuration().getMillis()==0?calldetail.getDataUsage():calldetail.getDuration().getMillis());
		}
	}
	
	public void segrgateCharges(Context ctx) throws HomeInternalException, HomeException
	{
		remainingUnitCharge = getTotalUnitsCharged();
		if(isUnitBundleCharged())
		{
			Iterator<ChargedBundleInfo> unitBundleItr = unitBundleCharged_.iterator();
			while(unitBundleItr.hasNext())
			{
				ChargedBundleInfo unitBundle = unitBundleItr.next();
				Home bbph = (Home)ctx.get(BundleProfileHome.class);
				BundleProfile bp = new BundleProfile();
				bp.setBundleId(unitBundle.getBundleId());
				BundleProfile bundleProfile = (BundleProfile)bbph.find(ctx, bp);
				long rate = 1;
				if(CategoryAssociationTypeEnum.CROSS_UNIT.equals(bundleProfile.getAssociationType()))
				{
					Map bundleCatagoryIdMap = bundleProfile.getBundleCategoryIds();
					if(bundleCatagoryIdMap!=null && bundleCatagoryIdMap.size()>0)
					{
						Set keySet= bundleCatagoryIdMap.entrySet();
						for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
							Map.Entry object = (Map.Entry) iterator.next();
							BundleCategoryAssociation association =(BundleCategoryAssociation)object.getValue();
							if(association.getType()== unitBundle.getUnitType().getIndex())
							{
								rate = association.getRate();
							}
						}
					}
				}
				remainingUnitCharge = remainingUnitCharge -  Math.round(unitBundle.getChargedAmount()/rate);
			}
		}
		if(remainingUnitCharge > 0)
		{
			remianingCurrencyCharge = getTotalSubscriptionCharge() - flatCharge();
			remianingCurrencyCharge = remianingCurrencyCharge + currencyBundleCharge();
			if(remianingCurrencyCharge > 0)
			{
				findEquivalentUnitsForVariableComponent(remainingUnitCharge,remianingCurrencyCharge);
				findEquivalentUnitForCurrencyBundle(remainingUnitCharge,remianingCurrencyCharge);
			}
		}else
		{
			setOnlyUnitBundleCharged(true);
		}
		modifyVariableComponetForZeroRatedCall();
	}
	
	private void findEquivalentUnitsForVariableComponent(long remainingUnitCharge,long remainingCurrencyCharged)
	{
		Iterator<ComponentChargeDetail> variableComponents = getVariableComponent().iterator();
		while(variableComponents.hasNext()){
			ComponentChargeDetail component = variableComponents.next();
			long currentComponentCharge = component.getCharge();
			int equivalentUnitWeightage = Math.round(currentComponentCharge*remainingUnitCharge/remainingCurrencyCharged);
			component.setEquivalentUnits(equivalentUnitWeightage);
		}
	}
	
	private void findEquivalentUnitForCurrencyBundle(long remainingUnitCharge, long remianingCurrencyCharge)
	{
		Iterator<CurrencyBundleCharge>	currencyBundles = getCurrencyBundleCharged().iterator();
		while(currencyBundles.hasNext()){
			CurrencyBundleCharge currencyBundleCharge = currencyBundles.next();
			long amountCharged = currencyBundleCharge.getBundleChargeInfo().getChargedAmount();
			int equivalentUnitWeightage = Math.round(amountCharged*remainingUnitCharge/remianingCurrencyCharge);
			currencyBundleCharge.setEquivalentUnits(equivalentUnitWeightage);
		}
	}
	private void populateComponentCharges(Context ctx,CallDetail cd,CRMSpid crmSpid)
    {
          
          if(crmSpid != null && crmSpid.isEnableChargingComponents())
          {
                ChargingComponentsConfig components = crmSpid.getChargingComponentsConfig();
                if(!cd.getComponentGLCode1().trim().isEmpty())
                {
                      ChargingComponentMetaData componentMetadata = components.getComponentFirst();
                      addComponent(componentMetadata,cd.getComponentGLCode1(),cd.getComponentCharge1(),cd.getComponentRate1());
                }
                if(!cd.getComponentGLCode2().trim().isEmpty())
                {
                      ChargingComponentMetaData componentMetadata = components.getComponentSecond();
                      addComponent(componentMetadata,cd.getComponentGLCode2(),cd.getComponentCharge2(),cd.getComponentRate2());
                }
                if(!cd.getComponentGLCode3().trim().isEmpty())
                {
                      ChargingComponentMetaData componentMetadata = components.getComponentThird();
                      addComponent(componentMetadata,cd.getComponentGLCode3(),cd.getComponentCharge3(),cd.getComponentRate3());
                }
                
          }
    }

	
	private void populateBundleCharges(Context ctx,Collection<ChargedBundleInfo> allChargedBundleInfo)
	{
		Iterator<ChargedBundleInfo> chargedBundleInfo = allChargedBundleInfo.iterator();
		 while(chargedBundleInfo.hasNext()){
			ChargedBundleInfo bundleInfo = chargedBundleInfo.next();
			if(bundleInfo.getUnitType() == UnitTypeEnum.CURRENCY)
			{
				CurrencyBundleCharge currencyBundleChargeInfo = new CurrencyBundleCharge();
				currencyBundleChargeInfo.setBundleChargeInfo(bundleInfo);
				getCurrencyBundleCharged().add(currencyBundleChargeInfo);
			}else
			{
				getUnitBundleCharged().add(bundleInfo);
			}
		 }
	}
	
	private void addComponent(ChargingComponentMetaData componentMetadata,String glCode,long charge,long rate)
	{
		if (componentMetadata.isVariable())
		{
			addVariableComponent(componentMetadata,glCode,charge,rate);
			
		}
		else{
			addFlatComponent(componentMetadata,glCode,charge,rate);
		}
	}
	
	public void addFlatComponent(ChargingComponentMetaData componentMetadata,String glCode,long charge,long rate)
	{
		ComponentChargeDetail componentCharge = new ComponentChargeDetail();
		componentCharge.setComponentId((int)componentMetadata.getID());
		componentCharge.setComponentName(componentMetadata.getName());
		componentCharge.setComponentRate((int)rate);
		componentCharge.setCharge(charge);
		componentCharge.setGlCode(glCode);
		componentCharge.setIsFlat(true);
		componentCharge.setEquivalentUnits(0);
		setFlatComponent(componentCharge);
	}
	
	public void addVariableComponent(ChargingComponentMetaData componentMetadata,String glCode,long charge,long rate)
	{
		ComponentChargeDetail componentCharge = new ComponentChargeDetail();
		componentCharge.setComponentId((int)componentMetadata.getID());
		componentCharge.setComponentName(componentMetadata.getName());
		componentCharge.setComponentRate((int)rate);
		componentCharge.setCharge(charge);
		componentCharge.setGlCode(glCode);
		componentCharge.setIsFlat(false);
		getVariableComponent().add(componentCharge);
	}
	
	public boolean isUnitBundleCharged()
	{
		return getUnitBundleCharged().size() > 0;
	}
	
	private long flatCharge()
	{
		if(getFlatComponent()!= null)
		{
			return getFlatComponent().getCharge();
		}
		return 0L;
	}
	
	public long currencyBundleCharge()
	{
		long currencyBundleCharge = 0L;
		for(int i=0;i<currencyBundleCharged_.size();i++)
		{
			currencyBundleCharge += ((CurrencyBundleCharge)getCurrencyBundleCharged().get(i)).getBundleChargeInfo().getChargedAmount();
		}
		return currencyBundleCharge;
	}
	
	private void setBundleComponentInfo()
	{
		ComponentChargeDetail firstVariableComponent = (getVariableComponent().size()>0)?(ComponentChargeDetail)getVariableComponent().get(0):null;
		if(firstVariableComponent != null){
			setBundleGLCode(firstVariableComponent.getGlCode());
			setBundleComponentId(firstVariableComponent.getComponentId());
		}
	}
	
	private void modifyVariableComponetForZeroRatedCall()
	{
		if(!isUnitBundleCharged() && !isCurrencyBundleCharged() && getTotalSubscriptionCharge()==0)
		{
			List<ComponentChargeDetail> variableComponents = getVariableComponent();
			for(ComponentChargeDetail variableComponent : variableComponents)
			{
				variableComponent.setEquivalentUnits((int)getTotalUnitsCharged());
			}
		}
	}
	
	public boolean isCurrencyBundleCharged()
	{
		return getCurrencyBundleCharged().size() > 0;
	}
	private long remainingUnitCharge;
	private long remianingCurrencyCharge;
	
}
