package com.trilogy.app.crm.bucket;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Comparator;

import com.trilogy.app.crm.bean.BucketHistoryXDBHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

import com.trilogy.app.crm.bean.*;

/**
 * 
 * @author abhurke
 *
 */
public class BucketHistoryPipelineFactory implements PipelineFactory {

	private String _tableName = "BucketHistory";
	
	public BucketHistoryPipelineFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public BucketHistoryPipelineFactory(String tableName) {
		_tableName = tableName;
	}

	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		// TODO Auto-generated method stub
		return new SortingHome(ctx, new BucketHistoryComparator(), new BucketHistoryXDBHome(ctx, _tableName) );
	}
	
	private class BucketHistoryComparator implements Comparator<BucketHistory> {

		public int compare(BucketHistory o1, BucketHistory o2) {

			if(o1.getAdjustmentDate().after(o2.getAdjustmentDate())) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}

}
