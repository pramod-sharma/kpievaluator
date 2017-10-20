package me.pramod.kpi_evaluator_streaming.mappers;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.lang.Long;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pramod on 16/10/17.
 */
public class KpiStreamMapper implements PairFlatMapFunction<Tuple2<String,String>, Tuple2<String, Integer>, Tuple2<Integer, Integer>> {
    private static final String COMMA_SEPERATOR = ",";
    private final Integer hourOfData;

    public KpiStreamMapper() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        hourOfData = calendar.get(Calendar.HOUR_OF_DAY);
    }

    public Iterator<Tuple2<Tuple2<String, Integer>, Tuple2<Integer, Integer>>> call(Tuple2<String, String> inputKpiRecord) throws Exception {
        List<Tuple2<Tuple2<String, Integer>, Tuple2<Integer, Integer>>> kpiRecords = new ArrayList<Tuple2<Tuple2<String, Integer>, Tuple2<Integer, Integer>>>();
        String inputKpiData = inputKpiRecord._2();
        String[] inputData = inputKpiData.split(COMMA_SEPERATOR);

        if (inputData.length == 4) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(inputData[1]));

            Integer dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

            kpiRecords.add(new Tuple2<Tuple2<String, Integer>, Tuple2<Integer, Integer>>(new Tuple2<String, Integer>(inputData[0], dayOfYear), new Tuple2<Integer, Integer>(hourOfData, 1)));
        }

        return kpiRecords.iterator();
    }
}
