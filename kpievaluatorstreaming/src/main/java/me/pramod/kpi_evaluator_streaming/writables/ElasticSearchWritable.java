package me.pramod.kpi_evaluator_streaming.writables;

import me.pramod.kpi_evaluator_streaming.KpiRecord;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pramod on 16/10/17.
 */
public class ElasticSearchWritable implements VoidFunction<JavaRDD<KpiRecord>>{

    private final String kpiCollectionName;
    private final Map<String, String> configurations;

    public ElasticSearchWritable(String kpiCollectionName) {
        Map<String,String> conf = new HashMap<String,String>();
        conf.put("mapred.output.format.class", "org.elasticsearch.hadoop.mr.EsOutputFormat");
//        conf.put(ConfigurationOptions.ES_RESOURCE_WRITE, "kpiHourly/aggregatedKPI");
        conf.put(ConfigurationOptions.ES_NODES, "127.0.0.1");
        conf.put(ConfigurationOptions.ES_MAPPING_ID, "id");
        configurations = conf;
        this.kpiCollectionName = kpiCollectionName;
    }

    public void call(JavaRDD<KpiRecord> kpiRecordJavaRDD) throws Exception {
        String collectionName = "kpirecords/hourlykpidata";
        JavaEsSpark.saveToEs(kpiRecordJavaRDD, collectionName, configurations);
    }
}
