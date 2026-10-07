package ru.randomwords

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

object Store {
    private const val PREF="random_words"
    private const val DRAFTS="drafts"; private const val WORDS="words"; private const val FOLDERS="folders"
    private const val STREAK="streak"; private const val BEST_STREAK="best_streak"; private const val BEST="best"; private const val TOTAL="total"
    private const val ACH="achievements"; private const val XP="xp"; private const val INK="ink"; private const val BOOK="book_words"
    private const val LAST_DAY="last_day"; private const val SEASON_START="season_start"; private const val MARATHON="marathon"
    private const val LAST_BAILOUT_DAY="last_bailout_day"
    private fun p(c:Context)=c.getSharedPreferences(PREF,0)

    fun words(c:Context):MutableList<String>{
        val raw=p(c).getString(WORDS,null)
        if(raw==null)return WordBank.defaults().toMutableList()
        return try {
            val a=JSONArray(raw)
            val saved=MutableList(a.length()){a.getString(it)}
            if(saved.size<1000)(saved+WordBank.defaults()).distinct().toMutableList() else saved
        } catch(_:Exception){WordBank.defaults().toMutableList()}
    }
    fun saveWords(c:Context,list:List<String>){val a=JSONArray();list.map{it.trim().lowercase()}.filter{it.isNotBlank()}.distinct().forEach{a.put(it)};p(c).edit().putString(WORDS,a.toString()).apply()}
    fun folders(c:Context):MutableList<String>{val raw=p(c).getString(FOLDERS,null)?:return mutableListOf("Без папки");val a=JSONArray(raw);return MutableList(a.length()){a.getString(it)}}
    fun addFolder(c:Context,name:String){val f=folders(c);val clean=name.trim();if(clean.isNotBlank()&&!f.contains(clean)){f.add(clean);val a=JSONArray();f.forEach{a.put(it)};p(c).edit().putString(FOLDERS,a.toString()).apply()}}
    fun drafts(c:Context):MutableList<Draft>{
        val a=JSONArray(p(c).getString(DRAFTS,"[]"))
        return MutableList(a.length()){i->val o=a.getJSONObject(i);val w=o.optJSONArray("words")?:JSONArray();Draft(o.getLong("id"),o.getString("title"),o.getString("text"),MutableList(w.length()){j->w.getString(j)},o.getLong("date"),o.getInt("seconds"),o.getInt("score"),o.optString("note"),o.optString("folder"))}
    }
    private fun writeDrafts(c:Context,list:List<Draft>){val a=JSONArray();list.forEach{x->a.put(JSONObject().apply{put("id",x.id);put("title",x.title);put("text",x.text);put("words",JSONArray(x.words));put("date",x.date);put("seconds",x.seconds);put("score",x.score);put("note",x.note);put("folder",x.folder)})};p(c).edit().putString(DRAFTS,a.toString()).apply()}
    fun saveDraft(c:Context,d:Draft){val list=drafts(c);list.removeAll{it.id==d.id};list.add(0,d);writeDrafts(c,list)}
    fun deleteDraft(c:Context,id:Long){val list=drafts(c);list.removeAll{it.id==id};writeDrafts(c,list)}

    fun streak(c:Context)=p(c).getInt(STREAK,0)
    fun bestStreak(c:Context)=p(c).getInt(BEST_STREAK,0)
    fun best(c:Context)=p(c).getInt(BEST,0)
    fun total(c:Context)=p(c).getInt(TOTAL,0)
    fun xp(c:Context)=p(c).getLong(XP,0L)
    fun level(c:Context)=1+(xp(c)/100).toInt()
    fun xpIntoLevel(c:Context)=xp(c)%100
    fun ink(c:Context)=p(c).getInt(INK,0)
    fun bookWords(c:Context)=p(c).getInt(BOOK,0)
    fun rank(c:Context):String=when(level(c)){in 1..4->"Новичок";in 5..9->"Рассказчик";in 10..19->"Автор";in 20..34->"Мастер";in 35..49->"Романист";else->"Архитектор историй"}
    fun seasonDay(c:Context):Int{val now=System.currentTimeMillis();var start=p(c).getLong(SEASON_START,0L);if(start==0L){start=now;p(c).edit().putLong(SEASON_START,start).apply()};return (((now-start)/(24*60*60*1000L)).toInt()+1).coerceAtMost(30)}
    fun marathon(c:Context)=p(c).getInt(MARATHON,0)
    fun addMarathon(c:Context,words:Int){p(c).edit().putInt(MARATHON,marathon(c)+words).apply()}

