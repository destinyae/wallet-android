package com.tari.android.wallet.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.tari.android.wallet.extension.observe
import com.tari.android.wallet.ui.component.MutedBackPressedCallback
import com.tari.android.wallet.ui.dialog.confirm.ConfirmDialog
import com.tari.android.wallet.ui.dialog.error.ErrorDialog

abstract class CommonFragment<Binding: ViewBinding, VM: CommonViewModel> : Fragment() {

    protected val blockingBackPressDispatcher = MutedBackPressedCallback(false)

    protected lateinit var ui: Binding

    protected lateinit var viewModel: VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, blockingBackPressDispatcher)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun bindViewModel(viewModel: VM) = with(viewModel) {
        this@CommonFragment.viewModel = this

        observe(backPressed) { requireActivity().onBackPressed() }

        observe(openLink) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }

        observe(confirmDialog) { ConfirmDialog(requireContext(), it).show() }

        observe(errorDialog) { ErrorDialog(requireContext(), it).show() }

        observe(blockedBackPressed) { blockingBackPressDispatcher.isEnabled = it }
    }
}