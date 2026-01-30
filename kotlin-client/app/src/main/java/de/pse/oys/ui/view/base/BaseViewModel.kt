package de.pse.oys.ui.view.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

interface IBaseViewModel {
    fun navigateBack()
}

abstract class BaseViewModel(
    protected val navController: NavController
) : ViewModel(), IBaseViewModel {

    override fun navigateBack() {
        navController.popBackStack()
    }
}