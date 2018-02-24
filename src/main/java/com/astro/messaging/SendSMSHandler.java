package com.astro.messaging;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.astro.util.CommonConstants;

public class SendSMSHandler implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		context.getLogger().log("Input: " + input);
		JSONObject result = new JSONObject();
		try {
			AmazonSNS snsClient = AmazonSNSClient.builder().withRegion(Regions.AP_SOUTHEAST_1).build();
			String mobile = (String) input.get("mobile");
			String message = (String) input.get("message");
			Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
			smsAttributes.put("AWS.SNS.SMS.SenderID",
					new MessageAttributeValue().withStringValue("Astro").withDataType("String"));
			smsAttributes.put("AWS.SNS.SMS.SMSType",
					new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));
			PublishResult publishResult = snsClient.publish(new PublishRequest().withMessage(message)
					.withPhoneNumber(mobile).withMessageAttributes(smsAttributes));
			result.put(CommonConstants.MESSAGE_ID, publishResult.getMessageId());
			result.put(CommonConstants.STATUS, CommonConstants.SUCCESS);
		} catch (Exception e) {
			context.getLogger().log("Exception at SendSMSHandler::handleRequest: " + e.getMessage());
			result.put(CommonConstants.STATUS, CommonConstants.FAILED);
			result.put(CommonConstants.ERRORDESC, e.getMessage());
		}
		context.getLogger().log("result: " + result);
		return result;
	}

}
