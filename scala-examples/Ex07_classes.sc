import util.Random._
object Ex07_classes {
val letters = alphanumeric.take(20).toList.mkString
                                                  //> letters  : String = NTv6yzaGzYgeNfvynJAD
val numbers = shuffle(1 to 20)                    //> numbers  : scala.collection.immutable.IndexedSeq[Int] = Vector(18, 13, 15, 1
                                                  //| , 10, 17, 12, 19, 9, 20, 5, 8, 2, 3, 4, 14, 7, 11, 6, 16)
}