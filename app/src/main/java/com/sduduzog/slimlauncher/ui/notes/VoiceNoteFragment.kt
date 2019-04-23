package com.sduduzog.slimlauncher.ui.notes

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.data.MainViewModel
import com.sduduzog.slimlauncher.data.model.Note
import com.sduduzog.slimlauncher.dialogs.RenameNoteDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import kotlinx.android.synthetic.main.voice_note_fragment.*
import java.io.File


class VoiceNoteFragment : BaseFragment() {

    override fun getFragmentView(): ViewGroup = voice_note_fragment

    private lateinit var viewModel: MainViewModel
    private var mediaPlayer: MediaPlayer? = null
    private val customHandler = Handler()
    private var timeInMilliseconds = 0L
    private var startTime = 0L
    private var updateTime = 0L
    private var pauseTime = 0L
    private val updateTimerThread = object : Runnable {
        override fun run() {
            try {
                timeInMilliseconds = SystemClock.uptimeMillis()
                updateTime = (timeInMilliseconds + pauseTime) - startTime
                val time = DateUtils.formatElapsedTime(updateTime / 1000)
                voice_note_fragment_counter.text = getString(R.string.notes_fragment_counter, time)
                customHandler.postDelayed(this, 10)
            } finally {
                // Do something
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.voice_note_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        } ?: throw Error("This is just dumb")
        var id = 0L
        arguments.let {
            id = if (it != null && it.containsKey(getString(R.string.nav_key_note))) {
                it.getLong(getString(R.string.nav_key_note))
            } else throw Exception("How did we get here??")
        }
        viewModel.notes.observe(this, Observer {
            it?.let { notes ->
                notes.firstOrNull { n -> n.id == id }?.let { note -> loadNote(note) }
            }
        })
        voice_note_fragment_play.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                pauseTimer()
                voice_note_fragment_play.text = getString(R.string.voice_note_fragment_play)
            } else {
                mediaPlayer?.start()
                startTimer()
                voice_note_fragment_play.text = getString(R.string.voice_note_fragment_pause)
            }
        }
        voice_note_fragment_stop.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            stopTimer()
            showDuration()
            voice_note_fragment_play.text = getString(R.string.voice_note_fragment_play)
        }
    }

    private fun startTimer() {
        startTime = SystemClock.uptimeMillis()
        customHandler.postDelayed(updateTimerThread, 0)
    }

    private fun pauseTimer() {
        pauseTime = updateTime
        customHandler.removeCallbacks(updateTimerThread)
    }

    private fun stopTimer() {
        pauseTime = 0L
        customHandler.removeCallbacks(updateTimerThread)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    private fun loadNote(note: Note) {
        note.title?.let { title -> if (title.isNotBlank()) voice_note_fragment_title.text = title }
        voice_note_fragment_body.text = note.body
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            stopTimer()
            showDuration()
            voice_note_fragment_play.text = getString(R.string.voice_note_fragment_play)
        }
        val imagePath = File(context!!.filesDir, getString(R.string.audio_file_path))
        val newFile = File(imagePath, note.filename)
        Log.d("check", newFile.absolutePath)
        mediaPlayer?.setDataSource(newFile.absolutePath)
        mediaPlayer?.prepare()
        showDuration()
        voice_note_fragment_options.setOnClickListener { showPopupMenu(it, note) }
    }

    private fun showDuration() {
        mediaPlayer?.let {
            val duration = it.duration
            voice_note_fragment_counter.text = DateUtils.formatElapsedTime(duration.toLong() / 1000)
        }
    }


    private fun showPopupMenu(view: View, note: Note) {
        stopTimer()
        showDuration()
        voice_note_fragment_play.text = getString(R.string.voice_note_fragment_play)
        val popup = PopupMenu(context!!, view)
        popup.menuInflater.inflate(R.menu.voice_note_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            when {
                it.itemId == R.id.vn_menu_rename -> RenameNoteDialog.getInstance(note, viewModel)
                        .show(fragmentManager, "note_dialog")
                it.itemId == R.id.vn_menu_share -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    val imagePath = File(context!!.filesDir, getString(R.string.audio_file_path))
                    val newFile = File(imagePath, note.filename)
                    val contentUri = FileProvider.getUriForFile(context!!, getString(R.string.app_authority_for_file_provider), newFile)
                    val type = context!!.contentResolver.getType(contentUri)
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    intent.type = type
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "Send audio note"))
                }
                it.itemId == R.id.vn_menu_delete -> {
                    Navigation.findNavController(voice_note_fragment).popBackStack()
                    context!!.deleteFile(note.filename)
                    viewModel.remove(note)
                }
            }
            true
        }
        popup.show()
    }
}