import scala.concurrent.Promise
import scala.concurrent.Future

object Main extends App {

  def inc(i:Int): Stream[Int] = i #:: inc(i+1)

  def to(start: Char, end: Char): Stream[Char] = start > end match{
    case true => Stream.Empty
    case false => start #:: to((start+1).toChar, end) 
  }
  
  println(to('A', 'B').take(10).toList)
  
  
  Promise
  Future
}