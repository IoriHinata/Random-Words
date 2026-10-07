package ru.randomwords

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import androidx.appcompat.widget.AppCompatEditText

class PasteBlockEditText(context:Context,attrs:AttributeSet?=null):AppCompatEditText(context,attrs){
    override fun onTextContextMenuItem(id:Int):Boolean{
        if(id==android.R.id.paste || id==android.R.id.pasteAsPlainText)return true
        return super.onTextContextMenuItem(id)
    }
    override fun onDragEvent(event:DragEvent):Boolean{
        if(event.action==DragEvent.ACTION_DROP)return true
        return super.onDragEvent(event)
    }
}