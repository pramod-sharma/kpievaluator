package me.pramod.kpi_evaluator_streaming;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import me.pramod.kpi_evaluator_streaming.mappers.KpiRecordMapper;
import me.pramod.kpi_evaluator_streaming.mappers.KpiStreamMapper;
import me.pramod.kpi_evaluator_streaming.reducers.KpiStreamAggregator;
import me.pramod.kpi_evaluator_streaming.writables.ElasticSearchWritable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.yaml.snakeyaml.Yaml;
import scala.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pramod on 16/10/17.
 */
public final class KpiStreamingJob {
    private static KpiJobConfiguration jobConfiguration;

    public static void main(String[] args) throws IOException, InterruptedException {
        loadDefaultConfiguration();
        parseArguments(args);

        SparkConf sparkConf = new SparkConf().setAppName("KPIAggregator");
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, Durations.minutes (jobConfiguration.getBatchSizeInMinutes()));
        String zookeeperQuorum = new StringBuilder().append(jobConfiguration.getZookeeperIp()).append(":").
                append(jobConfiguration.getZookeeperPort()).toString();

        Map<String, Integer> topicToConsumerThreadsCount = new HashMap<String, Integer>();
        topicToConsumerThreadsCount.put(jobConfiguration.getTopic(), jobConfiguration.getNumThreads());

        JavaPairReceiverInputDStream<String, String> kpiStream = KafkaUtils.createStream(ssc, zookeeperQuorum
                , jobConfiguration.getGroup(), topicToConsumerThreadsCount);

        JavaPairDStream<Tuple2<String, Integer>, Tuple2<Integer, Integer>> kpiStreamData = kpiStream.flatMapToPair(new KpiStreamMapper());

        JavaPairDStream<Tuple2<String, Integer>, Tuple2<Integer, Integer>> aggregatedKpiPairStreamData = kpiStreamData.reduceByKey(new KpiStreamAggregator());

        JavaDStream<KpiRecord> aggregatedKpiStreamData = aggregatedKpiPairStreamData.map(new KpiRecordMapper());

        aggregatedKpiStreamData.foreachRDD(new ElasticSearchWritable(jobConfiguration.getKpiCollectionName()));

        ssc.start();
        ssc.awaitTermination();
    }

    private static void loadDefaultConfiguration() {
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.yml");
        Yaml yaml = new Yaml();

        jobConfiguration = yaml.loadAs(configStream, KpiJobConfiguration.class);
    }

    private static void parseArguments(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        final OptionSpec<String> zookeeperIpOpt = parser.acceptsAll(Arrays.asList("z", "zookeeperIp"), "Zookeeper IP").withRequiredArg()
                .ofType(String.class).defaultsTo(jobConfiguration.getZookeeperIp());
        final OptionSpec<Integer> zookeeperPortOpt = parser.acceptsAll(Arrays.asList("p", "zookeeperPort"), "Zookeeper Port")
                .withRequiredArg().ofType(Integer.class).defaultsTo(jobConfiguration.getZookeeperPort());
        final OptionSpec<String> topicOpt = parser.acceptsAll(Arrays.asList("t", "topic"), "Compress Output")
                .withRequiredArg().ofType(String.class).defaultsTo(jobConfiguration.getTopic());
        final OptionSpec<String> groupOpt = parser.acceptsAll(Arrays.asList("g", "group"), "Interpret Time in Unit")
                .withRequiredArg().ofType(String.class).defaultsTo(jobConfiguration.getGroup());
        final OptionSpec<Integer> numThreadsOpt = parser.acceptsAll(Arrays.asList("n", "numThreads"), "Number of threads for kafka consumer")
                .withRequiredArg().ofType(Integer.class).defaultsTo(jobConfiguration.getNumThreads());
        final OptionSpec<Integer> batchSizeInMinutesOpt = parser.acceptsAll(Arrays.asList("b", "batchSizeInMinutes"), "Size of Processing Batch in minutes")
                .withRequiredArg().ofType(Integer.class).defaultsTo(jobConfiguration.getBatchSizeInMinutes());
        final OptionSpec<String> kpiCollectionNameOpt = parser.acceptsAll(Arrays.asList("c", "kpiCollectionName"), "Collection Name of KPI in Elastic Search")
                .withRequiredArg().ofType(String.class).defaultsTo(jobConfiguration.getKpiCollectionName());
        final OptionSpec<Void> helpOpt = parser.acceptsAll(Arrays.asList("h", "help"), "Print help(add true)").forHelp();

        final OptionSet opts = parser.parse(args);

        if (checkOptPresence(opts, helpOpt)) {
            parser.printHelpOn(System.err);
            System.exit(0);
        }

        jobConfiguration.setZookeeperIp(zookeeperIpOpt.value(opts));
        jobConfiguration.setZookeeperPort(zookeeperPortOpt.value(opts));
        jobConfiguration.setTopic(topicOpt.value(opts));
        jobConfiguration.setGroup(groupOpt.value(opts));
        jobConfiguration.setNumThreads(numThreadsOpt.value(opts));
        jobConfiguration.setBatchSizeInMinutes(batchSizeInMinutesOpt.value(opts));
        jobConfiguration.setKpiCollectionName(kpiCollectionNameOpt.value(opts));
    }

    private static <Y> boolean checkOptPresence(OptionSet opts, OptionSpec<Y> option) {
        boolean argumentPresent = true;
        if (option instanceof ArgumentAcceptingOptionSpec) {
            argumentPresent = opts.hasArgument(option);
        }
        final boolean valid = opts.has(option) && argumentPresent;
        return valid;
    }
}
