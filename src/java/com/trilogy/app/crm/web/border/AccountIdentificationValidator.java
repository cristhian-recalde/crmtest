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
package com.trilogy.app.crm.web.border;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroupXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.processor.account.ResponsibleAccountMoveProcessor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.IdentificationSupport;

/**
 * Identification Validator at Account level
 * 
 * @author arturo.medina@redknee.com
 * @author marcio.marques@redknee.com
 * @author aaron.gourley@redknee.com
 * @author cindy.wong@redknee.com
 */
public final class AccountIdentificationValidator implements Validator
{
    public static final Object FORCE_VALIDATION_CTX_KEY = "AccountIdentificationValidator.ForceValidation";
    
    private static AccountIdentificationValidator instance_;

    private AccountIdentificationValidator()
    {
    }

    public static synchronized AccountIdentificationValidator instance()
    {
        if (instance_ == null)
        {
            instance_ = new AccountIdentificationValidator();
        }
        return instance_;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;

        List<AccountIdentification> accountIdentifications = new ArrayList<AccountIdentification>();
        List<AccountIdentification> filledAccountIdentifications = new ArrayList<AccountIdentification>();
        List<AccountIdentification> noGroupAccountIdentifications = new ArrayList<AccountIdentification>();

        CompoundIllegalStateException el = new CompoundIllegalStateException();
        try
        {
            fillInAccountIdentificationLists(account, accountIdentifications, filledAccountIdentifications, noGroupAccountIdentifications);
            Home home = (Home) ctx.get(SpidIdentificationGroupsHome.class);
            SpidIdentificationGroups spidIdGroups = null;
            try
            {
                spidIdGroups = (SpidIdentificationGroups) home.find(new EQ(SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(account.getSpid())));
            } 
            catch (HomeException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, "Error retrieving SpidIdentificationGroups for spid " + account.getSpid(), e.getMessage(), e);
                }
            }

            if (ctx.getBoolean(FORCE_VALIDATION_CTX_KEY, false)
                    || needsValidation(ctx, account, filledAccountIdentifications, noGroupAccountIdentifications))
            {
                validateNoGroupAccountIdentification(ctx, el, noGroupAccountIdentifications, spidIdGroups);
            }

            // Verifying if all required fields are filled in with valid values.
            if (spidIdGroups != null)
            {
                validAccountIdentificationFilled(ctx, el, account, accountIdentifications, spidIdGroups);
            }
            else
            {
                validAccountIdentificationFilled(ctx, el, account, filledAccountIdentifications, spidIdGroups);
            }

            validateNoIdFilledMoreThanOnce(ctx, el, filledAccountIdentifications);

