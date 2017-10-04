package com.trilogy.app.crm.util.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
public class LogProcessor
{
	public final static String INFO = "INFO";
	public final static String DEBUG = "DEBUG";
	public final static String MINOR = "MINOR";
	public final static String MAJOR = "MAJOR";
	public final static String CRITICAL = "CRITICAL";

    public static void main(String[] args)
    {
    	if (args.length != 0)
    	{
			LogProcessor logProcessor = new LogProcessor(args[0]);
		
			logProcessor.process(args[0]);
			logProcessor.sort();
			
			System.out.println("Done");
    	}
    	else
    	{
    		System.err.println("Please provide log file to be processed");
    	}
    }

	public LogProcessor(String filename)
	{
		inputFileName_ = filename;
	}
	
	private void process(String filename)
	{
		File logFile = new File(filename);
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(logFile));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		try
		{
			info_ = new MessageSorter(INFO, inputFileName_);
			debug_ = new MessageSorter(DEBUG, inputFileName_);
			minor_ = new MessageSorter(MINOR, inputFileName_);
			major_ = new MessageSorter(MAJOR, inputFileName_);
			critical_ = new MessageSorter(CRITICAL, inputFileName_);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		String logMsg = "";
		String severity = "";
		String moduleName = "";
		try
		{
			String line = reader.readLine();
			while (line != null)
			{	
				String[] fields = line.split(" ");
				if (fields.length >= 5)
				{
					if (fields[3].equals(INFO) ||
						fields[3].equals(DEBUG) ||
						fields[3].equals(MINOR) ||
						fields[3].equals(MAJOR) ||
						fields[3].equals(CRITICAL))
					{
						// a new line - finish with the old one
						if (! severity.equals(""))
						{		
							processLogMsg(severity, moduleName, logMsg);
						}
						
						logMsg = line;
						severity = fields[3].trim();
						moduleName = fields[4].trim();
					}
				}
				else
				{
					// an old log entry
                    logMsg += "\n";
					logMsg += line;
				}
				line = reader.readLine();
			}
		}
		catch (IOException e)
		{
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}
		if (! logMsg.equals(""))
		{
			processLogMsg(severity, moduleName, logMsg);
		}
	}
	
	private void processLogMsg(String severity, String moduleName, String line)
	{
		MessageSorter messageSorter = getMessageSorter(severity);
							
		ModuleStatistics stat = 
			(ModuleStatistics)messageSorter.getData(moduleName);
								
		if (stat == null)
		{
			stat = new ModuleStatistics(moduleName, 1, line);
		}
		else
		{
			stat.addLine(line);
		}
		messageSorter.addData(moduleName, stat);
		
	}
	
	private void sort()
	{
		debug_.sort();
		info_.sort();
		minor_.sort();
		major_.sort();
		critical_.sort();
	}
	
	private MessageSorter getMessageSorter(String type)
	{
		MessageSorter messageSorter = null;
		
		if (INFO.equals(type))
		{
			messageSorter = info_;
		}
		else if (DEBUG.equals(type))
		{
			messageSorter = debug_;
		}
		else if (MINOR.equals(type))
		{
			messageSorter = minor_;
		}
		else if (MAJOR.equals(type))
		{
			messageSorter = major_;
		}
		else if (CRITICAL.equals(type))
		{
			messageSorter = critical_;
		}
		return messageSorter;
	}
	
	private MessageSorter debug_ = null;
	private MessageSorter info_ = null;
	private MessageSorter minor_ = null;
	private MessageSorter major_ = null;
	private MessageSorter critical_ = null;
	private String inputFileName_;
}
