package com.trilogy.app.crm.inboundfile.validators;

import java.util.Collection;

import com.trilogy.app.crm.bean.PaymentFileAdjTypeMapping;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMappingHome;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMappingXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

/**
 * In this validator we are going to do duplicate check
 * if duplicate reason code found for the respective serviceprovider
 * then we are going to throw an error message
 * 
 * @author skambab
 *
 */
public class PaymentFileAdjTypeMappingValidator implements Validator{

	@Override
	public void validate(Context ctx, Object obj)throws IllegalStateException
	{
		CompoundIllegalStateException cise = new CompoundIllegalStateException();
		if((obj != null)&&(obj instanceof PaymentFileAdjTypeMapping))
		{
			try
			{			
				PaymentFileAdjTypeMapping bean = (PaymentFileAdjTypeMapping)obj;
				Home home = (Home) ctx.get(PaymentFileAdjTypeMappingHome.class);
				String msg = "For Service Provider "+bean.getSpid()+" already Payment method "+bean.getPaymentMethod()+" and bank code "+bean.getBankCode()+
							  "has been selected";
				And filter = new And();
				filter.add(new EQ(PaymentFileAdjTypeMapping.ID_PROPERTY, bean.getID()));
				filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.SPID,bean.getSpid()));
				filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.PAYMENT_METHOD, bean.getPaymentMethod()));
				filter.add(new EQ(PaymentFileAdjTypeMappingXInfo.BANK_CODE, bean.getBankCode()));
				Collection<PaymentFileAdjTypeMapping> beanColl = home.select(ctx, filter);
				if(beanColl.size()>0)
				{
					cise.thrown(new HomeException(msg));
				}
			} catch (HomeException e)
			{
				e.printStackTrace();
			}
		}
		cise.throwAll();
	}

}
