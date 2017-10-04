package com.trilogy.app.crm.web.control;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentHome;
import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.bean.account.AccountAttachmentXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;


/*
 * @author: simar.singh@redknee.com
 */
public class AccountAttachmentSupport
{

    public static File getFilePath(Context ctx, AccountAttachment attachment)
    {
        return new File(getAccountAttachmentPathConfig(ctx,attachment.getBAN()), (attachment.getFileLocation()));
    }


    public static File getAccountAttachmentPathConfig(Context ctx, final String ban)
    {
        AccountAttachmentManagementConfig  config = getAccountMangement(ctx);   
        String currentPath = config.getAttachmentPath()+ File.separatorChar + config.getCurrentDirectory() ;
        return new File(currentPath, ban);
    }
    
    public static File getAvailableFolderPath(final Context ctx, final String ban)
    {
        AccountAttachmentManagementConfig  config = getAccountMangement(ctx);
        String currentPath = config.getAttachmentPath()+ File.separatorChar + config.getCurrentDirectory() ;
        if (  config.getCurrentDirectory().equals("") || (getNumberOfDirectories(currentPath) > config.getMaxNumSubDirectories() ))
        {
            String curDatePath = getNewCurrentDatePath(ctx);   
            currentPath = config.getAttachmentPath() + File.separatorChar + curDatePath;
            
        }
        return new File(currentPath, ban);
    }

   
    
    private static String getNewCurrentDatePath(final Context ctx)
    {
        Date curDate = new Date();
        SimpleDateFormat formate = new SimpleDateFormat("yyyyMMddHHmm");
        String dateFormated = formate.format(curDate);
        AccountAttachmentManagementConfig  config = getAccountMangement(ctx);
        config.setCurrentDirectory(dateFormated);
        return dateFormated;

    }
    

    public static File getPreviewFilePath(Context ctx, AccountAttachment attachment)
    {
        return new File(new File(attachment.getFilePath()), (attachment.getPreviewLocation()));
    }
    
    
    public static AccountAttachmentManagementConfig getAccountMangement(Context ctx)
    {
        return (AccountAttachmentManagementConfig) ctx.get(AccountAttachmentManagementConfig.class);
    }


    public static String generatePreviewFileLocationName(Context ctx, AccountAttachment attachment)
    {
        final AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
        return new StringBuilder().append(attachment.getFileLocation()).append("_").append(config.getMaxWidthImage())
                .append("x").append(config.getMaxHeightImage()).append(".").append(config.getMimeType()).toString();
    }


    public static String generateFileLocationName(Context ctx, AccountAttachment attachment)
    {
        return new StringBuilder().append(attachment.getBAN()).append("-").append(System.currentTimeMillis())
                .append("-").append(attachment.getFileName()).toString();
    }

    public static String generateFilePathName(Context ctx, AccountAttachment attachment)
    {
        final AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
        return new StringBuilder().append(config.getAttachmentPath()).append(attachment.getBAN()).append("").toString();
    }
    

    public static int getNumberOfAttachements(Context ctx, Home home) throws HomeException
    {
        class AttachmentCounter implements Visitor
        {

            private static final long serialVersionUID = 1L;
            private int counter = 0;


            public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
            {
                ++counter;
            }
        }
        AttachmentCounter counter = new AttachmentCounter();
        home.forEach(ctx, counter);
        return counter.counter;
    }


    public static int getNumberOfAttachmentsForBan(Context ctx, String ban) throws HomeException
    {
        return getNumberOfAttachements(ctx, getBanAttachmentsHome(ctx, ban));
    }


    public static Home getBanAttachmentsHome(Context ctx, String ban)
    {
        Home siftedHome = ((Home) ctx.get(AccountAttachmentHome.class)).where(ctx, new EQ(AccountAttachmentXInfo.BAN,
                ban));
        return siftedHome;
    }


    public static boolean isAttachmentDisplayableImage(Context ctx, AccountAttachment attachment)
    {
        if ("".equals(attachment.getPreviewLocation()))
        {
            // no preview images were generated for the attachment
            return false;
        }
        else
        {
            return true;
        }
    }

    // public static String THUMBNAIL_PREFIX = "THUMBNAIL_";
    public static String PREVIEW_PREFIX = "PREVIEW_";


    // public static String PROFILE_PHOTO_PREFIX = "PROFILE_";
    // public static String DEFAULT_PHOTO = "attachment.jpg";
    public static AccountAttachment getAttachment(Context ctx, String attachmentKey) throws HomeException
    {
        return (AccountAttachment) ((Home) ctx.get(AccountAttachmentHome.class)).find(ctx, attachmentKey);
    }

    public static int getNumberOfDirectories(String path)
    {
        File currentDirectory = new File(path);

        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        
        File[] files = currentDirectory.listFiles(fileFilter);
        
        return files.length;
        
    }
}


