/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bulkloader.BulkLoadIdentification;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This is a collection of utility methods for working with Accounts.
 * 
 * @author marcio.marques@redknee.com
 */
public class AccountIdentificationSupport
{

	public static final String DEFAULT_EXPIRY_DATE_FORMAT = "yyyy-MM-dd";
	public static final String DEFAULT_IDENTIFICATION_DELIMITER = "\\|";
	public static final String DEFAULT_IDENTIFICATION_FIELD_DELIMIBER = ";";
	public static final int TOTAL_IDENTIFICATION_FIELDS = 3;
	public static final int TOTAL_IDENTIFICATION_FIELDS_NO_EXPIRY = 2;
	public static final int IDENTIFICATION_ID_TYPE_FIELD = 0;
	public static final int IDENTIFICATION_ID_NUMBER_FIELD = 1;
	public static final int IDENTIFICATION_EXPIRY_DATE_FIELD = 2;

    public static AccountIdentificationGroup addDefaultAccountIdentificationGroupToList(Context ctx, Account account)
    {
        List<AccountIdentificationGroup> idList = account.getIdentificationGroupList();
        return addDefaultAccountIdentificationGroupToList(ctx, idList);
    }
    
    /**
     * Adds a DEFAULT_GROUP account identification group to the groups list of
     * the provided account. This is used when identifications that do not fit
     * in any of the defined groups are retrieved from old accounts, or provided
     * through AUDI Tool or API.
     * 
     * It's worth mention that subscribers won't be created if this list is
     * present. Nevertheless, it needs to be here for displaying purposes.
     * 
     * @param ctx
     * @param account
     * @return
     */
    public static AccountIdentificationGroup addDefaultAccountIdentificationGroupToList(Context ctx, List<AccountIdentificationGroup> identificationsList)
    {
        AccountIdentificationGroup accountIdGroup = null;
        try
        {
            accountIdGroup = (AccountIdentificationGroup) XBeans.instantiate(AccountIdentificationGroup.class, ctx);
        }
        catch (Exception e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport
                        .debug(
                                ctx,
                                AccountSupport.class.getName(),
                                "Exception while instantiating a new AccountIdentificationGroup object. Creating an empty one.",
                                e);
            }
            accountIdGroup = new AccountIdentificationGroup();
        }
        accountIdGroup.setGroup("--");
        accountIdGroup.setIdGroup(-1);
        identificationsList.add(accountIdGroup);
        return accountIdGroup;
    }


    /**
     * Adds an account identification to the account. This method will check add
     * the identification to the first group where the identification is allowed
     * and is not full. If no group where the identification can be added is found, 
     * it will be added to the default group.
     * 
     * @param ctx
     * @param ai
     * @param account
     * @param spidIdGroups
     */
	public static boolean addAccountIdentification(Context ctx,
	    AccountIdentification ai, Account account,
            SpidIdentificationGroups spidIdGroups)
    {
        boolean added = false;

        if (spidIdGroups != null)
        {
            Iterator<IdentificationGroup> i = spidIdGroups.getGroups().iterator();

            while (i.hasNext())
            {
                IdentificationGroup group = i.next();
                if (group.getIdentificationList().contains(String.valueOf(ai.getIdType())))
                {
                    added = addToGroup(ctx, ai, group, account);
                    if (added)
                    {
                        break;
                    }
                }
            }
        }

        if (!added)
        {
            ai.setIdGroup(AccountIdentification.DEFAULT_IDGROUP);
			return addToGroup(ctx, ai, null, account);
        }
		return added;
    }


    /**
     * Adds an account identification to a group if it's not full. Returns true
     * if the identification could be successfully added to the group.
     * 
     * @param ctx
     * @param ai
     * @param account
     * @param spidIdGroups
     */
	private static boolean addToGroup(Context ctx, AccountIdentification ai, IdentificationGroup group, ConvertAccountBillingTypeRequest account,List<AccountIdentificationGroup> aig)
    {
        boolean result = false;
        int idGroup = IdentificationGroup.DEFAULT_IDGROUP;
        int noOfrequiredValidation = Integer.MAX_VALUE;

        if (group != null)
        {
            idGroup = group.getIdGroup();
            noOfrequiredValidation = group.getRequiredNumber();
        }
      
        List<AccountIdentification> list = null;
        AccountIdentificationGroup accountIdGroup = null;
        Iterator<AccountIdentificationGroup> it = aig.iterator();
      
        while ( it.hasNext())
        {
        	accountIdGroup = it.next();
        	if(accountIdGroup.getIdGroup() == idGroup){
        		break;
        	}
        	
		}
        
        if (accountIdGroup == null)
        {
        	try
        	{
        		accountIdGroup = (AccountIdentificationGroup) XBeans.instantiate(AccountIdentificationGroup.class, ctx);
        	}
        	catch (Exception e)
        	{
        		if (LogSupport.isDebugEnabled(ctx))
        		{
        			LogSupport
        			.debug(
        					ctx,
        					AccountSupport.class.getName(),
        					"Exception while instantiating a new AccountIdentificationGroup object. Creating an empty one.",
        					e);
        		}
        		accountIdGroup = new AccountIdentificationGroup();
        		accountIdGroup.setIdGroup(idGroup);
        	}
        }
           
        
        list = accountIdGroup.getIdentificationList();

        if (list.size() <= noOfrequiredValidation)
        {
        	AccountIdentification accountIdentification = null;
        	int count=0;
        	boolean isChanged = false;
        	for (Iterator iterator = list.iterator(); iterator.hasNext();)
        	{
        		accountIdentification = (AccountIdentification) iterator.next();
				if(accountIdentification.getIdType() == -1)
				{
					ai.setIdGroup(idGroup);
					accountIdentification = ai;
					isChanged = true;
					break;
				}
				count ++;
				
			}
        	if(isChanged)
        		list.set(count,accountIdentification );
            
        	result = true;
        }
       
        return result;
    }


    /**
     * This method is using to retrieve example identifications. This is used to fill the account identifications
     * for postpaid accounts created by AUDI Tool or bulk load with example values. The sets allowedIds and usedIds
     * are used to define what identifications can be still generated. 
     * @param ctx
     * @param spid Service provider identifier.
     * @param allowedIds List of allowed ids. If empty, all ids in the spid are allowed.
     * @param usedIds List of ids already used which cannot be used again.
     * @return
     */
    private static AccountIdentification getNextDefaultAccountIdentification(final Context ctx, final int spid,
            final Set<Integer> allowedIds, final Set<Integer> usedIds)
    {
        final AccountIdentification ai = new AccountIdentification();
        final Home h = (Home) ctx.get(IdentificationHome.class);
        if (null != h)
        {
            final And where = new And();
            where.add(new EQ(IdentificationXInfo.SPID, Integer.valueOf(spid)));
            where.add(new Not(new In(IdentificationXInfo.CODE, usedIds)));
            if (allowedIds != null)
            {
                where.add(new In(IdentificationXInfo.CODE, allowedIds));
            }

            try
            {
                final Iterator i = h.select(ctx, where).iterator();
                if (i.hasNext())
                {
                    final Identification id = (Identification) i.next();
                    usedIds.add(Integer.valueOf(id.getCode()));
                    ai.setIdType(id.getCode());
                    ai.setIdNumber(IdentificationSupport.getExample(id));
                }
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, AccountSupport.class.getName(),
                        "Exception caught accessing Account Identification home.", e);
            }
        }
        else
        {
            LogSupport.minor(ctx, AccountSupport.class.getName(),
                    "System Error: Unable to find Account Identification home in the context.");
        }

        return ai;
    }

    /**
     * This method is used to fill in the required identifications with example
     * values. Right now, it is used to fill the account identifications for
     * postpaid accounts created by AUDI Tool or bulk load.
     * 
     * @param ctx
     * @param account
     * @param spidIdGroups
     * @param usedIds
     */
    public static void fillInIdentificationSpots(Context ctx, Account account, SpidIdentificationGroups spidIdGroups,
            Set<Integer> usedIds)
    {
        if (spidIdGroups != null)
        {
            Iterator<IdentificationGroup> i = spidIdGroups.getGroups().iterator();
            while (i.hasNext())
            {
                IdentificationGroup idGroup = i.next();
                if (idGroup.getRequiredNumber() > 0)
                {
                    List<AccountIdentification> aiList = account.getIdentificationList(idGroup.getIdGroup());
                    if (aiList == null)
                    {
                        AccountIdentificationGroup accountIdGroup = null;
                        try
                        {
                            accountIdGroup = (AccountIdentificationGroup) XBeans.instantiate(
                                    AccountIdentificationGroup.class, ctx);
                        }
                        catch (Exception e)
                        {
                            accountIdGroup = new AccountIdentificationGroup();
                        }
                        accountIdGroup.setIdGroup(idGroup.getIdGroup());
                        account.getIdentificationGroupList().add(accountIdGroup);
                        aiList = accountIdGroup.getIdentificationList();
                    }
                    int size = aiList.size();
                    Set<Integer> identificationSets = null;

                    /*
                     * If accept any identification, leave identificationSet
                     * equals null so that method
                     * getNextDefaultAccountIdentification won't use it to
                     * restrict the search.
                     */
                    if (!idGroup.isAcceptAny())
                    {

                        identificationSets = new HashSet<Integer>();
                        StringTokenizer st = new StringTokenizer(idGroup.getIdentificationIdList(), ",");
                        while (st.hasMoreTokens())
                        {
                            identificationSets.add(Integer.valueOf(st.nextToken()));
                        }
                    }

                    for (int j = size; j < idGroup.getRequiredNumber(); j++)
                    {
                        final AccountIdentification ai = getNextDefaultAccountIdentification(ctx, account.getSpid(),
                                identificationSets, usedIds);
                        if (ai != null)
                        {
                            ai.setIdGroup(idGroup.getIdGroup());
                            LogSupport.debug(ctx, AccountSupport.class.getName(), "ADDING ID TO LIST [" + ai + "]");
                            aiList.add(ai);
                        }
                        else
                        {
                            LogSupport.debug(ctx, AccountSupport.class.getName(),
                                    "ERROR: NO MORE AVAILABLE IDS TO ADD TO GROUP [" + idGroup.getIdGroup() + "]");
                            break;
                        }
                    }
                }
            }
        }
    }


	/**
	 * Creates the AccountIdentification object. Also parses and validates the
	 * IDType with respect to SPID.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param spid
	 *            SPID.
	 * @param BAN
	 *            BAN.
	 * @param idType
	 *            ID Type.
	 * @param idNumber
	 *            ID Number.
	 * @param expiryDate
	 *            Expiry Date. Leave as null if none is provided.
	 * @return The AccountIdentification object corresponding to the provided
	 *         data.
	 */
	private static AccountIdentification createAccountIdentificationObject(
	    Context ctx, int spid, String BAN, String idType, String idNumber,
	    String expiryDate)
	{
		AccountIdentification id = new AccountIdentification();
		int idTypeInt = Integer.MIN_VALUE;
		try
		{
			idTypeInt = Integer.parseInt(idType);
			Identification identification =
			    IdentificationSupport.getIdentification(ctx, idTypeInt);
			if (identification == null || identification.getSpid() != spid)
			{
				LogSupport.info(ctx, AccountIdentificationSupport.class,
				    "ID Type " + idType + " does not exist for SPID " + spid);
				return null;
			}
		}
		catch (NumberFormatException e)
		{
			LogSupport.info(ctx, AccountIdentificationSupport.class,
			    "ID Type [" + idType + "] is invalid [BAN=" + BAN + "]");
			return null;
		}
		
		
		id.setIdType(idTypeInt);
		id.setBAN(BAN);
		id.setIdNumber(idNumber);
		Date date = null;
		if (expiryDate != null && !expiryDate.trim().isEmpty())
			try
			{
				DateFormat format =
				    new SimpleDateFormat(DEFAULT_EXPIRY_DATE_FORMAT);
				date = format.parse(expiryDate);
			}
			catch (ParseException e)
			{
				LogSupport.info(ctx, AccountIdentificationSupport.class,
				    "Expiry Date [" + expiryDate + "] is invalid [BAN=" + BAN
				        + "]");
				return null;
			}
		id.setExpiryDate(date);
		return id;
	}

	public static Identification
	    getIdType(Context ctx, int spid, String idType1)
	{
		int idType;
		try
		{
			idType = Integer.parseInt(idType1);
		}
		catch (NumberFormatException exception)
		{
			LogSupport.info(ctx, AccountIdentificationSupport.class, "ID Type "
			    + idType1 + " is malformed");
			return null;
		}

		And and = new And();
		and.add(new EQ(IdentificationXInfo.SPID, spid));
		and.add(new EQ(IdentificationXInfo.CODE, idType));

		Identification id = null;
		try
		{
			id =
			    (Identification) ((Home) ctx.get(IdentificationHome.class))
			        .find(ctx, and);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, AccountIdentificationSupport.class,
			    "HomeException caught while looking up identification "
			        + idType1, exception);
			return null;
		}
		catch (NullPointerException exception)
		{
			LogSupport.minor(ctx, AccountIdentificationSupport.class,
			    "IdentificationHome does not exist in context!", exception);
			return null;
		}

		return id;
	}

	public static boolean isNewIdentificationFormat(String idTypeStr,
	    String idNumber)
	{
		if (idTypeStr == null || idTypeStr.trim().isEmpty())
		{
			return (idNumber != null && !idNumber.trim().isEmpty());
		}
		return false;
	}

	public static BulkLoadIdentification createBulkLoadIdentificationObject(
	    String idType, String idNumber, String expiryDate)
	    throws AgentException
	{
		BulkLoadIdentification id = new BulkLoadIdentification();
		try
		{
			int idTypeInt = Integer.parseInt(idType);
			id.setIdType(idTypeInt);
			id.setIdNumber(idNumber);
			id.setExpiryDate(expiryDate);
		}
		catch (NumberFormatException e)
		{
			throw new AgentException("ID Type \"" + idType + "\" is invalid");
		}
		return id;
	}

	public static List<BulkLoadIdentification> parseIdentifications(
	    Context ctx, String idType1, String idNumber1, String idType2,
	    String idNumber2) throws AgentException
	{
		List<BulkLoadIdentification> results =
		    new ArrayList<BulkLoadIdentification>();

		// legacy format
		if (!isNewIdentificationFormat(idType1, idNumber1) && idType1 != null
		    && !idType1.isEmpty())
		{
			BulkLoadIdentification id =
			    createBulkLoadIdentificationObject(idType1, idNumber1, null);
			if (id != null)
			{
				results.add(id);
			if (idType2 != null && !idType2.isEmpty())
			{
					id =
					    createBulkLoadIdentificationObject(idType2, idNumber2,
					        null);
					results.add(id);
				}
			}
		}
		// new format
		else
		{
			String[] idStrings =
			    idNumber1.split(DEFAULT_IDENTIFICATION_DELIMITER);
			for (int i = 0; i < idStrings.length; i++)
			{
				String[] idFields =
				    idStrings[i].split(DEFAULT_IDENTIFICATION_FIELD_DELIMIBER);
				if (idFields.length == TOTAL_IDENTIFICATION_FIELDS)
				{
					results.add(createBulkLoadIdentificationObject(
					    idFields[IDENTIFICATION_ID_TYPE_FIELD],
					    idFields[IDENTIFICATION_ID_NUMBER_FIELD],
					    idFields[IDENTIFICATION_EXPIRY_DATE_FIELD]));
				}
				else if (idFields.length == TOTAL_IDENTIFICATION_FIELDS_NO_EXPIRY)
				{
					results.add(createBulkLoadIdentificationObject(
					    idFields[IDENTIFICATION_ID_TYPE_FIELD],
					    idFields[IDENTIFICATION_ID_NUMBER_FIELD], ""));
				}
				else
				{
					int n = i + 1;
					LogSupport.info(ctx, AccountIdentificationSupport.class,
					    "ID " + n
					        + " does not have the required number of fields");
					continue;
				}
			}
		}
		return results;
	}

	public static Set<Integer> addAccountIdentifications(Context ctx,
	    Account account, List<BulkLoadIdentification> identifications)
	{
		SpidIdentificationGroups spidIdGroups = null;

		try
		{
			spidIdGroups =
			    SpidSupport.getSpidIdentificationGroups(ctx, account.getSpid());
		}
		catch (Exception e)
		{
			LogSupport
			    .info(
			        ctx,
			        AccountIdentificationSupport.class,
			        "Exception caught while trying to find SPID Identification groups info for SPID "
			            + account.getSpid() + "[BAN=" + account.getBAN() + "]",
			        e);
		}

		if (null == spidIdGroups)
		{
			LogSupport.info(ctx, AccountIdentificationSupport.class,
			    "No SPID Identification Groups configuration defined for SPID "
			        + account.getSpid() + "[BAN=" + account.getBAN() + "]");
		}

		List<AccountIdentification> ids =
		    new ArrayList<AccountIdentification>(identifications.size());
		for (BulkLoadIdentification in : identifications)
		{
			AccountIdentification id =
			    createAccountIdentificationObject(ctx, account.getSpid(),
			        account.getBAN(), String.valueOf(in.getIdType()),
			        in.getIdNumber(), in.getExpiryDate());
			if (id != null)
			{
				ids.add(id);
			}
		}

		Set<Integer> addedIdTypes = new HashSet<Integer>(ids.size());
		for (AccountIdentification id : ids)
		{
			boolean added =
			    addAccountIdentification(ctx, id, account, spidIdGroups);
			if (added)
			{
				addedIdTypes.add(id.getIdType());
			}
		}
		return addedIdTypes;
	}

	public static boolean updateAccountIdentification(Context ctx,
	    AccountIdentification id, Account account,
	    SpidIdentificationGroups spidIdGroups)
	{
		boolean updatedOrAdded = false;
		And and = new And();
		and.add(new EQ(AccountIdentificationXInfo.BAN, account.getBAN()));
		and.add(new EQ(AccountIdentificationXInfo.ID_TYPE, id.getIdType()));
		AccountIdentification storedId = null;
		try
		{
			storedId =
			    HomeSupportHelper.get(ctx).findBean(ctx,
			        AccountIdentification.class, and);
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, AccountIdentificationSupport.class,
			    "Exception caught while looking up AccountIdentification [BAN="
			        + account.getBAN() + ",idType=" + id.getIdType() + "]", e);
		}

		if (storedId == null
		    || (!SafetyUtil
		        .safeEquals(storedId.getIdNumber(), id.getIdNumber())))
		{
			updatedOrAdded = true;
		}
		return updatedOrAdded;
	}

	public static Set<Integer> updateAccountIdentifications(Context ctx,
	    Account account, List<BulkLoadIdentification> identifications)
	{
		SpidIdentificationGroups spidIdGroups = null;

		try
		{
			spidIdGroups =
			    SpidSupport.getSpidIdentificationGroups(ctx, account.getSpid());
		}
		catch (Exception e)
		{
			LogSupport
			    .info(
			        ctx,
			        AccountIdentificationSupport.class,
			        "Exception caught while trying to find SPID Identification groups info for SPID "
			            + account.getSpid() + "[BAN=" + account.getBAN() + "]",
			        e);
		}

		if (null == spidIdGroups)
		{
			LogSupport.info(ctx, AccountIdentificationSupport.class,
			    "No SPID Identification Groups configuration defined for SPID "
			        + account.getSpid() + "[BAN=" + account.getBAN() + "]");
		}

		List<AccountIdentification> ids =
		    new ArrayList<AccountIdentification>(identifications.size());
		for (BulkLoadIdentification in : identifications)
		{
			AccountIdentification id =
			    createAccountIdentificationObject(ctx, account.getSpid(),
			        account.getBAN(), String.valueOf(in.getIdType()),
			        in.getIdNumber(), in.getExpiryDate());
			if (id != null)
			{
				ids.add(id);
			}
		}

		Set<Integer> updatedIdTypes = new HashSet<Integer>(ids.size());
		for (AccountIdentification id : ids)
		{
			boolean updated =
			    updateAccountIdentification(ctx, id, account, spidIdGroups);
			if (updated)
			{
				updatedIdTypes.add(id.getIdType());
			}
		}
		return updatedIdTypes;
	}

	 /**
     * Adds an account identification to the account. This method will check add
     * the identification to the first group where the identification is allowed
     * and is not full. If no group where the identification can be added is found, 
     * it will be added to the default group.
     * 
     * @param ctx
     * @param ai
     * @param account
     * @param spidIdGroups
     */
	public static boolean addAccountIdentification(Context ctx,
	    AccountIdentification ai, ConvertAccountBillingTypeRequest account,
            SpidIdentificationGroups spidIdGroups , List <AccountIdentificationGroup> aig)
    {
        boolean added = false;

        if (spidIdGroups != null)
        {
            Iterator<IdentificationGroup> i = spidIdGroups.getGroups().iterator();

            while (i.hasNext())
            {
                IdentificationGroup group = i.next();
                if (group.getIdentificationList().contains(String.valueOf(ai.getIdType())))
                {
                    added = addToGroup(ctx, ai, group, account, aig);
                    if (added)
                    {
                        break;
                    }
                }
            }
        }

        if (!added)
        {
            ai.setIdGroup(AccountIdentification.DEFAULT_IDGROUP);
			return addToGroup(ctx, ai, null, account,aig);
        }
		return added;
    }


    /**
     * Adds an account identification to a group if it's not full. Returns true
     * if the identification could be successfully added to the group.
     * 
     * @param ctx
     * @param ai
     * @param account
     * @param spidIdGroups
     */
    private static boolean addToGroup(Context ctx, AccountIdentification ai, IdentificationGroup group, Account account)
    {
        boolean result = false;
        int idGroup = IdentificationGroup.DEFAULT_IDGROUP;
        int size = Integer.MAX_VALUE;

        if (group != null)
        {
            idGroup = group.getIdGroup();
            size = group.getRequiredNumber();
        }
        List<AccountIdentification> list = account.getIdentificationList(idGroup);

        if (list == null)
        {
            AccountIdentificationGroup accountIdGroup = null;
            try
            {
                accountIdGroup = (AccountIdentificationGroup) XBeans.instantiate(AccountIdentificationGroup.class, ctx);
            }
            catch (Exception e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport
                            .debug(
                                    ctx,
                                    AccountSupport.class.getName(),
                                    "Exception while instantiating a new AccountIdentificationGroup object. Creating an empty one.",
                                    e);
                }
                accountIdGroup = new AccountIdentificationGroup();
            }
            accountIdGroup.setIdGroup(idGroup);
            account.getIdentificationGroupList().add(accountIdGroup);
            list = accountIdGroup.getIdentificationList();
        }

        /*
         * Groups can be filled with empty identifications for displaying
         * purposes. When adding a new identification to the end of the group,
         * the group is not considered to be full if it has empties
         * identifications. An empty identification should be removed from the
         * beginning of it and the new valid identification is added to the end of it.
         */
        if (list.size() < size)
        {
            ai.setIdGroup(idGroup);
            list.add(ai);
            result = true;
        }
        else if (list.get(0).getIdType() == AccountIdentification.DEFAULT_IDTYPE
                && list.get(0).getIdNumber().equals(AccountIdentification.DEFAULT_IDNUMBER))
        {
            ai.setIdGroup(idGroup);
            list.add(ai);
            list.remove(0);
            result = true;
        }

        return result;
    }
 
}
