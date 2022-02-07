package xyz.juncat.databinding

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val textField = ObservableField<String>()
}