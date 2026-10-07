package ru.randomwords
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 override fun onResume(){super.onResume();if(::root.isInitialized)render()}
 private fun render(){
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,28,24,24);setBackgroundColor(Color.parseColor(Ui.BG))}
  val scroll=ScrollView(this);scroll.addView(root);setContentView(scroll)
  val title=TextView(this).apply{text="RANDOM WORDS";Ui.text(this,29);setTypeface(typeface,1)};root.addView(title,lp(48))
  val sub=TextView(this).apply{text="Пиши, пока мысль не остановилась.";Ui.text(this,15,Ui.MUTED)};root.addView(sub,lp(34))
  val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};stat(stats,"СЕРИЯ",Store.streak(this).toString());stat(stats,"РЕКОРД",Store.best(this).toString());stat(stats,"ТЕКСТОВ",Store.total(this).toString());root.addView(stats,lp(84))
  button("✦  НОВОЕ ИСПЫТАНИЕ","Случайные слова → текст"){startActivity(Intent(this,ChallengeActivity::class.java))}
  button("▤  БИБЛИОТЕКА","Сохранённые тексты, заметки и папки"){startActivity(Intent(this,LibraryActivity::class.java))}
  button("◈  СТАТИСТИКА И ДОСТИЖЕНИЯ","Прогресс писателя"){showStats()}
  button("⚙  СЛОВАРЬ И НАСТРОЙКИ","TXT, режимы и параметры"){startActivity(Intent(this,SettingsActivity::class.java))}
  val quote=TextView(this).apply{text="«Слова случайны. История — твоя.»";gravity=Gravity.CENTER;Ui.text(this,16,Ui.GOLD);setPadding(8,35,8,10)};root.addView(quote,lp(80))
 }
 private fun stat(row:LinearLayout,label:String,value:String){val c=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=Ui.bg(Ui.CARD,18f);setPadding(10,8,10,8)};val a=TextView(this).apply{text=value;gravity=Gravity.CENTER;Ui.text(this,24,Ui.GOLD)};val z=TextView(this).apply{text=label;gravity=Gravity.CENTER;Ui.text(this,10,Ui.MUTED)};c.addView(a,LinearLayout.LayoutParams(-1,40));c.addView(z,LinearLayout.LayoutParams(-1,24));row.addView(c,LinearLayout.LayoutParams(0,80,1f).apply{setMargins(4,0,4,0)})}
 private fun button(t:String,s:String,click:()->Unit){val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=Ui.bg(Ui.CARD,22f);setPadding(20,14,20,14);isClickable=true;setOnClickListener{click()}};val a=TextView(this).apply{text=t;Ui.text(this,17,Ui.TEXT);setTypeface(typeface,1)};val z=TextView(this).apply{text=s;Ui.text(this,12,Ui.MUTED)};b.addView(a);b.addView(z);root.addView(b,lp(78).apply{setMargins(0,8,0,0)})}
 private fun showStats(){val ach=Store.achievements(this);AlertDialog.Builder(this).setTitle("Прогресс писателя").setMessage("Серия: "+Store.streak(this)+"\nЛучший счёт: "+Store.best(this)+"\nЗавершено текстов: "+Store.total(this)+"\n\nДостижения: "+if(ach.isEmpty())"пока нет" else ach.joinToString(" · ")).setPositiveButton("Понятно",null).show()}
 private fun lp(h:Int)=LinearLayout.LayoutParams(-1,h)
}
