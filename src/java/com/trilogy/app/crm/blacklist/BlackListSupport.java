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
package com.trilogy.app.crm.blacklist;

import java.security.Principal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.BlackListConfig;
import com.trilogy.app.crm.bean.BlackListConfigHome;
import com.trilogy.app.crm.bean.BlackListHome;
import com.trilogy.app.crm.bean.BlackListXInfo;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Support class for blacklist and graylist.
 *
 * @author joe.chen@redknee.com
 */
public final class BlackListSupport
{

	public static final String BYPASS_BLACKLIST_VALIDATION =
	    "BYPASS_BLACKLIST_VALIDATION";

	public static final String BLACKLIST_OVERRIDE_COLOUR =
	    "BLACKLIST_OVERRIDE_COLOUR";

	public static final SimplePermission BLACKLIST_OVERRIDE_PERMISSION =
	    new SimplePermission("app.crm.blacklist.override");

    /**
     * Creates a new <code>BlackListSupport</code> instance. This method is made private
     * to prevent instantiation of utility class.
     */
    private BlackListSupport()
    {
        // empty
    }


    /**
     * Returns blacklist item with the provided ID.
     *
     * @param ctx
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @return Blacklist item with the provided ID, <code>null</code> if none exists.
     * @throws HomeException
     *             Thrown if there are problems looking up the blacklist item.
     */
    public static BlackList getIdList(Context ctx, final int idType, final String idNumber) throws HomeException
    {
        if (ctx==null)
        {
            LogSupport.minor(ctx, BlackListSupport.class, "No context received. Using context locator");
            ctx = ContextLocator.locate();
        }

        final Home blackListHome = (Home) ctx.get(BlackListHome.class);

        BlackList item = null;
        if (idNumber != null && idNumber.length() > 0)
        {
            /*
             * [Cindy] 2007-11-26: Changed to e-lang.
             */
            final And and = new And();
            and.add(new EQ(BlackListXInfo.ID_TYPE, idType));
            and.add(new EQ(BlackListXInfo.ID_NUMBER, idNumber));
            item = (BlackList) blackListHome.find(ctx, and);
        }
        return item;
    }


    /**
     * Returns the blacklist colour of the provided ID.
     *
     * @param ctx
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @return The list type (Black/Gray) the ID is in, or <code>null</code> if it is
     *         not in any black/gray list.
     * @throws HomeException
     *             Thrown if there are problems looking up the blacklist.
     */
    public static BlackTypeEnum getIdListType(final Context ctx, final int idType, final String idNumber)
        throws HomeException
    {
        BlackTypeEnum result = null;
        // look up the BlackListHome to see if the number is blacklisted
        final BlackList item = getIdList(ctx, idType, idNumber);
        if (item != null)
        {
            result = item.getBlackType();
        }
        return result;
    }


    /**
     * Determines if the provided ID is on blacklist/graylist.
     *
     * @param ctx
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @return Returns <code>true</code> if the ID is on blacklist/graylist,
     *         <code>false</code> otherwise.
     * @throws HomeException
     *             Thrown if there are problems looking up the ID on blacklist.
     */
    public static boolean isIdInList(final Context ctx, final int idType, final String idNumber) throws HomeException
    {
        final BlackTypeEnum isInList = getIdListType(ctx, idType, idNumber);
        return isInList != null;
    }


    /**
     * Returns the configuration of a particular blacklist type.
     *
     * @param ctx
     *            The operating context.
     * @param blackListType
     *            Black list type.
     * @return Blacklist configuration of the provided type.
     * @throws HomeException
     *             Thrown if there are problems looking up the configuration of the
     *             provided type.
     */
    public static BlackListConfig getListTypeConfig(final Context ctx, final int blackListType) throws HomeException
    {
        final Home blackListConfigHome = (Home) ctx.get(BlackListConfigHome.class);
        final BlackListConfig blackConfig = (BlackListConfig) blackListConfigHome.find(ctx,
                Integer.valueOf(blackListType));
        return blackConfig;
    }


