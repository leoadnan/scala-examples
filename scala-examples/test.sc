object test {
val arr = Array("a", "b", "c")                    //> arr  : Array[String] = Array(a, b, c)

for (i <- 0 until arr.length) println(s"$i , ${arr(i)}")
                                                  //> 0 , a
                                                  //| 1 , b
                                                  //| 2 , c
                                                  
for ((i,v) <- arr.zipWithIndex) println(i,v)      //> (a,0)
                                                  //| (b,1)
                                                  //| (c,2)
}