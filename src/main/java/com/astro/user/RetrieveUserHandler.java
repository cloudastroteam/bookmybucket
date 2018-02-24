package com.astro.user;

import org.json.simple.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.astro.util.CommonConstants;

public class RetrieveUserHandler implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		context.getLogger().log("Input: " + input);
		JSONObject result = new JSONObject();
		try {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).build();
			DynamoDB dynamoDB = new DynamoDB(client);
			Table table = dynamoDB.getTable("user");
			String mobile = (String) input.get("mobile");
			GetItemSpec spec = new GetItemSpec().withPrimaryKey("mobile", mobile)
					.withProjectionExpression("mobile, uname").withConsistentRead(true);
			Item item = table.getItem(spec);
			context.getLogger().log("item: " + item.toJSONPretty());
			result.put(CommonConstants.UNAME, item.get("uname"));
			result.put(CommonConstants.MOBILE, item.get("mobile"));
			result.put(CommonConstants.STATUS, CommonConstants.SUCCESS);
		} catch (Exception e) {
			context.getLogger().log("Exception at RetrieveUserHandler::handleRequest: " + e.getMessage());
			result.put(CommonConstants.STATUS, CommonConstants.FAILED);
			result.put(CommonConstants.ERRORDESC, e.getMessage());
		}
		context.getLogger().log("result: " + result);
		return result;
	}

}
