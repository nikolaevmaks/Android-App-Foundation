package com.application.foundation.network;

import androidx.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.sentry.SentryLevel;
import com.application.foundation.utils.CommonUtils;
import com.application.foundation.utils.SentryUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import static com.application.foundation.utils.MapsExtensionsKt.mapCapacity;
import static com.application.foundation.App.getInjector;

public class WebResponseHandlerUtils {

	private static @Nullable String getRequestBody(WebRequestInterface request) {

		Buffer buffer = new Buffer();

		String requestString = null;
		RequestBody body;
		MediaType type;

		if ((body = request.getRequest().body()) != null &&
				(type = body.contentType()) != null &&
				("application".equalsIgnoreCase(type.type()) && "json".equalsIgnoreCase(type.subtype()))) {
			try {
				body.writeTo(buffer);
				requestString = buffer.snapshot().string(StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return requestString;
	}

	private static Map<String, String> getTags(WebRequestInterface request, Integer responseCode) {

		Map<String, String> tags = new HashMap<>(mapCapacity(3));
		tags.put("response code", responseCode == null ? null : Integer.toString(responseCode));
		tags.put("method", request.getRequest().method());
		tags.put("url", request.getRequest().url().toString());

		return tags;
	}

	private static final String[] KEYS = new String[] {"password", "token"};


	static void logJsonValidationError(@Nullable String message, WebRequestInterface request, int responseCode) {

		// no need passwords and tokens in Sentry and analytics

		Map<String, String> extras = null;

		String requestString = getRequestBody(request);
		if (requestString != null) {
			requestString = hideValueInJson(requestString, KEYS);

			extras = new HashMap<>(mapCapacity(1));
			extras.put("request", requestString);
		}

		SentryUtils.log("json_validation",
				SentryLevel.ERROR,
				message,
				getTags(request, responseCode),
				extras);

		getInjector().getAnalytics().logWebResponseError("json_validation",
				message,
				requestString,
				request.getRequest().method(),
				request.getRequest().url().toString(),
				responseCode,
				null);
	}

	static <T> void logWebResponseError(@Nullable String error, WebRequestInterface request, Integer responseCode, @Nullable T responseBody, boolean networkError) {

		// no need passwords and tokens in Sentry and analytics

		Map<String, String> extras = null;

		String requestString = getRequestBody(request);
		if (requestString != null) {
			requestString = hideValueInJson(requestString, KEYS);

			extras = new HashMap<>(mapCapacity(2));
			extras.put("request", requestString);
		}

		String responseString = responseBody == null ? null : CommonUtils.toJson(responseBody);
		if (responseString != null) {
			responseString = hideValueInJson(responseString, KEYS);

			if (extras == null) {
				extras = new HashMap<>(mapCapacity(1));
			}
			extras.put("response", responseString);
		}


		String message = (responseCode == null ? "" : responseCode + " ") +
				request.getRequest().method() + " " +
				request.getRequest().url() +
				(CommonUtils.isStringEmpty(error) ? "" : " " + error);

		SentryUtils.log(networkError ? "web_response_network_error" : "web_response",
				SentryLevel.ERROR,
				message,
				getTags(request, responseCode),
				extras);

		getInjector().getAnalytics().logWebResponseError(networkError ? "web_response_network_error" : "web_response",
				message,
				requestString,
				request.getRequest().method(),
				request.getRequest().url().toString(),
				responseCode,
				responseString);
	}

	// see JSONTokener, JSONStringer string()
	static String hideValueInJson(String body, String... keys) {

		StringBuilder sb = new StringBuilder(body);

		StringBuilder expression = new StringBuilder();

		for (int i = 0; i < keys.length; i++) {
			expression.append(keys[i]);
			if (i != keys.length - 1) {
				expression.append('|');
			}
		}

		Pattern pattern = Pattern.compile(expression.toString(), Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(body);

		int i = 0;
		int changedCharCount = 0;

		do {
			if (!matcher.find(i - changedCharCount)) {
				break;
			}
			i = matcher.end() + changedCharCount;

			// need to find ":" sequence after the key
			boolean isColonFound = false;
			boolean isQuoteFound = false;

			for (; i < sb.length(); i++) {
				char c = sb.charAt(i);
				if (c == '\\' || // escape starts
						c == '{' || c == '}' || c == '[' || c == ']') {
					i++;
					break;
				} else if (c == '"') {
					if (isColonFound) {
						int prevBodyLength = sb.length();
						i = hideUntilQuote(sb, i + 1);
						changedCharCount += sb.length() - prevBodyLength;
						break;
					} else if (isQuoteFound) {
						i++;
						break;
					}
					isQuoteFound = true;
				} else if (c == ':') {
					isColonFound = true;
					if (!isQuoteFound) {
						i++;
						break;
					}
				}
			}

			if (i == sb.length()) {
				break;
			}
		} while (true);

		matcher.reset(sb.toString());
		i = 0;
		do {
			if (!matcher.find(i)) {
				break;
			}

			i = matcher.end();
			// Sentry can filter passwords
			reverseKey(sb, matcher.start(), i);
		} while (true);

		return sb.toString();
	}


	private static void reverseKey(StringBuilder sb, int startPos, int endPos) {

		String oldKey = sb.substring(startPos, endPos);

		StringBuilder keySb = new StringBuilder(oldKey);
		keySb.reverse();

		sb.replace(startPos, endPos, keySb.toString());
	}

	// return next symbol after " pos
	private static int hideUntilQuote(StringBuilder sb, int startPos) {
		for (int i = startPos; i < sb.length(); i++) {
			if (sb.charAt(i) == '\\') {
				// escaping
				sb.setCharAt(i, '*');
				if (i + 1 < sb.length()) {
					sb.deleteCharAt(i + 1);
				}
			} if (sb.charAt(i) == '"') {
				return i + 1;
			} else {
				sb.setCharAt(i, '*');
			}
		}
		return sb.length();
	}
}
