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
package com.trilogy.app.crm.unit_test.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xhome.xdb.Max;
import com.trilogy.framework.xhome.xdb.Min;

/**
 * Overwrite CMD method for decorated Homes.
 * Implement emulator for MAX/MIN using Home interface
 * 
 * @author ali
 *
 */
public class TransientHomeXDBCmdEmulator extends HomeProxy 
{
    
    public TransientHomeXDBCmdEmulator(Context context, Home delegate)
    {
        super(context, delegate);
    }
    
    /**
     * Returns the object requested in the command.  Command could be Max, Min.
     * Returns Null if there are no objects to return.
     */
    public Object cmd(Context ctx, Object command)
        throws HomeException
    {
        //Cheat.  This decorator assumes MAX/MIN will be the first xdb commands
        Collection collection = new ArrayList();
        PropertyInfo propertyInfo = null;
        if (command instanceof Max)
        {
            Max maxCmd = (Max)command;
            // Get all items sorted in descending 
            propertyInfo = maxCmd.getArg();
            Home sortedHome = applyFilter(ctx, getDelegate(), maxCmd.getFunction());
            sortedHome = new SortingHome(sortedHome, new DescendingPropertyComparator(propertyInfo));
            collection = sortedHome.selectAll(getContext());
        }
        else if (command instanceof Min)
        {
            Min minCmd = (Min)command;
            // Get all items sorted in descending 
            propertyInfo = minCmd.getArg();
            Home sortedHome = applyFilter(ctx, getDelegate(), minCmd.getFunction());
            sortedHome = new SortingHome(sortedHome, new AscendingPropertyComparator(propertyInfo));
            collection = sortedHome.selectAll();
        }
        
        // Return the first item in the collection
        Object returnObj = null;
        Iterator iter = collection.iterator();
        if (iter.hasNext())
        {
            returnObj = iter.next();
        }
        if (returnObj != null)
        {
            return formatXDBReturnValue(propertyInfo.get(returnObj));
        }
        return null;
    }
    
    /**
     * Returned filtered home.  If no filter is provided, return given Home.
     * @param delegate
     * @param function
     * @return
     */
    private Home applyFilter(Context context, Home delegate, Object function)
    {
        if (function instanceof Predicate)
        {
            WhereHome decoratedHome = new WhereHome(context, delegate);
            decoratedHome.setWhere(function);
            return decoratedHome;
        }
        return delegate;
    }
    
    /**
     * Format return value.
     * For example, Dates are saved in to the XDB as Numbers, Boolean are saved as characters (Y/N), etc.
     * TODO: implement other formats besides Date.
     * @param obj
     * @return
     */
    private Object formatXDBReturnValue(Object obj)
    {
        if (obj instanceof Date)
        {
            /* See InvoiceSupport.getPreviousBillingDateForSubscriber() and 
             * InvoiceSupport.getPreviousBillingDateForAccount() methods for uses of this for querying Max(Date) */
            return BigDecimal.valueOf(((Date)obj).getTime());
        }
        return obj;
    }
    
    private class DescendingPropertyComparator implements Comparator, Serializable
    {
        PropertyInfo propertyInfo_ = null;
        
        
        DescendingPropertyComparator(PropertyInfo property)
        {
            propertyInfo_ = property;
        }
        
        public int compare(Object obj1, Object obj2) 
        {
            // Extract fields being compared
            Object property1 = propertyInfo_.get(obj1);
            Object property2 = propertyInfo_.get(obj2);
            if (property1 instanceof Comparable)  // then by extension so property2 is Comparable
            {
                // Sort Descending
                int r = ((Comparable) property2).compareTo(property1);

                // if == then make sure they're different beans by comparing the keys below
                // this prevents different beans with the same key values from being filtered out

                if ( r != 0 ) return r;
            }

            return ((Comparable) XBeans.getIdentifier(obj2)).compareTo(XBeans.getIdentifier(obj1));
        }
    }
    
    private class AscendingPropertyComparator implements Comparator, Serializable
    {
        PropertyInfo propertyInfo_ = null;
        
        AscendingPropertyComparator(PropertyInfo property)
        {
            propertyInfo_ = property;
        }
        
        public int compare(Object obj1, Object obj2) 
        {
            // Extract fields being compared
            Object property1 = propertyInfo_.get(obj1);
            Object property2 = propertyInfo_.get(obj2);
            if (property1 instanceof Comparable)  // then by extension so property2 is Comparable
            {
                // Sort Ascending
                int r = ((Comparable) property1).compareTo(property2);

                // if == then make sure they're different beans by comparing the keys below
                // this prevents different beans with the same key values from being filtered out

                if ( r != 0 ) return r;
            }

            return ((Comparable) XBeans.getIdentifier(obj1)).compareTo(XBeans.getIdentifier(obj2));
        }
    }
}
