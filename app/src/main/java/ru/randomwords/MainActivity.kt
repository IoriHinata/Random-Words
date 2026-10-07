package ru.randomwords

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    override fun onCreate(b: Bundle?) { super.onCreate(b); render() }
    override fun onResume() { super.onResume(); if (::root.isInitialized) render() }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 28)
            setBackgroundColor(Color.parseColor(Ui.BG))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        scroll.addView(root); setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "RANDOM WORDS  ✦"; Ui.text(this, 13, Ui.GOLD)
            letterSpacing = .16f; setTypeface(typeface, Typeface.BOLD)
        }, lp(28))
        root.addView(TextView(this).apply {
            text = "Пиши быстрее,\nчем думаешь."; Ui.text(this, 36, Ui.TEXT)
            setTypeface(typeface, Typeface.BOLD); setPadding(0, 8, 0, 0)
        }, lp(105))
        root.addView(TextView(this).apply {
            text = "Случайные слова. Жёсткие правила.\nНикаких оправданий для внутреннего критика."
            Ui.text(this, 15, Ui.MUTED)
        }, lp(58))

        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stat(stats, "СЕРИЯ", Store.streak(this).toString())
        stat(stats, "РЕКОРД", Store.best(this).toString())
        stat(stats, "ТЕКСТОВ", Store.total(this).toString())
        root.addView(stats, lp(88))

        actionCard("✦", "НОВОЕ ИСПЫТАНИЕ", "Собери сюжет из случайных слов", Ui.GOLD) {
            startActivity(Intent(this, ChallengeActivity::class.java))
        }
        actionCard("☼", "ЕЖЕДНЕВНЫЙ ВЫЗОВ", "Один и тот же вызов сегодня для тебя", Ui.BLUE) {
            startActivity(Intent(this, ChallengeActivity::class.java).putExtra("daily", true))
        }
        actionCard("▤", "БИБЛИОТЕКА", "Тексты, заметки, папки и экспорт", Ui.TEXT) {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
        actionCard("◆", "СТАТИСТИКА", "Достижения и история писателя", Ui.GOLD) { showStats() }
        actionCard("⚙", "СЛОВАРЬ И НАСТРОЙКИ", "Импорт TXT и параметры игры", Ui.MUTED) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        root.addView(TextView(this).apply {
            text = "«Слова случайны. История — твоя.»"; gravity = Gravity.CENTER
            Ui.text(this, 16, Ui.GOLD); setPadding(8, 30, 8, 6)
        }, lp(64))
    }

    private fun stat(row: LinearLayout, label: String, value: String) {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = Ui.bg(Ui.CARD, 18f)
        }
        c.addView(TextView(this).apply {
            text = value; gravity = Gravity.CENTER; Ui.text(this, 24, Ui.GOLD)
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(-1, 44))
        c.addView(TextView(this).apply {
            text = label; gravity = Gravity.CENTER; Ui.text(this, 9, Ui.MUTED)
            letterSpacing = .12f
        }, LinearLayout.LayoutParams(-1, 24))
        row.addView(c, LinearLayout.LayoutParams(0, 82, 1f).apply { setMargins(4, 0, 4, 0) })
    }

    private fun actionCard(icon: String, title: String, sub: String, accent: String, click: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            background = Ui.bg(Ui.CARD, 22f); setPadding(16, 12, 16, 12)
            setOnClickListener { click() }
        }
        card.addView(TextView(this).apply {
            text = icon; gravity = Gravity.CENTER; Ui.text(this, 25, accent)
            background = Ui.bg("#222530", 16f)
        }, LinearLayout.LayoutParams(54, 54))
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(15, 0, 0, 0) }
        box.addView(TextView(this).apply {
            text = title; Ui.text(this, 16, Ui.TEXT); setTypeface(typeface, Typeface.BOLD)
        })
        box.addView(TextView(this).apply { text = sub; Ui.text(this, 12, Ui.MUTED); setPadding(0, 3, 0, 0) })
        card.addView(box, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(card, lp(82).apply { setMargins(0, 7, 0, 7) })
    }

    private fun showStats() {
        val a = Store.achievements(this)
        val achievements = if (a.isEmpty()) "Пока пусто." else a.sorted().joinToString("  •  ")
        AlertDialog.Builder(this).setTitle("ТВОЙ АРХИВ ПИСАТЕЛЯ")
            .setMessage("Серия: ${Store.streak(this)}\nЛучший счёт: ${Store.best(this)}\nТекстов завершено: ${Store.total(this)}\n\nДостижения (${a.size}):\n$achievements")
            .setPositiveButton("Продолжить", null).show()
    }
    private fun lp(h: Int) = LinearLayout.LayoutParams(-1, h)
}