/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Map;

import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PackageStateStats;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PackageBatchState1147ER
{

    public PackageBatchState1147ER(Map<PackageStateEnum, PackageStateStats> initialStats, PackageBulkTask task,
            Map<PackageStateEnum, PackageStateStats> finalStats) throws PackageProcessingException
    {
        this.intialStats_ = initialStats;
        this.finalStats_ = finalStats;
        this.task_ = task;
    }


    public void fail(Context ctx, boolean authStatus, Status status)
    {
        new ERLogMsg(ER_ID, ER_CLASS, SID, -1, getFields(authStatus, status)).log(ctx);
    }


    public void success(Context ctx)
    {
        new ERLogMsg(ER_ID, ER_CLASS, SID, -1, getFields(true, Status.FULL_COMPLETE)).log(ctx);
    }


    private String[] getFields(boolean authStatus, Status status)
    {
        String[] fields = new String[15];
        fields[0] = task_.getLastModifiedBy(); // User
        fields[1] = task_.getBatchId(); // Batch ID
        fields[2] = (authStatus?"true":"false"); // Authentication Valid
        fields[3] = getPackageNewStateString(task_.getChangeStateTo()); // New State
        fields[4] = status.getResultCode(); // status;
        fields[5] = getPackageStateCountString(intialStats_.get(PackageStateEnum.AVAILABLE)); // Initial
        // Available
        fields[6] = getPackageStateCountString(finalStats_.get(PackageStateEnum.AVAILABLE)); // Final
        // Available
        fields[7] = getPackageStateCountString(intialStats_.get(PackageStateEnum.IN_USE)); // Initial
        // In
        // Use
        fields[8] = getPackageStateCountString(finalStats_.get(PackageStateEnum.IN_USE)); // Final
        // In
        // Use
        fields[9] = getPackageStateCountString(intialStats_.get(PackageStateEnum.HELD)); // Initial
        // Held
        fields[10] = getPackageStateCountString(finalStats_.get(PackageStateEnum.HELD)); // Final
        // Held
        fields[11] = getPackageStateCountString(intialStats_.get(PackageStateEnum.STOLEN)); // Initial
        // Lost/Stolen
        fields[12] = getPackageStateCountString(finalStats_.get(PackageStateEnum.STOLEN)); // Final
        // Lost/Stolen
        fields[13] = getPackageStateCountString(intialStats_.get(PackageStateEnum.DAMAGED)); // Initial
        // Damaged
        fields[14] = getPackageStateCountString(finalStats_.get(PackageStateEnum.DAMAGED)); // Final
        // Damaged
        return fields;
    }


    public String getPackageNewStateString(PackageStateEnum state)
    {
        if (null != state)
        {
            if (PackageStateEnum.DAMAGED == state)
            {
                return "5";
            }
            else if (PackageStateEnum.STOLEN == state)
            {
                return "4";
            }
            return String.valueOf(state);
        }
        return "";
    }
    
    public String getPackageStateCountString(PackageStateStats packageStats)
    {
        if (null == packageStats)
        {
            return "";
        }
        else
        {
            return String.valueOf(packageStats.getPackageCount());
        }
    }

    final Map<PackageStateEnum, PackageStateStats> intialStats_;
    final Map<PackageStateEnum, PackageStateStats> finalStats_;
    final PackageBulkTask task_;

    public static enum Status {
        FULL_COMPLETE {

            @Override
            public String getResultCode()
            {
                return "1";
            }
        },
        PARTIAL_COMPLETE {

            @Override
            public String getResultCode()
            {
                return "2";
            }
        },
        ABORTED {

            @Override
            public String getResultCode()
            {
                return "3";
            }
        };

        public abstract String getResultCode();


        @Override
        public String toString()
        {
            return getResultCode();
        }
    }

    private static final String SID = "Card Package Batch State Update:1:0";
    private static final int ER_ID = 1147;
    private static final int ER_CLASS = 1100;
}
