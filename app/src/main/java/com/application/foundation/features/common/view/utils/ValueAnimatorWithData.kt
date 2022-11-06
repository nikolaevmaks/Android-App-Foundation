package com.application.foundation.features.common.view.utils

import android.animation.ValueAnimator
import android.animation.TimeInterpolator
import android.os.Build
import androidx.annotation.RequiresApi

class ValueAnimatorWithData private constructor() {

	lateinit var animator: ValueAnimator
		private set

	var updateListener: UpdateListener? = null
		private set

	var data: Any? = null
		private set


	companion object {

		@JvmStatic
		fun ofInt(vararg values: Int): ValueAnimatorWithData {
			return ValueAnimatorWithData().apply {
				animator = ValueAnimator.ofInt(*values)
			}
		}

		@JvmStatic
		fun ofInt(duration: Long, vararg values: Int): ValueAnimatorWithData {
			return ofInt(duration, null, *values)
		}

		@JvmStatic
		fun ofInt(duration: Long, updateListener: UpdateListener?, vararg values: Int): ValueAnimatorWithData {
			return ofInt(duration, null, updateListener, *values)
		}

		@JvmStatic
		fun ofInt(duration: Long, data: Any?, updateListener: UpdateListener?, vararg values: Int): ValueAnimatorWithData {
			return ofInt(*values).apply {
				set(0, duration, data, updateListener)
			}
		}

		@JvmStatic
		fun ofInt(startDelay: Long, duration: Long, data: Any?, updateListener: UpdateListener?, vararg values: Int): ValueAnimatorWithData {
			return ofInt(*values).apply {
				set(startDelay, duration, data, updateListener)
			}
		}

		@JvmStatic
		fun ofInt(duration: Long, repeatCount: Int, repeatMode: Int, data: Any?, updateListener: UpdateListener?, vararg values: Int): ValueAnimatorWithData {
			return ofInt(*values).apply {
				set(0, duration, repeatCount, repeatMode, data, updateListener)
			}
		}

		@JvmStatic
		fun ofInt(startDelay: Long, duration: Long, repeatCount: Int, repeatMode: Int, data: Any?, updateListener: UpdateListener?, vararg values: Int): ValueAnimatorWithData {
			return ofInt(*values).apply {
				set(startDelay, duration, repeatCount, repeatMode, data, updateListener)
			}
		}



		@JvmStatic
		fun ofFloat(vararg values: Float): ValueAnimatorWithData {
			return ValueAnimatorWithData().apply {
				animator = ValueAnimator.ofFloat(*values)
			}
		}

		@JvmStatic
		fun ofFloat(duration: Long, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(duration, null, *values)
		}

		@JvmStatic
		fun ofFloat(duration: Long, updateListener: UpdateListener?, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(duration, null, updateListener, *values)
		}

		@JvmStatic
		fun ofFloat(duration: Long, data: Any?, updateListener: UpdateListener?, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(*values).apply {
				set(0, duration, data, updateListener)
			}
		}

		@JvmStatic
		fun ofFloat(startDelay: Long, duration: Long, data: Any, updateListener: UpdateListener?, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(*values).apply {
				set(startDelay, duration, data, updateListener)
			}
		}

		@JvmStatic
		fun ofFloat(duration: Long, repeatCount: Int, repeatMode: Int, data: Any, updateListener: UpdateListener?, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(*values).apply {
				set(0, duration, repeatCount, repeatMode, data, updateListener)
			}
		}

		@JvmStatic
		fun ofFloat(startDelay: Long, duration: Long, repeatCount: Int, repeatMode: Int, data: Any, updateListener: UpdateListener?, vararg values: Float): ValueAnimatorWithData {
			return ofFloat(*values).apply {
				set(startDelay, duration, repeatCount, repeatMode, data, updateListener)
			}
		}



		@JvmStatic
		fun waitAnimator(duration: Long): ValueAnimatorWithData {
			return ofInt(duration, 0, 1)
		}

		@JvmStatic
		fun waitAnimator(duration: Long, updateListener: UpdateListener): ValueAnimatorWithData {
			return ofInt(duration, updateListener, 0, 1)
		}
	}









	fun setListener(updateListener: UpdateListener): ValueAnimatorWithData {
		animator.addUpdateListener(updateListener)
		animator.addListener(updateListener)
		return this
	}

	fun setDuration(duration: Long): ValueAnimatorWithData {
		animator.duration = duration
		return this
	}


	fun setRepeatCount(value: Int): ValueAnimatorWithData {
		animator.repeatCount = value
		return this
	}

	fun setRepeatMode(value: Int): ValueAnimatorWithData {
		animator.repeatMode = value
		return this
	}


	fun setStartDelay(startDelay: Long): ValueAnimatorWithData {
		animator.startDelay = startDelay
		return this
	}

	fun setInterpolator(value: TimeInterpolator): ValueAnimatorWithData {
		animator.interpolator = value
		return this
	}

	fun setCurrentPlayTime(playTime: Long): ValueAnimatorWithData {
		animator.currentPlayTime = playTime
		return this
	}

	val currentPlayTime: Long
		get() = animator.currentPlayTime


	fun setData(data: Any?): ValueAnimatorWithData {
		this.data = data
		return this
	}


	private fun set(startDelay: Long, duration: Long, repeatCount: Int, repeatMode: Int, data: Any?, updateListener: UpdateListener?): ValueAnimatorWithData {

		set(startDelay, duration, data, updateListener)

		setRepeatCount(repeatCount)
		setRepeatMode(repeatMode)

		return this
	}

	private fun set(duration: Long, repeatCount: Int, repeatMode: Int, data: Any?, updateListener: UpdateListener?): ValueAnimatorWithData {
		return set(0, duration, repeatCount, repeatMode, data, updateListener)
	}

	private fun set(startDelay: Long, duration: Long, data: Any?, updateListener: UpdateListener?): ValueAnimatorWithData {

		setDuration(duration)

		if (updateListener != null) {
			setListener(updateListener)
		}

		setStartDelay(startDelay)

		setData(data)

		return this
	}


	fun start(): ValueAnimatorWithData {
		animator.start()
		return this
	}

	fun cancel() {
		animator.cancel()
	}

	fun end() {
		animator.end()
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	fun resume() {
		animator.resume()
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	fun pause() {
		animator.pause()
	}


	val isStarted: Boolean
		get() = animator.isStarted

	fun isStartedWithData(data: Any): Boolean {
		return animator.isStarted && data == data
	}
}