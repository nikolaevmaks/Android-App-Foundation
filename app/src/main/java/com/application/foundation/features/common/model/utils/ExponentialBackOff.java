package com.application.foundation.features.common.model.utils;

import com.application.foundation.utils.CommonUtils;
import java.util.Random;

public class ExponentialBackOff {

	// see https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/util/ExponentialBackOff#DEFAULT_MAX_INTERVAL_MILLIS

	private static final int START_DELAY = 500;
	private static final float DELAY_MULTIPLIER = 1.5f;
	private static final float RANDOMIZATION_FACTOR = 0.5f;

	private int startDelay = START_DELAY;
	private float delayMultiplier = DELAY_MULTIPLIER;
	private final int maxRetryCount;
	private final int maxIntervalMillis;
	private final Random random;

	private long startTime;
	private int retryCount;
	private int delay;

	public ExponentialBackOff(int retryCount, int maxIntervalMillis) {
		maxRetryCount = retryCount;
		this.maxIntervalMillis = maxIntervalMillis;
		random = new Random();

		reset();
	}

	public ExponentialBackOff(int retryCount) {
		this(retryCount, -1);
	}


	/**
	 * delay *= 1 - RANDOMIZATION_FACTOR (0.5) + 2 * RANDOMIZATION_FACTOR * random.nextFloat() <br/>
	 * delay *= delayMultiplier
	 */
	public ExponentialBackOff(int startDelay, float delayMultiplier, int retryCount) {
		this(retryCount, -1);
		this.startDelay = startDelay;
		this.delayMultiplier = delayMultiplier;

		reset();
	}

	public int nextBackOffMillis() {
		if (retryCount == 0) {
			startTime = CommonUtils.getCurrentTimeMillisForDuration();
		}
		retryCount++;

		if (retryCount > maxRetryCount ||
				maxIntervalMillis != -1 && CommonUtils.getCurrentTimeMillisForDuration() - startTime > maxIntervalMillis) {
			return -1;
		} else {
			delay *= 1 - RANDOMIZATION_FACTOR + 2 * RANDOMIZATION_FACTOR * random.nextFloat(); // delay * (random value in range [1 - randomization_factor, 1 + randomization_factor])

			int millis = delay;
			delay *= delayMultiplier;
			return millis;
		}
	}

	public void reset() {
		retryCount = 0;
		delay = startDelay;
	}
}