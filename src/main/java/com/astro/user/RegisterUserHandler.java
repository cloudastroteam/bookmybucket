package com.astro.user;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.astro.util.CommonConstants;
import com.astro.util.CommonUtil;

public class RegisterUserHandler implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		context.getLogger().log("Input: " + input);
		JSONObject result = new JSONObject();
		try {
			//Register
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).build();
			DynamoDB dynamoDB = new DynamoDB(client);
			int otp = CommonUtil.generateOTP();
			String uname = (String) input.get("name");
			String email = (String) input.get("email");
			String mobile = (String) input.get("mobile");
			String password = (String) input.get("password");
			Table table = dynamoDB.getTable("user");
			Item item = new Item().withPrimaryKey("mobile", mobile).withString("uname", uname).withString("email", email)
					.withString("password", password).withBoolean("isactive", false).withNumber("otp", otp);
			table.putItem(item);

			//Send OTP
			AmazonSNS snsClient = AmazonSNSClient.builder().withRegion(Regions.AP_SOUTHEAST_1).build();
			String message = "Use " + otp + " as OTP to verify your mobile number with Book My Bucket";
			Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
			smsAttributes.put("AWS.SNS.SMS.SenderID",
					new MessageAttributeValue().withStringValue("Astro").withDataType("String"));
			smsAttributes.put("AWS.SNS.SMS.SMSType",
					new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));
			PublishResult publishResult = snsClient.publish(new PublishRequest().withMessage(message)
					.withPhoneNumber(mobile).withMessageAttributes(smsAttributes));
			result.put(CommonConstants.MOBILE, mobile);
			result.put(CommonConstants.MESSAGE_ID, publishResult.getMessageId());
			result.put(CommonConstants.STATUS, CommonConstants.SUCCESS);
		} catch (Exception e) {
			context.getLogger().log("Exception at RegisterUserHandler::handleRequest: " + e.getMessage());
			result.put(CommonConstants.STATUS, CommonConstants.FAILED);
			result.put(CommonConstants.ERRORDESC, e.getMessage());
		}
		context.getLogger().log("result: " + result);
		return result;
	}

}
