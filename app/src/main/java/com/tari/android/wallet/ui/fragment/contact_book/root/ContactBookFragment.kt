package com.tari.android.wallet.ui.fragment.contact_book.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.tari.android.wallet.R
import com.tari.android.wallet.databinding.FragmentContactBookRootBinding
import com.tari.android.wallet.extension.observe
import com.tari.android.wallet.ui.common.CommonFragment
import com.tari.android.wallet.ui.component.tari.toolbar.TariToolbarActionArg
import com.tari.android.wallet.ui.extension.PermissionExtensions.runWithPermission
import com.tari.android.wallet.ui.extension.setVisible
import com.tari.android.wallet.ui.extension.string
import com.tari.android.wallet.ui.fragment.contact_book.contacts.ContactsFragment
import com.tari.android.wallet.ui.fragment.contact_book.favorites.FavoritesFragment
import com.tari.android.wallet.ui.fragment.home.HomeActivity
import com.tari.android.wallet.ui.fragment.home.navigation.Navigation
import java.lang.ref.WeakReference

class ContactBookFragment : CommonFragment<FragmentContactBookRootBinding, ContactBookViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentContactBookRootBinding.inflate(inflater, container, false).also { ui = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: ContactBookViewModel by viewModels()
        bindViewModel(viewModel)

        setupUI()

        subscribeUI()

//        initTests()
    }

    override fun onResume() {
        super.onResume()
        grantPermission()
    }

    private fun subscribeUI() = with(viewModel) {
        observe(contactsRepository.loadingState) {
            ui.isSyncingProgressBar.setVisible(it.isLoading)
            ui.syncingStatus.text = it.name + " " + it.time + "s"
        }

        observe(sharedState) { updateSharedState(it) }
    }

    private fun grantPermission() {
        runWithPermission(android.Manifest.permission.READ_CONTACTS, false) {
            viewModel.contactsRepository.contactPermission.value = true
            viewModel.contactsRepository.phoneBookRepositoryBridge.loadFromPhoneBook()
        }
    }

    private fun initTests() {
        ui.testButtons.visibility = View.VISIBLE
        ui.removeAllButton.setOnClickListener { viewModel.contactsRepository.phoneBookRepositoryBridge.clean() }
        ui.add1000Button.setOnClickListener { viewModel.contactsRepository.phoneBookRepositoryBridge.addTestContacts() }
    }

    private fun setupUI() {
        ui.viewPager.adapter = ContactBookAdapter(requireActivity())

        TabLayoutMediator(ui.viewPagerIndicators, ui.viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.contact_book_contacts_title
                    else -> R.string.contact_book_favorites_title
                }
            )
        }.attach()

        ui.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            (requireActivity() as? HomeActivity)?.setBottomBarVisibility(!hasFocus)
        }

        ui.searchView.setIconifiedByDefault(false)

        ui.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                (ui.viewPager.adapter as ContactBookAdapter).fragments.forEach { it.get()?.search(newText.orEmpty()) }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean = true
        })
    }

    private fun updateSharedState(sharedState: Boolean) {
        if (sharedState) {
            val shareArgs = TariToolbarActionArg(title = string(R.string.common_share)) {
                viewModel.shareSelectedContacts()
            }
            ui.toolbar.setRightArgs(shareArgs)
            val cancelArgs = TariToolbarActionArg(title = string(R.string.common_cancel)) {
                viewModel.sharedState.postValue(false)
            }
            ui.toolbar.setLeftArgs(cancelArgs)
        } else {
            val addContactArg = TariToolbarActionArg(icon = R.drawable.vector_add_contact) {
                viewModel.navigation.postValue(Navigation.ContactBookNavigation.ToAddContact)
            }
            val shareContactArg = TariToolbarActionArg(icon = R.drawable.vector_share) {
                viewModel.sharedState.postValue(true)
            }
            ui.toolbar.setRightArgs(shareContactArg, addContactArg)
        }
    }

    private inner class ContactBookAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

        val fragments = mutableListOf<WeakReference<ContactsFragment>>()

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> ContactsFragment()
            else -> FavoritesFragment()
        }.also { fragments.add(WeakReference(it)) }
    }
}

