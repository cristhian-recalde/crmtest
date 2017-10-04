package com.trilogy.app.crm.home;

import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.DepositType;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.home.core.CoreAdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Creates new adjustment types for new Deposit type, updates and look-ups
 * existing adjustment types for existing Deposit type.
 */
public class DepositCreateAdjustmentTypeHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DepositCreateAdjustmentTypeHome(final Home delegate) {
		super(delegate);
	}

	public DepositCreateAdjustmentTypeHome(final Context context,
			final Home delegate) {
		super(delegate);
		setContext(context);
	}

	// INHERIT
	@Override
	public Object create(Context ctx, final Object bean) throws HomeException {
		DepositType depositType = (DepositType) bean;
		try {
			AdjustmentType type = null;
			if (depositType.getAdjustmentType() == -1) {
				// only if really new depositType created
				type = createAdjustmentTypeFor(ctx, depositType);
				depositType.setAdjustmentType(type.getCode());
			}
			if (type != null) {
				depositType = (DepositType) super.create(ctx, depositType);
			} else {
				new MinorLogMsg(this,
						"Failed to Create Adjustment Type for deposit type "
								+ depositType.getIdentifier()).log(ctx);
				new MinorLogMsg(this, "Failed to Create depositType "
						+ depositType.getIdentifier()).log(ctx);
			}
		} catch (final HomeException exception) {
			new MinorLogMsg(this, "Failed to Create depositType", exception)
					.log(ctx);
			throw exception;
		}
		return depositType;
	}

	public Object store(Context ctx, Object bean) throws HomeException,
			HomeInternalException {
		DepositType depositType = (DepositType) bean;
		boolean status = updateAdjustmentType(ctx, depositType);
		if (status) {
			LogSupport.info(
					ctx,
					this,
					"Udpate Adjustment type :"
							+ depositType.getAdjustmentType());
		}
		return getDelegate(ctx).store(ctx, bean);
	}

	private boolean updateAdjustmentType(Context ctx, DepositType depositType) {
		boolean status = false;
		try {
			And filter = new And();
			filter.add(new EQ(AdjustmentTypeXInfo.CODE, depositType
					.getAdjustmentType()));
			AdjustmentType adjustmentType = (AdjustmentType) HomeSupportHelper
					.get(ctx).findBean(ctx, AdjustmentType.class, filter);

			if (adjustmentType != null) {

				adjustmentType.setName(depositType.getDepositType());
				adjustmentType.setDesc(depositType.getDescription());

				Map spidInformation = adjustmentType.getAdjustmentSpidInfo();
				Object key = Integer.valueOf(depositType.getSpid());
				AdjustmentInfo information = (AdjustmentInfo) spidInformation
						.get(key);
				if (information != null) {

					final String glCode = depositType.getGlCode();
					String depositTypeGlCode = glCode.trim();

					information.setSpid(depositType.getSpid());
					information.setGLCode(depositTypeGlCode);
					information.setInvoiceDesc(depositType.getDepositType());

					adjustmentType.setAdjustmentSpidInfo(spidInformation);
				}
			}
			HomeSupportHelper.get(ctx).storeBean(ctx, adjustmentType);
			status = true;
		} catch (Exception e) {
			LogSupport.major(ctx, this, "Cannot find Adjustment type "
					+ depositType.getAdjustmentType());
		}
		return status;
	}

	private AdjustmentType createAdjustmentTypeFor(final Context ctx,
			final DepositType depositType) throws HomeException {

		final String glCode = depositType.getGlCode();

		AdjustmentType type = null;
		try {
			type = (AdjustmentType) XBeans.instantiate(AdjustmentType.class,
					ctx);
		} catch (final Exception e) {
			throw new HomeException("Failed to instantiate AdjustmentType", e);
		}

		type.setParentCode(AdjustmentTypeSupportHelper.get(ctx)
				.getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
						AdjustmentTypeEnum.DepositPayments));
		type.setName(depositType.getDepositType());
		type.setDesc(depositType.getDescription());

		type.setAction(AdjustmentTypeActionEnum.CREDIT);
		type.setCategory(false);
		type.setLoyaltyEligible(true);
//		type.setShowInInvoice(false);

		final Map spidInformation = type.getAdjustmentSpidInfo();
		final Object key = Integer.valueOf(depositType.getSpid());
		AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);
		if (information == null) {
			information = new AdjustmentInfo();
			spidInformation.put(key, information);
		}
		information.setSpid(depositType.getSpid());
		information.setTaxAuthority(getDefaltTaxAuthority(ctx));///**/

		String depositTypeGlCode = glCode.trim();
		information.setGLCode(depositTypeGlCode);
		information.setInvoiceDesc(depositType.getDepositType());

		type.setAdjustmentSpidInfo(spidInformation);

		final Home home = (Home) ctx.get(CoreAdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
		
		type = (AdjustmentType) home.create(ctx, type);

		return type;
	}

	private int getDefaltTaxAuthority(Context ctx) throws HomeException {
		TaxAuthority taxAuthority = null;
		final GeneralConfig config = (GeneralConfig) ctx
				.get(GeneralConfig.class);
		if (config == null) {
			final IllegalStateException exception = new IllegalStateException(
					"System Error: GeneralConfig does not exist in context");
			new DebugLogMsg(this, exception.getMessage(), exception).log(ctx);
			throw exception;
		}else{
			if(config.getTaxAuthority() >= 0){
			And filter = new And();
			filter.add(new EQ(TaxAuthorityXInfo.TAX_ID, config.getTaxAuthority()));
			try {
				taxAuthority = (TaxAuthority) HomeSupportHelper.get(ctx)
						.findBean(ctx, TaxAuthority.class, filter);
			}catch (Exception e) {
				throw new HomeException("Error in Get tax authority");
			}
			if (taxAuthority == null) {
				LogSupport.major(
						ctx,
						this,
						"can not find tax authority for "
								+ config.getTaxAuthority());
				throw new HomeException("can not find configured tax authority");
			}
		  }else{
			  LogSupport.major(
						ctx,
						this,
						"Tax Authority can not find in system configuration");
				throw new HomeException("can not find tax authority in system configuration");
		  }
		}
		return taxAuthority.getTaxId();
	}

}
