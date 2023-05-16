package top.rectorlee.interfacelimit.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.rectorlee.constant.SystemConstant;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lee
 * @description sentinel限流
 * @date 2023-05-11  12:36:55
 */
@Slf4j
@Component
public class SentinelConfig {
    @PostConstruct
    private void init() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule rule = new FlowRule(SystemConstant.PRODUCT_LIST_KEY);
        rule.setCount(2);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);

        FlowRuleManager.loadRules(rules);
    }
}
