package ru.randomwords

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@PrivacyPolicyActivity, 20), Ui.dp(this@PrivacyPolicyActivity, 24), Ui.dp(this@PrivacyPolicyActivity, 20), Ui.dp(this@PrivacyPolicyActivity, 24))
            setBackgroundColor(Color.parseColor(Ui.BG))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        scroll.addView(root)
        Ui.applyContentWidth(root, this)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "ПОЛИТИКА КОНФИДЕНЦИАЛЬНОСТИ"
            Ui.text(this, 24, Ui.TEXT)
        }, Ui.spaced(this, 52, 0, 10))

        root.addView(TextView(this).apply {
            text = """
Random Words
Последнее обновление: 8 октября 2026 г.

1. Общая информация
Random Words — офлайн-приложение для писательских испытаний. Основные игровые данные, тексты, заметки, статистика, достижения и словарь хранятся локально на устройстве.

2. Рекламные сервисы
Приложение использует Google Mobile Ads (AdMob) для показа рекламных баннеров. Google и его партнёры могут обрабатывать технические идентификаторы и данные, необходимые для показа, измерения и персонализации рекламы, в соответствии с настройками согласия пользователя и политиками Google. В регионах, где требуется согласие, приложение использует Google User Messaging Platform (UMP).

3. Согласие и настройки рекламы
Пользователь может изменить доступные настройки согласия через раздел «Конфиденциальность» в приложении. Рекламные баннеры размещаются ненавязчиво и не показываются поверх процесса написания текста.

4. Что приложение не делает
Random Words не требует регистрации, не имеет собственного аккаунта пользователя, не хранит тексты на сервере разработчика и не продаёт пользовательские тексты.

5. Файлы
Если пользователь импортирует TXT-файл, файл читается локально для добавления слов в словарь. Приложение не отправляет содержимое импортированного файла на сервер разработчика.

6. Удаление данных
Пользователь может удалить локальные данные приложения через системные настройки Android. Удаление приложения также удаляет локально сохранённые данные.

7. Изменения политики
Эта политика может обновляться при изменении функциональности приложения или требований законодательства.

8. Контакты
По вопросам конфиденциальности используйте контакт разработчика, указанный на странице Random Words в Google Play.
            """.trimIndent()
            Ui.text(this, 14, Ui.TEXT)
            setTextIsSelectable(true)
            movementMethod = LinkMovementMethod.getInstance()
        }, Ui.spaced(this, -2, 0, 0))
    }
}
