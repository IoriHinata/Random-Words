package ru.randomwords

object WordMatcher {
 private val suffixes=listOf("иями","ями","ами","ого","ему","ому","ыми","ими","ее","ие","ые","ое","ей","ий","ый","ой","ем","ом","ам","ям","ах","ях","ов","ев","ью","ия","ие","ии","ию","ою","ею","ую","юю","ая","яя","а","я","ы","и","у","ю","е","о","ь","й").sortedByDescending{it.length}
 private val prefixes=listOf("пере","пред","пре","при","под","над","от","об","раз","рас","воз","вос","без","бес","вы","до","за","на","по","про","у","с","со","из","вз","вс")
 fun normalize(word:String):String{
  var w=word.lowercase().replace('ё','е').replace(Regex("[^а-я]"),"")
  if(w.length<3)return w
  for(p in prefixes)if(w.startsWith(p)&&w.length-p.length>=4){w=w.removePrefix(p);break}
  for(s in suffixes)if(w.endsWith(s)&&w.length-s.length>=3){w=w.dropLast(s.length);break}
  return w
 }
 fun matches(inputText:String,target:String):Boolean{
  val t=normalize(target)
  if(t.length<3)return tokenize(inputText).any{it==target.lowercase()}
  return tokenize(inputText).any{val n=normalize(it);n==t||(n.startsWith(t)&&n.length-t.length<=3)||(t.startsWith(n)&&t.length-n.length<=2)}
 }
 fun tokenize(text:String)=text.lowercase().replace('ё','е').split(Regex("[^а-я]+")).filter{it.isNotBlank()}
}
