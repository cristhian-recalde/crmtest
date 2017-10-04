/*
 * Created on Apr 1, 2003
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
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.service.http.MultipartWrapper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.FileUploadRequestServicer;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * upload msisdn file to subscribers set of cug
 * 
 * @author kwong
 *
 */
public class CUGMsisdnUploadRequestServicer 
    extends FileUploadRequestServicer
{
    public CUGMsisdnUploadRequestServicer() 
    {
        super();
    }

    /** Template method to be overriden by subclasses as required. **/
    public void outputPreForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
    { 
    }
    
    public void outputStats(Context ctx, PrintWriter out, File file, String name, MultipartWrapper multi)
    {
       String type = multi.getContentType(name);

       out.println("length: "   + file.length());
       out.println("name: "     + name);
       out.println("filename: " + multi.getFilesystemName(name));
       out.println("type: "     + type);
       Link url = new Link(ctx);
       url.add("cmd", "appCRMConfigCUG&CMD=Edit");
       out.println("<input type=\"button\" value=\"Back\" name=\"Back\" onclick=\"location.href=\"");
       url.write(out);
       out.println("\"></input>");
       out.println();
    }
}
