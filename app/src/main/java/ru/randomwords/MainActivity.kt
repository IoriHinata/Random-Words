package ru.randomwords

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity:AppCompatActivity(){
 private lateinit var root:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b);render();Ads.requestConsentAndInitialize(this){addBannerIfNeeded()}}
 override fun onResume(){super.onResume();if(::root.isInitialized)render()}
 private fun render(){
  root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@MainActivity,20),Ui.dp(this@MainActivity,24),Ui.dp(this@MainActivity,20),Ui.dp(this@MainActivity,28));setBackgroundColor(Color.parseColor(Ui.BG))}
  val scroll=ScrollView(this).apply{isFillViewport=true};scroll.addView(root);Ui.applyContentWidth(root,this);setContentView(scroll)
  root.addView(TextView(this).apply{text="RANDOM WORDS  ✦";Ui.text(this,13,Ui.GOLD);letterSpacing=.16f;setTypeface(typeface,Typeface.BOLD)},Ui.lp(this,30))
  root.addView(TextView(this).apply{text="Пиши быстрее,\nчем думаешь.";Ui.text(this,36,Ui.TEXT);setTypeface(typeface,Typeface.BOLD);setPadding(0,Ui.dp(this@MainActivity,8),0,0)},Ui.lp(this,112))
  root.addView(TextView(this).apply{text="Слова случайны. Испытания — нет.";Ui.text(this,15,Ui.MUTED)},Ui.lp(this,50))
  val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=Ui.bg(Ui.CARD,22f);setPadding(Ui.dp(context,16),Ui.dp(context,14),Ui.dp(context,16),Ui.dp(context,14))}
  hero.addView(TextView(this).apply{text="🔥  СЕРИЯ ${Store.streak(this@MainActivity)} ДНЕЙ   •   УРОВЕНЬ ${Store.level(this@MainActivity)}";Ui.text(this,13,Ui.GOLD);setTypeface(typeface,Typeface.BOLD)})
  hero.addView(TextView(this).apply{text="${Store.xpIntoLevel(this@MainActivity)} / 100 XP   •   ${Store.rank(this@MainActivity)}   •   ✦ ${Store.ink(this@MainActivity)} ЧЕРНИЛ";Ui.text(this,16,Ui.TEXT);setPadding(0,Ui.dp(context,8),0,Ui.dp(context,6))})
  hero.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=100;progress=Store.xpIntoLevel(this@MainActivity).toInt()})
  hero.addView(TextView(this).apply{text="Книга: ${Store.bookWords(this@MainActivity)} слов   •   Сезон: день ${Store.seasonDay(this@MainActivity)}/30   •   Рекорд: ${Store.best(this@MainActivity)}";Ui.text(this,11,Ui.MUTED);setPadding(0,Ui.dp(context,8),0,0)})
  root.addView(hero,Ui.spaced(this,112,8,8))
  actionCard("✦","НОВОЕ ИСПЫТАНИЕ","Основное испытание всегда бесплатно",Ui.GOLD){startActivity(Intent(this,ChallengeActivity::class.java))}
  actionCard("☼","ЕЖЕДНЕВНЫЙ ВЫЗОВ","Один детерминированный вызов на сегодня",Ui.BLUE){startActivity(Intent(this,ChallengeActivity::class.java).putExtra("daily",true))}
  actionCard("👁","БОСС НЕДЕЛИ","Сложный режим с особой наградой",Ui.RED){startActivity(Intent(this,ChallengeActivity::class.java).putExtra("boss",true))}
  actionCard("♟","МИНИ-ИГРЫ","Память, ассоциации и скорость",Ui.GREEN){startActivity(Intent(this,MiniGamesActivity::class.java))}
  actionCard("▤","БИБЛИОТЕКА","Тексты, заметки, папки и экспорт",Ui.TEXT){startActivity(Intent(this,LibraryActivity::class.java))}
  actionCard("◆","ДОСТИЖЕНИЯ","${Store.achievements(this).size} открыто • ${Store.allAchievements().size} всего",Ui.GOLD){showAchievements()}
  actionCard("📖","МОЯ КНИГА","Глобальная цель: 100 000 слов",Ui.BLUE){showBook()}
  actionCard("⚙","СЛОВАРЬ И НАСТРОЙКИ","Импорт TXT и параметры игры",Ui.MUTED){startActivity(Intent(this,SettingsActivity::class.java))}
  actionCard("◌","КОНФИДЕНЦИАЛЬНОСТЬ","Настройки согласия на рекламу",Ui.MUTED){Ads.showPrivacyOptions(this)}
  root.addView(TextView(this).apply{text="«Слова случайны. История — твоя.»";gravity=Gravity.CENTER;Ui.text(this,16,Ui.GOLD);setPadding(Ui.dp(this@MainActivity,8),Ui.dp(this@MainActivity,26),Ui.dp(this@MainActivity,8),Ui.dp(this@MainActivity,6))},Ui.lp(this,70))
 }
 private fun addBannerIfNeeded(){
  if(!::root.isInitialized || root.findViewWithTag<AdView>("main_banner")!=null)return
  val adView=AdView(this).apply{
   tag="main_banner"
   adUnitId=Ads.TEST_BANNER_ID
   val widthDp=(resources.displayMetrics.widthPixels/resources.displayMetrics.density).toInt()
   setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this@MainActivity,widthDp))
  }
  root.addView(adView,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply{
   topMargin=Ui.dp(this@MainActivity,12);bottomMargin=Ui.dp(this@MainActivity,8)
  })
  adView.loadAd(AdRequest.Builder().build())
 }
 private fun actionCard(icon:String,title:String,sub:String,accent:String,click:()->Unit){
  val card=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;background=Ui.bg(Ui.CARD,22f);setPadding(Ui.dp(this@MainActivity,16),Ui.dp(this@MainActivity,14),Ui.dp(this@MainActivity,16),Ui.dp(this@MainActivity,14));setOnClickListener{click()}}
  card.addView(TextView(this).apply{text=icon;gravity=Gravity.CENTER;Ui.text(this,25,accent);background=Ui.bg("#222530",16f)},LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,54)))
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@MainActivity,15),0,0,0)}
  box.addView(TextView(this).apply{text=title;Ui.text(this,16,Ui.TEXT);setTypeface(typeface,Typeface.BOLD)})
  box.addView(TextView(this).apply{text=sub;Ui.text(this,12,Ui.MUTED);setPadding(0,Ui.dp(this@MainActivity,4),0,0)})
  card.addView(box,LinearLayout.LayoutParams(0,-2,1f));root.addView(card,Ui.spaced(this,84,7,7))
 }
 private fun showAchievements(){
  val unlocked=Store.achievements(this);val all=Store.allAchievements()
  val text=all.map{name->if(unlocked.contains(name))"✓ $name" else if(name.startsWith("Тайное"))"🔒 Скрытое достижение" else"○ $name"}.joinToString("\n")
  AlertDialog.Builder(this).setTitle("ДОСТИЖЕНИЯ  ${unlocked.size}/${all.size}").setMessage(text).setPositiveButton("Продолжить",null).show()
 }
 private fun showBook(){
  val n=Store.bookWords(this);val pct=(n/1000).coerceAtMost(100)
  AlertDialog.Builder(this).setTitle("МОЯ КНИГА").setMessage("Накоплено: $n / 100 000 слов\nПрогресс: $pct%\n\nКаждый завершённый текст добавляет слова в общий объём. На 1 000 / 10 000 / 50 000 / 100 000 слов открываются достижения.").setPositiveButton("Продолжить",null).show()
 }
}
