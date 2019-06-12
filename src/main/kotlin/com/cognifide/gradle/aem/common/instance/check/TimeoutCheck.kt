package com.cognifide.gradle.aem.common.instance.check

import com.cognifide.gradle.aem.common.instance.InstanceException
import com.cognifide.gradle.aem.common.utils.Formats
import java.util.concurrent.TimeUnit

class TimeoutCheck(group: CheckGroup) : DefaultCheck(group) {

    var stateTime: Long = TimeUnit.MINUTES.toMillis(5)

    var constantTime: Long = TimeUnit.MINUTES.toMillis(30)

    override fun check() {
        if (progress.stateTime >= stateTime) {
            throw InstanceException("Instance state timeout reached '${Formats.duration(progress.stateTime)}' for $instance!")
        }

        if (runner.runningTime >= constantTime) {
            throw InstanceException("Instance constant timeout reached '${Formats.duration(runner.runningTime)}'!")
        }
    }
}