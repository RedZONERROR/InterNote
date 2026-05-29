package com.example.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.NoteRepository

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            val database = AppDatabase.getInstance(application)
            val repository = NoteRepository(application, database.noteDao())
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
