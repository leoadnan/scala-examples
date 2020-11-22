import collection.JavaConverters._
object Ex05_common_collection {
val numbers = List(1,2,3,4,5)                     //> numbers  : List[Int] = List(1, 2, 3, 4, 5)
val colors = List("red","green","blue")           //> colors  : List[String] = List(red, green, blue)
println (s"I have ${colors.size} colors: $colors")//> I have 3 colors: List(red, green, blue)

colors.head                                       //> res0: String = red
colors.tail                                       //> res1: List[String] = List(green, blue)
colors(0)                                         //> res2: String = red
colors(1)                                         //> res3: String = green

var total=0;for(i <- numbers) { total += i}       //> total  : Int = 15
for (c <- colors) println(c)                      //> red
                                                  //| green
                                                  //| blue
colors.foreach ( println(_) )                     //> red
                                                  //| green
                                                  //| blue
colors.map(_.size)                                //> res4: List[Int] = List(3, 5, 4)
numbers.reduce(_+_)                               //> res5: Int = 15

val unique = Set(10,20,30,20,20,10)               //> unique  : scala.collection.immutable.Set[Int] = Set(10, 20, 30)
unique.reduce((a:Int, b:Int)=>a+b)                //> res6: Int = 60

val colorMap = Map("red"->0xFF0000, "green"->0xFF00, "blue"->0xFF)
                                                  //> colorMap  : scala.collection.immutable.Map[String,Int] = Map(red -> 16711680
                                                  //| , green -> 65280, blue -> 255)
val redRGB = colorMap("red")                      //> redRGB  : Int = 16711680
val cyanRGB = colorMap("green") | colorMap("blue")//> cyanRGB  : Int = 65535
val hasWhite = colorMap.contains("white")         //> hasWhite  : Boolean = false
for (pairs <- colorMap) (println(pairs))          //> (red,16711680)
                                                  //| (green,65280)
                                                  //| (blue,255)
val oddsAndEvens = List(List(1,3,5),List(2,4,6))  //> oddsAndEvens  : List[List[Int]] = List(List(1, 3, 5), List(2, 4, 6))
val keyValues =List(('A',65),('B',66),('C',67))   //> keyValues  : List[(Char, Int)] = List((A,65), (B,66), (C,67))
val primes = List(2,3,7,11,13)                    //> primes  : List[Int] = List(2, 3, 7, 11, 13)
val first = primes.head                           //> first  : Int = 2
val remaining = primes.tail                       //> remaining  : List[Int] = List(3, 7, 11, 13)

var i = primes                                    //> i  : List[Int] = List(2, 3, 7, 11, 13)

while (! i.isEmpty) {print(i.head+", "); i = i.tail}
                                                  //> 2, 3, 7, 11, 13, 

def visit(i: List[Int]) {
   if (i.size>0){
     print(i.head+",")
     visit(i.tail)
   }
}                                                 //> visit: (i: List[Int])Unit

visit(primes)                                     //> 2,3,7,11,13,

val l : List[Int] = List()                        //> l  : List[Int] = List()
l== Nil                                           //> res7: Boolean = true

val m:List[String] = List("a")                    //> m  : List[String] = List(a)
m.head                                            //> res8: String = a
m.tail == Nil                                     //> res9: Boolean = true

val numbers2 = 1::2::3::Nil                       //> numbers2  : List[Int] = List(1, 2, 3)

val first2 = Nil.::(1)                            //> first2  : List[Int] = List(1)
first2.tail==Nil                                  //> res10: Boolean = true
val second = 2 :: first2                          //> second  : List[Int] = List(2, 1)
second.tail == first2                             //> res11: Boolean = true

List(1,2) ::: List (2,3)                          //> res12: List[Int] = List(1, 2, 2, 3)

List(1,2) ++ Set(3,4,3)                           //> res13: List[Int] = List(1, 2, 3, 4)

List(3,5,3,4).distinct                            //> res14: List[Int] = List(3, 5, 4)

List(3,5,4,3,4).drop(2)                           //> res15: List[Int] = List(4, 3, 4)

List(23,8,14,21).filter ( _ > 18 )                //> res16: List[Int] = List(23, 21)

List(List(1,2,3),List(3,4)).flatten               //> res17: List[Int] = List(1, 2, 3, 3, 4)

List(1,2,3,4,5).partition { _ < 3 }               //> res18: (List[Int], List[Int]) = (List(1, 2),List(3, 4, 5))

List(1,2,3,4,5) slice (1,3)                       //> res19: List[Int] = List(2, 3)

List("apply","to").sortBy { _.size }              //> res20: List[String] = List(to, apply)

List("apply","Apply","to","To") sorted            //> res21: List[String] = List(Apply, To, apply, to)

List(1,2,3,4,5,6) splitAt 2                       //> res22: (List[Int], List[Int]) = (List(1, 2),List(3, 4, 5, 6))

List(1,2,3,4,5) zip List("a","b")                 //> res23: List[(Int, String)] = List((1,a), (2,b))

val appended = List(1,2,3,4) :+ 5                 //> appended  : List[Int] = List(1, 2, 3, 4, 5)
val suffix = appended takeRight 3                 //> suffix  : List[Int] = List(3, 4, 5)
val middle = suffix dropRight 2                   //> middle  : List[Int] = List(3)

List(0,1,0) collect { case 1=> "ok"}              //> res24: List[String] = List(ok)

List("milk,tea,bread").flatMap(_.split(","))      //> res25: List[String] = List(milk, tea, bread)

def contains(x:Int, l:List[Int]):Boolean = {
  var a:Boolean=false
  for(i<-l) {if (!a) a = (i==x) }
  a
}                                                 //> contains: (x: Int, l: List[Int])Boolean

def boolReduce(l:List[Int], start:Boolean)(f:(Boolean,Int)=>Boolean):Boolean = {
  var a = start
  for(i <- l) a = f(a,i)
  a
}                                                 //> boolReduce: (l: List[Int], start: Boolean)(f: (Boolean, Int) => Boolean)Boo
                                                  //| lean

boolReduce(List(1,2,3,4,5),false) {(a,i)=> if (a) a else (i==4)}
                                                  //> res26: Boolean = true
def reduceOp[A,B] (l:List[A], start:B)(f:(B,A)=>B):B={
  var a=start
  for(i <- l) a = f(a,i)
  a
}                                                 //> reduceOp: [A, B](l: List[A], start: B)(f: (B, A) => B)B

reduceOp(List(1,2,3,4,5),false)((a,i)=>if(a)a else (i==4))
                                                  //> res27: Boolean = true
                                                  
reduceOp(List(1,2,3,4,5),0) (_+_)                 //> res28: Int = 15

List(1,2,3,4,5).fold(0)(_+_)                      //> res29: Int = 15
List(1,2,3,4,5).scan(0)(_+_)                      //> res30: List[Int] = List(0, 1, 3, 6, 10, 15)

List(24,99,104).mkString(",")                     //> res31: String = 24,99,104
List(1,2,3,4,5).toSet                             //> res32: scala.collection.immutable.Set[Int] = Set(5, 1, 2, 3, 4)

val statuses = List(500,400)                      //> statuses  : List[Int] = List(500, 400)
statuses.head match {
 case x if x < 500 => "Okay"
 case _ => "Error"
}                                                 //> res33: String = Error

statuses match {
case x if x contains(500) => "has error"
case _ => "okay"
}                                                 //> res34: String = has error

statuses match {
case List(500,x)=>s"error followed by $x"
case List(e,x)=>s"$e was followed by $x"
}                                                 //> res35: String = error followed by 400

val code = ('h',204,true) match {
  case (_,_,false) => 501
  case ('c',_,true)=>302
  case ('h',x,true)=>x
  case (c,x,true) => {
    println(s"did not expect code $c")
    x
  }
}                                                 //> code  : Int = 204
}