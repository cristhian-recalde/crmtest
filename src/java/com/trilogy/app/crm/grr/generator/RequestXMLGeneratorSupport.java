/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.grr.generator;

import java.util.Collection;

import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.calculator.ConstantValueCalculator;
import com.trilogy.app.crm.calculator.ToStringValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.support.KeyValueSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.snippet.log.Logger;

/**
 * This class will provide support methods required for Generation of GRR Request XML.
 * @author sgaidhani
 * @since 9.4
 */
public class RequestXMLGeneratorSupport {

	private static final ConstantValueCalculator NO_VALUE_CALCULATOR = new ConstantValueCalculator("");

	/**
	 * Method to replace the keys by calculated values.
	 * @param ctx
	 * @param text
	 * @param features
	 * @return
	 */
	public static String replaceVariables(Context ctx, String text, KeyValueFeatureEnum... features)
	{
		StringBuilder sb = new StringBuilder(text);

		if (features == null || features.length == 0)
		{
			// If no features are passed in, then assume no variable replacement is required.
			features = new KeyValueFeatureEnum[] {};
		}

		Collection<KeyConfiguration> keys = KeyValueSupportHelper.get(ctx).getConfiguredKeys(ctx, true, features);
		if (keys != null)
		{
			for (KeyConfiguration key : keys)
			{
				String keyword = key.getKey();

				ValueCalculator valueCalculator = key.getValueCalculator();
				if (valueCalculator == null)
				{
					valueCalculator = NO_VALUE_CALCULATOR;
				}
				else
				{
					valueCalculator = new ToStringValueCalculator(valueCalculator);
				}


				if(sb.indexOf(keyword) >= 0)
				{
					Logger.debug(ctx, RequestXMLGeneratorSupport.class, "Trying to evaluate Key : ["+keyword+"]");
					String value = (String) valueCalculator.getValue(ctx);
					Logger.debug(ctx, RequestXMLGeneratorSupport.class, "Evaluation complete for Key ["+keyword+"] ,value ["+value+"]");
					for (int start = sb.indexOf(keyword); start >= 0; start = sb.indexOf(keyword, start))
					{
						int end = start + keyword.length();
						sb.replace(start, end, value);
					}
				}
			}
		}

		return sb.toString();
	}
}
