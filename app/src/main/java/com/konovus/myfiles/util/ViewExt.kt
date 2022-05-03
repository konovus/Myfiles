package com.konovus.myfiles.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import java.io.File


inline fun TabLayout.onTabSelectedMyListener(crossinline listener: (TabLayout.Tab?) -> Unit){
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {
            listener(tab)

        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    })
}

inline fun SearchView.onQueryTextChanged(
    searchView: SearchView?,
    crossinline listener: (String) -> Unit
) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            searchView?.clearFocus()
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}

inline fun MenuItem.onActionExpanded( crossinline listener: (String) -> Unit) {
    this.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
            return true
        }

        override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
            listener("")
            return true
        }
    })
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

suspend fun saveDataStore(key: String, value: String, context: Context) {
    val dataStoreKey = stringPreferencesKey(key)
    context.dataStore.edit { settings ->
        settings[dataStoreKey] = value
    }
}

suspend fun readDataStore(key: String, context: Context) : String? {
    val dataStoreKey = stringPreferencesKey(key)
    val preferences = context.dataStore.data.first()
    return preferences[dataStoreKey]
}

suspend fun clearDataStore(key: String, context: Context) {
    val dataStoreKey = stringPreferencesKey(key)
    context.dataStore.edit { settings ->
        settings.remove(dataStoreKey)
    }
}

inline fun myItemTouchHelper(recyclerView: RecyclerView, crossinline listener: (Int) -> Unit) {
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            listener(viewHolder.adapterPosition)
        }
    }).attachToRecyclerView(recyclerView)
}

fun <T> MutableList<T>.removeFromTo(from: Int, to: Int = this.size - 1) {
    val list = mutableListOf<T>()
    this.forEachIndexed { index, it ->
        if (index !in from..to)
            list.add(it)
    }
    this.clear()
    this.addAll(list)
}

fun File.deleteFile(context: Context): Boolean {
    val where = MediaStore.MediaColumns.DATA + "=?"
    val selectionArgs = arrayOf(
        this.absolutePath
    )
    val contentResolver = context.contentResolver
    val filesUri: Uri = MediaStore.Files.getContentUri("external")
    contentResolver.delete(filesUri, where, selectionArgs)
    if (this.exists()) {
        contentResolver.delete(filesUri, where, selectionArgs)
    }
    return !this.exists()
}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024