            validateFilledIdNumbersFormat(ctx, el, filledAccountIdentifications);
        }
        catch (Throwable t)
        {
            String msg = "Unexpected error during identification validation: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getName());
            new MinorLogMsg(this, msg, t).log(ctx);
            el.thrown(new IllegalPropertyArgumentException(AccountXInfo.IDENTIFICATION_GROUP_LIST, msg));
        }
        el.throwAll();
    }
    
    private boolean needsValidation(Context ctx, Account account, List<AccountIdentification> filledAccountIdentifications, List<AccountIdentification> noGroupAccountIdentifications)
    {
        boolean differs = false;
        
        if (account.getBAN()==null || account.getBAN().isEmpty() || account.getBAN().startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
        {
            differs = true;
        }
        else
        {
            try
            {
                Account oldAccount = AccountSupport.getAccount(ctx, account.getBAN());
                if (oldAccount!=null)
                {
                    int oldNumberOfIdentifications = oldAccount.getIdentificationList().size();
                    
                    if ((account.isHybrid() || account.isPostpaid()) && oldAccount.isPrepaid())
                    {
                        differs = true;
                    }
                    else if (oldNumberOfIdentifications!= filledAccountIdentifications.size() + noGroupAccountIdentifications.size())
                    {
                        differs = true;
                    }
                    else
                    {
                        Iterator<AccountIdentification> i = oldAccount.getIdentificationList().iterator();
                        while (i.hasNext() && !differs)
                        {
                            boolean found = false;
                            AccountIdentification ai = i.next();
                            Iterator<AccountIdentification> j = filledAccountIdentifications.iterator();
                            while (j.hasNext() && !found)
                            {
                                AccountIdentification savedAi = j.next();
                                if (ai.getIdGroup()==savedAi.getIdGroup() && ai.getIdType()==savedAi.getIdType() && ai.getIdNumber().equals(savedAi.getIdNumber()))
                                {
                                    found = true;
                                }
                            }
                            Iterator<AccountIdentification> k = noGroupAccountIdentifications.iterator();
                            while (k.hasNext() && !found)
                            {
                                AccountIdentification savedAi = k.next();
                                if (ai.getIdGroup() == savedAi.getIdGroup() && ai.getIdType() == savedAi.getIdType()
                                        && ai.getIdNumber().equals(savedAi.getIdNumber()))
                                {
                                    found = true;
                                }
                            }
                            differs = !found;
                        }                        
                    }
                }
                else
                {
                    differs = true;
                }
            }
            catch (HomeException e)
            {
                differs = true;
            }
        }
        return differs;
    }
    
    /**
     * Creates a single list with all the account identifications.
     * @param account
     * @return
     */
    private void fillInAccountIdentificationLists(Account account, List<AccountIdentification> accountIdentifications, List<AccountIdentification> filledAccountIdentifications, List<AccountIdentification> noGroupAccountIdentifications)
    {
        List<AccountIdentificationGroup> accountIdentificationGroups = account.getIdentificationGroupList();
        if (accountIdentificationGroups!=null)
        {
            Iterator<AccountIdentificationGroup> i = accountIdentificationGroups.iterator();
            while (i.hasNext())
            {
                AccountIdentificationGroup aig = i.next();
                Iterator j = aig.getIdentificationList().iterator();
                while(j.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification) j.next();
                    accountIdentifications.add(ai);
                    
                    if (aig.getIdGroup()==-1)
                    {
                        noGroupAccountIdentifications.add(ai);
                    } 
                    else if (ai.getIdType()!=AccountIdentification.DEFAULT_IDTYPE || ai.getIdNumber()!=AccountIdentification.DEFAULT_IDNUMBER)
                    {
                        filledAccountIdentifications.add(ai);
                    }
                }
            }
        }
    }

    private void validAccountIdentificationFilled(final Context ctx, final ExceptionListener el, final Account account, List<AccountIdentification> ais, SpidIdentificationGroups spidIdGroups)
    {        
        if(null == ais || ais.isEmpty())
        {
             el.thrown(new IllegalPropertyArgumentException(AccountXInfo.IDENTIFICATION_GROUP_LIST, "Identification list cannot be empty."));
             return;
        }        
        boolean validIdTypeRequiredException = false;
        Map<Integer, Boolean> numberOfIdRequiredException = new HashMap<Integer, Boolean>();
        Iterator<AccountIdentification> i = ais.iterator();
        
        while (i.hasNext())
        {
            AccountIdentification ai = i.next();
            String groupName = String.valueOf(ai.getIdGroup());
            Integer key = Integer.valueOf(ai.getIdGroup());
            if (ai.getIdType() == AccountIdentification.DEFAULT_IDTYPE && ai.getIdNumber().equals(AccountIdentification.DEFAULT_IDNUMBER) && numberOfIdRequiredException.get(key)==null)
            {
             	 CRMSpid sp=null;
             	 try {
				
             		 sp = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, account.getSpid());
             	
             	 } catch (Exception e){
             		 
             		LogSupport.minor(ctx, ResponsibleAccountMoveProcessor.class.getName(), "Exception Occured while retrieving the SPID.");
             	 }
            	 if (sp != null){
            		 
            		 if(sp.getSkipSecurityAndIdentityCheck() && !account.isResponsible()) 
            			 return;
            	 }
            	
                IdentificationGroup group = retrieveIdentificationGroup(spidIdGroups, ai.getIdGroup());
                if (group!=null)
                {
                    groupName = group.getName();
                }
                numberOfIdRequiredException.put(key, Boolean.TRUE);
                int required = account.getIdentificationList(ai.getIdGroup()).size();
                el.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE,
                        "Total of " + required + " Ids in identification group '" + groupName + "' required."));
            }
            else if (!validIdTypeRequiredException && ai.getIdType() == AccountIdentification.DEFAULT_IDTYPE && !ai.getIdNumber().equals(AccountIdentification.DEFAULT_IDNUMBER))
            {
                validIdTypeRequiredException = true;
                el.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE,
                        "Valid Id Type required."));
            }
            else if (ai.getIdType() != AccountIdentification.DEFAULT_IDTYPE && spidIdGroups!=null && ai.getIdGroup()!=AccountIdentificationGroup.DEFAULT_IDGROUP)
            {
                IdentificationGroup group = retrieveIdentificationGroup(spidIdGroups, ai.getIdGroup());
                if (group==null)
                {
                    el.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE,
                            "Group '" + ai.getIdGroup() + "' not found for spid '" + spidIdGroups.getSpid() + "'."));
                }
                else if (!group.getIdentificationList().contains(String.valueOf(ai.getIdType())))
                {
                    Identification id = IdentificationSupport.getIdentification(ctx, ai.getIdType());
                    if (id!=null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE,
                        "Identification type '" + ai.getIdType() + " - " + id.getDesc() + "' is not a valid selection for group '" + group.getName() + "' anymore."));
                    }
                    else
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE,
                                "Identification id '" + ai.getIdType() + "' is not a valid selection for group '" + group.getName() + "' anymore."));
                    }
                }
            }
        }
    }
    
    private IdentificationGroup retrieveIdentificationGroup(SpidIdentificationGroups spidIdGroups, int idGroup)
    {
        IdentificationGroup result = null;
        Iterator<IdentificationGroup> iter = spidIdGroups.getGroups().iterator();
        while (iter.hasNext())
        {
            IdentificationGroup group = iter.next();
            if (group.getIdGroup()==idGroup)
            {
                result = group;
                break;
            }
        }
        return result;
    }
    
    private void validateNoGroupAccountIdentification(final Context ctx, final ExceptionListener el, List<AccountIdentification> noGroupAccountIdentifications, SpidIdentificationGroups spidIdGroups)
    {
        if (spidIdGroups!=null)
        {
            Iterator<AccountIdentification> i = noGroupAccountIdentifications.iterator();
            while (i.hasNext())
            {
                AccountIdentification ai = i.next();
                Identification id = IdentificationSupport.getIdentification(ctx, ai.getIdType());
                if (id!=null)
                {
                    el.thrown(new IllegalPropertyArgumentException(AccountIdentificationGroupXInfo.GROUP,
                            "Id type '" +  ai.getIdType() + " - " + id.getDesc() + "' does not fit in any of the configured SPID identification groups and should be removed."));
                }
                else
                {
                    el.thrown(new IllegalPropertyArgumentException(AccountIdentificationGroupXInfo.GROUP,
                            "Id type '" + ai.getIdType() + "' is not in any of the configured SPID identification groups and should be moved or removed."));
                }
            }
        }
    }
    
    private void validateNoIdFilledMoreThanOnce(final Context ctx, final CompoundIllegalStateException el, List<AccountIdentification> accountIdList)
    {
        if(accountIdList.size() > 1)
        {
            boolean error = false;
            for(int i = 0; i < accountIdList.size() - 1 && !error; i++)
            {
                AccountIdentification ai1 = accountIdList.get(i);
                for(int j = i + 1; j < accountIdList.size(); j++)
                {
                    AccountIdentification ai2 = accountIdList.get(j);
                    if(ai1.getIdType() == ai2.getIdType())
                    {
                        Identification id = IdentificationSupport.getIdentification(ctx, ai1.getIdType());
                        if (id!=null)
                        {
                            throwPropertyException(AccountIdentificationXInfo.ID_TYPE, "ID type '" + ai1.getIdType() + " - " + id.getDesc() + "' cannot be selected more than once.", null, el);
                        }
                        else
                        {
                            throwPropertyException(AccountIdentificationXInfo.ID_TYPE, "Cannot select ID type more than once.", null, el);
                        }
                        error = true;
                        break;
                    }
                }
            }
        }    
    }
    
    private void validateFilledIdNumbersFormat(final Context ctx, final CompoundIllegalStateException el, List<AccountIdentification> accountIdList)
    {
        Iterator i = accountIdList.iterator();
        while(i.hasNext())
        {
            AccountIdentification ai = (AccountIdentification)i.next();
			IdentificationSupport.validateIdNumber(ctx, ai.getIdType(),
			    ai.getIdNumber(), ai.getExpiryDate(),
			    AccountIdentificationXInfo.ID_TYPE,
			    AccountIdentificationXInfo.ID_NUMBER,
			    AccountIdentificationXInfo.EXPIRY_DATE, el);
        }
    }

    private static void throwPropertyException(final PropertyInfo idProperty, final String msg, final Exception cause, CompoundIllegalStateException el)
    {
        final IllegalPropertyArgumentException exception = new IllegalPropertyArgumentException(idProperty,
                msg);
        if (cause != null)
        {
            exception.initCause(cause);
        }
        el.thrown(exception);
    }
}