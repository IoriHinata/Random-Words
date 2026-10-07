package ru.randomwords
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MiniGamesActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 private val bank by lazy{Store.words(this)}
 override fun onCreate(b:Bundle?){super.onCreate(b);menu()}
 private fun base(title:String,sub:String){
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@MiniGamesActivity,18),Ui.dp(this@MiniGamesActivity,20),Ui.dp(this@MiniGamesActivity,18),Ui.dp(this@MiniGamesActivity,28));setBackgroundColor(Color.parseColor(Ui.BG))}
  val sc=ScrollView(this).apply{isFillViewport=true};sc.addView(root);Ui.applyContentWidth(root,this);setContentView(sc)
  root.addView(TextView(this).apply{text=title;Ui.text(this,27);setTypeface(typeface,Typeface.BOLD)},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,44)))
  root.addView(TextView(this).apply{text=sub;Ui.text(this,13,Ui.MUTED);setMinLines(2);setPadding(0,0,0,Ui.dp(context,18))})
 }
 private fun card(title:String,desc:String,go:()->Unit){
  val c=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=Ui.bg(Ui.CARD,22f);setPadding(Ui.dp(context,18),Ui.dp(context,18),Ui.dp(context,18),Ui.dp(context,18))}
  c.addView(TextView(this).apply{text=title;Ui.text(this,19,Ui.GOLD);setTypeface(typeface,Typeface.BOLD)})
  c.addView(TextView(this).apply{text=desc;Ui.text(this,13,Ui.MUTED);setPadding(0,Ui.dp(context,6),0,Ui.dp(context,12))})
  c.addView(Button(this).apply{text="ИГРАТЬ";minHeight=Ui.dp(context,52);setOnClickListener{go()}})
  root.addView(c,LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT).apply{setMargins(0,Ui.dp(this@MiniGamesActivity,7),0,Ui.dp(this@MiniGamesActivity,7))})
 }
 private fun back(){root.addView(Button(this).apply{text="←  НАЗАД";setOnClickListener{menu()}},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,58)).apply{setMargins(0,Ui.dp(this@MiniGamesActivity,10),0,0)})}
 private fun menu(){
  base("МИНИ-ИГРЫ","Короткие режимы для разогрева. Все три тренируют память, ассоциации и скорость письма.")
  card("01  ЦЕПОЧКА","60 секунд. Дай ассоциацию на слово и переходи к следующему.",::chain)
  card("02  ЗАПРЕТНОЕ СЛОВО","Напиши мини-сцену на 50 слов, не употребив запрещённое слово.",::forbidden)
  card("03  СЛОВОЗАСЕЧКА","Запомни 7 слов за 30 секунд, затем восстанови их без подсказки.",::memory)
  back()
 }
 private fun chain(){
  base("ЦЕПОЧКА","Каждый ход — новая ассоциация. Нельзя повторять предыдущее слово.")
  val word=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,30,Ui.GOLD);background=Ui.bg(Ui.CARD,20f);setPadding(Ui.dp(this@MiniGamesActivity,8),Ui.dp(this@MiniGamesActivity,25),Ui.dp(this@MiniGamesActivity,8),Ui.dp(this@MiniGamesActivity,25))}
  val input=EditText(this).apply{hint="Твоя ассоциация";textSize=20f}
  val info=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,14,Ui.MUTED)}
  root.addView(word,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,100)));root.addView(input,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,70)));root.addView(info,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,55)))
  var current=bank.random();var n=0;word.text=current
  object:CountDownTimer(60000,1000){
   override fun onTick(ms:Long){info.text="Связей: "+n+"  •  "+(ms/1000)+" сек"}
   override fun onFinish(){AlertDialog.Builder(this@MiniGamesActivity).setTitle("Цепочка завершена").setMessage("Связей: "+n).setPositiveButton("Ещё раз"){_,_->chain()}.setNegativeButton("Меню"){_,_->menu()}.show()}
  }.start()
  root.addView(Button(this).apply{text="СЛЕДУЮЩАЯ АССОЦИАЦИЯ";setOnClickListener{val a=input.text.toString().trim();if(a.isEmpty()){info.text="Напиши ассоциацию.";return@setOnClickListener};if(a.equals(current,true)){info.text="Нельзя повторять слово.";return@setOnClickListener};n++;current=bank.filterNot{it.equals(current,true)}.random();word.text=current;input.setText("");info.text="Связей: "+n+"  •  Хороший темп"}},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,58)))
  back()
 }
 private fun forbidden(){
  base("ЗАПРЕТНОЕ СЛОВО","Одно слово вычёркивается из твоего словаря. Напиши сцену и проверь себя.")
  val forbidden=bank.random()
  root.addView(TextView(this).apply{text="НЕ ГОВОРИ:  "+forbidden;gravity=Gravity.CENTER;Ui.text(this,20,Ui.RED);background=Ui.bg("#2A1D22",18f);setPadding(Ui.dp(this@MiniGamesActivity,10),Ui.dp(this@MiniGamesActivity,18),Ui.dp(this@MiniGamesActivity,10),Ui.dp(this@MiniGamesActivity,18))},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,70)))
  val e=EditText(this).apply{hint="Сцена начинается здесь…";gravity=Gravity.TOP;minLines=10;textSize=18f}
  val info=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,14,Ui.MUTED)}
  root.addView(e,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,280)));root.addView(info,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,45)))
  root.addView(Button(this).apply{text="ПРОВЕРИТЬ";setOnClickListener{val n=count(e.text.toString());val bad=e.text.toString().contains(Regex("(?iu)(^|\\W)"+Regex.escape(forbidden)+"(\\W|$)"));when{bad->info.text="Провал: запретное слово найдено.";n<50->info.text="Ещё "+(50-n)+" слов.";else->{info.text="Принято. Чисто: "+n+" слов.";Store.unlock(this@MiniGamesActivity,"Запретное слово")}}}},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,58)))
  back()
 }
 private fun memory(){
  base("СЛОВОЗАСЕЧКА","30 секунд на запоминание. После этого подсказка исчезнет.")
  val set=bank.shuffled(Random(System.currentTimeMillis())).take(7)
  val box=TextView(this).apply{text=set.joinToString("  •  ");gravity=Gravity.CENTER;Ui.text(this,19,Ui.GOLD);background=Ui.bg(Ui.CARD,20f);setPadding(Ui.dp(this@MiniGamesActivity,12),Ui.dp(this@MiniGamesActivity,24),Ui.dp(this@MiniGamesActivity,12),Ui.dp(this@MiniGamesActivity,24))}
  val answer=EditText(this).apply{hint="Слова через пробел или запятую";minLines=3}
  val info=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,14,Ui.MUTED)}
  root.addView(box,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,110)));root.addView(info,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,55)));root.addView(answer,LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,100)))
  object:CountDownTimer(30000,1000){override fun onTick(ms:Long){info.text="Запоминай • "+(ms/1000+1)+" сек"};override fun onFinish(){box.text="••• СЛОВА ИСЧЕЗЛИ •••";info.text="Теперь восстанови 7 слов"}}.start()
  root.addView(Button(this).apply{text="ПРОВЕРИТЬ";setOnClickListener{val got=answer.text.toString().lowercase().split(Regex("[,;\\s]+")).filter{it.isNotBlank()};val hit=set.count{got.contains(it.lowercase())};info.text="Совпадений: "+hit+" / "+set.size;if(hit==set.size)Store.unlock(this@MiniGamesActivity,"Память писателя")}},LinearLayout.LayoutParams(-1,Ui.dp(this@MiniGamesActivity,58)))
  back()
 }
 private fun count(s:String)=s.trim().let{if(it.isBlank())0 else it.split(Regex("\\s+")).size}
}