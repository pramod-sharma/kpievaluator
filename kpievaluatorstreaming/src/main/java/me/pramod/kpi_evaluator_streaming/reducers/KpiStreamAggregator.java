package me.pramod.kpi_evaluator_streaming.reducers;

import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

/**
 * Created by pramod on 16/10/17.
 */
public class KpiStreamAggregator implements Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> {
    public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> firstEventKpiCount, Tuple2<Integer, Integer> secondEventKpiCount) throws Exception {
        return new Tuple2<Integer, Integer>(firstEventKpiCount._1(), firstEventKpiCount._2() + secondEventKpiCount._2());
    }
}
