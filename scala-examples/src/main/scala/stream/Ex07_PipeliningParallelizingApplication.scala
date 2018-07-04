package stream

import akka.stream.scaladsl.Flow

object SynchronousPipeliningApplication extends Ex07_PipeliningParallelizing {
  runGraph(Flow[Wash].via(washStage).via(dryStage))
}

object AsynchronousPipeliningApplication extends Ex07_PipeliningParallelizing {
  runGraph(Flow[Wash].via(washStage.async).via(dryStage.async))
}

object ParallelizingApplication extends Ex07_PipeliningParallelizing {
  runGraph(Flow[Wash].via(parallelStage))
}