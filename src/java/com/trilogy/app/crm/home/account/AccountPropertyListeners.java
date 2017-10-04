package com.trilogy.app.crm.home.account;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ContactTypeEnum;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.app.crm.bean.ProvinceHome;
import com.trilogy.app.crm.bean.ProvinceXInfo;
import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.SupplementaryDataHome;
import com.trilogy.app.crm.bean.SupplementaryDataXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This listener will listen to properties in account which are lazy loaded will update them accordingly
 * @author ksivasubramaniam
 *
 */
public class AccountPropertyListeners implements PropertyChangeListener
{

    private static Map<String, Integer> contactPropertiesMap_ = new HashMap<String, Integer>();
    
    private static PropertyInfo[][] contactProperties_ =
        {
            {
                // AccountXInfo.FIRST_NAME,
                // AccountXInfo.LAST_NAME,
                AccountXInfo.INITIALS,
                // AccountXInfo.BILLING_ADDRESS1,
                AccountXInfo.BILLING_ADDRESS2, AccountXInfo.BILLING_ADDRESS3,
                AccountXInfo.BILLING_CITY, AccountXInfo.BILLING_PROVINCE,
                AccountXInfo.BILLING_COUNTRY, AccountXInfo.CONTACT_TEL,
                AccountXInfo.CONTACT_FAX, AccountXInfo.EMAIL_ID, AccountXInfo.SECONDARY_EMAIL_ADDRESSES,AccountXInfo.CSA,
                AccountXInfo.EMPLOYER, AccountXInfo.EMPLOYER_ADDRESS,
                AccountXInfo.DATE_OF_BIRTH, AccountXInfo.OCCUPATION,
	        AccountXInfo.BILLING_POSTAL_CODE
            },
                {
                // AccountXInfo.COMPANY_NAME,
                AccountXInfo.TRADING_NAME, AccountXInfo.REGISTRATION_NUMBER,
                AccountXInfo.COMPANY_TEL, AccountXInfo.COMPANY_FAX,
                AccountXInfo.COMPANY_ADDRESS1, AccountXInfo.COMPANY_ADDRESS2,
                AccountXInfo.COMPANY_ADDRESS3, AccountXInfo.COMPANY_CITY,
                AccountXInfo.COMPANY_PROVINCE, AccountXInfo.COMPANY_COUNTRY,
	        AccountXInfo.COMPANY_POSTAL_CODE
            },
            {
                AccountXInfo.BANK_NAME, AccountXInfo.BANK_PHONE,
                AccountXInfo.BANK_ADDRESS1, AccountXInfo.BANK_ADDRESS2,
                AccountXInfo.BANK_ACCOUNT_NUMBER, AccountXInfo.BANK_ACCOUNT_NAME,
            },
        };
    
    
    public static Set<PropertyInfo> getLazyLoadedProperties()
    {
        HashSet<PropertyInfo> properties = new HashSet<PropertyInfo>();
        if (contactProperties_ != null)
        {
            for (int i = 0; i < contactProperties_.length; i++)
            {
                for (int j = 0; j < contactProperties_[i].length; j++)
                {
                    properties.add(contactProperties_[i][j]);
                }
            }
        }
        properties.add(AccountXInfo.SECURITY_QUESTIONS_AND_ANSWERS);
        properties.add(AccountXInfo.IDENTIFICATION_GROUP_LIST);
        properties.add(AccountXInfo.SUPPLEMENTARY_DATA_LIST);
        return properties;        
        
    }
    
    static
    {
        for (int y = 0; y < contactProperties_.length; y++)
        {
            final Integer id = Integer.valueOf(y + 1);
            for (int x = 0; x < contactProperties_[y].length; x++)
            {
                final String name = contactProperties_[y][x].getName();
                contactPropertiesMap_.put(name, id);
            }
        }
    }
    
    public void AccountPropertyListeners()
    {
    }


