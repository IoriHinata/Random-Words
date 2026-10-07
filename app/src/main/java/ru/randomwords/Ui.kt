package ru.randomwords
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
object Ui {
    const val BG="#101116"; const val CARD="#191B23"; const val GOLD="#C7A46A"; const val TEXT="#F1E9DB"; const val MUTED="#9B9AA3"; const val BLUE="#8FA7C7"; const val RED="#D47B7B"
    fun bg(color:String,r:Float=20f)=GradientDrawable().apply{setColor(Color.parseColor(color));cornerRadius=r}
    fun text(v:TextView,size:Number,color:String=TEXT){v.textSize=size.toFloat();v.setTextColor(Color.parseColor(color))}
}
