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
 *
 */
package com.trilogy.app.crm.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class ListComparator<T> implements Comparator<List>
{
	/**
	 * Constructor for ListComparator. This version assumes the list elements
	 * are comparable and uses the internal order as provided by the elements.
	 */
	public ListComparator()
	{
		comp_ = null;
	}

	/**
	 * Constructor for ListComparator.
	 * 
	 * @param elementComparator
	 *            Comparator for the elements.
	 */
	public ListComparator(Comparator<T> elementComparator)
	{
		comp_ = elementComparator;
	}

	/**
	 * Compares elements of two lists. In actual usage, the elements of the
	 * lists probably should be sorted in the same order of the list comparison.
	 * 
	 * @param list1
	 *            First list.
	 * @param list2
	 *            Second list.
	 * @return Compared order of the two lists.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(List list1, List list2)
	{
		if (list1 == null && list2 == null)
		{
			return 0;
		}
		else if (list1 == null)
		{
			return -1;
		}
		else if (list2 == null)
		{
			return 1;
		}

		Iterator i1 = list1.iterator();
		Iterator i2 = list2.iterator();
		int result = 0;

		while (i1.hasNext() && i2.hasNext())
		{
			T e1 = (T) i1.next();
			T e2 = (T) i2.next();
			result = 0;
			if (comp_ == null)
			{
				if (!(e1 instanceof Comparable) || !(e2 instanceof Comparable))
				{
					throw new IllegalArgumentException(
					    "element is not comparable");
				}
				result = ((Comparable) e1).compareTo(e2);
			}
			else
			{
				result = comp_.compare(e1, e2);
			}
			if (result != 0)
			{
				return result;
			}
		}

		if (!i1.hasNext() && !i2.hasNext())
		{
			return 0;
		}
		else if (!i1.hasNext())
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}

	private final Comparator<T> comp_;

}
