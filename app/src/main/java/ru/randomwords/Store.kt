package ru.randomwords

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object Store {
    private const val PREF="random_words"; private const val DRAFTS="drafts"; private const val WORDS="words"
    private const val FOLDERS="folders"; private const val STREAK="streak"; private const val BEST="best"
    private const val TOTAL="total"; private const val ACH="achievements"
    private fun p(c:Context)=c.getSharedPreferences(PREF,0)

    fun words(c:Context):MutableList<String>{
        val raw=p(c).getString(WORDS,null)?:return WordBank.defaults().toMutableList()
        val a=JSONArray(raw); return MutableList(a.length()){a.getString(it)}
    }
    fun saveWords(c:Context,list:List<String>){
        val a=JSONArray(); list.map{it.trim().lowercase()}.filter{it.isNotBlank()}.distinct().forEach{a.put(it)}
        p(c).edit().putString(WORDS,a.toString()).apply()
    }
    fun folders(c:Context):MutableList<String>{
        val raw=p(c).getString(FOLDERS,null)?:return mutableListOf("Без папки")
        val a=JSONArray(raw); return MutableList(a.length()){a.getString(it)}
    }
    fun addFolder(c:Context,name:String){
        val f=folders(c); val clean=name.trim()
        if(clean.isNotBlank()&&!f.contains(clean)){f.add(clean);val a=JSONArray();f.forEach{a.put(it)};p(c).edit().putString(FOLDERS,a.toString()).apply()}
    }
    fun drafts(c:Context):MutableList<Draft>{
        val a=JSONArray(p(c).getString(DRAFTS,"[]"))
        return MutableList(a.length()){i->val o=a.getJSONObject(i);val w=o.optJSONArray("words")?:JSONArray()
            Draft(o.getLong("id"),o.getString("title"),o.getString("text"),MutableList(w.length()){j->w.getString(j)},
                o.getLong("date"),o.getInt("seconds"),o.getInt("score"),o.optString("note"),o.optString("folder"))}
    }
    private fun writeDrafts(c:Context,list:List<Draft>){
        val a=JSONArray(); list.forEach{x->a.put(JSONObject().apply{
            put("id",x.id);put("title",x.title);put("text",x.text);put("words",JSONArray(x.words))
            put("date",x.date);put("seconds",x.seconds);put("score",x.score);put("note",x.note);put("folder",x.folder)
        })}; p(c).edit().putString(DRAFTS,a.toString()).apply()
    }
    fun saveDraft(c:Context,d:Draft){val list=drafts(c);list.removeAll{it.id==d.id};list.add(0,d);writeDrafts(c,list)}
    fun deleteDraft(c:Context,id:Long){val list=drafts(c);list.removeAll{it.id==id};writeDrafts(c,list)}
    fun streak(c:Context)=p(c).getInt(STREAK,0)
    fun best(c:Context)=p(c).getInt(BEST,0)
    fun total(c:Context)=p(c).getInt(TOTAL,0)
    fun registerSuccess(c:Context,score:Int){val q=p(c);q.edit().putInt(STREAK,q.getInt(STREAK,0)+1).putInt(BEST,maxOf(score,q.getInt(BEST,0))).putInt(TOTAL,q.getInt(TOTAL,0)+1).apply()}
    fun breakStreak(c:Context){p(c).edit().putInt(STREAK,0).apply()}
    fun achievements(c:Context)=p(c).getStringSet(ACH,emptySet())?.toMutableSet()?:mutableSetOf()
    fun unlock(c:Context,key:String){val s=achievements(c);s.add(key);p(c).edit().putStringSet(ACH,s).apply()}
}