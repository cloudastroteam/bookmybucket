package com.astro.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.astro.util.CommonConstants;

public class ActivateUserHandler implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		context.getLogger().log("Input: " + input);
		JSONObject result = new JSONObject();
		try {
			// Register
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).build();
			DynamoDB dynamoDB = new DynamoDB(client);
			String mobile = (String) input.get("mobile");
			Integer otp = Integer.parseInt((String) input.get("otp"));
			Table table = dynamoDB.getTable("user");
			
			Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
			expressionAttributeValues.put(":o", otp);
			QuerySpec spec = new QuerySpec().withHashKey("mobile", mobile)
					.withFilterExpression("otp = :o").withValueMap(expressionAttributeValues);
			
			ItemCollection<QueryOutcome> items = table.query(spec);
			Iterator<Item> iterator = items.iterator();
			while (iterator.hasNext()) {
				context.getLogger().log(iterator.next().toJSONPretty());
			}
			if (items != null && items.getAccumulatedItemCount() > 0) {
				result.put(CommonConstants.OTPVALID, CommonConstants.YES);
				UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("mobile", mobile)
						.withReturnValues(ReturnValue.ALL_NEW).withUpdateExpression("set #ia = :val1")
						.withConditionExpression("#o = :val2").withNameMap(new NameMap().with("#ia", "isactive").with("#o", "otp"))
						.withValueMap(new ValueMap().withBoolean(":val1", true).withNumber(":val2", otp));
				UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
			} else {
				result.put(CommonConstants.OTPVALID, CommonConstants.NO);
			}
			result.put(CommonConstants.STATUS, CommonConstants.SUCCESS);
		} catch (Exception e) {
			context.getLogger().log("Exception at ActivateUserHandler::handleRequest: " + e.getMessage());
			result.put(CommonConstants.STATUS, CommonConstants.FAILED);
			result.put(CommonConstants.ERRORDESC, e.getMessage());
		}
		context.getLogger().log("result: " + result);
		return result;
	}

}
