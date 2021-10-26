package com.tari.android.wallet.ui.common.gyphy.placeholder

import android.graphics.drawable.Drawable
import com.tari.android.wallet.ui.common.gyphy.placeholder.GifColorPlaceholder.Companion.generate

interface GifPlaceholder {
    fun asDrawable(): Drawable

    companion object {
        fun color(target: Any): GifPlaceholder = generate(target)

        fun color(target: Any, cornerRadius: Float): GifPlaceholder = generate(target, cornerRadius)
    }
}

