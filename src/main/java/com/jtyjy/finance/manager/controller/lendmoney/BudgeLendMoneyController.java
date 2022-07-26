package com.jtyjy.finance.manager.controller.lendmoney;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.tools.DeviceTools;
import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.jtyjy.finance.manager.bean.BudgetLendmoneyUselog;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.event.lendmoney.FkCodeRequest;
import com.jtyjy.finance.manager.event.lendmoney.FkCodeUtil;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetLendmoneyService;
import com.jtyjy.finance.manager.service.WbUserService;
import com.jtyjy.finance.manager.service.WeChatService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.RequestAnswerTool;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetPayMoneyDetailVO;
import com.jtyjy.finance.manager.vo.BudgetRepayMoneyDetailVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author User
 */
@Api(tags = {"借款管理-员工借款"})
@RestController
@CrossOrigin
@RequestMapping("/api/lendMoney")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgeLendMoneyController extends BaseController<BudgetLendmoney> {

    @Value("${service.domain}")
    private String domain;

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final WbUserService userService;
    private final WeChatService weChatService;
    private final MessageSender messageSender;
    private final BudgetLendmoneyService budgetLendmoneyService;

    @ApiOperation(value = "查询员工借款（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工姓名/工号", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "还款状态 0:未还清 1:已还清", name = "paymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "借款单类型 11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款 15: 合同借款 16：非合同借款", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "借款日期", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "借款单号", name = "lendMoneyCode", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listLendMoneyPage")
    public ResponseEntity<PageResult<BudgetLendMoneyVO>> listLendMoneyPage(String name,
                                                                           String lendMoneyCode,
                                                                           Integer paymentStatus,
                                                                           Integer payMoneyStatus,
                                                                           Integer lendType,
                                                                           @DateTimeFormat(pattern = "yyyy-MM-dd") Date lendDate,
                                                                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                           @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("paymentStatus", paymentStatus);
        paramMap.put("payMoneyStatus", payMoneyStatus);
        paramMap.put("lendMoneyCode", lendMoneyCode);
        paramMap.put("lendType", lendType);
        paramMap.put("lendDate", lendDate != null ? Constants.FORMAT_10.format(lendDate) : null);
        return ResponseEntity.ok(this.budgetLendmoneyService.listLendMoneyPage(page, rows, paramMap));
    }

    @ApiOperation(value = "查询员工还款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "借款Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getRepayMoneyDetail")
    public ResponseEntity<PageResult<BudgetRepayMoneyDetailVO>> getRepayMoneyDetail(@RequestParam Long id,
                                                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                    @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.getRepayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "查询员工付款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "借款Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getPayMoneyDetail")
    public ResponseEntity<PageResult<BudgetPayMoneyDetailVO>> getPayMoneyDetail(@RequestParam Long id,
                                                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.getPayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "锁定明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "借款Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/lendByBxLocked")
    public ResponseEntity<PageResult<BudgetLendmoneyUselog>> lendByBxLocked(@RequestParam Long id,
                                                                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                            @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.lendByBxLocked(page, rows, id));
    }

    @ApiOperation(value = "现金还款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "借款Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "还款金额", name = "money", dataType = "Double", required = true),
            @ApiImplicitParam(value = "计划还款时间", name = "planPayTime", dataType = "Date", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/cashRepayMoney")
    public ResponseEntity<String> cashRepayMoney(@RequestParam Long id,
                                                 @RequestParam BigDecimal money,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date planPayTime) throws Exception {
        if (planPayTime == null) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetLendmoneyService.cashRepayMoney(id, money, planPayTime);
        return ResponseEntity.ok("现金还款成功");
    }

    @ApiOperation(value = "还款导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportRepayMoneyTemplate")
    public void exportRepayMoneyTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportRepayMoneyExcelFile(null, EasyExcelUtil.getOutputStream("导入还款模板", response));
    }

    @ApiOperation(value = "还款导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importRepayMoney")
    public ResponseEntity<String> importRepayMoney(@RequestParam("file") MultipartFile srcFile) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetLendmoneyService.importRepayMoney(excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_还款导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_repayMoneyErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "还款导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_repayMoneyErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("还款导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorMap = JSON.parseObject(errorData, List.class);

        // 文件导出
        ResponseUtil.exportRepayMoneyExcelFile(errorMap, EasyExcelUtil.getOutputStream("还款导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    @ApiOperation(value = "员工借款明细导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工姓名/工号", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "还款状态 0:未还清 1:已还清", name = "paymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "借款单类型 11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款 15: 合同借款 16：非合同借款", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "借款日期", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportLendMoney")
    public void exportLendMoney(String name,
                                Integer paymentStatus,
                                Integer payMoneyStatus,
                                Integer lendType,
                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date lendDate,
                                HttpServletResponse response) throws Exception {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("payMoneyStatus", payMoneyStatus);
        paramMap.put("paymentStatus", paymentStatus);
        paramMap.put("lendType", lendType);
        paramMap.put("lendDate", lendDate != null ? Constants.FORMAT_10.format(lendDate) : null);

        List<List<String>> dataList = this.budgetLendmoneyService.exportLendMoney(paramMap);

        // 文件导出
        ResponseUtil.exportLendMoneyExcelFile(dataList, EasyExcelUtil.getOutputStream("员工借款明细表", response));
    }

    @ApiOperation(value = "通过报销人获取借款信息", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "搜索关键字", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getUserLendMoneyByBxr")
    public ResponseEntity<PageResult<BudgetLendMoneyVO>> getUserLendMoneyByBxr(@RequestParam String name,
                                                                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                               @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.getUserLendMoneyByBxr(name, page, rows));
    }

    @GetMapping("code")
    @NoLoginAnno
    public void code(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FkCodeRequest codeRequest = null;
        boolean mobileDevice = DeviceTools.isMobileDevice(request);
        //是否企业微信
        boolean weChat = DeviceTools.iswxWork(request);
        if (mobileDevice && !weChat) {
            throw new Exception("请使用企业微信或扫码枪扫码！！");
        }
        //从企业微信获取
        if (mobileDevice && weChat) {
            String code = request.getParameter("code");
            if (StringUtils.isEmpty(code)) {
                String _url = this.domain + RequestAnswerTool.getUrlSAndParameter(request);
                String redirect_uri = URLEncoder.encode(_url, "UTF-8");
                String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx08fc8ba1546d5bac&redirect_uri=" + redirect_uri + "&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
                response.sendRedirect(url);
                return;
            }
            codeRequest = FkCodeUtil.getWeChatRequest(request, this.weChatService);
        }
        //从优库扫码枪获取
        if (codeRequest == null) {
            codeRequest = FkCodeUtil.getGunRequest(request);
        }
        if (codeRequest == null) {
            throw new Exception("获取扫码信息失败！！");
        }
        //设置用户
        WbUser user = this.userService.getByEmpNo(codeRequest.getEmpNo());
        UserThreadLocal.set(user);
        BudgetLendmoney bean = null;
        int success = this.budgetLendmoneyService.updateLendPayStatus(codeRequest, bean);
        String content = "扫码成功！";
        if (success > 0 && null != bean) {
            content = "借款单【" + bean.getLendmoneycode() + "】接收成功。";
        }
        messageSender.sendQywxMsg(new QywxTextMsg(codeRequest.getEmpNo(), null, null, 0, content, 0));
        UserThreadLocal.remove();
        return;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 移动支付平台获取借款单
     */
    @ApiOperation(value = "移动支付平台获取借款单", httpMethod = "GET")
    @NoLoginAnno
    @PostMapping(value = "/getBudgetLendMoneyList")
    public Map<String, Object> getLendMoneyList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(5);
        try {
            result.put("data", this.budgetLendmoneyService.getBudgetLendMoneyList(params));
            result.put("code", 0);
            result.put("message", "成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 移动支付项目获取还款记录
     */
    @ApiOperation(value = "移动支付项目获取还款记录", httpMethod = "GET")
    @NoLoginAnno
    @GetMapping(value = "/getRepaymoneyList")
    public Map<String, Object> getRepayMoneyList(@RequestParam(value = "lendMoneyId", required = false) Integer lendMoneyId,
                                                 @RequestParam(value = "startDate", required = false) String startDate,
                                                 @RequestParam(value = "endDate", required = false) String endDate,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer rows) {
        Map<String, Object> result = new HashMap<>(5);
        try {
            List<Map<String, Object>> repayMoneyList = this.budgetLendmoneyService.getRepayMoneyList(lendMoneyId, startDate, endDate);
            List<Map<String, Object>> data1 = repayMoneyList.stream().skip((long) rows * (page - 1)).limit(rows).collect(Collectors.toList());
            result.put("data", data1);
            result.put("total", repayMoneyList.size());
            result.put("code", 0);
            result.put("message", "成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 移动支付回调
     */
    @ApiOperation(value = "移动支付回调", httpMethod = "GET")
    @NoLoginAnno
    @GetMapping(value = "/callback/repayMoney")
    public Map<String, Object> repayMoney(@RequestParam(value = "lendMoneyId") Long lendMoneyId,
                                          @RequestParam(value = "repayMoney") String repayMoney) throws Exception {
        Map<String, Object> result = new HashMap<>(2);
        try {
            this.budgetLendmoneyService.moblieRepayMoney(lendMoneyId, repayMoney);
            result.put("code", 0);
            result.put("message", "成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

}
