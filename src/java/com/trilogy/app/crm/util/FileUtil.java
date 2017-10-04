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
package com.trilogy.app.crm.util;

import java.io.File;

import com.trilogy.framework.core.http.MimeType;
import com.trilogy.framework.core.http.MimeTypeHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/*
 * author: simar.singh@redknee.com
 * A utility to manipulate files and folders
 */

public class FileUtil
{
    
    public static String getFileExtension(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        if (index > 0 && fileName.length() > (index + 2))
        {
            return fileName.substring(index + 1);
        }
        else
        {
            return "";
        }
    }


    public static String getFileFirstName(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        if (index > 0)
        {
            return fileName.substring(0, index);
        }
        else
        {
            return "";
        }
    }


    public static MimeType getMimeTypeForFile(Context ctx, String fileName) throws HomeException
    {
        return (MimeType) (((Home) (ctx.get(MimeTypeHome.class))).find(ctx, getFileExtension(fileName)));
    }


    public static boolean isMimeTypeImage(MimeType type)
    {
        return type.getMimeType().startsWith("image");
    }


    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir)
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
