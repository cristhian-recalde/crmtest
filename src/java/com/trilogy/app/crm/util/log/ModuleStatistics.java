package com.trilogy.app.crm.util.log;

import java.util.ArrayList;

/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */

/**
 * @author Margarita Alp
 * Created on Aug 27, 2004
 */
public class ModuleStatistics implements Comparable
{
	public ModuleStatistics(String id, int count, String line)
	{
		id_ = id;
		count_ = count;
		list_.add(line);
	}
	public int compareTo(Object obj)
	{
		if (obj instanceof ModuleStatistics)
		{
			ModuleStatistics stat = (ModuleStatistics) obj;
            return stat.count_ - this.count_;
		}
		return 0;
	}
	public void addLine(String line)
	{
		count_++;
		list_.add(line);
	}

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ModuleStatistics))
        {
            return false;
        }

        final ModuleStatistics that = (ModuleStatistics) o;

        if (count_ != that.count_)
        {
            return false;
        }
        if (id_ != null ? !id_.equals(that.id_) : that.id_ != null)
        {
            return false;
        }
        if (list_ != null ? !list_.equals(that.list_) : that.list_ != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id_ != null ? id_.hashCode() : 0);
        // ignore list and count because these may change
        return result;
    }

    String id_;
	int count_;
	ArrayList list_ = new ArrayList();
}
