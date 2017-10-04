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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.CSVSupport;
import com.trilogy.framework.xhome.csv.Constants;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.web.XlogExceptionListener;

/**
 * Much of this class is taken directly from com.redknee.framework.xhome.csv.GenericCSVHome.
 * The reason that the BulkLoadAccountCSVHome shouldn't use the xgenerated BulkLoadAccountCSVHome
 * (com.redknee.app.crm.customer.hummingbird.bean.account.BulkLoadAccountCSVHome) since it
 * doesn't return rich enough Exception descriptions.
 *
 * Noticibly different is load method.  The load method
 * uses a specific Exception class. A new Constructor was added too:
 * BulkLoadAccountCSVHome(Context ctx, String filename, char seperator)
 *
 * We couldn't simply just extend the GenericCSVHome because all of its useful constructors
 * invoke the GenericCSVHome.load method, which was overwritten here.
 *
 * @author ali@redknee.com
 */
public class CustomizedBulkLoadAccountCSVHome extends AbstractHome implements Constants
{
    private final FileChannel fileChannel_;
    
    private final CSVSupport csvSupport_;

    private final String filename_;

    private final char seperator_;

    public CustomizedBulkLoadAccountCSVHome(final Context ctx, final Home cache, final CSVSupport csvSupport,
            final String filename, final FileChannel fileChannel) throws HomeException
    {
        this(ctx, csvSupport, filename, fileChannel, DEFAULT_SEPERATOR);
    }

    public CustomizedBulkLoadAccountCSVHome(final Context ctx, final CSVSupport csvSupport,
            final String filename, final FileChannel fileChannel, final char seperator) throws HomeException
    {
        super(ctx);
        csvSupport_ = csvSupport;
        filename_ = filename;
        fileChannel_ = fileChannel;
        seperator_ = seperator;
    }

    public CustomizedBulkLoadAccountCSVHome(final Context ctx, final String filename, final FileChannel fileChannel, final char seperator)
        throws HomeException
    {
        this(ctx, BulkLoadAccountCSVSupport.instance(), filename, fileChannel, seperator);
    }

    public CustomizedBulkLoadAccountCSVHome(final CSVSupport csvSupport, final String filename, final FileChannel fileChannel)
        throws HomeException
    {
        this(csvSupport, filename, fileChannel, DEFAULT_SEPERATOR);
    }

    public CustomizedBulkLoadAccountCSVHome(final CSVSupport csvSupport, final String filename, final FileChannel fileChannel,
            final char seperator) throws HomeException
    {
        super(null);
        csvSupport_ = csvSupport;
        filename_ = filename;
        seperator_ = seperator;
        fileChannel_ = fileChannel;
    }

  public Object find(Context ctx, Object obj)
        throws HomeException
  {
      throw new HomeException("Not implemented");
  }


  public Collection select(Context ctx, Object obj)
      throws HomeException
  {
      throw new HomeException("Not implemented");
  }

  public Object create(final Context ctx, final Object obj) throws HomeException
    {
        throw new HomeException("Not implemented");
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        throw new HomeException("Not implemented");
    }

    public void removeAll(final Context ctx, final Object where) throws HomeException
    {
        throw new HomeException("Not implemented");
    }

    public void remove(final Context ctx, final Object bean) throws HomeException
    {
        throw new HomeException("Not implemented");
    }


    public Visitor forEach(final Context ctx, final Visitor visitor, final Object where) throws HomeException
    {
        ExceptionListener listener = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (listener == null)
        {
            listener = new XlogExceptionListener(ctx);
        }

        //for (Iterator i = new CSVIterator(csvSupport_, getFilename(), seperator_); i.hasNext();)
        final BulkLoadCSVIterator i = new BulkLoadCSVIterator(csvSupport_, getFilename(), fileChannel_, seperator_);
        while (i.hasNext())
        {
            String line = "";
            try
            {
                line = i.getLine();
                final Object bean = i.next();
                
                visitor.visit(ctx, bean);
            }
            catch (Throwable th)
            {
                //Indicate which line threw the exception
                PrintWriter pw = ((CSVErrorFileExceptionListener) listener).getLogWriter();
                pw.println("Account Load Error: Failure parsing the following line: ");
                pw.print("  ");
                pw.println(line);
                pw.println("Due to following exception:");

                pw = ((CSVErrorFileExceptionListener) listener).getErrWriter();
                pw.println("Account Load Error: Failure parsing the following line: ");
                pw.print("  ");
                pw.println(line);
                pw.println("Due to following exception:");

                listener.thrown(th);
            }
        }
        return visitor;
    }

    ////////////////////////////////////////////////////////////////// INTERNAL

    protected String getFilename()
    {
        return filename_;
    }

}
