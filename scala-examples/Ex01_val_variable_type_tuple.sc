object Ex01_val_variable_type_tuple {
//Literal, Values, variables and types

//Explicitly define data types
val x: Int = 5                                    //> x  : Int = 5
val greeting: String = "Hello World"              //> greeting  : String = Hello World
val c: Char = '@'                                 //> c  : Char = @

//Infer data types
val x2=5                                          //> x2  : Int = 5
val greeting2="Hello World"                       //> greeting2  : String = Hello World
val c2='@'                                        //> c2  : Char = @

//String
val greeting3 = "Hello, "+"World"                 //> greeting3  : String = Hello, World
val greeting4 = """She suggested reformatting the file
by replacing tabs (\t) with newlines (\n);
"Why do that?", he asked.
"""                                               //> greeting4  : String = "She suggested reformatting the file
                                                  //| by replacing tabs (\t) with newlines (\n);
                                                  //| "Why do that?", he asked.
                                                  //| "
//String interpolation
val approx = 355/113f                             //> approx  : Float = 3.141593
println(s"Pi, using 355/113, is about $approx.")  //> Pi, using 355/113, is about 3.141593.

val item = "apple"                                //> item  : String = apple
s"How do you like them ${item}s"                  //> res0: String = How do you like them apples
s"Fish n chips n vinegar, ${"pepper "*3}salt ${"="*3}"
                                                  //> res1: String = Fish n chips n vinegar, pepper pepper pepper salt ===

//Common method in all object
 5.asInstanceOf[Long]                             //> res2: Long = 5
 (7.0/5.0).getClass()                             //> res3: Class[Double] = double
 (5.0).isInstanceOf[Float]                        //> res4: Boolean = false
 20.toByte                                        //> res5: Byte = 20
 10.toLong                                        //> res6: Long = 10
 
 //Tuple
 val info = (5,"Adnan",true)                      //> info  : (Int, String, Boolean) = (5,Adnan,true)
 val id = info._1                                 //> id  : Int = 5
 val name = info._2                               //> name  : String = Adnan
 
 //2-sized tuple
 val red = "red" -> "0xff0000"                    //> red  : (String, String) = (red,0xff0000)
 val reversed = red._2 -> red._1                  //> reversed  : (String, String) = (0xff0000,red)
}