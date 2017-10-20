package me.pramod.kpi_evaluator_streaming.mappers;

import me.pramod.kpi_evaluator_streaming.KpiRecord;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

/**
 * Created by pramod on 20/10/17.
 */
public class KpiRecordMapper implements Function<Tuple2<Tuple2<String,Integer>,Tuple2<Integer,Integer>>, KpiRecord> {
    public KpiRecord call(Tuple2<Tuple2<String, Integer>, Tuple2<Integer, Integer>> kpiData) throws Exception {
        String userId = kpiData._1()._1();
        Integer dayOfYear = kpiData._1()._2();
        Integer hourOfDay = kpiData._2()._1();
        Integer kpiCOunt = kpiData._2()._2();


        KpiRecord kpiRecord = new KpiRecord(userId, hourOfDay, dayOfYear, kpiCOunt);
        return kpiRecord;
    }
}
