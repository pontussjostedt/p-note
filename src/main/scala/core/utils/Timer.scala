package core


final case class Timer(msDelay: Long, private val startOnCd: Boolean = true):
    var lastProc = if startOnCd then System.currentTimeMillis() else -msDelay
    def isOver: Boolean = lastProc + msDelay <= System.currentTimeMillis()
    def isOverReset: Boolean =
        val out = lastProc + msDelay <= System.currentTimeMillis()
        if out then lastProc = System.currentTimeMillis()
        out


case class FPSMeassure(collectionPeriod: Long):
    val timer = Timer(collectionPeriod)
    var frameCount = 0
    def tick(): Unit = 
        frameCount += 1
        if timer.isOverReset then
            //println(s"FPS = ${1000*frameCount/collectionPeriod.toDouble}")
            frameCount = 0

