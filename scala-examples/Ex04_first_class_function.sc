object Ex04_first_class_function {

//Function Type and values

//Function type
//([<type>, ...]) => <type>

def double(x:Int) = x * 2                         //> double: (x: Int)Int
val doubler: (Int)=>(Int) = double                //> doubler  : Int => Int = <function1>

//Assigning a Function with the Wildcard Operator
//val <identifier> = <function name> _

val mydouble = double _                           //> mydouble  : Int => Int = <function1>
mydouble(2)                                       //> res0: Int = 4

def max(x:Int,y:Int):Int=if(x>y)x else y          //> max: (x: Int, y: Int)Int
val maximum: (Int,Int) => Int = max               //> maximum  : (Int, Int) => Int = <function2>
maximum(10,2)                                     //> res1: Int = 10

//Higher-Order Functions
def safeStringOp(s: String, f: String => String) = {
   if (s != null) f(s) else s
}                                                 //> safeStringOp: (s: String, f: String => String)String
def reverser(s: String) = s.reverse               //> reverser: (s: String)String
safeStringOp("Ready", reverser)                   //> res2: String = ydaeR

//Function Literals
//([<identifier>: <type>, ... ]) => <expression>
val greet = s => s"Hello, $s"                     //> greet  : Seq[Any] => String = <function1>
val hi = greet("World")                           //> hi  : String = Hello, World

//The original max() function
def max2(a: Int, b: Int) = if (a > b) a else b    //> max2: (a: Int, b: Int)Int

//as assigned to a function value
val maximize: (Int, Int) => Int = max2            //> maximize  : (Int, Int) => Int = <function2>

//as redefined with a function literal
val maximize2 = (a:Int,b:Int)=>if (a > b) a else b//> maximize2  : (Int, Int) => Int = <function2>

//Placeholder Syntax
val double2: Int => Int = _ * 2                   //> double2  : Int => Int = <function1>

def tripleOp(a: Int, b: Int, c: Int, f: (Int, Int, Int) => Int) = f(a,b,c)
                                                  //> tripleOp: (a: Int, b: Int, c: Int, f: (Int, Int, Int) => Int)Int
tripleOp(23, 92, 14, _ * _ + _)                   //> res3: Int = 2130

def tripleOp2[A,B](a: A, b: A, c: A, f: (A, A, A) => B) = f(a,b,c)
                                                  //> tripleOp2: [A, B](a: A, b: A, c: A, f: (A, A, A) => B)B

tripleOp2[Int,Int](23, 92, 14, _ * _ + _)         //> res4: Int = 2130
tripleOp2[Int,Double](23, 92, 14, 1.0 * _ / _ / _)//> res5: Double = 0.017857142857142856
tripleOp2[Int,Boolean](93, 92, 14, _ > _ + _)     //> res6: Boolean = false

//Partially Applied Functions and Currying
def factorOf(x: Int, y: Int) = y % x == 0         //> factorOf: (x: Int, y: Int)Boolean
val multipleOf3 = factorOf(3, _: Int)             //> multipleOf3  : Int => Boolean = <function1>

//By-Name Parameters
//<identifier>: => <type>

def doubles(x: => Int) = {
  println("Now doubling " + x)
  x * 2
}                                                 //> doubles: (x: => Int)Int

doubles(5)                                        //> Now doubling 5
                                                  //| res7: Int = 10

def f(i: Int) = { println(s"Hello from f($i)"); i }
                                                  //> f: (i: Int)Int

doubles( f(8) )                                   //> Hello from f(8)
                                                  //| Now doubling 8
                                                  //| Hello from f(8)
                                                  //| res8: Int = 16
}