package com.arc.nas.controller.media.cms;

import com.arc.nas.service.system.common.impl.MQMocker;
import com.arc.nas.service.system.common.impl.MqTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 媒体管理（CMS）：内容管理
 */
@RestController
@RequestMapping("/media/tool/mq")
public class MQMockerRestController {

    private static final Logger log = LoggerFactory.getLogger(MQMockerRestController.class);

    private final MQMocker mqMocker;

    public MQMockerRestController(
            MQMocker mqMocker) {
        this.mqMocker = mqMocker;

    }

    @PostMapping("/send")
    public ResponseEntity<Object> send(@RequestBody MqTask mqTask) {
        mqMocker.send(mqTask.getTopic(), mqTask.getPayload());
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/mq/status")
    public String getStatus() {
        return "当前MQ状态: " + mqMocker.getCurrentState();
    }

    @PostMapping("/mq/pause")
    public String pause() {
        mqMocker.pause();
        return "SUCCESS";
    }

    @PostMapping("/mq/resume")
    public String resume() {
        mqMocker.resume();
        return "SUCCESS";
    }

    /**
     * 手动塞入并立即触发消费
     */
    @PostMapping("/pushAndTrigger")
    public ResponseEntity<String> pushAndTrigger(@RequestBody MqTask mqTask) {
        // 1. 发送消息入库
        mqMocker.send(mqTask.getTopic(), mqTask.getPayload());

        // 2. 延迟极短时间（确保数据库写入完成，尤其是针对某些有写入延迟的 DB）
        // 然后立即触发消费逻辑
        mqMocker.forceDispatch(mqTask.getTopic(), payload -> {
            log.info(">>> [即时消费测试] 内容: {}", payload);
        });

        return ResponseEntity.ok("Message pushed and force dispatch called.");
    }

    /**
     * 仅消费已存在的任务
     */
    @PostMapping("/pollAndConsume")
    public ResponseEntity<String> pollAndConsume(@RequestParam String topic) {
        // 直接去库里找这个 topic 的活儿干
        mqMocker.forceDispatch(topic, payload -> {
            log.info(">>> [手动补偿消费] 内容: {}", payload);
        });

        return ResponseEntity.ok("Force dispatch executed for topic: " + topic);
    }
}

