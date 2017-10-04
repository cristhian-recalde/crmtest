package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Account/Subscriber Note Creation Support for Payment Gateway Integration updates.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class PGINoteSupport {

	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNote(Context ctx , String accountId , String message)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, message
	        		, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.ACCUPDATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}

	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForCreateToken(Context ctx , String accountId , CreditCardToken token)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:createToken Token[id:" + token.getId() + ", maskedCreditCardNumber:" + token.getMaskedCreditCardNumber() +"] successfully created."
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_TOKEN_CREATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForCreateTokenFailure(Context ctx , String accountId , Exception failure)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:createToken failed. Reason :" + failure.getMessage()
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_TOKEN_CREATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}	
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForUpdateToken(Context ctx , String accountId , CreditCardToken token)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:updateToken[id:" + token.getId() + ", maskedCreditCardNumber:" + token.getMaskedCreditCardNumber() +"]"
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_TOKEN_UPDATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}	
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForDeleteToken(Context ctx , String accountId , CreditCardToken token)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:deleteToken Token[id:" + token.getId() + ", maskedCreditCardNumber:" + token.getMaskedCreditCardNumber() +"] has been deleted."
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_TOKEN_DELETE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}	
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForCreateSchedule(Context ctx , String accountId , TopUpSchedule schedule)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:createSchedule Schedule[id:" + schedule.getId() + ", amount:" + schedule.getAmount() +"] successfully created."
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_SCHEDULE_CREATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
	}
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForCreateScheduleFailure(Context ctx , String accountId , String message)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:createSchedule failed. Reason: "  + message
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_SCHEDULE_CREATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
	}	
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForUpdateSchedule(Context ctx , String accountId , TopUpSchedule schedule)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:updateSchedule[id:" + schedule.getId() + ", amount:" + schedule.getAmount() +"]"
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_SCHEDULE_UPDATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}
	
	/**
	 * Add an Account Note for Payment Gateway Integration events.
	 * 
	 * @param ctx
	 * @param accountId - BAN
	 * @param message - Account Note Message
	 */
	public static void addAccountNoteForDeleteSchedule(Context ctx , String accountId , TopUpSchedule schedule)
	{
		try
		{
			NoteSupportHelper.get(ctx).addAccountNote(ctx, accountId, "Account:deleteSchedule Schedule[id:" + schedule.getId() + ", amount:" + schedule.getAmount() +"] deleted."
	        		, SystemNoteTypeEnum.CC_TOP_UP_ACTION, SystemNoteSubTypeEnum.CC_SCHEDULE_DELETE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Account Note for BAN:" + accountId , e);
		}
		
	}
		
		
	/**
	 * Add a Subscriber Note for Payment Gateway Integration events.
	 * @param ctx
	 * @param subscriptionId
	 * @param message
	 */
	public static void addSubscriberNote(Context ctx , String subscriptionId , String message)
	{
		try
		{
			NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subscriptionId, message, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
		}
		catch( HomeException e)
		{
			LogSupport.major(ctx, PGINoteSupport.class.getName(), "Could not add Subscription Note for id:" + subscriptionId , e);
		}
	}
}
