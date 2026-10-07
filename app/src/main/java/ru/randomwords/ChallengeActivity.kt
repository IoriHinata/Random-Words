package ru.randomwords
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max
import kotlin.random.Random
class ChallengeActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout;private lateinit var editor:EditText;private lateinit var wordsBox:TextView;private lateinit var info:TextView
 private var config=ChallengeConfig();private var words=listOf<String>();private var seconds=0;private var timer:CountDownTimer?=null;private val h=Handler(Looper.getMainLooper());private var reset=false
 override fun onCreate(b:Bundle?){super.onCreate(b);setup()}
 private fun setup(){
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,20,20,20);setBackgroundColor(Color.parseColor(Ui.BG))};val sc=ScrollView(this);sc.addView(root);setContentView(sc)
  addTitle("НОВОЕ ИСПЫТАНИЕ","Настрой правила. Потом останется только писать.")
  label("КОЛИЧЕСТВО СЛОВ");val count=Spinner(this);count.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("1","2","3","5","7","10"));count.setSelection(2);root.addView(count,lp(52))
  label("ЦЕЛЬ");val goal=Spinner(this);goal.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("100 слов","200 слов","300 слов","2 минуты","5 минут","10 минут","∞ без цели"));root.addView(goal,lp(52))
  label("СЛОЖНОСТЬ ПАУЗЫ");val idle=Spinner(this);idle.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Легко — 6 сек","Норма — 5 сек","Жёстко — 4 сек","Безумие — 3 сек"));idle.setSelection(1);root.addView(idle,lp(52))
  val blind=CheckBox(this).apply{text="Слепой режим — слова исчезают через 5 секунд"};root.addView(blind);val chaos=CheckBox(this).apply{text="Хаос — новое слово каждые 45 секунд"};root.addView(chaos);val boss=CheckBox(this).apply{text="Слово-босс — последнее слово важнее"};root.addView(boss);val repeat=CheckBox(this).apply{text="Повторить предыдущий набор"};root.addView(repeat)
  root.addView(Button(this).apply{text="НАЧАТЬ";setOnClickListener{val c=count.selectedItem.toString().toInt();val g=goal.selectedItem.toString();val gt=when{g.startsWith("∞")->GoalType.ENDLESS;g.contains("мин")->GoalType.TIME;else->GoalType.WORDS};val gv=when{g.startsWith("100")->100;g.startsWith("200")->200;g.startsWith("300")->300;g.startsWith("2")->120;g.startsWith("5")->300;g.startsWith("10")->600;else->0};val sec=when(idle.selectedItemPosition){0->6;1->5;2->4;else->3};config=ChallengeConfig(c,gt,gv,sec,blind.isChecked,chaos.isChecked,boss.isChecked,repeat.isChecked);startChallenge()}},lp(58))
 }
 private fun startChallenge(){
  root.removeAllViews();seconds=0;val bank=Store.words(this);val rnd=Random(System.currentTimeMillis());words=(if(config.repeatWords&&lastWords.isNotEmpty())lastWords else bank.shuffled(rnd).take(config.count)).toMutableList();if(config.boss&&words.isNotEmpty())words=words.dropLast(1)+words.last()+" ★";lastWords=words
  addTitle("ПИШИ","Пауза дольше "+config.idleSeconds+" сек — текст будет сброшен.")
  wordsBox=TextView(this).apply{text=words.joinToString("   ·   ");gravity=Gravity.CENTER;Ui.text(this,22,Ui.GOLD);background=Ui.bg(Ui.CARD,22f);setPadding(18,22,18,22)};root.addView(wordsBox,lp(110))
  info=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,13,Ui.MUTED)};root.addView(info,lp(42))
  editor=EditText(this).apply{hint="Начни писать здесь…";gravity=Gravity.TOP;setTextColor(Color.parseColor(Ui.TEXT));setHintTextColor(Color.parseColor(Ui.MUTED));textSize=18f;setPadding(18,18,18,18);background=Ui.bg("#171920",20f);minHeight=360};root.addView(editor,LinearLayout.LayoutParams(-1,0,1f))
  root.addView(Button(this).apply{text="СОХРАНИТЬ ТЕКСТ";setOnClickListener{finishSuccess()}},lp(58))
  editor.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){if(reset)return;armIdle();updateInfo();if(goalReached())finishSuccess()};override fun afterTextChanged(s:android.text.Editable?){}});editor.requestFocus();armIdle();startGoalTimer()
 }
 private fun updateInfo(){val wc=countWords(editor.text.toString());val missing=words.count{!containsWord(editor.text.toString(),it.replace(" ★",""))};info.text="Слов: "+wc+"  •  Осталось: "+missing+"  •  "+if(config.goalType==GoalType.TIME)"Время: "+format(config.goal-seconds)else if(config.goalType==GoalType.WORDS)"Цель: "+config.goal else "Без цели"}
 private fun armIdle(){h.removeCallbacksAndMessages(null);h.postDelayed({if(editor.text.isNotBlank())timeout()},config.idleSeconds*1000L)}
 private fun timeout(){reset=true;timer?.cancel();editor.setText("");reset=false;AlertDialog.Builder(this).setTitle("Пауза поймала тебя").setMessage("Тишина превысила "+config.idleSeconds+" секунд. Текст уничтожен.").setNegativeButton("Новые слова"){_,_->startChallenge()}.setPositiveButton("Повторить"){_,_->startChallenge()}.show()}
 private fun startGoalTimer(){timer?.cancel();timer=object:CountDownTimer(if(config.goalType==GoalType.TIME)config.goal*1000L else 24*60*60*1000L,1000){override fun onTick(ms:Long){seconds++;updateInfo()};override fun onFinish(){if(config.goalType==GoalType.TIME)finishSuccess()}}.start()}
 private fun goalReached():Boolean=when(config.goalType){GoalType.WORDS->countWords(editor.text.toString())>=config.goal&&words.all{containsWord(editor.text.toString(),it.replace(" ★",""))};else->false}
 private fun finishSuccess(){if(editor.text.isBlank())return;timer?.cancel();h.removeCallbacksAndMessages(null);val text=editor.text.toString();if(!words.all{containsWord(text,it.replace(" ★",""))}){AlertDialog.Builder(this).setTitle("Не все слова использованы").setMessage("Сначала используй каждое ключевое слово.").setPositiveButton("Продолжить",null).show();return};val wc=countWords(text);val base=wc+words.size*10;val mult=when(config.idleSeconds){3->2.0;4->1.5;5->1.25;else->1.0};val score=max(1,(base*mult).toInt());Store.registerSuccess(this,score);if(wc>=100)Store.unlock(this,"100 слов");if(config.idleSeconds<=4)Store.unlock(this,"Без паузы");if(Store.total(this)>=10)Store.unlock(this,"10 текстов");val d=Draft(System.currentTimeMillis(),"Без названия",text,words,System.currentTimeMillis(),seconds,score);Store.saveDraft(this,d);rename(d,score,wc)}
 private fun rename(d:Draft,score:Int,wc:Int){val e=EditText(this);e.hint="Название текста";AlertDialog.Builder(this).setTitle("Текст спасён • "+score+" очков").setMessage("Слов: "+wc+"\nВремя: "+format(seconds)).setView(e).setPositiveButton("Сохранить"){_,_->d.title=if(e.text.isBlank())"Текст "+java.text.SimpleDateFormat("dd.MM.yyyy",java.util.Locale.getDefault()).format(java.util.Date())else e.text.toString();Store.saveDraft(this,d);finish()}.setNegativeButton("Позже"){_,_->finish()}.show()}
 private fun containsWord(text:String,w:String)=text.lowercase().replace(Regex("[^\\p{L}\\s]")," ").split(Regex("\\s+")).contains(w.lowercase())
 private fun countWords(s:String)=s.trim().let{if(it.isEmpty())0 else it.split(Regex("\\s+")).size}
 private fun format(s:Int)="%02d:%02d".format(max(0,s)/60,max(0,s)%60)
 private fun addTitle(a:String,b:String){root.addView(TextView(this).apply{text=a;Ui.text(this,25);setTypeface(typeface,1);setPadding(0,0,0,6)},lp(40));root.addView(TextView(this).apply{text=b;Ui.text(this,13,Ui.MUTED);setPadding(0,0,0,12)},lp(42))}
 private fun label(t:String){root.addView(TextView(this).apply{text=t;Ui.text(this,11,Ui.MUTED);setPadding(4,12,4,4)})}
 private fun lp(h:Int)=LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)}
 companion object{var lastWords=listOf<String>()}
}
