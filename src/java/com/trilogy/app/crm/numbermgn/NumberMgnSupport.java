/*
 * Created on 2005-1-10
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.numbermgn;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.framework.xhome.xdb.AbstractXDBHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author jchen
 *
 * This class provides supporting common functions for Number management, 
 * bulking loading, etc.
 * 
 * Since in CRM some, JDBC home does not support SelectAll, so we have to use raw JDBC access 
 * to reterive data
 */
public class NumberMgnSupport 
{

    /**
     * Gets the first available msisdn from the msisdn group within the spid 
     * @param ctx
     * @param spid
     * @param msisdnGroup
     * @return
     * @throws HomeException
     */
    public static Msisdn getFirstAvailMsisdn(Context ctx, int spid, int msisdnGroup, short subType) throws HomeException
    {
        Home home = getAvailMsisdnHome(ctx, spid, msisdnGroup, subType, null, 1, false);
        return (Msisdn)home.find(ctx, True.instance());
    }
    
    public static Msisdn getLastAvailMsisdn(Context ctx, int spid, int msisdnGroup, short subType) throws HomeException
    {
        Home home = getAvailMsisdnHome(ctx, spid, msisdnGroup, subType, null, 1, true);
        return (Msisdn)home.find(ctx, True.instance());
    }
    
    public static long getAvailMsisdnCnt(Context ctx, int spid, int msisdnGroup, short subType, 
    		String startingMsisdn) 
    	throws HomeException
    {
        long size = 0;
        Home home = getAvailMsisdnHome(ctx, spid, msisdnGroup, subType, startingMsisdn, -1, false);

        size = ((CountingVisitor)home.forEach(ctx, new CountingVisitor())).getCount();
        return size;
    }
    
