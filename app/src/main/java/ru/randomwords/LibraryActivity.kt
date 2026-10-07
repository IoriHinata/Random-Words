package ru.randomwords
import android.os.Bundle
import android.graphics.Color
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
class LibraryActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 override fun onResume(){super.onResume();if(::root.isInitialized)render()}
 private fun render(){root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,24,20,20);setBackgroundColor(Color.parseColor(Ui.BG))};val sc=ScrollView(this);sc.addView(root);setContentView(sc);root.addView(TextView(this).apply{text="БИБЛИОТЕКА";Ui.text(this,28);setTypeface(typeface,1)},lp(55));root.addView(Button(this).apply{text="+ ПАПКА";setOnClickListener{newFolder()}},lp(52));val list=Store.drafts(this);if(list.isEmpty()){root.addView(TextView(this).apply{text="Здесь появятся законченные тексты.\n\nБиблиотека хранится прямо на устройстве.";Ui.text(this,15,Ui.MUTED);setPadding(20,60,20,60)});return};list.forEach{d->val c=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=Ui.bg(Ui.CARD,20f);setPadding(18,15,18,15);setOnClickListener{open(d)}};c.addView(TextView(this).apply{text=d.title;Ui.text(this,18);setTypeface(typeface,1)});c.addView(TextView(this).apply{text=SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(Date(d.date))+"  •  "+d.text.trim().split(Regex("\\s+")).size+" слов  •  "+d.score+" очков"+(if(d.folder.isBlank())"" else "  •  "+d.folder);Ui.text(this,12,Ui.MUTED)});root.addView(c,lp(80).apply{setMargins(0,6,0,6)})}}
 private fun open(d:Draft){val note=EditText(this).apply{setText(d.note);hint="Заметка к тексту"};val folders=Store.folders(this);val pick=Spinner(this);pick.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,folders.toTypedArray());val current=maxOf(0,folders.indexOf(d.folder));pick.setSelection(current);val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,0,10,0)};box.addView(TextView(this).apply{text="Папка";Ui.text(this,12,Ui.MUTED)});box.addView(pick);box.addView(note);AlertDialog.Builder(this).setTitle(d.title).setMessage(d.text.take(1000)+(if(d.text.length>1000)"…" else "")).setView(box).setPositiveButton("Сохранить"){_,_->d.note=note.text.toString();d.folder=pick.selectedItem.toString();Store.saveDraft(this,d);render()}.setNeutralButton("Переименовать"){_,_->rename(d)}.setNegativeButton("Удалить"){_,_->Store.deleteDraft(this,d.id);render()}.show()}
 private fun rename(d:Draft){val e=EditText(this).apply{setText(d.title)};AlertDialog.Builder(this).setTitle("Название").setView(e).setPositiveButton("Сохранить"){_,_->d.title=e.text.toString();Store.saveDraft(this,d);render()}.setNegativeButton("Отмена",null).show()}
 private fun newFolder(){val e=EditText(this).apply{hint="Название папки"};AlertDialog.Builder(this).setTitle("Новая папка").setView(e).setPositiveButton("Создать"){_,_->Store.addFolder(this,e.text.toString());Toast.makeText(this,"Папка создана",Toast.LENGTH_SHORT).show()}.setNegativeButton("Отмена",null).show()}
 private fun lp(h:Int)=LinearLayout.LayoutParams(-1,h)
}
