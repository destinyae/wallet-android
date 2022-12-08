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
package com.tari.android.wallet.ui.fragment.send.addAmount

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import com.tari.android.wallet.R
import com.tari.android.wallet.R.string.*
import com.tari.android.wallet.databinding.FragmentAddAmountBinding
import com.tari.android.wallet.extension.getWithError
import com.tari.android.wallet.extension.observe
import com.tari.android.wallet.model.*
import com.tari.android.wallet.ui.common.CommonFragment
import com.tari.android.wallet.ui.component.EmojiIdSummaryViewController
import com.tari.android.wallet.ui.component.FullEmojiIdViewController
import com.tari.android.wallet.ui.dialog.error.ErrorDialogArgs
import com.tari.android.wallet.ui.dialog.modular.ModularDialog
import com.tari.android.wallet.ui.dialog.tooltipDialog.TooltipDialogArgs
import com.tari.android.wallet.ui.extension.*
import com.tari.android.wallet.ui.fragment.home.HomeActivity
import com.tari.android.wallet.ui.fragment.send.addAmount.feeModule.NetworkSpeed
import com.tari.android.wallet.ui.fragment.send.addAmount.keyboard.KeyboardController
import com.tari.android.wallet.ui.fragment.send.amountView.AmountStyle
import com.tari.android.wallet.ui.fragment.send.common.TransactionData
import com.tari.android.wallet.util.Constants
import com.tari.android.wallet.util.WalletUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class AddAmountFragment : CommonFragment<FragmentAddAmountBinding, AddAmountViewModel>() {

    private lateinit var addAmountListenerWR: WeakReference<AddAmountListener>

    /**
     * Recipient is either an emoji id or a user from contacts or recent txs.
     */
    private var recipientUser: User? = null

    /**
     *     Control full emoji popups
     */
    private lateinit var fullEmojiIdViewController: FullEmojiIdViewController

    /**
     * Formats the summarized emoji id.
     */
    private lateinit var emojiIdSummaryController: EmojiIdSummaryViewController

    private var keyboardController: KeyboardController = KeyboardController()

    private var isFirstLaunch: Boolean = false

    private lateinit var balanceInfo: BalanceInfo
    private lateinit var availableBalance: MicroTari

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentAddAmountBinding.inflate(inflater, container, false).also { ui = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: AddAmountViewModel by viewModels()
        bindViewModel(viewModel)
        subscribeVM()

        isFirstLaunch = savedInstanceState == null
        ui.modifyButton.setOnClickListener { viewModel.showFeeDialog() }
    }

    private fun subscribeVM() = with(viewModel) {
        observe(isOneSidePaymentEnabled) { ui.oneSidePaymentSwitchView.isChecked = it }

        observe(serviceConnected) { setupUI() }

        observe(feePerGrams) { showOrHideCustomFeeDialog(it) }
    }

    private fun setupUI() {
        val amount = arguments?.parcelable<MicroTari>(HomeActivity.PARAMETER_AMOUNT)
        keyboardController.setup(requireContext(), AmountCheckRunnable(), ui.numpad, ui.amount, amount?.tariValue?.toDouble() ?: Double.MIN_VALUE)
        recipientUser = arguments?.parcelable(HomeActivity.PARAMETER_USER)
        // hide tx fee
        ui.txFeeContainerView.invisible()
        // hide/disable continue button
        ui.continueButton.invisible()
        ui.disabledContinueButton.visible()
        // add first digit to the element list
        val fullEmojiIdListener = object : FullEmojiIdViewController.Listener {
            override fun animationHide(value: Float) {
                ui.backCtaView.alpha = 1 - value
            }

            override fun animationShow(value: Float) {
                ui.backCtaView.alpha = 1 - value
            }
        }
        emojiIdSummaryController = EmojiIdSummaryViewController(ui.emojiIdSummaryView)
        fullEmojiIdViewController = FullEmojiIdViewController(
            ui.emojiIdOuterContainer,
            ui.emojiIdSummaryView,
            requireContext(),
            fullEmojiIdListener
        )
        fullEmojiIdViewController.fullEmojiId = recipientUser?.walletAddress?.emojiId.orEmpty()
        fullEmojiIdViewController.emojiIdHex = recipientUser?.walletAddress?.hexString.orEmpty()
        displayAliasOrEmojiId()
        setActionBindings()
    }

    private fun setActionBindings() {
        ui.backCtaView.setOnClickListener { onBackButtonClicked(it) }
        ui.emojiIdSummaryContainerView.setOnClickListener { emojiIdClicked() }
        ui.txFeeDescTextView.setOnClickListener { showTxFeeToolTip() }
        ui.oneSidePaymentHelp.setOnClickListener { showOneSidePaymentTooltip() }
        ui.continueButton.setOnClickListener { continueButtonClicked() }
        ui.oneSidePaymentSwitchView.setOnClickListener { viewModel.toggleOneSidePayment() }
    }

    private fun showOrHideCustomFeeDialog(feePerGram: FeePerGramOptions) {
        ui.feeCalculating.setVisible(false)
        ui.networkTrafficText.setVisible(true)
        ui.modifyButton.setVisible(feePerGram.networkSpeed != NetworkSpeed.Slow, View.INVISIBLE)
        val iconId = when (feePerGram.networkSpeed) {
            NetworkSpeed.Slow -> R.drawable.ic_network_slow
            NetworkSpeed.Medium -> R.drawable.ic_network_medium
            NetworkSpeed.Fast -> R.drawable.ic_network_fast
        }
        ui.networkTrafficIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), iconId))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addAmountListenerWR = WeakReference(context as AddAmountListener)
    }

    private fun displayAliasOrEmojiId() {
        if (recipientUser is Contact) {
            ui.emojiIdSummaryContainerView.gone()
            ui.titleTextView.visible()
            ui.titleTextView.text = (recipientUser as Contact).alias
        } else {
            recipientUser?.walletAddress?.emojiId?.let {
                displayEmojiId(it)
            }
        }
    }

    private fun displayEmojiId(emojiId: String) {
        ui.emojiIdSummaryContainerView.visible()
        emojiIdSummaryController.display(emojiId)
        ui.titleTextView.gone()
    }

    private fun onBackButtonClicked(view: View) {
        view.temporarilyDisableClick()
        val mActivity = activity ?: return
        mActivity.onBackPressed()
    }

    /**
     * Display full emoji id and dim out all other views.
     */
    private fun emojiIdClicked() {
        fullEmojiIdViewController.showFullEmojiId()
    }

    private fun showTxFeeToolTip() {
        val args = TooltipDialogArgs(string(tx_detail_fee_tooltip_transaction_fee), string(tx_detail_fee_tooltip_desc))
            .getModular(viewModel.resourceManager)
        ModularDialog(requireContext(), args).show()
    }

    private fun showOneSidePaymentTooltip() {
        val args = TooltipDialogArgs(
            string(add_amount_one_side_payment_switcher),
            string(add_amount_one_side_payment_question_mark)
        ).getModular(viewModel.resourceManager)
        ModularDialog(requireContext(), args).show()
    }

    private fun continueButtonClicked() {
        ui.continueButton.isClickable = false
        lifecycleScope.launch(Dispatchers.IO) { checkAmountAndFee() }
    }

    private fun checkAmountAndFee() {
        val error = WalletError()
        val balanceInfo = viewModel.walletService.getBalanceInfo(error)
        val fee = viewModel.selectedFeeData?.calculatedFee
        val amount = keyboardController.currentAmount
        if (error == WalletError.NoError && fee != null) {
            if (amount > balanceInfo.availableBalance) {
                lifecycleScope.launch(Dispatchers.Main) {
                    actualBalanceExceeded()
                }
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    if (fee > amount) {
                        val args = ErrorDialogArgs(
                            string(error_fee_more_than_amount_title),
                            string(error_fee_more_than_amount_description)
                        )
                        ModularDialog(requireContext(), args.getModular(viewModel.resourceManager)).show()
                        ui.continueButton.isClickable = true
                    } else {
                        continueToNote()
                        ui.continueButton.isClickable = true
                    }
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                ui.continueButton.isClickable = true
            }
        }
    }

    private fun updateBalanceInfo() {
        balanceInfo = viewModel.walletService.getWithError { error, wallet -> wallet.getBalanceInfo(error) }
        availableBalance = balanceInfo.availableBalance
        ui.availableBalanceContainerView.setupArgs(availableBalance)
    }

    private fun actualBalanceExceeded() {
        addAmountListenerWR.get()?.onAmountExceedsActualAvailableBalance(this)
        ui.continueButton.isClickable = true
    }

    private fun continueToNote() {
        val isOneSidePayment = ui.oneSidePaymentSwitchView.isChecked
        val transactionData =
            TransactionData(recipientUser, keyboardController.currentAmount, null, viewModel.selectedFeeData!!.feePerGram, isOneSidePayment)
        if (isOneSidePayment) {
            addAmountListenerWR.get()?.continueToFinalizing(transactionData)
        } else {
            addAmountListenerWR.get()?.continueToAddNote(transactionData)
        }
    }

    /**
     * Checks/validates the amount entered.
     */
    private inner class AmountCheckRunnable : Runnable {

        override fun run() {
            val error = WalletError()
            viewModel.calculateFee(keyboardController.currentAmount, error)
            if (error != WalletError.NoError) {
                showErrorState(error)
                return
            }
            viewModel.selectedFeeData ?: return

            updateBalanceInfo()

            if ((keyboardController.currentAmount + viewModel.selectedFeeData?.calculatedFee!!) > availableBalance) {
                showErrorState()
            } else {
                showSuccessState(viewModel.selectedFeeData?.calculatedFee!!)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun showSuccessState(fee: MicroTari) = with(ui) {
            notEnoughBalanceDescriptionTextView.text = string(add_amount_wallet_balance)
            availableBalanceContainerView.visible()

            txFeeTextView.text = "+${WalletUtil.amountFormatter.format(fee.tariValue)}"
            val showsTxFee: Boolean = if (keyboardController.currentAmount.value.toInt() == 0) {
                hideContinueButton()
                false
            } else {
                showContinueButtonAnimated()
                true
            }

            showBalance()

            showOrHideFeeViewAnimated(showsTxFee)
        }

        private fun showOrHideFeeViewAnimated(showsTxFee: Boolean) = with(ui) {
            if (showsTxFee && txFeeContainerView.visibility == View.VISIBLE ||
                !showsTxFee && txFeeContainerView.visibility == View.INVISIBLE
            ) {
                return@with
            }

            if (showsTxFee) {
                txFeeContainerView.alpha = 0f
                txFeeContainerView.visible()
            }

            ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener { valueAnimator: ValueAnimator ->
                    val value = valueAnimator.animatedValue as Float

                    if (showsTxFee) {
                        txFeeContainerView.translationY = (1f - value) * 100
                        txFeeContainerView.alpha = value
                    } else {
                        txFeeContainerView.translationY = value * 100
                        txFeeContainerView.alpha = (1f - value)

                        if (value == 1f) {
                            txFeeContainerView.invisible()
                        }
                    }
                }
                duration = Constants.UI.shortDurationMs
                interpolator = EasingInterpolator(Ease.CIRC_OUT)
                start()
            }
        }

        private fun showErrorState(error: WalletError? = null) = with(ui) {
            if (error?.code == 115) {
                availableBalanceContainerView.gone()
                notEnoughBalanceDescriptionTextView.text =
                    string(add_amount_funds_pending)
            } else {
                availableBalanceContainerView.visible()
                notEnoughBalanceDescriptionTextView.text =
                    string(add_amount_not_enough_available_balance)
            }

            hideContinueButton()

            showAvailableBalanceError()

            keyboardController.nudgeAmountView()

            showOrHideFeeViewAnimated(true)
        }

        private fun showAvailableBalanceError() = with(ui) {
            notEnoughBalanceView.background = ContextCompat.getDrawable(requireContext(), R.drawable.validation_error_box_border_bg)
            ui.availableBalanceContainerView.setupArgs(AmountStyle.Warning)
        }

        private fun showBalance() = with(ui) {
            notEnoughBalanceView.background = null
            ui.availableBalanceContainerView.setupArgs(AmountStyle.Normal)
        }

        private fun showContinueButtonAnimated() = with(ui) {
            if (continueButton.visibility == View.VISIBLE) {
                return@with
            }
            continueButton.alpha = 0f
            continueButton.visible()
            ObjectAnimator.ofFloat(continueButton, "alpha", 0f, 1f).apply {
                duration = Constants.UI.shortDurationMs
                start()
            }
        }

        private fun hideContinueButton() = with(ui) {
            continueButton.invisible()
        }
    }
}