    /**
     * Returns the configuration of blacklisting.
     *
     * @param ctx
     *            The operating context.
     * @return Configuration of blacklisting.
     * @throws HomeException
     *             Thrown if there are problems looking up the configuration.
     */
    public static BlackListConfig getBlackListConfig(final Context ctx) throws HomeException
    {
        return getListTypeConfig(ctx, BlackTypeEnum.BLACK_INDEX);
    }


    /**
     * Returns the configuration of graylisting.
     *
     * @param ctx
     *            The operating context.
     * @return Configuration of graylisting.
     * @throws HomeException
     *             Thrown if there are problems looking up the configuration.
     */
    public static BlackListConfig getGreyListConfig(final Context ctx) throws HomeException
    {
        return getListTypeConfig(ctx, BlackTypeEnum.GRAY_INDEX);
    }

	/**
	 * Calculates the darkest(?) blacklist colour of this account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param account
	 *            Account in concern.
	 * @return The colour of the account.
	 * @throws HomeException
	 *             Thrown if there are errors looking up the account.
	 */
	public static BlackTypeEnum getAccountBlacklistType(Context ctx,
	    Account account) throws HomeException
	{
		BlackTypeEnum result = null;
		List accountIdList = account.getIdentificationList();
		if (null != accountIdList)
		{
			// test for black
			for (Object o : accountIdList)
			{
				AccountIdentification ai = (AccountIdentification) o;
				if (BlackTypeEnum.BLACK.equals(ai.getIsIdListed(ctx)))
				{
					result = BlackTypeEnum.BLACK;
					break;
				}
			}

			if (result == null)
			{
				for (Object o : accountIdList)
				{
					AccountIdentification ai = (AccountIdentification) o;
					if (BlackTypeEnum.GRAY.equals(ai.getIsIdListed(ctx)))
					{
						result = BlackTypeEnum.GRAY;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Whether blacklist override is enabled and authorized for the currently
	 * logged in user.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @return Whether blacklist override is enabled and authorized for the
	 *         currently logged in user.
	 */
	public static boolean isBlacklistOverrideEnabled(Context ctx)
	{
		boolean override = ctx.getBoolean(BYPASS_BLACKLIST_VALIDATION, false);
		if (override)
		{
		    override = FrameworkSupportHelper.get(ctx).hasPermission(ctx, BLACKLIST_OVERRIDE_PERMISSION);
		}
		return override;
	}

	/**
	 * Validates the ID associated with an account is not currently blacklisted
	 * or graylisted.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param account
	 *            The account being validated.
	 * @param oldAccount
	 *            The old account.
	 * @param el
	 *            Exception listener.
	 * @return Returns Colour of the overridden blacklist item, if any.
	 * @throws HomeException
	 *             Thrown if there are problems querying the blacklist database.
	 */
    public static BlackTypeEnum validateAccountBlacklist(final Context ctx,
	    final Account account, final Account oldAccount,
        final CompoundIllegalStateException el) throws HomeException
    {
		BlackTypeEnum overridden = null;
		BlackListConfig blackConfig = getBlackListConfig(ctx);
		BlackListConfig grayConfig = getGreyListConfig(ctx);

		boolean validateBlacklistOnCreation =
		    blackConfig == null || !blackConfig.getNewAccountActivation();
		boolean validateGraylistOnCreation =
		    grayConfig == null || !grayConfig.getNewAccountActivation();
		boolean validateBlacklistOnReactivation =
		    blackConfig == null
		        || !blackConfig.getExistingAccountReactivation();
		boolean validateGraylistOnReactivation =
		    grayConfig == null || !grayConfig.getExistingAccountReactivation();

		ExceptionListener warnings =
		    (ExceptionListener) ctx.get(HTMLExceptionListener.class);
		if (warnings == null)
		{
			warnings = (ExceptionListener) ctx.get(ExceptionListener.class);
		}

		List accountIdList = account.getIdentificationList();

		// check blacklist
		if (oldAccount == null)
        {
			if (validateBlacklistOnCreation)
			{
				overridden =
				    validateBlackListColour(ctx, accountIdList,
				        BlackTypeEnum.BLACK, "created", "Account.blackList",
				        isBlacklistOverrideEnabled(ctx), false, warnings, el);
			}

			// check gray list
			if (validateGraylistOnCreation)
			{
				BlackTypeEnum colour =
				    validateBlackListColour(ctx, accountIdList,
				        BlackTypeEnum.GRAY, "created", "Account.grayList",
				        false, true, warnings, warnings);
				if (overridden == null)
				{
					overridden = colour;
				}
			}
		}
		// prevents existing account reactivation
		else if (SafetyUtil.safeEquals(oldAccount.getState(),
		    AccountStateEnum.IN_ARREARS)
		    && SafetyUtil.safeEquals(account.getState(),
		        AccountStateEnum.ACTIVE))
        {
			if (validateBlacklistOnReactivation)
			{
				overridden =
				    validateBlackListColour(ctx, accountIdList,
				        BlackTypeEnum.BLACK, "reactivated",
				        "Account.blackList", isBlacklistOverrideEnabled(ctx),
				        false, warnings, el);
			}

			// check gray list
			if (validateGraylistOnReactivation)
			{
				BlackTypeEnum colour =
				    validateBlackListColour(ctx, accountIdList,
				        BlackTypeEnum.GRAY, "reactivated", "Account.grayList",
				        false, true, warnings, el);
				if (overridden == null)
				{
					overridden = colour;
				}
			}
        }

		return overridden;
    }
    
    private static String getTypeDescription(Context ctx, int typeId) throws HomeException
    {
        StringBuilder result = new StringBuilder("'");
        result.append(typeId);
        Identification identification = HomeSupportHelper.get(ctx).findBean(ctx, Identification.class, Integer.valueOf(typeId));
        if (identification!=null)
        {
            result.append(" - ");
            result.append(identification.getDesc());
        }
        result.append("'");
        return result.toString();
    }


	protected static BlackTypeEnum validateBlackListColour(final Context ctx,
	    List accountIdList, BlackTypeEnum colour, String action,
	    String propertyName, boolean override, boolean warnOnly,
	    ExceptionListener warnings, final ExceptionListener failures)
	    throws HomeException
	{
		BlackTypeEnum overridden = null;
		if (null != accountIdList)
		{
			Iterator i = accountIdList.iterator();
			while (i.hasNext())
			{
				AccountIdentification ai = (AccountIdentification) i.next();
				if (colour.equals(ai.getIsIdListed(ctx)))
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Identification with type ");
					sb.append(getTypeDescription(ctx, ai.getIdType()));
					sb.append(" and number '");
					sb.append(ai.getIdNumber());
					sb.append("' is on the ");
					sb.append(colour.getDescription());
					sb.append(" List");
					if (override || warnOnly)
					{
						sb.append(": The account is still ");
						sb.append(action);
						if (override)
						{
							sb.append(" because it is overridden");
						}
						overridden = colour;
						if (warnings != null)
						{
							warnings
							    .thrown(new IllegalPropertyArgumentException(
							        propertyName, sb.toString()));
						}
					}
					else
					{
						sb.append(": The account cannot be ");
						sb.append(action);
						if (failures != null)
						{
							failures
							    .thrown(new IllegalPropertyArgumentException(
							        propertyName, sb.toString()));
						}
					}

					if (LogSupport.isDebugEnabled(ctx))
					{
						new DebugLogMsg(BlackListSupport.class, sb.toString(),
						    null).log(ctx);
					}
				}
			}
		}
		return overridden;
	}

    /**
     * Adds an ID to black list.
     *
     * @param ctx
     *            The operating context.
     * @param type
     *            ID type.
     * @param id
     *            ID to be blacklisted/graylisted.
     * @param ban
     *            Account BAN.
     * @param state
     *            Account state.
     * @param colour
     *            Colour of the ID (black or gray).
     * @return The blacklist item.
     * @throws HomeException
     *             Thrown if there are problems adding the ID to blacklist.
     */
    public static BlackList addIdToBlackList(final Context ctx, final int type, final String id, final String ban,
        final AccountStateEnum state, final BlackTypeEnum colour) throws HomeException
    {
        final Home blackListHome = (Home) ctx.get(BlackListHome.class);
        BlackList result = null;

        result = getIdList(ctx, type, id);
        final StringBuilder note = new StringBuilder();
        note.append('[');
        note.append(new Date());
        note.append("] ID account ");
        note.append(ban);
        note.append(" state changed to ");
        note.append(state);
        if (result != null)
        {
            // entry already there
            if (BlackTypeEnum.BLACK.equals(result.getBlackType()))
            {
                /*
                 * we do not support demoting a black listed number to gray listed
                 * instead.
                 */
                if (LogSupport.isDebugEnabled(ctx))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("ID type=");
                    sb.append(type);
                    sb.append(", number=");
                    sb.append(id);
                    sb.append(" is already blacklisted as ");
                    sb.append(result.getBlackType());
                    LogSupport.debug(ctx, BlackListSupport.class, sb.toString());
                }
            }
            else
            {
                // update the colour of the id
                result.setBlackType(colour);
                result.setNote(note.toString());
                blackListHome.store(ctx, result);
            }
        }
        else
        {
            // insert new entry
            result = new BlackList();
            result.setBlackListID(0L);
            result.setIdType(type);
            result.setIdNumber(id);
            result.setBlackType(colour);
            result.setNote(note.toString());
            result = (BlackList) blackListHome.create(ctx, result);
        }
        return result;

    }


    /**
     * Removes an ID from gray list.
     *
     * @param context
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @throws HomeException
     *             Thrown if there are problems removing the ID from gray list.
     */
    public static void removeIdFromGrayList(final Context context, final int idType, final String idNumber)
        throws HomeException
    {
        final Home blackListHome = (Home) context.get(BlackListHome.class);
        if (blackListHome == null)
        {
            throw new HomeException("System error: BlackListHome not found in database");
        }

        final And and = new And();
        and.add(new EQ(BlackListXInfo.ID_TYPE, idType));
        and.add(new EQ(BlackListXInfo.ID_NUMBER, idNumber));
        // only gray will be removed
        and.add(new EQ(BlackListXInfo.BLACK_TYPE, BlackTypeEnum.GRAY));
        final BlackList item = (BlackList) blackListHome.find(context, and);
        if (item == null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Cannot find ID type=");
                sb.append(idType);
                sb.append(", number=");
                sb.append(idNumber);
                sb.append(" in gray list");
                LogSupport.debug(context, BlackListSupport.class, sb.toString());
            }
        }
        else
        {
            blackListHome.remove(context, item);
        }
    }


    /**
     * Validates that ID is not blacklisted.
     *
     * @param ctx
     *            The operating context.
     * @param idType
     *            ID type.
     * @param idNumber
     *            ID number.
     * @param property
     *            Property info of the bean field being validated.
     * @param el
     *            Exception listener.
     * @throws HomeException
     *             Thrown if there are problems examining the blacklist.
     */
    public static void validateIdBlacklist(final Context ctx, final int idType, final String idNumber,
        final PropertyInfo property, final CompoundIllegalStateException el) throws HomeException
    {
        final String msg = "Identification number '" + idNumber + "' of type " + getTypeDescription(ctx, idType) + 
        		" is blacklisted.";
        if (idNumber != null && idNumber.trim().length() > 0)
        {
            final BlackList item = getIdList(ctx, idType, idNumber);
            if (item != null && SafetyUtil.safeEquals(item.getBlackType(), BlackTypeEnum.BLACK))
            {
				if (ctx.getBoolean(BYPASS_BLACKLIST_VALIDATION, false))
				{
					LogSupport.debug(ctx, BlackListSupport.class, "ID type="
					    + idType + ", idNumber=" + idNumber
					    + " is blacklist, but blacklist is overridden.");
				}
				else
				{
					el.thrown(new IllegalPropertyArgumentException(property,
					    msg));
				}
            }
        }
    }
}
