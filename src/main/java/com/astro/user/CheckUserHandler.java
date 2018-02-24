package com.astro.user;

import java.util.Iterator;

import org.json.simple.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.astro.util.CommonConstants;

public class CheckUserHandler implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		context.getLogger().log("Input: " + input);
		JSONObject result = new JSONObject();
		try {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).build();
			DynamoDB dynamoDB = new DynamoDB(client);
			Table table = dynamoDB.getTable("user");
			QuerySpec spec = new QuerySpec().withKeyConditionExpression("mobile = :mobile")
					.withValueMap(new ValueMap().withString(":mobile", input.get("mobile").toString()));
			ItemCollection<QueryOutcome> items = table.query(spec);
			Iterator<Item> iterator = items.iterator();
			while (iterator.hasNext()) {
				context.getLogger().log(iterator.next().toJSONPretty());
			}
			if (items != null && items.getAccumulatedItemCount() > 0) {
				result.put(CommonConstants.ISEXISTS, CommonConstants.YES);
			} else {
				result.put(CommonConstants.ISEXISTS, CommonConstants.NO);
			}
			result.put(CommonConstants.STATUS, CommonConstants.SUCCESS);
		} catch (Exception e) {
			context.getLogger().log("Exception at CheckUserHandler::handleRequest: " + e.getMessage());
			result.put(CommonConstants.STATUS, CommonConstants.FAILED);
			result.put(CommonConstants.ERRORDESC, e.getMessage());
		}
		context.getLogger().log("result: " + result);
		return result;
	}

}
