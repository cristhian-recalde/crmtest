package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MapVisitor;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.SpidLang;
import com.trilogy.app.crm.bean.SpidLangHome;
import com.trilogy.app.crm.bean.SpidLangXInfo;


public class SpidLanguageAdapter implements Adapter
{

    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        CRMSpid spid = (CRMSpid) obj;
        
        Home langHome = (Home) ctx.get(SpidLangHome.class);
        
        final SortedSet selectedSet = new TreeSet();
        Collection lang = (Collection) langHome.where(ctx, new EQ(SpidLangXInfo.SPID, Integer.valueOf(spid.getSpid()))).forEach(ctx,
                new MapVisitor(SpidLangXInfo.LANGUAGE));
        selectedSet.addAll(lang);
        
        spid.setSubscriberLanguages(selectedSet);
        
        return spid;
    }


    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        CRMSpid spid = (CRMSpid) obj;
        Set newSelectedSet = spid.getSubscriberLanguages();

        Home langHome = (Home) ctx.get(SpidLangHome.class);
        
        SortedSet oldSelectedSet = new TreeSet();
        Collection lang = (Collection) langHome.where(ctx, new EQ(SpidLangXInfo.SPID, Integer.valueOf(spid.getSpid()))).forEach(ctx,
                new MapVisitor(SpidLangXInfo.LANGUAGE));
        oldSelectedSet.addAll(lang);
        
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
        return spid;
    }
}
