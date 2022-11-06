package com.application.foundation.network;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WebResponseHandlerUtilsTest {

	@Test
	public void testHideValueInJson() {

		String body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewPasswordNew\":\"dtdgf\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewdrowssaPNew\":\"*****\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"password\":\"dtdgf\",\"settings\":{\"country\":\"GB\"},\"Token\":\"\",\"password\":\"d\"}",
				"password", "token");
		assertEquals("{\"login\":\"vjvj\",\"drowssap\":\"*****\",\"settings\":{\"country\":\"GB\"},\"nekoT\":\"\",\"drowssap\":\"*\"}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"password\":\"\\\"\",\"password\":\"dt\\\"T\\\"dgf\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"drowssap\":\"*\",\"drowssap\":\"********\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"password\":\"dt\\\"\\\"dgf\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"drowssap\":\"*******\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"password\":\"\",\"password\":\"\\\"T\\\"dgf\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"drowssap\":\"\",\"drowssap\":\"******\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewPasswordNew\":\"password\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewdrowssaPNew\":\"********\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewPasswordNew\":\"password\\\"\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewdrowssaPNew\":\"*********\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewPasswordNew\":\"password\\\"\\n\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewdrowssaPNew\":\"**********\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewpAssworDNew\":\"pAssworD\\\"n\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewDrowssApNew\":\"**********\",\"settings\":{\"country\":\"GB\"}}", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewpAssworDNew\":\"pAssworD\\\"n",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewDrowssApNew\":\"**********", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewpAssworDNew\":\"pAssworD\\\"",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewDrowssApNew\":\"*********", body);


		body = WebResponseHandlerUtils.hideValueInJson("\\\"",
				"password");
		assertEquals("\\\"", body);


		body = WebResponseHandlerUtils.hideValueInJson("",
				"password");
		assertEquals("", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewpAssworDNew\\\"\":\"pAssworD\\\"n",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewDrowssApNew\\\"\":\"DrowssAp\\\"n", body);


		body = WebResponseHandlerUtils.hideValueInJson("{\"login\":\"vjvj\",\"NewPassword\\\"New\":\"password\\\"\\n\",\"settings\":{\"country\":\"GB\"}}",
				"password");
		assertEquals("{\"login\":\"vjvj\",\"NewdrowssaP\\\"New\":\"drowssap\\\"\\n\",\"settings\":{\"country\":\"GB\"}}", body);
	}
}