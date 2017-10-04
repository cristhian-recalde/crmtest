package com.trilogy.app.crm.home;

import java.io.IOException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SpidAwareSecurityQuestionAnswer;
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
 * This class is supposed to adapt the SecurityQuestionAnswer bean to SpidAwareSecurityQuestionAnswer bean
 * and invoke the corresponding operation on the other pipeline. 
 * @since 9.2
 */
public class SpidAwareSecurityQuestionAnswerInvocationHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpidAwareSecurityQuestionAnswerInvocationHome(final Home delegate)
	{
		super(delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		SecurityQuestionAnswer resutltBean = (SecurityQuestionAnswer) super.create(ctx, obj);

		try
		{
			SpidAwareSecurityQuestionAnswer spidAwareBean = adaptToSpidAwareBean(ctx, resutltBean);
			HomeSupportHelper.get(ctx).createBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
		return resutltBean;
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
	HomeInternalException {

		SecurityQuestionAnswer resultBean = (SecurityQuestionAnswer) super.store(ctx, obj);

		try
		{
			SpidAwareSecurityQuestionAnswer spidAwareBean = adaptToSpidAwareBean(ctx, resultBean);
			HomeSupportHelper.get(ctx).storeBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
		return resultBean;
	
	}

	@Override
	public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		super.remove(ctx, obj);
		
		SecurityQuestionAnswer resultBean = (SecurityQuestionAnswer) obj;

		try
		{
			SpidAwareSecurityQuestionAnswer spidAwareBean = adaptToSpidAwareBean(ctx, resultBean);
			HomeSupportHelper.get(ctx).removeBean(ctx, spidAwareBean);
		}
		//Any Exception occured in the config-share pipeline should not hamper the original pipeline.
		catch(Exception e)
		{
			new MajorLogMsg(this, e.getLocalizedMessage(), e).log(ctx);
		}
	}

	private SpidAwareSecurityQuestionAnswer adaptToSpidAwareBean(Context ctx, SecurityQuestionAnswer sourceBean) throws HomeException, IOException, InstantiationException
	{
		SpidAwareSecurityQuestionAnswer spidAwareBean = null;

		spidAwareBean = (SpidAwareSecurityQuestionAnswer) XBeans.instantiate(SpidAwareSecurityQuestionAnswer.class, ctx);

		Account account =  (Account) ctx.get(Account.class);

		if(account == null)
		{
			account = AccountSupport.getAccount(ctx, sourceBean.getBAN());
		}

		if(account != null)
		{
			
			spidAwareBean.setBAN(sourceBean.getBAN());
			spidAwareBean.setId(sourceBean.getId());
			spidAwareBean.setQuestion(sourceBean.getQuestion());
			spidAwareBean.setAnswer(sourceBean.getAnswer());
			spidAwareBean.setSpid(account.getSpid());
			
			
			if(LogSupport.isDebugEnabled(ctx))
			{
                StringBuilder buf = new StringBuilder();
                buf.append("Created SpidAwareSecurityQuestionAnswer Bean.");
                buf.append(" BAN : ");
                buf.append(spidAwareBean.getBAN());
                buf.append(" Id : ");
                buf.append(spidAwareBean.getId());
                buf.append(" Question : ");
                buf.append(spidAwareBean.getQuestion());
                buf.append(" Answer : ");
                buf.append(spidAwareBean.getAnswer());
                buf.append(" Spid : ");
                buf.append(spidAwareBean.getSpid());

                LogSupport.debug(ctx, this, buf.toString());
            }
		}
		else
		{
			throw new HomeException("Unable to obtain Account for BAN :" + sourceBean.getBAN());
		}

		return  spidAwareBean;
	}
}
