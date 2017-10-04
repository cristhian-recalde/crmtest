package com.trilogy.app.crm.util.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

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
public class MessageSorter
{	
	public MessageSorter(String level, String filename) throws IOException
	{
		level_ = level;
		writer_ = new PrintWriter(new BufferedWriter(new FileWriter(filename + "." + level_)));
	}
	
	public void addData(String module, Object data)
	{
		entries_.put(module, data);
	}

	public Object getData(String module)
	{
		return entries_.get(module);
	}
	
	public void sort()
	{
		TreeSet sortedEntries = new TreeSet(entries_.values());
		Iterator iter = sortedEntries.iterator();
		System.out.println(">>>>>>>>>>> " + level_);
		while (iter.hasNext())
		{
			ModuleStatistics stat = (ModuleStatistics)iter.next();
			System.out.println(stat.id_ + " "  + stat.count_);
			for (int i = 0; i < stat.list_.size(); i++)
			{
				String msg = (String)stat.list_.get(i);
				writer_.println(msg);
			}
			writer_.flush();			
		}
		writer_.close();
	}
	private Hashtable entries_ = new Hashtable();
	private PrintWriter writer_ = null;
	private String level_;
}