    private fun dayKey():String=java.text.SimpleDateFormat("yyyyMMdd",java.util.Locale.US).format(java.util.Date())

    fun ensureDailyBailout(c:Context):Int{
        val today=dayKey()
        val q=p(c)
        if(q.getString(LAST_BAILOUT_DAY,"")==today || ink(c)>0)return 0
        val amount=(200.0*(1.0+(level(c)-1)*0.01)).roundToInt()
        q.edit().putInt(INK,amount).putString(LAST_BAILOUT_DAY,today).apply()
        return amount
    }

    fun challengeCost(c:Context,base:Int):Int{
        if(base<=0)return 0
        val levelFactor=1.0+(level(c)-1)*0.05
        val streakDiscount=minOf(0.20,streak(c)*0.01)
        return max(100,(base*levelFactor*(1.0-streakDiscount)).roundToInt())
    }
    fun spendInk(c:Context,amount:Int):Boolean{
        if(amount<=0)return true
        val have=ink(c)
        if(have<amount)return false
        p(c).edit().putInt(INK,have-amount).apply()
        return true
    }
    fun addInk(c:Context,amount:Int){if(amount>0)p(c).edit().putInt(INK,ink(c)+amount).apply()}

    fun payForWrittenWords(c:Context,deltaWords:Int):Boolean{
        if(deltaWords<=0)return true
        return spendInk(c,deltaWords)
    }

    fun failurePenalty(c:Context,wordsLost:Int):Int{
        if(wordsLost<=0)return 0
        val actual=minOf(wordsLost,ink(c))
        if(actual>0)p(c).edit().putInt(INK,ink(c)-actual).apply()
        return actual
    }

    fun rewardSuccess(c:Context,score:Int,words:Int,entryCost:Int=0,bonusXp:Int=0,modeMultiplier:Double=1.0,difficultyMultiplier:Double=1.0):Int{
        val q=p(c)
        val oldStreak=streak(c)
        val newStreak=oldStreak+1
        val comboMultiplier=1.0+(minOf(newStreak,20)*0.02)
        val levelMultiplier=1.0+(level(c)-1)*0.005
        val multiplier=modeMultiplier*difficultyMultiplier*comboMultiplier*levelMultiplier
        val wordReward=(words*multiplier).roundToInt()
        val profit=(words*0.15).roundToInt().coerceAtLeast(5)
        val payout=maxOf(words+1,wordReward,words+entryCost+profit)
        val xpGain=10L+words/5+bonusXp+(if(words>=100)50 else 0)+(if(score>best(c))100 else 0)
        q.edit()
            .putLong(XP,xp(c)+xpGain)
            .putInt(INK,ink(c)+payout)
            .putInt(STREAK,newStreak)
            .putInt(BEST_STREAK,maxOf(bestStreak(c),newStreak))
            .putInt(BEST,maxOf(score,best(c)))
            .putInt(TOTAL,total(c)+1)
            .putInt(BOOK,bookWords(c)+words)
            .apply()
        addMarathon(c,words)
        if(newStreak>=3)unlock(c,"Серия 3");if(newStreak>=7)unlock(c,"Серия 7");if(newStreak>=30)unlock(c,"Серия 30");if(newStreak>=100)unlock(c,"Серия 100")
        if(total(c)>=10)unlock(c,"10 текстов");if(bookWords(c)>=1000)unlock(c,"1000 слов книги");if(bookWords(c)>=10000)unlock(c,"10000 слов книги")
        return payout
    }

    fun registerSuccess(c:Context,score:Int){rewardSuccess(c,score,0)}
    fun breakStreak(c:Context){p(c).edit().putInt(STREAK,0).apply()}
    fun achievements(c:Context)=p(c).getStringSet(ACH,emptySet())?.toMutableSet()?:mutableSetOf<String>()
    fun unlock(c:Context,key:String){val s=achievements(c);if(s.add(key))p(c).edit().putStringSet(ACH,s).apply()}
    fun allAchievements():List<String>{
        val a=mutableListOf<String>()
        a += listOf("Первый текст","10 текстов","100 текстов","1000 текстов","10000 слов","100000 слов","Серия 3","Серия 7","Серия 14","Серия 30","Серия 60","Серия 100","Без паузы","Скорость","Слепой режим","Хаос","Слово-босс","Проклятое слово","Финальный удар","Дуэль","Ежедневный вызов","Босс недели","Редкий жанр","Гибрид","Все жанры","Память писателя","Запретное слово","1000 слов книги","10000 слов книги","50000 слов книги","100000 слов книги")
        for(i in 1..70)a.add("Тайное достижение #$i")
        return a
    }
}
