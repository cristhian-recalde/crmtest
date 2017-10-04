package com.trilogy.app.crm.web.border;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.search.BundleProfileSearch;
import com.trilogy.app.crm.bean.search.BundleProfileSearchWebControl;
import com.trilogy.app.crm.bean.search.BundleProfileSearchXInfo;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.search.FindSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SearchFieldAgent;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.bean.ui.BundleProfile;

public class BundleProfileSearchBorder extends SearchBorder
{
    public BundleProfileSearchBorder(Context ctx)
    {
        super(ctx, BundleProfile.class, new BundleProfileSearchWebControl());
        
        // type
        SearchFieldAgent typeAgent;
        typeAgent = new SearchFieldAgent(BundleProfileXInfo.BUNDLE_ID, BundleProfileSearchXInfo.TYPE)
        {
            @Override
            public void execute(Context ctx) throws AgentException
            {
                CRMBundleCategory service = (CRMBundleCategory) ctx.get(CRMBundleCategory.class);
                Object value = getSearchCriteria(ctx);
                if (value != null && !"".equals(value.toString()) && !"-1".equals(value.toString()))
                {
                    Set set = new HashSet();
                    BundleTypeEnum bundleType = BundleTypeEnum.get(Short.parseShort(value.toString()));

                    Iterator it = UnitTypeEnum.COLLECTION.iterator();
                    while (it.hasNext())
                    {
                        UnitTypeEnum type = (UnitTypeEnum) it.next();
                        if (type.getBundleType().equals(bundleType))
                        {
                            set.add(type);
                            break;
                        }
                    }
                    Collection categories = new HashSet();
                    try
                    {
                        categories = service.getCategoriesByUnitTypeRange(ctx, set).selectAll();
                    }
                    catch (HomeException e)
                    {
                        new MajorLogMsg(this.getClass(), "Unable to select Bundle Categories", e);
                    }
                    catch (BundleManagerException e)
                    {
                        new MajorLogMsg(this.getClass(), "Unable to select Bundle Categories", e);
                    }
                    set.clear();
                    it = categories.iterator();
                    while (it.hasNext())
                    {
                        BundleCategory category = (BundleCategory) it.next();
                        set.add(Integer.valueOf(category.getCategoryId()));
                    }
                    CRMBundleProfile profileService = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
                    try
                    {
                        Or or = new Or();
                        or.add(new EQ(BundleProfileXInfo.TYPE, getSearchCriteria(ctx)));
                        Set<Long> bundleIDs = profileService.getBundleIdsByCategoryIds(ctx, set);
                        while (bundleIDs.size()>1000)
                        {
                        	Set<Long> first1000BundleIDs = new HashSet<Long>();
                        	Iterator<Long> iter = bundleIDs.iterator();
                        	while (iter.hasNext() && first1000BundleIDs.size()<1000)
                        	{
                        		Long bundleID = iter.next();
                        		first1000BundleIDs.add(bundleID);
                        		iter.remove();
                        	}
                        	or.add(new In(getPropertyInfo(), first1000BundleIDs));
                        }
                        
                        or.add(new In(getPropertyInfo(), bundleIDs));
                        
                        SearchBorder.doSelect(ctx, or);
                    }
                    catch (BundleManagerException e)
                    {
                        new MajorLogMsg(this.getClass(), "Unable to search bundle profiles: " + e.getMessage(), e);
                    }
                }
               delegate(ctx);
            }
        };
        addAgent(typeAgent);

        // SPID
        SelectSearchAgent spidAgent = new SelectSearchAgent(BundleProfileXInfo.SPID, BundleProfileSearchXInfo.SPID);
        addAgent(spidAgent.addIgnore(Integer.valueOf(9999)));
  
        
        // ID
        FindSearchAgent idAgent = new FindSearchAgent(com.redknee.app.crm.bundle.BundleProfileXInfo.BUNDLE_ID, BundleProfileSearchXInfo.ID);
        addAgent(idAgent.addIgnore(Integer.valueOf(-1)));
        
        //NAME
        addAgent(new WildcardSelectSearchAgent(BundleProfileXInfo.NAME, BundleProfileSearchXInfo.NAME, true));
        
    }
}
