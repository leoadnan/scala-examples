import concurrent.ExecutionContext.Implicits.global
import concurrent.Future
object Ex06_more_collection {
  val nums = collection.mutable.Buffer(1)         //> nums  : scala.collection.mutable.Buffer[Int] = ArrayBuffer(1)
  for (i <- 2 to 10) nums += i
  println(nums)                                   //> ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  val nums2 = collection.mutable.Buffer[Int]()    //> nums2  : scala.collection.mutable.Buffer[Int] = ArrayBuffer()
  for (i <- 1 to 10) nums2 += i
  println(nums2)                                  //> ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  val l = nums.toList                             //> l  : List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  val m = Map("APPL" -> 597, "MSFT" -> 40)        //> m  : scala.collection.immutable.Map[String,Int] = Map(APPL -> 597, MSFT -> 4
                                                  //| 0)
  val b = m.toBuffer                              //> b  : scala.collection.mutable.Buffer[(String, Int)] = ArrayBuffer((APPL,597)
                                                  //| , (MSFT,40))
  b trimStart 1
  b += ("GOOG" -> 521)                            //> res0: Ex06_more_collection.b.type = ArrayBuffer((MSFT,40), (GOOG,521))
  val n = b.toMap                                 //> n  : scala.collection.immutable.Map[String,Int] = Map(MSFT -> 40, GOOG -> 52
                                                  //| 1)

  val b2 = Set.newBuilder[Char]                   //> b2  : scala.collection.mutable.Builder[Char,scala.collection.immutable.Set[C
                                                  //| har]] = scala.collection.mutable.SetBuilder@39c0f4a
  b2 += 'h'                                       //> res1: Ex06_more_collection.b2.type = scala.collection.mutable.SetBuilder@39c
                                                  //| 0f4a
  b2 ++= List('e', 'l', 'l', 'o')                 //> res2: Ex06_more_collection.b2.type = scala.collection.mutable.SetBuilder@39c
                                                  //| 0f4a
  val helloSet = b2.result                        //> helloSet  : scala.collection.immutable.Set[Char] = Set(h, e, l, o)

  val colors = Array("red", "green", "blue")      //> colors  : Array[String] = Array(red, green, blue)
  colors(0) = "purple"
  colors                                          //> res3: Array[String] = Array(purple, green, blue)
  println("very purple: " + colors)               //> very purple: [Ljava.lang.String;@59ec2012
  val files = new java.io.File(".").listFiles     //> files  : Array[java.io.File] = Array(./eclipse)
  val scala = files map (_.getName) filter (_ endsWith "scala")
                                                  //> scala  : Array[String] = Array()

  val inks = Seq('C', 'M', 'Y', 'K')              //> inks  : Seq[Char] = List(C, M, Y, K)

  def inc(i: Int): Stream[Int] = Stream.cons(i, inc(i + 1))
                                                  //> inc: (i: Int)Stream[Int]
  val s = inc(1)                                  //> s  : Stream[Int] = Stream(1, ?)

  val l2 = s.take(5).toList                       //> l2  : List[Int] = List(1, 2, 3, 4, 5)

  def inc2(head: Int): Stream[Int] = head #:: inc(head + 1)
                                                  //> inc2: (head: Int)Stream[Int]
  inc(10).take(10).toList                         //> res4: List[Int] = List(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

  def to(head: Char, end: Char): Stream[Char] = (head > end) match {
    case true => Stream.empty
    case false => head #:: to((head + 1).toChar, end)
  }                                               //> to: (head: Char, end: Char)Stream[Char]

  to('A', 'F').take(20).toList                    //> res5: List[Char] = List(A, B, C, D, E, F)

  var s2 = "data"                                 //> s2  : String = data
  val a = Option(s2)                              //> a  : Option[String] = Some(data)
  s2 = null
  val b3 = Option(s2)                             //> b3  : Option[String] = None

  a.isDefined                                     //> res6: Boolean = true
  b3.isEmpty                                      //> res7: Boolean = true

  def divide(amt: Double, divisor: Double): Option[Double] = {
    if (divisor == 0) None
    else Option(amt / divisor)
  }                                               //> divide: (amt: Double, divisor: Double)Option[Double]

  val words = List("risible", "scavenger", "gist")//> words  : List[String] = List(risible, scavenger, gist)
  val uppercase = words find (w => w == w.toUpperCase)
                                                  //> uppercase  : Option[String] = None
  val lowercase = words find (w => w == w.toLowerCase)
                                                  //> lowercase  : Option[String] = Some(risible)

  def nextOption = if (util.Random.nextInt > 0) Some(1) else None
                                                  //> nextOption: => Option[Int]
  val aa = nextOption                             //> aa  : Option[Int] = None
  val bb = nextOption                             //> bb  : Option[Int] = Some(1)

  nextOption.fold(-1)(x => x)                     //> res8: Int = 1

  nextOption getOrElse -1                         //> res9: Int = -1

  nextOption orElse { Option(-1) }                //> res10: Option[Int] = Some(1)

  nextOption match {
    case Some(x) => x
    case None => -1
  }                                               //> res11: Int = 1

  def loopAndFail(end: Int, failAt: Int): Int = {
    for (i <- 1 to end) {
      println(s"$i) ")
      if (i == failAt) throw new Exception("Too many iterations")
    }
    end
  }                                               //> loopAndFail: (end: Int, failAt: Int)Int
  
  util.Try( loopAndFail (2,3) )                   //> 1) 
                                                  //| 2) 
                                                  //| res12: scala.util.Try[Int] = Success(2)
  util.Try( loopAndFail (4,2) )                   //> 1) 
                                                  //| 2) 
                                                  //| res13: scala.util.Try[Int] = Failure(java.lang.Exception: Too many iteratio
                                                  //| ns)

def nextError = util.Try{ 1 / util.Random.nextInt(2) }
                                                  //> nextError: => scala.util.Try[Int]
val x = nextError                                 //> x  : scala.util.Try[Int] = Success(1)
val y = nextError                                 //> y  : scala.util.Try[Int] = Success(1)

nextError flatMap { _ => nextError }              //> res14: scala.util.Try[Int] = Failure(java.lang.ArithmeticException: / by ze
                                                  //| ro)
nextError.foreach { x => println("success!"+x) }

nextError getOrElse 0                             //> res15: Int = 0

nextError map (_*2)                               //> res16: scala.util.Try[Int] = Success(2)
nextError match {
 case util.Success(x) => x
 case util.Failure(error) => -1
}                                                 //> res17: Int = -1

nextError                                         //> res18: scala.util.Try[Int] = Failure(java.lang.ArithmeticException: / by ze
                                                  //| ro)
val input = " 123 "                               //> input  : String = " 123 "
val result = util.Try(input.toInt) orElse util.Try(input.trim.toInt)
                                                  //> result  : scala.util.Try[Int] = Success(123)
result foreach { r => println(s"Parsed '$input' to $r!") }
                                                  //> Parsed ' 123 ' to 123!
  val xx = result match {
    case util.Success(x) => Some(x)
    case util.Failure(ex) => {
      println(s"Couldn't parse input '$input'")
      None
    }
}                                                 //> xx  : Option[Int] = Some(123)
}