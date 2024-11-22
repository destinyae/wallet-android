/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.ui.common.giphy.api.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Images(
    @Expose @SerializedName("original") val original: Original,
    @Expose @SerializedName("fixed_height") val fixedHeight: ImageVariant,
    @Expose @SerializedName("fixed_width") val fixedWidth: ImageVariant,
    @Expose @SerializedName("fixed_height_small") val fixedHeightSmall: ImageVariant,
    @Expose @SerializedName("fixed_width_small") val fixedWidthSmall: ImageVariant,
    @Expose @SerializedName("fixed_height_downsampled") val fixedHeightDownsampled: ImageVariant,
    @Expose @SerializedName("fixed_width_downsampled") val fixedWidthDownsampled: ImageVariant,
    @Expose @SerializedName("downsized") val downsized: ImageVariant,
    @Expose @SerializedName("downsized_small") val downsizedSmall: ImageVariant,
    @Expose @SerializedName("preview_gif") val previewGif: ImageVariant,
)
