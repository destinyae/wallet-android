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
package com.tari.android.wallet.ui.fragment.settings.backgroundService

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.tari.android.wallet.databinding.ActivityBackgroundServiceSettingsBinding
import com.tari.android.wallet.extension.observe
import com.tari.android.wallet.ui.common.CommonActivity
import com.tari.android.wallet.ui.extension.ThrottleClick

class BackgroundServiceSettingsActivity : CommonActivity<ActivityBackgroundServiceSettingsBinding, BackgroundServiceSettingsViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityBackgroundServiceSettingsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val viewModel: BackgroundServiceSettingsViewModel by viewModels()
        bindViewModel(viewModel)

        setupViews()

        observeUI()
    }

    private fun setupViews() = with(ui) {
        backCtaView.setOnClickListener(ThrottleClick { onBackPressedDispatcher.onBackPressed() })
        loadingSwitchView.setOnCheckedChangeListener { viewModel.toggleBackgroundServiceEnable(it) }
    }

    private fun observeUI() = with(viewModel) {
        observe(switchState) { ui.loadingSwitchView.setState(it) }
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, BackgroundServiceSettingsActivity::class.java))
        }
    }
}

