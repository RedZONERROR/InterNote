package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entities.Note

class NoteListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NoteListRemoteViewsFactory(applicationContext)
    }
}

class NoteListRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var notes: List<Note> = emptyList()

    override fun onCreate() {
        // Initial setup
    }

    override fun onDataSetChanged() {
        // Fetch active non-deleted non-archived notes synchronously in widget background threat
        try {
            val db = AppDatabase.getInstance(context)
            notes = db.noteDao().getActiveNotesDirect()
        } catch (e: Exception) {
            e.printStackTrace()
            notes = emptyList()
        }
    }

    override fun onDestroy() {
        notes = emptyList()
    }

    override fun getCount(): Int = notes.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= notes.size) return null
        
        val note = notes[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_note_item)

        // Set text properties
        rv.setTextViewText(R.id.txt_widget_note_title, if (note.title.isBlank()) "Untitled Note" else note.title)
        rv.setTextViewText(R.id.txt_widget_note_content, note.content)

        // Create fill-in intent containing specified note details to launch inside template
        val fillInIntent = Intent().apply {
            putExtra("action_trigger_open_note", true)
            putExtra("EXTRA_NOTE_ID", note.id)
        }
        rv.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position < notes.size) notes[position].id.toLong() else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
