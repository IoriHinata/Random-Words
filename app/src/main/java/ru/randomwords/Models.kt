package ru.randomwords

data class ChallengeConfig(
    val count:Int=3,
    val goalType:GoalType=GoalType.WORDS,
    val goal:Int=100,
    val idleSeconds:Int=5,
    val blind:Boolean=false,
    val chaos:Boolean=false,
    val boss:Boolean=false,
    val repeatWords:Boolean=false,
    val daily:Boolean=false,
    val duel:Boolean=false,
    val lastSentence:Boolean=false,
    val mode:ChallengeMode=ChallengeMode.STANDARD,
    val category:String="Все"
)
enum class GoalType { WORDS,TIME,ENDLESS }
enum class ChallengeMode { STANDARD,GENRE,RARE_GENRE,HYBRID,STORY,CURSED,BOSS,DUEL }
data class Genre(val name:String,val description:String,val rare:Boolean=false,val hybrid:Boolean=false)
data class Draft(val id:Long,var title:String,var text:String,var words:List<String>,var date:Long,var seconds:Int,var score:Int,var note:String="",var folder:String="")
