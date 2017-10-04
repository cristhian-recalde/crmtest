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


package com.trilogy.app.crm.secondarybalance.validator;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.defaultvalue.BooleanValue;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * 
 * Validating Home for Secondary Balance Bundle Validations. These bundles have 
 * Unit Type as {@link UnitTypeEnum#SECONDARY_BALANCE}.
 * 
 * <p>
 * The following validators are used - 
 * <ul>
 * 	<li>{@link CurrencyCategoryTypeAssociationValidator}
 *  <li>{@link SingleBundleProfilePerSecondaryBalanceCategoryValidator}
 *  <li>{@link NeverExpireOneTimeBundleValidator}
 *  <li>{@link NoGroupQuotaValidator}
 * </ul>
 * 
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class SecondaryBalanceValidatingHome extends ValidatingHome {

	/**
	 * Serilizable version u id.
	 */
	private static final long serialVersionUID = 6872349837497293479l;
	
	private static CompoundValidator validator = new CompoundValidator();
	
	static 
	{
		validator.add(new PTUBLicenseValidator());
		validator.add(new CurrencyCategoryTypeAssociationValidator(CurrencyCategoryTypeAssociationValidator.MESSAGE));
		validator.add(new SingleBundleProfilePerSecondaryBalanceCategoryValidator());
		validator.add(new NeverExpireOneTimeBundleValidator(
				NeverExpireOneTimeBundleValidator.OT_MESSAGE, 
				NeverExpireOneTimeBundleValidator.NEVER_EXPIRE_MESSAGE));
		validator.add(new NoGroupQuotaValidator(NoGroupQuotaValidator.MESSAGE));
	}
	
	public SecondaryBalanceValidatingHome(Home delegate) 
	{
		super(delegate, validator);		
	}

	/**
	 * The reason these methods are overridden is that before calling all the validators
	 * a check is done to ensure that the bundle profile is a Secondary Balance.
	 * 
	 * {@link BundleProfile#getType()}
	 * 
	 */
	@Override
	public Object create(Context ctx, Object obj) throws HomeException 
	{
		if(isSecondaryBalanceBundle(ctx, obj))
		{
			return super.create(ctx, obj);
		}
		else
		{
			return getDelegate(ctx).create(ctx, obj);
		}
	}

	/**
	 * The reason these methods are overridden is that before calling all the validators
	 * a check is done to ensure that the bundle profile is a Secondary Balance.
	 * 
	 * {@link BundleProfile#getType()}
	 * 
	 */
	@Override
	public Object store(Context ctx, Object obj) throws HomeException 
	{
		if(isSecondaryBalanceBundle(ctx, obj))
		{
			return super.store(ctx, obj);
		}
		else
		{
			return getDelegate(ctx).store(ctx, obj);
		}
	}

	/**
	 * 
	 * Verify two things - 
	 * 
	 * <ul>
	 * 	<li>Is this object really a {@link BundleProfile} ?
	 *  <li>Is this a Secondary Balance Bundle - {@link BundleProfile#isSecondaryBalance(Context)}
	 * </ul>
	 * 
	 * @param ctx
	 * @param object
	 * @return
	 * @throws HomeException
	 */
	private boolean isSecondaryBalanceBundle(Context ctx, Object object)
		throws HomeException
	{
		if(! (object instanceof BundleProfile) )
		{
			return BooleanValue.FALSE;
		}
		
		BundleProfile bundleProfile = (BundleProfile)object;
		
		return bundleProfile.isSecondaryBalance(ctx);
	}
}