    @Override
    public void propertyChange(final PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(AccountXInfo.SECURITY_QUESTIONS_AND_ANSWERS.getName()))
        {
            propertiesChanged_[SECURITY_QUESTION_INDEX] = true;
        }
        else if (evt.getPropertyName().equals(AccountXInfo.IDENTIFICATION_GROUP_LIST.getName()))
        {
            propertiesChanged_[ACCOUNT_IDENTIFICATION_INDEX] = true;
        }
        else if (evt.getPropertyName().equals(AccountXInfo.SUPPLEMENTARY_DATA_LIST.getName()))
        {
            propertiesChanged_[SUPPLEMENTARY_DATA_INDEX] = true;
        }
        else
        {
            final Integer type =
                contactPropertiesMap_.get(evt.getPropertyName());
            if (type == null)
            {
                return;
            }

            contactChanged_[type.intValue()] = true;
        }
    }


    /**
     * Clears the flags that track with records need to be updated.
     */
    public void clearPropertyInfoChange()
    {
        for (int i = 0; i < propertiesChanged_.length; i++)
        {
            propertiesChanged_[i] = false;
        }
        for (int i = 0; i < contactChanged_.length; i++)
        {
            contactChanged_[i] = false;
        }
    }


    /**
     * Saves the for lazyLoaded Properties.  Only records with updated fields will
     * be saved.
     */
    public void saveChangedInfo(Context ctx, Account account) throws HomeException
    {
        final Home securityHome = (Home) ctx.get(SecurityQuestionAnswerHome.class);
        for (int i = 0; i < propertiesChanged_.length; i++)
        {
            if (propertiesChanged_[SECURITY_QUESTION_INDEX] && i == SECURITY_QUESTION_INDEX)
            {
                performDeltaUpdateSecurityQuestionAndAnswer(ctx, account);
            }
            else if (propertiesChanged_[ACCOUNT_IDENTIFICATION_INDEX] && i == ACCOUNT_IDENTIFICATION_INDEX)
            {
                performDeltaUpdateAccountIdentification(ctx, account);
            }
            else if (propertiesChanged_[SUPPLEMENTARY_DATA_INDEX] && i == SUPPLEMENTARY_DATA_INDEX)
            {
                performDeltaUpdateSupplementaryData(ctx, account);
            }
        }
        saveChangedContactInfo(ctx, account);
    }


    /**
     * Checks for changes and sets the falgs that track which records need to be updated.
     */
    public void checkLazyLoadedPropertiesInfoChangedFromDefault(Account account)
    {
        if (account.getAccountIdentificationLoaded())
        {
            List identList = account.getIdentificationList();
            if (identList.size() > 0)
            {
                propertiesChanged_[ACCOUNT_IDENTIFICATION_INDEX] = true;
            }
        }

        if (account.getSecurityQuestionAndAnswerLoaded())
        {
            List securityQuestionsList = account.getSecurityQuestionsAndAnswers();
            
            if (securityQuestionsList.size() > 0)
            {
                propertiesChanged_[SECURITY_QUESTION_INDEX] = true;
            }
        }
        
        if (account.getSupplementaryDataLoaded())
        {
            List supplementaryData = account.getSupplementaryDataList();
            if (supplementaryData.size() > 0)
            {
                propertiesChanged_[SUPPLEMENTARY_DATA_INDEX] = true;
            }
        }
        checkContactInfoChangedFromDefault(account);
    }

    /**
     * Checks for changes and sets the falgs that track which records need to be
     * updated.
     */
    private void checkContactInfoChangedFromDefault(final Account account)
    {
        for (int y = 0; y < contactProperties_.length; y++)
        {
            contactChanged_[y + 1] = false;
            for (int x = 0; x < contactProperties_[y].length; x++)
            {
                final PropertyInfo prop = contactProperties_[y][x];
                if (!SafetyUtil.safeEquals(prop.getDefault(), (prop.get(account))))
                {
                    contactChanged_[y + 1] = true;
                    break;
                }
            }
        }
    }

    public Object cloneLazyLoadMetaData(final AccountPropertyListeners clone)
    {
        clone.contactChanged_ = new boolean[this.contactChanged_.length];
        for (int i = 0; i < this.contactChanged_.length; i++)
        {
            clone.contactChanged_[i] = contactChanged_[i];
        }
        clone.propertiesChanged_ = new boolean[3];
        for (int i = 0; i < this.propertiesChanged_.length; i++)
        {
            clone.propertiesChanged_[i] = propertiesChanged_[i];
        }
        
        return clone;
    }


    /**
     * Saves the contact information to ContactHome. Only records with updated
     * fields will be saved.
     */
    public void saveChangedContactInfo(final Context ctx, final Account account) throws HomeException
    {
        final Home contactHome = (Home) ctx.get(ContactHome.class);
        for (int i = 1; i < contactChanged_.length; i++)
        {
            if (contactChanged_[i])
            {
                final And condition = new And();
                condition.add(new EQ(ContactXInfo.ACCOUNT, account.getBAN()));
                condition.add(new EQ(ContactXInfo.TYPE, Integer.valueOf(i)));

                Contact newContact = null;
                try
                {
                    newContact = (Contact) contactHome.find(ctx, condition);
                }
                catch (HomeException e)
                {
                    LogSupport
                        .minor(
                            ctx,
                            this,
                            "Unable to load Contact info. Contact info NOT updated.",
                            e);
                    // cannot continue with this contact
                    continue;
                }

                boolean create = false;
                if (newContact == null)
                {
                    create = true;
                    newContact = new Contact();
                    newContact.setAccount(account.getBAN());
                    newContact.setType(i);
                }
                try
                {
                    switch (i)
                    {
                        case ContactTypeEnum.PERSON_INDEX:
                            fillinPersonFields(ctx, newContact, account);
                            break;
                        case ContactTypeEnum.COMPANY_INDEX:
                            fillinCompanyFields(ctx, newContact, account);
                            break;
                        case ContactTypeEnum.BANK_INDEX:
                            fillinBankFields(newContact, account);
                            break;
                        default:
                            LogSupport.debug(ctx, this,
                                "Unsupported Contact Type: " + i);
                            continue;
                    }

                }
                catch (IllegalArgumentException e)
                {
                    // throwing this error will get the error message displayed
                    // correctly on the screen.
                    throw new HomeException(e.getMessage(),
                        new CompoundIllegalStateException(e));
                }

                try
                {
                    if (create)
                    {
                        contactHome.create(ctx, newContact);
                    }
                    else
                    {
                        contactHome.store(ctx, newContact);
                    }

                    contactChanged_[i] = false;
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this,
                        "Unable to update Contact info", e);
                }
            }
        }
    }
    
    public void fillinPersonFields(Context ctx, final Contact contact, final Account account)
    {
        contact.setInitials(account.getInitials());
        // this field is still in Account table
        // contact.setAddressLineOne(this.billingAddress1_);
        contact.setAddressLineTwo(account.getBillingAddress2());
        contact.setAddressLineThree(account.getBillingAddress3());
        contact.setCity(account.getBillingCity());
        Province province = null;
        try
        {
        	
        	String billingProvince = account.getBillingProvince();
        	if(billingProvince != null)
        	{
        		province = HomeSupportHelper.get(ctx).findBean(ctx,Province.class, billingProvince);
        	}
        }
        catch (final Exception exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append(getClass().getSimpleName());
                sb.append(".fillinPersonFields(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(ctx, this, sb.toString(), exception);
            }
        }
        
        if(province != null)
        {
        	contact.setProvince(province.getDisplayName());
        }
        
		contact.setPostalCode(account.getBillingPostalCode());
        contact.setCountry(account.getBillingCountry());
        contact.setPhone(account.getContactTel());
        contact.setFax(account.getContactFax());
        contact.setEmail(account.getEmailID());
        contact.setEmployer(account.getEmployer());
        contact.setEmployerAddress(account.getEmployerAddress());
        contact.setDateOfBirth(account.getDateOfBirth());
        contact.setOccupation(account.getOccupation());
        contact.setSecondaryEmailAddresses(account.getSecondaryEmailAddresses());
        contact.setCsa(account.getCsa());
    }
    
    public void fillinBankFields(final Contact contact, final Account account)
    {
        contact.setBankName(account.getBankName());
        contact.setPhone(account.getBankPhone());
        contact.setAddressLineOne(account.getBankAddress1());
        contact.setAddressLineTwo(account.getBankAddress2());
        contact.setBankAccountNumber(account.getBankAccountNumber());
        contact.setBankAccountName(account.getBankAccountName());
        contact.setOccupation(account.getOccupation());
    }
    
    public void fillinCompanyFields(Context ctx, final Contact contact, final Account account)
    {
        // this field is still in Account table
        // contact.setCompanyName(this.companyName_);
        contact.setTradingName(account.getTradingName());
        contact.setRegistrationNumber(account.getRegistrationNumber());
        contact.setPhone(account.getCompanyTel());
        contact.setFax(account.getCompanyFax());
        contact.setAddressLineOne(account.getCompanyAddress1());
        contact.setAddressLineTwo(account.getCompanyAddress2());
        contact.setAddressLineThree(account.getCompanyAddress3());
        contact.setCity(account.getCompanyCity());
        
        Province province = null;
        try
        {
        	
        	String companyProvince = account.getCompanyProvince();
        	if(companyProvince != null)
        	{
        		province = HomeSupportHelper.get(ctx).findBean(ctx,Province.class, companyProvince);
        	}
        }
        catch (final Exception exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append(getClass().getSimpleName());
                sb.append(".fillinCompanyFields(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(ctx, this, sb.toString(), exception);
            }
        }
        
        if(province != null)
        {
        	contact.setProvince(province.getDisplayName());
        }
        
        contact.setCountry(account.getCompanyCountry());
		contact.setPostalCode(account.getCompanyPostalCode());
		contact.setOccupation(account.getOccupation());
    }
    
    public void performDeltaUpdateSecurityQuestionAndAnswer(Context parentCtx, Account account)
    {
    	Context ctx = parentCtx.createSubContext();
    	ctx.put(Account.class, account);
        final And condition = new And();
        condition.add(new EQ(SecurityQuestionAnswerXInfo.BAN, account.getBAN()));
        try
        {
            Home securityHome = (Home) ctx.get(SecurityQuestionAnswerHome.class);
            Collection<SecurityQuestionAnswer> listQuestions = securityHome.select(ctx, condition);
            Collection<SecurityQuestionAnswer> changedQuestions = account.getSecurityQuestionsAndAnswers();
            Map<String, String> oldSecurityQuestoinMap = new HashMap<String, String>();
            Map<String, String> newSecurityQuestoinMap = new HashMap<String, String>();
            Map<String, Long> oldSecurityQuestoinIdMap = new HashMap<String, Long>();
            long maxIndex = 0;
            for (SecurityQuestionAnswer question : listQuestions)
            {
                oldSecurityQuestoinMap.put(question.getQuestion(), question.getAnswer());
                oldSecurityQuestoinIdMap.put(question.getQuestion(), question.getId());
                if (maxIndex < question.getId())
                {
                    maxIndex = question.getId();
                }
            }
            for (SecurityQuestionAnswer question : changedQuestions)
            {
                newSecurityQuestoinMap.put(question.getQuestion(), question.getAnswer());
            }
            for (SecurityQuestionAnswer question : listQuestions)
            {
                String questionanswer = newSecurityQuestoinMap.get(question.getQuestion());
                if (questionanswer == null)
                {
                    securityHome.remove(ctx, question);
                }
            }
            for (SecurityQuestionAnswer question : changedQuestions)
            {
                String questionanswer = oldSecurityQuestoinMap.get(question.getQuestion());
                if (questionanswer == null)
                {
                    question.setBAN(account.getBAN());
                    maxIndex++;
                    question.setId(maxIndex);
                    securityHome.create(ctx, question);
                }
                else if (!questionanswer.equals(question.getAnswer()))
                {
                    Long id = oldSecurityQuestoinIdMap.get(question.getQuestion());
                    if(id != null)
                    {
                        question.setId(id);
                    }
                    securityHome.store(ctx, question);
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(parentCtx, this, "Unable to load and update SecurityQuestionAnswers.", e);
            // cannot continue with this contact
        }
    }

    
    public void performDeltaUpdateAccountIdentification(Context parentCtx, Account account)
    {
        final And condition = new And();
        condition.add(new EQ(AccountIdentificationXInfo.BAN, account.getBAN()));
        try
        {
        	Context ctx = parentCtx.createSubContext();
        	ctx.put(Account.class, account);
            Home identHome = (Home) ctx.get(AccountIdentificationHome.class);
            Collection<AccountIdentification> oldIdentifications = identHome.select(ctx, condition);
            List<AccountIdentification> changedIdentifications = account.getIdentificationList();
            Map<Long, AccountIdentification> oldAccountIdentificationMap = new HashMap<Long, AccountIdentification>();
            Map<Long, AccountIdentification> newAccountIdentificationMap = new HashMap<Long, AccountIdentification>();
            int count = 0;
            
            for (AccountIdentification identification : oldIdentifications)
            {
                oldAccountIdentificationMap.put(Long.valueOf(identification.getId()), identification);
            }
            
            for (AccountIdentification identification : changedIdentifications)
            {
                identification.setId(count++);
                newAccountIdentificationMap.put(Long.valueOf(identification.getId()), identification);
            }
            
            for (AccountIdentification identification : oldIdentifications)
            {
                AccountIdentification oldIdentification = newAccountIdentificationMap.get(Long.valueOf(identification
                        .getId()));
                if (oldIdentification == null)
                {
                    identHome.remove(ctx, identification);
                }
            }
            for (AccountIdentification newIdentification : changedIdentifications)
            {
                newIdentification.setBAN(account.getBAN());
                AccountIdentification oldIdentification = oldAccountIdentificationMap.get(newIdentification.getId());
                if (oldIdentification == null)
                {
                    identHome.create(ctx, newIdentification);
                }
                else if ((newIdentification.getIdType() != AccountIdentification.DEFAULT_IDTYPE)
                        && !oldIdentification.equals(newIdentification))
                {
                    identHome.store(ctx, newIdentification);
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(parentCtx, this, "Unable to load and update AccountIdentification.", e);
            // cannot continue with this contact
        }
    }

    public void performDeltaUpdateSupplementaryData(Context ctx, Account account)
    {
        try
        {
            List<SupplementaryData> changeSupplementaryData = account.getSupplementaryDataList();

            for (SupplementaryData newSupplementaryData : changeSupplementaryData)
            {
                newSupplementaryData.setIdentifier(account.getBAN());
                newSupplementaryData.setEntity(SupplementaryDataEntityEnum.ACCOUNT_INDEX);
                SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, newSupplementaryData);
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to load and update Account supplementary data.", e);
            // cannot continue with this contact
        }
    }

    private boolean[] contactChanged_ =
        new boolean[ContactTypeEnum.BANK_INDEX + 1];
    
    private boolean[] propertiesChanged_ = new boolean[3];
    public static final int SECURITY_QUESTION_INDEX = 0;
    public static final int ACCOUNT_IDENTIFICATION_INDEX = 1;
    public static final int SUPPLEMENTARY_DATA_INDEX= 2;
    public static final int CONTACT_PERSON = 3;
    public static final int CONTACT_COMPANY= 4;
    public static final int CONTACT_BANK= 5;
    
}