package ru.randomwords

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LibraryActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private var folderFilter = "Все тексты"
    private var pendingExport: Draft? = null

    override fun onCreate(b: Bundle?) { super.onCreate(b); render() }
    override fun onResume() { super.onResume(); if (::root.isInitialized) render() }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@LibraryActivity,20),Ui.dp(this@LibraryActivity,22),Ui.dp(this@LibraryActivity,20),Ui.dp(this@LibraryActivity,24))
            setBackgroundColor(Color.parseColor(Ui.BG))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        scroll.addView(root); Ui.applyContentWidth(root,this); setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "БИБЛИОТЕКА"; Ui.text(this, 28); setTypeface(typeface, Typeface.BOLD)
        }, lp(50))
        root.addView(TextView(this).apply {
            text = "Твои тексты остаются на устройстве."; Ui.text(this, 13, Ui.MUTED)
        }, lp(32))

        val folders = Store.folders(this)
        val filter = Spinner(this).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item,
                (listOf("Все тексты") + folders).toTypedArray())
            setSelection(if (folderFilter == "Все тексты") 0 else maxOf(0, folders.indexOf(folderFilter) + 1))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                    val selected = parent?.getItemAtPosition(pos)?.toString() ?: "Все тексты"
                    if (selected != folderFilter) { folderFilter = selected; render() }
                }
            }
        }
        root.addView(filter, lp(52))
        root.addView(Button(this).apply {
            text = "+  НОВАЯ ПАПКА"; setOnClickListener { newFolder() }
        }, lp(52))

        val all = Store.drafts(this)
        val list = if (folderFilter == "Все тексты") all else all.filter { it.folder == folderFilter }
        if (list.isEmpty()) {
            root.addView(TextView(this).apply {
                text = "Пока пусто.\n\nЗакончи испытание — и текст появится здесь."
                gravity = Gravity.CENTER; Ui.text(this, 15, Ui.MUTED)
                setPadding(Ui.dp(this@LibraryActivity,20),Ui.dp(this@LibraryActivity,65),Ui.dp(this@LibraryActivity,20),Ui.dp(this@LibraryActivity,65))
            }, lp(190))
        } else list.forEach { card(it) }
    }

    private fun card(d: Draft) {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = Ui.bg(Ui.CARD, 20f)
            setPadding(Ui.dp(this@LibraryActivity,17),Ui.dp(this@LibraryActivity,14),Ui.dp(this@LibraryActivity,17),Ui.dp(this@LibraryActivity,14)); setOnClickListener { open(d) }
        }
        c.addView(TextView(this).apply {
            text = d.title; Ui.text(this, 18); setTypeface(typeface, Typeface.BOLD)
        })
        val wc = d.text.trim().split(Regex("\\s+")).let { if (it.size == 1 && it[0].isBlank()) 0 else it.size }
        c.addView(TextView(this).apply {
            text = "${date(d.date)}  •  $wc слов  •  ${d.score} очков" +
                    if (d.folder.isBlank()) "" else "  •  ${d.folder}"
            Ui.text(this, 12, Ui.MUTED); setPadding(0, 5, 0, 0)
        })
        root.addView(c, lp(80).apply { setMargins(0, 6, 0, 6) })
    }

    private fun open(d: Draft) {
        val note = EditText(this).apply { setText(d.note); hint = "Заметка к тексту" }
        val folders = Store.folders(this)
        val pick = Spinner(this).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, folders.toTypedArray())
            setSelection(maxOf(0, folders.indexOf(d.folder)))
        }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 0, 8, 0) }
        box.addView(TextView(this).apply { text = "ПАПКА"; Ui.text(this, 10, Ui.MUTED); letterSpacing = .12f })
        box.addView(pick); box.addView(note)

        AlertDialog.Builder(this).setTitle(d.title)
            .setMessage(d.text.take(1800) + if (d.text.length > 1800) "…" else "")
            .setView(box)
            .setPositiveButton("СОХРАНИТЬ") { _, _ ->
                d.note = note.text.toString()
                d.folder = pick.selectedItem?.toString().orEmpty()
                Store.saveDraft(this, d); render()
            }
            .setNeutralButton("ЭКСПОРТ TXT") { _, _ -> export(d) }
            .setNegativeButton("УДАЛИТЬ") { _, _ -> Store.deleteDraft(this, d.id); render() }
            .show()
    }

    private fun rename(d: Draft) {
        val e = EditText(this).apply { setText(d.title); selectAll() }
        AlertDialog.Builder(this).setTitle("НАЗВАНИЕ ТЕКСТА").setView(e)
            .setPositiveButton("Сохранить") { _, _ ->
                d.title = e.text.toString().ifBlank { d.title }
                Store.saveDraft(this, d); render()
            }
            .setNegativeButton("Отмена", null).show()
    }

    private fun newFolder() {
        val e = EditText(this).apply { hint = "Например: хоррор / идеи / романы" }
        AlertDialog.Builder(this).setTitle("НОВАЯ ПАПКА").setView(e)
            .setPositiveButton("Создать") { _, _ ->
                Store.addFolder(this, e.text.toString()); render()
            }
            .setNegativeButton("Отмена", null).show()
    }

    private fun export(d: Draft) {
        pendingExport = d
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "${d.title}.txt")
        }, 77)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == Activity.RESULT_OK && data?.data != null) {
            val d = pendingExport ?: return
            try {
                contentResolver.openOutputStream(data.data!!)?.bufferedWriter()?.use { it.write(d.text) }
                Toast.makeText(this, "Текст экспортирован", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(this, "Не удалось сохранить файл", Toast.LENGTH_LONG).show()
            }
            pendingExport = null
        }
    }

    private fun date(ms: Long) = java.text.SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault()).format(Date(ms))
    private fun lp(h: Int) = Ui.spaced(this,h,5,5)
}