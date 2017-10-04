package com.trilogy.app.crm.bulkloader;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.CSVIterator;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.StaffWriteOffBulkLoadCSVSupport;

public abstract class StaffWriteOffBulkLoadSupport
{

    public static Collection parseFile(Context ctx, File file,
            PrintWriter reportOut) throws HomeException
    {
        Collection ret = new ArrayList();
        char seperator = com.redknee.framework.xhome.csv.Constants.DEFAULT_SEPERATOR;
        for (Iterator i = new CSVIterator(StaffWriteOffBulkLoadCSVSupport
                .instance(), file.getAbsolutePath(), seperator); i.hasNext();)
        {
            try
            {
                Object obj = i.next();
                ret.add(obj);
            }
            catch (Throwable th)
            {
                reportOut.println(th.getClass().getName() + ": " + th.getMessage());
            }
        }

        return ret;
    }
}
