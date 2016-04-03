object Ex02_expression_condition_loop {

//Expression, Condition and Loop
val a = 2 * 5; val b = a * 20;                    //> a  : Int = 10
                                                  //| b  : Int = 200
val amount = {val x = 2 * 5; x * 20}              //> amount  : Int = 200

//Match Expressions
val x=5; val y=10;                                //> x  : Int = 5
                                                  //| y  : Int = 10
val max = x > y match {
	case true => x
	case false => y
}                                                 //> max  : Int = 10

val status = 200;                                 //> status  : Int = 200
val message = status match {
	case 200 => "Ok"
	case 400 => {
		println("ERROR - we called the service incorrectly")
		"error"
	}
	case 500 => {
		println("ERROR - the service encountered an error")
		"error"
	}
}                                                 //> message  : String = Ok

val day = "MON"                                   //> day  : String = MON
val kind = day match {
	case "MON" | "TUE" | "WED" | "THU" | "FRI" => "Weekday"
	case "SAT" | "SUN" => "Weekend"
}                                                 //> kind  : String = Weekday

val status2 = message match {
	case "Ok" => 200
	case other => {
		println (s"Couldn't parse $other")
		-1
	}
}                                                 //> status2  : Int = 200

val str = null                                    //> str  : Null = null
str match {
	case s if s!=null =>  println(s"Received '$s'")
	case s => println("Error! Received a null response")
}                                                 //> Error! Received a null response

val x2: Long = 10                                 //> x2  : Long = 10
val y2: Any = x2                                  //> y2  : Any = 10
y2 match {
	case x: String => s"'x'"
	case x: Double => f"$x%.2f"
	case x: Float => f"$x%.2f"
	case x: Long => s"${x}l"
	case x: Int => s"${x}i"
}                                                 //> res0: String = 10l

//Loop
for(x <- 1 to 7) {print(s"Day-$x ")}              //> Day-1 Day-2 Day-3 Day-4 Day-5 Day-6 Day-7 
for(x <- 1 to 7) yield {s"Day $x"}                //> res1: scala.collection.immutable.IndexedSeq[String] = Vector(Day 1, Day 2, 
                                                  //| Day 3, Day 4, Day 5, Day 6, Day 7)
for(x <- 1 to 30 if x%3==0 ) yield x              //> res2: scala.collection.immutable.IndexedSeq[Int] = Vector(3, 6, 9, 12, 15, 
                                                  //| 18, 21, 24, 27, 30)
for { x <- 1 to 2
      y <- 1 to 3 }
{ print(s"($x,$y) ") }                            //> (1,1) (1,2) (1,3) (2,1) (2,2) (2,3) 

for(i <- 1 to 10; pow = 1 << i) yield pow         //> res3: scala.collection.immutable.IndexedSeq[Int] = Vector(2, 4, 8, 16, 32, 
                                                  //| 64, 128, 256, 512, 1024)
	 
}