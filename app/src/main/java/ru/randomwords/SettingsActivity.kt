package ru.randomwords
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
class SettingsActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 private fun render(){root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,24,20,20);setBackgroundColor(Color.parseColor(Ui.BG))};val sc=ScrollView(this);sc.addView(root);setContentView(sc);root.addView(TextView(this).apply{text="СЛОВАРЬ И НАСТРОЙКИ";Ui.text(this,26);setTypeface(typeface,1)},lp(60));root.addView(TextView(this).apply{text="Слов в локальном словаре: "+Store.words(this@SettingsActivity).size;Ui.text(this,15,Ui.MUTED)},lp(42));btn("Импортировать TXT"){pickTxt()};btn("Восстановить 40 встроенных существительных"){Store.saveWords(this,WordBank.defaults());render()};btn("Показать словарь"){AlertDialog.Builder(this).setTitle("Локальный словарь").setMessage(Store.words(this).joinToString("  ·  ")).setPositiveButton("ОК",null).show()};btn("Сбросить серию"){getSharedPreferences("random_words",0).edit().putInt("streak",0).apply();Toast.makeText(this,"Серия сброшена",Toast.LENGTH_SHORT).show()};root.addView(TextView(this).apply{text="\nРежимы:\n• Слепой — слова исчезают после старта.\n• Хаос — дополнительные ограничения.\n• Слово-босс — последнее слово особенно важно.\n• Ежедневное испытание строится по локальной дате.\n• Все тексты, статистика и словарь хранятся на устройстве.";Ui.text(this,14,Ui.MUTED)},lp(180))}
 private fun btn(t:String,click:()->Unit){root.addView(Button(this).apply{text=t;setOnClickListener{click()}},lp(58))}
 private fun pickTxt(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="text/plain";addCategory(Intent.CATEGORY_OPENABLE)},10)}
 override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==10&&c==Activity.RESULT_OK&&data?.data!=null){try{val text=contentResolver.openInputStream(data.data!!)?.bufferedReader()?.use{it.readText()}?:"";val list=text.split(Regex("[,;\\r\\n\\t]+")).map{it.trim().lowercase()}.filter{it.length in 2..32&&it.matches(Regex("[\\p{L} -]+"))};Store.saveWords(this,list);Toast.makeText(this,"Импортировано слов: "+list.distinct().size,Toast.LENGTH_LONG).show();render()}catch(e:Exception){Toast.makeText(this,"Не удалось прочитать TXT",Toast.LENGTH_LONG).show()}}}
 private fun lp(h:Int)=LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)}
}
