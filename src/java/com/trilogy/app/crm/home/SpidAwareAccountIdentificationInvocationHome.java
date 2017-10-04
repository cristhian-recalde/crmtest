package com.trilogy.app.crm.home;

import java.io.IOException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.SpidAwareAccountIdentification;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * @author suyash.gaidhani@redknee.com	
 * This class is supposed to adapt the AccountIdentification bean to SpidAwareAccountIdentification bean
 * and invoke the corresponding operation on the other pipeline. 
 * @since 9.2
 */
public class SpidAwareAccountIdentificationInvocationHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpidAwareAccountIdentificationInvocationHome(final Home delegate)
	{
		super(delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		AccountIdentification identification = (AccountIdentification) super.create(ctx, obj);

		try
		{
			SpidAwareAccountIdentification spidAwareBean = adaptToSpidAwareBean(ctx, identification);
			HomeSupportHelper.get(ctx).createBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
		return identification;
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
	HomeInternalException {

		AccountIdentification identification = (AccountIdentification) super.store(ctx, obj);

		try
		{
			SpidAwareAccountIdentification spidAwareBean = adaptToSpidAwareBean(ctx, identification);
			HomeSupportHelper.get(ctx).storeBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
		return identification;
	
	}
	
	@Override
	public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		super.remove(ctx, obj);
		
		AccountIdentification identification = (AccountIdentification) obj;
		
		try
		{
			SpidAwareAccountIdentification spidAwareBean = adaptToSpidAwareBean(ctx, identification);
			HomeSupportHelper.get(ctx).removeBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
	}

	private SpidAwareAccountIdentification adaptToSpidAwareBean(Context ctx, AccountIdentification identification) throws HomeException, IOException, InstantiationException
	{
		SpidAwareAccountIdentification spidAwareBean = null;

		spidAwareBean = (SpidAwareAccountIdentification) XBeans.instantiate(SpidAwareAccountIdentification.class, ctx);

		Account account =  (Account) ctx.get(Account.class);

		if(account == null)
		{
			account = AccountSupport.getAccount(ctx, identification.getBAN());
		}

		if(account != null)
		{
			spidAwareBean.setBAN(identification.getBAN());
			spidAwareBean.setId(identification.getId());
			spidAwareBean.setIdGroup(identification.getIdGroup());
			spidAwareBean.setIdType(identification.getIdType());
			spidAwareBean.setIdNumber(identification.getIdNumber());
			spidAwareBean.setExpiryDate(identification.getExpiryDate());
			spidAwareBean.setSpid(account.getSpid());
			
			if(LogSupport.isDebugEnabled(ctx))
			{
                StringBuilder buf = new StringBuilder();
                buf.append("Created SpidAwareAccountIdentification Bean.");
                buf.append(" BAN : ");
                buf.append(spidAwareBean.getBAN());
                buf.append(" ID : ");
                buf.append(spidAwareBean.getId());
                buf.append(" IdGroup : ");
                buf.append(spidAwareBean.getIdGroup());
                buf.append(" IdType : ");
                buf.append(spidAwareBean.getIdType());
                buf.append(" IdNumber : ");
                buf.append(spidAwareBean.getIdNumber());
                buf.append(" ExpirtyDate : ");
                buf.append(spidAwareBean.getExpiryDate());
                buf.append(" Spid : ");
                buf.append(spidAwareBean.getSpid());
                LogSupport.debug(ctx, this, buf.toString());
            }
		}
		else
		{
			throw new HomeException("Unable to obtain Account for BAN :" + identification.getBAN());
		}

		return  spidAwareBean;
	}
}
