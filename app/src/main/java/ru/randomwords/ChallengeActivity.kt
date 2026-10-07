package ru.randomwords

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max
import kotlin.random.Random

class ChallengeActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 private lateinit var editor:PasteBlockEditText
 private lateinit var wordsBox:LinearLayout
 private lateinit var info:TextView
 private var config=ChallengeConfig()
 private var words=mutableListOf<String>()
 private var seconds=0
 private var timer:CountDownTimer?=null
 private val h=Handler(Looper.getMainLooper())
 private var reset=false
 private var shield=false
 private var blindHidden=false
 private var lastSentenceWord=""
 private var currentGenre:Genre?=null

 override fun onCreate(b:Bundle?){super.onCreate(b);setup()}

 private fun setup(){
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,24));setBackgroundColor(Color.parseColor(Ui.BG))}
  val sc=ScrollView(this).apply{isFillViewport=true};sc.addView(root);Ui.applyContentWidth(root,this);setContentView(sc)
  addTitle("НОВОЕ ИСПЫТАНИЕ","Основное испытание бесплатно. Особые режимы оплачиваются чернилами.")
  label("РЕЖИМ")
  val mode=Spinner(this)
  mode.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf(
   "Основное — бесплатно","Случайный жанр — 3 чернила","Непопулярный жанр — 4 чернила","Гибрид жанров — 6 чернил",
   "Сюжетный режим — 5 чернил","Проклятое слово — 4 чернила","Босс недели — 7 чернил","Дуэль с рекордом — 5 чернил"))
  root.addView(mode,lp(54))
  val modeInfo=TextView(this).apply{Ui.text(this,13,Ui.MUTED);setPadding(4,4,4,12)}
  root.addView(modeInfo,LinearLayout.LayoutParams(-1,-2))
  fun updateModeInfo(){
   val pos=mode.selectedItemPosition
   val base=when(pos){1->3;2->4;3->6;4->5;5->4;6->7;7->5;else->0}
   modeInfo.text=if(base==0)"Бесплатно • серия не расходуется." else "Цена: ${Store.challengeCost(this,base)} чернил • у тебя ${Store.ink(this)} • серия ${Store.streak(this)} снижает цену."
  }
  mode.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
   override fun onNothingSelected(p:AdapterView<*>?){}
   override fun onItemSelected(p:AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){updateModeInfo()}
  }
  label("КОЛИЧЕСТВО СЛОВ")
  val count=Spinner(this);count.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("1","2","3","5","7","10"));count.setSelection(2);root.addView(count,lp(52))
  label("ЦЕЛЬ")
  val goal=Spinner(this);goal.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("100 слов","200 слов","300 слов","2 минуты","5 минут","10 минут","∞ без цели"));root.addView(goal,lp(52))
  label("СЛОЖНОСТЬ ПАУЗЫ")
  val idle=Spinner(this);idle.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Легко — 6 сек","Норма — 5 сек","Жёстко — 4 сек","Безумие — 3 сек"));idle.setSelection(1);root.addView(idle,lp(52))
  val blind=CheckBox(this).apply{text="Слепой режим — слова исчезают через 5 секунд"}
  val chaos=CheckBox(this).apply{text="Хаос — новое слово каждые 45 секунд"}
  val boss=CheckBox(this).apply{text="Слово-босс — последнее слово даёт бонус"}
  val repeat=CheckBox(this).apply{text="Повторить предыдущий набор"}
  val duel=CheckBox(this).apply{text="Дуэль с рекордом"}
  val last=CheckBox(this).apply{text="Финальный удар — дополнительное слово в последнем предложении"}
  listOf(blind,chaos,boss,repeat,duel,last).forEach{root.addView(it)}
  root.addView(Button(this).apply{text="✦  НАЧАТЬ ИСПЫТАНИЕ";setOnClickListener{
   val c=count.selectedItem.toString().toInt();val g=goal.selectedItem.toString()
   val gt=when{g.startsWith("∞")->GoalType.ENDLESS;g.contains("мин")->GoalType.TIME;else->GoalType.WORDS}
   val gv=when{g.startsWith("100")->100;g.startsWith("200")->200;g.startsWith("300")->300;g.startsWith("2")->120;g.startsWith("5")->300;g.startsWith("10")->600;else->0}
   val sec=when(idle.selectedItemPosition){0->6;1->5;2->4;else->3}
   val pos=mode.selectedItemPosition
   val cm=when(pos){1->ChallengeMode.GENRE;2->ChallengeMode.RARE_GENRE;3->ChallengeMode.HYBRID;4->ChallengeMode.STORY;5->ChallengeMode.CURSED;6->ChallengeMode.BOSS;7->ChallengeMode.DUEL;else->ChallengeMode.STANDARD}
   val base=when(cm){ChallengeMode.STANDARD->0;ChallengeMode.GENRE->3;ChallengeMode.RARE_GENRE->4;ChallengeMode.HYBRID->6;ChallengeMode.STORY->5;ChallengeMode.CURSED->4;ChallengeMode.BOSS->7;ChallengeMode.DUEL->5}
   val cost=Store.challengeCost(this,base)
   if(!Store.spendInk(this,cost)){AlertDialog.Builder(this).setTitle("Недостаточно чернил").setMessage("Нужно ${cost} чернил. Основное испытание всегда бесплатно — сыграй его, чтобы заработать ресурс.").setPositiveButton("Основное"){_,_->mode.setSelection(0)}.setNegativeButton("Отмена",null).show();return@setOnClickListener}
   config=ChallengeConfig(c,gt,gv,sec,blind.isChecked,chaos.isChecked,boss.isChecked,repeat.isChecked,false,duel.isChecked,last.isChecked,cm)
   startChallenge()
  }},lp(60))
  root.addView(Button(this).apply{text="☼  ЕЖЕДНЕВНЫЙ ВЫЗОВ — БЕСПЛАТНО";setOnClickListener{config=ChallengeConfig(3,GoalType.WORDS,100,5,false,false,true,false,true,false,true,ChallengeMode.STANDARD);startChallenge()}},lp(60))
 }

 private fun startChallenge(){
  root.removeAllViews();seconds=0;shield=false;blindHidden=false;lastSentenceWord=""
  val seed=if(config.daily)dateSeed() else System.currentTimeMillis().toInt()
  val rnd=Random(seed)
  currentGenre=when(config.mode){ChallengeMode.GENRE,ChallengeMode.RARE_GENRE,ChallengeMode.HYBRID->GenreBank.random(config.mode,seed);else->null}
  val bank=Store.words(this)
  words=(if(config.repeatWords&&lastWords.isNotEmpty())lastWords.toMutableList()else bank.shuffled(rnd).take(config.count).toMutableList())
  if(config.mode==ChallengeMode.CURSED&&words.isNotEmpty())words.add(bank.filterNot{words.contains(it)}.randomOrNull(rnd)?:"память")
  if(config.boss&&words.isNotEmpty())words[words.lastIndex]=words.last()+" ★"
  lastWords=words.toMutableList()
  val title=when{config.daily->"ЕЖЕДНЕВНЫЙ ВЫЗОВ";config.mode==ChallengeMode.BOSS->"БОСС НЕДЕЛИ";config.mode==ChallengeMode.STORY->"СЮЖЕТНЫЙ РЕЖИМ";config.mode==ChallengeMode.CURSED->"ПРОКЛЯТОЕ СЛОВО";else->"ПИШИ"}
  addTitle(title,"Пауза дольше ${config.idleSeconds} сек — текст будет сброшен.")
  currentGenre?.let{g->addInfoCard("ЖАНР: ${g.name}\n${g.description}")}
  if(config.mode==ChallengeMode.STORY)addInfoCard(storyPrompt(seed))
  wordsBox=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(context,8),Ui.dp(context,8),Ui.dp(context,8),Ui.dp(context,8));background=Ui.bg(Ui.CARD,20f)}
  root.addView(wordsBox,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,Ui.dp(this@ChallengeActivity,8))})
  info=TextView(this).apply{gravity=Gravity.CENTER;Ui.text(this,13,Ui.MUTED);setPadding(0,4,0,4)}
  root.addView(info,LinearLayout.LayoutParams(-1,-2))
  editor=PasteBlockEditText(this).apply{
   hint="Начни писать здесь…";gravity=Gravity.TOP;setTextColor(Color.parseColor(Ui.TEXT));setHintTextColor(Color.parseColor(Ui.MUTED));textSize=18f
   setPadding(Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,18),Ui.dp(this@ChallengeActivity,18))
   background=Ui.bg("#171920",20f);minHeight=Ui.dp(this@ChallengeActivity,300);setSingleLine(false)
   inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
  }
  root.addView(editor,LinearLayout.LayoutParams(-1,Ui.dp(this,320)))
  renderWords()
  root.addView(Button(this).apply{text="СОХРАНИТЬ ТЕКСТ";setOnClickListener{finishSuccess()}},lp(58))
  editor.addTextChangedListener(object:android.text.TextWatcher{
   override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){}
   override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){if(reset)return;armIdle();updateInfo();renderWords();if(goalReached())finishSuccess()}
   override fun afterTextChanged(s:android.text.Editable?){}
  })
  editor.requestFocus();armIdle();startGoalTimer()
  if(config.blind)h.postDelayed({if(!isFinishing&&!editor.text.isBlank()){blindHidden=true;renderWords()}},5000)
 }

 private fun addInfoCard(text:String){root.addView(TextView(this).apply{this.text=text;Ui.text(this,14,Ui.GOLD);background=Ui.bg(Ui.CARD,18f);setPadding(Ui.dp(context,14),Ui.dp(context,12),Ui.dp(context,14),Ui.dp(context,12))},LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,Ui.dp(this@ChallengeActivity,10))})}
 private fun storyPrompt(seed:Int):String{
  val r=Random(seed);val hero=listOf("курьер","архивист","бывший следователь","подросток","ночной сторож").random(r)
  val place=listOf("пустой вокзал","дом без окон","заброшенная библиотека","ночной автобус","деревня у леса").random(r)
  val secret=listOf("он уже видел это место","время здесь идёт иначе","пропавший человек оставил письмо","никто не помнит героя","главная улика находится у него в кармане").random(r)
  return "СЮЖЕТ: ${hero} оказывается в месте «${place}». Секрет: ${secret}. Напиши историю так, чтобы ответ не был объяснён полностью."
 }
 private fun renderWords(){
  if(!::wordsBox.isInitialized||!::editor.isInitialized)return
  wordsBox.removeAllViews()
  if(config.blind&&blindHidden){wordsBox.addView(TextView(this).apply{text="•••  СЛОВА СКРЫТЫ  •••";gravity=Gravity.CENTER;Ui.text(this,16,Ui.MUTED)},LinearLayout.LayoutParams(-1,Ui.dp(this,48)));return}
  var row:LinearLayout?=null
  words.forEachIndexed{idx,w->
   if(idx%2==0){row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};wordsBox.addView(row,LinearLayout.LayoutParams(-1,Ui.dp(this,56)))}
   val clean=w.replace(" ★","");val used=containsWord(editor.text.toString(),clean)
   val tv=TextView(this).apply{text=clean+if(w.endsWith(" ★"))" ★" else "";gravity=Gravity.CENTER;Ui.text(this,14,if(used)Ui.GREEN else if(w.endsWith(" ★"))Ui.GOLD else Ui.TEXT);setTypeface(typeface,if(used)Typeface.BOLD else Typeface.NORMAL);background=Ui.bg(if(used)"#213528" else Ui.CARD2,14f);setPadding(Ui.dp(context,7),Ui.dp(context,8),Ui.dp(context,7),Ui.dp(context,8))}
   row?.addView(tv,LinearLayout.LayoutParams(0,Ui.dp(this,48),1f).apply{setMargins(Ui.dp(this@ChallengeActivity,3),4,Ui.dp(this@ChallengeActivity,3),4)})
  }
 }
 private fun updateInfo(){if(!::editor.isInitialized||!::info.isInitialized)return;val wc=countWords(editor.text.toString());val used=words.count{containsWord(editor.text.toString(),it.replace(" ★",""))};val goal=if(config.goalType==GoalType.TIME)"Время: "+format(config.goal-seconds)else if(config.goalType==GoalType.WORDS)"Цель: "+config.goal else "Без цели";info.text="Слов: ${wc} • Ключевых слов: ${used}/${words.size} • ${goal} • Чернила: ${Store.ink(this)}"}
 private fun armIdle(){h.removeCallbacksAndMessages(null);h.postDelayed({if(editor.text.isNotBlank())timeout()},config.idleSeconds*1000L)}
 private fun timeout(){Store.breakStreak(this);val saved=editor.text.toString();reset=true;editor.setText("");reset=false;timer?.cancel();AlertDialog.Builder(this).setTitle("Пауза поймала тебя").setMessage("Тишина превысила ${config.idleSeconds} секунд. Текст удалён. Серия сброшена.").setNegativeButton("Новые слова"){_,_->startChallenge()}.setPositiveButton("Последний шанс"){_,_->shield=true;editor.setText(saved);editor.setSelection(editor.text.length);armIdle()}.show()}
 private fun startGoalTimer(){timer?.cancel();timer=object:CountDownTimer(if(config.goalType==GoalType.TIME)config.goal*1000L else 24*60*60*1000L,1000){override fun onTick(ms:Long){seconds++;if(config.chaos&&seconds%45==0)addChaosWord();updateInfo()};override fun onFinish(){if(config.goalType==GoalType.TIME)finishSuccess()}}.start()}
 private fun addChaosWord(){val extra=Store.words(this).filterNot{words.contains(it)}.shuffled().firstOrNull()?:return;words.add(extra);renderWords();Toast.makeText(this,"ХАОС: добавлено слово «${extra}»",Toast.LENGTH_SHORT).show()}
 private fun goalReached():Boolean{
  if(config.goalType!=GoalType.WORDS||countWords(editor.text.toString())<config.goal)return false
  if(words.any{!containsWord(editor.text.toString(),it.replace(" ★",""))})return false
  if(config.lastSentence&&lastSentenceWord.isBlank()){lastSentenceWord=Store.words(this).filterNot{words.contains(it)}.randomOrNull()?:"финал";words.add(lastSentenceWord);renderWords();Toast.makeText(this,"ФИНАЛ: последнее предложение должно содержать «${lastSentenceWord}»",Toast.LENGTH_LONG).show();return false}
  return true
 }
 private fun finishSuccess(){
  if(config.goalType==GoalType.WORDS&&!goalReached())return
  if(editor.text.isBlank())return
  timer?.cancel();h.removeCallbacksAndMessages(null)
  val text=editor.text.toString();val cleanWords=words.map{it.replace(" ★","")}
  if(cleanWords.any{!containsWord(text,it)}){AlertDialog.Builder(this).setTitle("Не все слова использованы").setMessage("Используй каждое ключевое слово.").setPositiveButton("Продолжить",null).show();return}
  if(config.lastSentence&&lastSentenceWord.isNotBlank()){val last=text.split(Regex("[.!?]+")).map{it.trim()}.filter{it.isNotBlank()}.lastOrNull()?.lowercase()?:"";if(!last.contains(lastSentenceWord.lowercase())){AlertDialog.Builder(this).setTitle("Финальный удар не принят").setMessage("Последнее предложение должно содержать «${lastSentenceWord}».").setPositiveButton("Продолжить",null).show();return}}
  val wc=countWords(text);val base=wc+cleanWords.size*10+(if(config.boss)25 else 0)+(if(config.mode==ChallengeMode.RARE_GENRE)20 else 0)+(if(config.mode==ChallengeMode.HYBRID)30 else 0)
  val difficulty=when(config.idleSeconds){3->2.0;4->1.5;5->1.25;else->1.0};val combo=1.0+minOf(1.0,Store.streak(this)*0.1);val score=max(1,(base*difficulty*combo).toInt())
  if(config.duel&&score<=Store.best(this)){AlertDialog.Builder(this).setTitle("Рекорд устоял").setMessage("Счёт: ${score}\nЛучший: ${Store.best(this)}").setPositiveButton("Ещё раз"){_,_->startChallenge()}.setNegativeButton("В меню"){_,_->finish()}.show();return}
  val bonusInk=when(config.mode){ChallengeMode.RARE_GENRE->2;ChallengeMode.HYBRID->3;ChallengeMode.BOSS->3;else->0}
  Store.rewardSuccess(this,score,wc,bonusXp=if(config.mode!=ChallengeMode.STANDARD)25 else 0,bonusInk=bonusInk)
  if(config.idleSeconds<=4)Store.unlock(this,"Без паузы")
  if(config.blind)Store.unlock(this,"Слепой режим")
  if(config.chaos)Store.unlock(this,"Хаос")
  if(config.boss)Store.unlock(this,"Слово-босс")
  if(config.mode==ChallengeMode.CURSED)Store.unlock(this,"Проклятое слово")
  if(config.mode==ChallengeMode.RARE_GENRE)Store.unlock(this,"Редкий жанр")
  if(config.mode==ChallengeMode.HYBRID)Store.unlock(this,"Гибрид")
  if(config.mode==ChallengeMode.BOSS)Store.unlock(this,"Босс недели")
  if(config.duel)Store.unlock(this,"Дуэль")
  if(config.lastSentence)Store.unlock(this,"Финальный удар")
  if(config.daily)Store.unlock(this,"Ежедневный вызов")
  val d=Draft(System.currentTimeMillis(),"Без названия",text,cleanWords,System.currentTimeMillis(),seconds,score)
  showAnalysis(d,score,wc)
 }
 private fun showAnalysis(d:Draft,score:Int,wc:Int){
  val sentences=d.text.split(Regex("[.!?]+")).map{it.trim()}.filter{it.isNotBlank()};val chars=d.text.count{it.isLetter()};val avg=if(wc==0)0.0 else chars.toDouble()/wc;val longest=sentences.maxByOrNull{it.length}?.length?:0
  val message="Счёт: ${score}\nСлов: ${wc}\nПредложений: ${sentences.size}\nСредняя длина слова: ${String.format(java.util.Locale.US,"%.1f",avg)}\nСамое длинное предложение: ${longest} символов\n\nXP и чернила начислены.\nСерия: ${Store.streak(this)} • Уровень: ${Store.level(this)} • Чернила: ${Store.ink(this)}"
  val e=EditText(this);e.hint="Название текста"
  AlertDialog.Builder(this).setTitle("Текст завершён").setMessage(message).setView(e).setPositiveButton("В библиотеку"){_,_->d.title=if(e.text.isBlank())"Текст "+java.text.SimpleDateFormat("dd.MM.yyyy",java.util.Locale.getDefault()).format(java.util.Date())else e.text.toString();Store.saveDraft(this,d);finish()}.setNeutralButton("Сохранить без названия"){_,_->Store.saveDraft(this,d);finish()}.show()
 }
 private fun containsWord(text:String,w:String)=text.lowercase().replace(Regex("[^\\p{L}\\s]")," ").split(Regex("\\s+")).contains(w.lowercase())
 private fun countWords(s:String)=s.trim().let{if(it.isEmpty())0 else it.split(Regex("\\s+")).size}
 private fun format(s:Int)="%02d:%02d".format(max(0,s)/60,max(0,s)%60)
 private fun dateSeed()=java.text.SimpleDateFormat("yyyyMMdd",java.util.Locale.US).format(java.util.Date()).hashCode()
 private fun addTitle(a:String,b:String){root.addView(TextView(this).apply{text=a;Ui.text(this,25);setTypeface(typeface,Typeface.BOLD);setPadding(0,0,0,6)},lp(40));root.addView(TextView(this).apply{text=b;Ui.text(this,13,Ui.MUTED);setPadding(0,0,0,12)},lp(42))}
 private fun label(t:String){root.addView(TextView(this).apply{text=t;Ui.text(this,11,Ui.MUTED);setPadding(4,12,4,4)})}
 private fun lp(h:Int)=Ui.spaced(this,h,5,5)
 companion object{var lastWords=listOf<String>()}
}
