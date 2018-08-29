import Filters._
import H5Read._
import H5DataFrame._
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd._
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object SoWTest {
  def runSoWtest(sc: SparkContext, spark: SparkSession, dname: String) {
    import spark.implicits._
    val genevtinfo_df = createGenInfoDF(sc, spark, dname)
    genevtinfo_df.persist()
    //genevtinfo_df.show()
    val wts = new Array[Any](5)
    //println("Num events: " + genevtinfo_df.count())
    for (i <- 0 to 4) {
    var t0 = System.nanoTime()
    wts(i) =  genevtinfo_df.agg(sum("weight")).first.get(0)
    var t1 = System.nanoTime()
    //df.show()
    println("Sum of Weights is: " + wts(i))
    println("It took :" + (t1 - t0) +" ns to calculate the weight")
    }
    println(genevtinfo_df.count)
  }

  def createGenInfoDF(sc: SparkContext, spark: SparkSession, dname: String) : DataFrame = {
       /*Gen Event Info to calculate sum of weights*/
    val genevtinfo_gn = "/GenEvtInfo/"
    val genevtinfo_ds: List[String] = List("GenEvtInfo.runNum", "GenEvtInfo.lumisec", "GenEvtInfo.evtNum", "weight", "scalePDF")
    val genevtinfo_pl = getPartitionInfo(dname, genevtinfo_gn+"weight", 100000)
    val genevtinfo_rdd = sc.parallelize(genevtinfo_pl, genevtinfo_pl.length).flatMap(x=> readDatasets(x.dname+x.fname, genevtinfo_gn, genevtinfo_ds, x.begin, x.end))
    val genevtinfo_df = createH5DataFrame(spark, genevtinfo_rdd, genevtinfo_gn)
    genevtinfo_df
  }

}

