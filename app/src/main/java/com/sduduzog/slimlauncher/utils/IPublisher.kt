package com.sduduzog.slimlauncher.utils

interface IPublisher {
    fun attachSubscriber(s: ISubscriber)
    fun detachSubscriber(s: ISubscriber)
}