    /**
     * Returns a number of available msisdns
     * 
     * @param ctx
     * @param spid
     * @param msisdnGroup
     * @param subType
     * @param startingMsisdn
     * @param recordCnt
     * @param descOrderMsisdn
     * @return
     * @throws HomeException
     */
    public static Collection getAvailMsisdns(Context ctx, int spid, int msisdnGroup, short subType, 
    	String startingMsisdn, int recordCnt, boolean descOrderMsisdn) 
    		throws HomeException
    {
	    And and = new And(); 

	    if (startingMsisdn != null && startingMsisdn.length() > 0)
	    {
	        and.add(new GTE(MsisdnXInfo.MSISDN, startingMsisdn)); 
	    }
	    and.add(new EQ(MsisdnXInfo.SPID, spid));
	    and.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, Short.valueOf(subType)));
	    and.add(new EQ(MsisdnXInfo.GROUP, Integer.valueOf(msisdnGroup)));
	    and.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));
	    and.add(new Limit(100)); 
	    and.add(new OrderBy(MsisdnXInfo.MSISDN, !descOrderMsisdn)); 
 
	    return HomeSupportHelper.get(ctx).getBeans(ctx, Msisdn.class, and);
	    
    }

    public static Home getAvailMsisdnHome(
          Context ctx, 
          int spid, 
          int msisdnGroup, 
          short subType,
          String startingMsisdn, 
          int recordCnt, 
          boolean descOrderMsisdn)
       throws HomeException
    {
       Home home = (Home)ctx.get(MsisdnHome.class);
       home = home.where(ctx, 
             new And()
               .add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(spid)))
               .add(new EQ(MsisdnXInfo.GROUP, Integer.valueOf(msisdnGroup)))
               .add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.get(subType)))
               .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE)));
       /*if (startingMsisdn != null && startingMsisdn.length() > 0)
       {
          home = home.where(ctx,
                new GTE(MsisdnXInfo.MSISDN, startingMsisdn));
       }*/
       // TODO orderBy
	    //String orderBy = " ORDER BY msisdn " + (descOrderMsisdn ? "desc" : "asc"); 
       SortingHome sortingHome = new SortingHome(home, new SortOnMsisdnComparator(descOrderMsisdn));
       sortingHome.setSortOnForEach(true);
       home = sortingHome;

       if (recordCnt > 0)
       {
          // TODO limit number of rows return
       }
       return home;
    }
    
    public static boolean isInUsed(Context ctx, int msisdnGroup) throws HomeException
    {
	    Home home = (Home) ctx.get(MsisdnHome.class);
	    
       return (home.find(ctx, new EQ(MsisdnXInfo.GROUP, Integer.valueOf(msisdnGroup))) != null);
    }    
    
    /**
     * Get MSISDN table name from jdbc home, instead of hardcoding
     * @param ctx
     * @return
     */  
    public static String getMsisdnTable(Context ctx)  
    {
        Home home = (Home)ctx.get(MsisdnHome.class);
        try
		{
			return (String) home.cmd(ctx,AbstractXDBHome.TABLE_NAME);
		}
		catch (HomeException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(NumberMgnSupport.class.getName(),e.getMessage(),e).log(ctx);
			}
		}
		
		return null;
    }


    /**
     * Gets a collection of available card packages.
     *
     * @param context The operating context.
     * @param technology The technology type of the cards.
     * @param spid The service provider for which to get cards.
     * @param packageGroupName The name of the group to which the cards belong.
     * @param recordCount The limit on the maximum number of cards to get.
     *
     * @exception HomeException Thrown if there is a problem accessing Home data
     * in the context.
     */
    public static Collection getAvailPackages(
        final Context context,
        final TechnologyEnum technology,
        final int spid,
        final String packageGroupName,
        final int recordCount)
        throws HomeException
    {
        // This method was added to abstract out the technology type so we could
        // hide the separate implementations of getAvailGSMPackages() and
        // getAvailTDMAPackages() below.

        final Collection packages;
        
        if (technology == TechnologyEnum.GSM)
        {
            packages = getAvailGSMPackages(context, spid, packageGroupName, recordCount);
        }
        else if (technology == TechnologyEnum.TDMA || technology == TechnologyEnum.CDMA )
        {
            packages = getAvailTDMAPackages(context, spid, packageGroupName, recordCount);
        }
        else
        {
            throw new IllegalStateException(
                "The given technology type: " + technology + " is not supported.");
        }

        return packages;
    }

    /**
     * Gets a count of the available card packages.
     *
     * @param context The operating context.
     * @param technology The technology type of the cards.
     * @param spid The service provider for which to get cards.
     * @param packageGroupName The name of the group to which the cards belong.
     *
     * @exception HomeException Thrown if there is a problem accessing Home data
     * in the context.
     */
    public static long getAvailPackageCount(
        final Context context,
        final TechnologyEnum technology,
        final int spid,
        final String packageGroupName)
        throws HomeException
    {
        // This method was added to abstract out the technology type so we could
        // hide the separate implementations of getAvailGSMPackages() and
        // getAvailTDMAPackages() below.


    	final long count;
        
    	if (technology != null)
    	{
	        switch (technology.getIndex())
	        {
	        	case TechnologyEnum.GSM_INDEX:
	        		count = getAvailGSMPackageCount(context, spid, packageGroupName);
	        		break;
	        	case TechnologyEnum.TDMA_INDEX:
	        	case TechnologyEnum.CDMA_INDEX:
	        		count = getAvailTDMAPackageCount(context, spid, packageGroupName);
	        		break;
	        	case TechnologyEnum.ANY_INDEX:
	        		count = getAvailGSMPackageCount(context, spid, packageGroupName) +
	        		getAvailTDMAPackageCount(context, spid, packageGroupName);
	        		break;
	        	default:
	        		count=0;
	        }
    	}
        else
        {
            throw new IllegalArgumentException("The technology parameter cannot be null.");
        }

        return count;
    }
    
    public static boolean checkNumberMgmtHistory(Context ctx,String subscriberId ,String packageId) throws HomeException
    {
    	final And filter = new And();       
        
        filter.add(new EQ(ImsiMgmtHistoryXInfo.TERMINAL_ID, packageId));
        filter.add(new EQ(ImsiMgmtHistoryXInfo.SUBSCRIBER_ID, subscriberId));
        filter.add(new EQ(ImsiMgmtHistoryXInfo.LATEST, true));
        
        Home imsiHistoryHome = (Home) ctx.get(ImsiMgmtHistoryHome.class);
        return (imsiHistoryHome.find(ctx, filter) != null);
    }

    private static Home getAvailGSMPackageHome(Context ctx, int spid, String packageGroupName, int recordCount)
		throws HomeException
	{
		Home home = (Home) ctx.get(GSMPackageHome.class);
        final And and = new And();
        and.add(new EQ(GSMPackageXInfo.SPID, Integer.valueOf(spid)));
		home = home.where(ctx,new EQ(GSMPackageXInfo.SPID, Integer.valueOf(spid)));

		if (packageGroupName != null && packageGroupName.length() > 0)
		{
			and.add(new EQ(GSMPackageXInfo.PACKAGE_GROUP,packageGroupName));
		}
		and.add(new EQ(GSMPackageXInfo.STATE,PackageStateEnum.AVAILABLE));

        if (recordCount >0)
        {
            and.add(new Limit(recordCount));
        }

        return home.where(ctx, and);
	}

    private static long getAvailGSMPackageCount(Context ctx, int spid, String packageGroupName)
        throws HomeException
    {
        Home home = getAvailGSMPackageHome(ctx, spid, packageGroupName, -1);
        CountingVisitor cv  = ((CountingVisitor)home.forEach(ctx, new CountingVisitor()));
        return cv.getCount();
    }

    private static Collection getAvailGSMPackages(Context ctx, int spid, String packageGroupName, int recordCount)
        throws HomeException
    {
        return getAvailGSMPackageHome(ctx, spid, packageGroupName, recordCount).selectAll();
    }
    
    private static Home getAvailTDMAPackageHome(Context ctx, int spid, String packageGroupName, int recordCount)
        throws HomeException
    {
        Home home = (Home) ctx.get(TDMAPackageHome.class);
        final And and = new And();
        and.add(new EQ(TDMAPackageXInfo.SPID, Integer.valueOf(spid)));

        /*
         * TT 6112241835
         * Skip the package group constraint if the group name is null or empty, which prevents NullPointerException on 
         * the first load of the Subscriber Bulk Create form with only TDMA technology enabled. In this case, the 
         * resulting home effectively contains packages in *ANY* package group which satisfy the other constraints.
         * This behaviour is consistent with getAvailGSMPackageHome().  
         */
        if (packageGroupName != null && packageGroupName.length() > 0)
        {
            and.add(new EQ(TDMAPackageXInfo.PACKAGE_GROUP,packageGroupName));
        }
        and.add(new EQ(TDMAPackageXInfo.STATE,PackageStateEnum.AVAILABLE));
        
        if (recordCount > 0)
        {
            and.add(new Limit(recordCount));
        }

        return home.where(ctx, and);
    }
    
    private static long getAvailTDMAPackageCount(Context ctx, int spid, String packageGroupName)
        throws HomeException
    {
        Home home = getAvailTDMAPackageHome(ctx, spid, packageGroupName, -1);
        return ((CountingVisitor)home.forEach(ctx, new CountingVisitor())).getCount();
    }

    private static Collection getAvailTDMAPackages(Context ctx, int spid, String packageGroupName, int recordCount)
        throws HomeException
    {
        return getAvailTDMAPackageHome(ctx, spid, packageGroupName, recordCount).selectAll();
    }
}
