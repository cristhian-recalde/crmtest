package com.trilogy.app.crm.home;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ContactTypeEnum;
import com.trilogy.app.crm.bean.SpidLang;
import com.trilogy.app.crm.bean.SpidLangHome;
import com.trilogy.app.crm.bean.SpidLangXInfo;
import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXInfo;
import com.trilogy.app.crm.home.account.AccountPropertyListeners;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

public class CRMSpidPropertyListeners implements PropertyChangeListener
{


    public static Set<PropertyInfo> getLazyLoadedProperties()
    {
        HashSet<PropertyInfo> properties = new HashSet<PropertyInfo>();
        properties.add(CRMSpidXInfo.SUBSCRIBER_LANGUAGES);
        return properties;        
        
    }
    
    public void CRMSpidPropertyListeners()
    {
    }


    @Override
    public void propertyChange(final PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(CRMSpidXInfo.SUBSCRIBER_LANGUAGES.getName()))
        {
            propertiesChanged_[SUBSCRIBER_LANGUAGES_INDEX] = true;
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
    }


    /**
     * Saves the for lazyLoaded Properties.  Only records with updated fields will
     * be saved.
     */
    public void saveChangedInfo(Context ctx, CRMSpid spid) throws HomeException
    {
        final Home securityHome = (Home) ctx.get(SecurityQuestionAnswerHome.class);
        for (int i = 0; i < propertiesChanged_.length; i++)
        {
            if (propertiesChanged_[SUBSCRIBER_LANGUAGES_INDEX] && i == SUBSCRIBER_LANGUAGES_INDEX)
            {
            	performDeltaUpdateSpidSubscriberLanguages(ctx, spid);
            }
        }
    }


    /**
     * Checks for changes and sets the falgs that track which records need to be updated.
     */
    public void checkLazyLoadedPropertiesInfoChangedFromDefault(CRMSpid spid)
    {
        for (int y = 0; y < propertiesChanged_.length; y++)
        {
            if (spid.getSubscriberLanguagesLoaded())
            {
                propertiesChanged_[SUBSCRIBER_LANGUAGES_INDEX] = true;
            }
        }
    }



    public Object cloneLazyLoadMetaData(final CRMSpidPropertyListeners clone)
    {
        clone.propertiesChanged_ = new boolean[1];
        for (int i = 0; i < this.propertiesChanged_.length; i++)
        {
            clone.propertiesChanged_[i] = propertiesChanged_[i];
        }
        
        return clone;
    }




    public void performDeltaUpdateSpidSubscriberLanguages(Context ctx, CRMSpid spid)
    {
        final And condition = new And();
        condition.add(new EQ(SpidLangXInfo.SPID, Integer.valueOf(spid.getId())));
        try
        {
            Home langHome = (Home) ctx.get(SpidLangHome.class);
            SortedSet oldSelectedSet = new TreeSet();
            Collection lang = (Collection) langHome.where(ctx, new EQ(SpidLangXInfo.SPID, Integer.valueOf(spid.getSpid()))).forEach(ctx,
                    new MapVisitor(SpidLangXInfo.LANGUAGE));
            oldSelectedSet.addAll(lang);
            Set newSelectedSet = spid.getSubscriberLanguages();
            
            Iterator iter = newSelectedSet.iterator();
            while (iter.hasNext())
            {
                String language = (String) iter.next();
                if (!oldSelectedSet.contains(language))
                {
                    SpidLang spidLang = new SpidLang();
                    spidLang.setSpid(spid.getSpid());
                    spidLang.setLanguage(language);
                    
                    langHome.create(spidLang);
                    
                    oldSelectedSet.remove(language);
                }
            }
            
            iter = oldSelectedSet.iterator();
            while (iter.hasNext())
            {
                String language = (String) iter.next();
                if (!newSelectedSet.contains(language))
                {
                    SpidLang spidLang = (SpidLang) langHome.find(new EQ(SpidLangXInfo.LANGUAGE, language));
                    langHome.remove(spidLang);
                }
            }

        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to load and update Subscriber Languages.", e);
            // cannot continue with this contact
        }
    }

    

    private boolean[] propertiesChanged_ = new boolean[1];
    public static final int SUBSCRIBER_LANGUAGES_INDEX = 0;
    
}