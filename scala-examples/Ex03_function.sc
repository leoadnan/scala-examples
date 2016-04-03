object Ex03_function {

//Defining an Input-less Function
//def <identifier> = <expression>
	def hi="hi"                               //> hi: => String
	hi                                        //> res0: String = hi
	def add=2*3                               //> add: => Int
	add                                       //> res1: Int = 6

//Defining a Function with a Return Type
//def <identifier>: <type> = <expression>
def hi2 : String = "hi"                           //> hi2: => String

//Defining a Function
//def <identifier>(<identifier>: <type>[, ... ]): <type> = <expression>
def multiplier(x:Int, y:Int) = x*y                //> multiplier: (x: Int, y: Int)Int
multiplier(2,3)                                   //> res2: Int = 6

def safeTrim(s:String): String = {
	if (s==null) return null
	s.trim
}                                                 //> safeTrim: (s: String)String

//Function Invocation with Expression Blocks
//<function identifier> <expression block>

def formatEuro(amt: Double) = f"€$amt%.2f"        //> formatEuro: (amt: Double)String
formatEuro {val rate=1.32; 0.235 + 0.7123 + rate * 5.32}
                                                  //> res3: String = €7.97

                                                  
}