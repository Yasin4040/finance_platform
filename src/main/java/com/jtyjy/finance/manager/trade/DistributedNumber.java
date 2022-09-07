package com.jtyjy.finance.manager.trade;

import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.finance.manager.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 分布式单号
 *
 * @author User
 */
@Component
public class DistributedNumber {

    @Autowired
    private RedisClient redis;

    @Value("${bx.redis.key}")
    private String BX_REDIS_KEY;
    @Value("${invoice.redis.key}")
    private String INVOICE_REDIS_KEY;


    @Value("${tc.redis.key}")
    private String TC_REDIS_KEY;

    @Value("${hk.redis.key}")
    private String HK_REDIS_KEY;

    @Value("${fk.redis.key}")
    private String FK_REDIS_KEY;

    @Value("${nzj.redis.key}")
    private String NZJ_REDIS_KEY;

    @Value("${yzj.redis.key}")
    private String YZJ_REDIS_KEY;

    @Value("${cj.redis.key}")
    private String CJ_REDIS_KEY;

    @Value("${jk.redis.key}")
    private String JK_REDIS_KEY;

    @Value("${xm.redis.key}")
    private String XM_REDIS_KEY;

    @Value("${num.len}")
    private Integer NUM_LEN;

    @Value("${num.ttl}")
    private Integer NUM_TTL;

    @Value("${bx.num.ttl}")
    private Integer BX_NUM_TTL;

    @Value("${cj.num.len}")
    private Integer CJ_NUM_LEN;

    @Value("${month.num.ttl}")
    private Integer MONTH_NUM_TTL;

    @Value("${num.fill.value}")
    private String NUM_FILL_VALUE;

    /**
     * 获取报销单号
     */
    public String getBxdNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.BX_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 4, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取收票流水号
     */
    public String getInvoiceNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.INVOICE_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 4, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取项目单号
     */
    public String getXmNum() {
        //组装key
        String key = Constants.FORMAT_6.format(new Date());
        key = this.XM_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, this.NUM_LEN, this.MONTH_NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取提成单号
     */
    public String getExtractNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.TC_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 3, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取借款单号
     */
    public String getLendNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.JK_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 5, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取还款单号
     */
    public String getRepayNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.HK_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 5, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取付款单号
     */
    public String getPaymoneyNum() {
        //组装key
        String key = Constants.FORMAT_8.format(new Date());
        key = this.FK_REDIS_KEY + key;
        return this.redis.getRedisIncrNum(key, 5, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取年度动因追加单号
     */
    public String getYearAgentAddNum() {
        //
        String key = this.NZJ_REDIS_KEY + Constants.FORMAT_8.format(new Date());
        return this.redis.getRedisIncrNum(key, 3, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取月度动因追加单号
     */
    public String getMonthAgentAddNum() {
        // 组装key
        String key = this.YZJ_REDIS_KEY + Constants.FORMAT_8.format(new Date());
        return this.redis.getRedisIncrNum(key, 3, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

    /**
     * 获取年度动因拆借单号
     */
    public String getYearAgentLendNum() {
        // 组装key
        String key = this.CJ_REDIS_KEY + Constants.FORMAT_8.format(new Date());
        return this.redis.getRedisIncrNum(key, this.CJ_NUM_LEN, this.NUM_TTL, this.NUM_FILL_VALUE);
    }

}
