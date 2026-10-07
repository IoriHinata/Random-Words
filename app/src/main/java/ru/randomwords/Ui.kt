package ru.randomwords
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min
object Ui {
 const val BG="#0D0E12"; const val CARD="#171922"; const val CARD2="#1E202A"; const val GOLD="#D2AD70"; const val TEXT="#F4EEE4"; const val MUTED="#9A9AA4"; const val BLUE="#91B0D6"; const val GREEN="#82C49A"; const val RED="#D77D7D"
 fun dp(c:Context,n:Int)=(n*c.resources.displayMetrics.density+.5f).toInt()
 fun bg(color:String,r:Float=20f)=GradientDrawable().apply{setColor(Color.parseColor(color));cornerRadius=r}
 fun text(v:TextView,size:Number,color:String=TEXT){v.textSize=size.toFloat();v.setTextColor(Color.parseColor(color))}
 fun contentWidth(c:Context):Int=min(c.resources.displayMetrics.widthPixels-dp(c,32),dp(c,760))
 fun applyContentWidth(v:android.view.View,c:Context){v.layoutParams=android.widget.FrameLayout.LayoutParams(contentWidth(c),-2,Gravity.CENTER)}
 fun lp(c:Context,h:Int)=LinearLayout.LayoutParams(-1,dp(c,h))
 fun spaced(c:Context,h:Int,top:Int=5,bottom:Int=5)=LinearLayout.LayoutParams(-1,dp(c,h)).apply{setMargins(0,dp(c,top),0,dp(c,bottom))}
}
