package me.pramod.kpi_evaluator_streaming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by pramod on 16/10/17.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KpiJobConfiguration implements Serializable {
    private String zookeeperIp;
    private Integer zookeeperPort;
    private String topic;
    private String group;
    private Integer numThreads;
    private Integer batchSizeInMinutes;
    private String kpiCollectionName;
}
