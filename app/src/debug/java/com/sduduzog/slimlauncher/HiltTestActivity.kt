package com.sduduzog.slimlauncher

import androidx.appcompat.app.AppCompatActivity
import com.sduduzog.slimlauncher.utils.IPublisher
import com.sduduzog.slimlauncher.utils.ISubscriber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity(), IPublisher {
    override fun attachSubscriber(s: ISubscriber) {
    }

    override fun detachSubscriber(s: ISubscriber) {
    }
}
