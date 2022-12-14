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
@Api(tags = {"????????????-????????????"})
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

    @ApiOperation(value = "??????????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "????????????/??????", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "???????????? 0:????????? 1:?????????", name = "paymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "???????????? 0:???????????????1??????????????????2??????????????????3???????????????", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????? 11??????????????? 12??????????????? 13????????????????????????????????? 14?????????????????? 15: ???????????? 16??????????????????", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "????????????", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "????????????", name = "lendMoneyCode", dataType = "String"),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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

    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getRepayMoneyDetail")
    public ResponseEntity<PageResult<BudgetRepayMoneyDetailVO>> getRepayMoneyDetail(@RequestParam Long id,
                                                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                    @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.getRepayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getPayMoneyDetail")
    public ResponseEntity<PageResult<BudgetPayMoneyDetailVO>> getPayMoneyDetail(@RequestParam Long id,
                                                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.getPayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/lendByBxLocked")
    public ResponseEntity<PageResult<BudgetLendmoneyUselog>> lendByBxLocked(@RequestParam Long id,
                                                                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                            @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetLendmoneyService.lendByBxLocked(page, rows, id));
    }

    @ApiOperation(value = "????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "????????????", name = "money", dataType = "Double", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "planPayTime", dataType = "Date", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/cashRepayMoney")
    public ResponseEntity<String> cashRepayMoney(@RequestParam Long id,
                                                 @RequestParam BigDecimal money,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date planPayTime) throws Exception {
        if (planPayTime == null) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetLendmoneyService.cashRepayMoney(id, money, planPayTime);
        return ResponseEntity.ok("??????????????????");
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportRepayMoneyTemplate")
    public void exportRepayMoneyTemplate(HttpServletResponse response) throws Exception {
        // ????????????
        ResponseUtil.exportRepayMoneyExcelFile(null, EasyExcelUtil.getOutputStream("??????????????????", response));
    }

    @ApiOperation(value = "????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importRepayMoney")
    public ResponseEntity<String> importRepayMoney(@RequestParam("file") MultipartFile srcFile) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetLendmoneyService.importRepayMoney(excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_????????????????????????_" + System.currentTimeMillis() + ".json";

            // ????????????????????????
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // ??????Redis????????????, ?????????????????????
            this.redisClient.set(empNo + "_repayMoneyErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_repayMoneyErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("?????????????????????????????????????????????");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorMap = JSON.parseObject(errorData, List.class);

        // ????????????
        ResponseUtil.exportRepayMoneyExcelFile(errorMap, EasyExcelUtil.getOutputStream("????????????????????????", response));

        // ????????????
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "????????????/??????", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "???????????? 0:????????? 1:?????????", name = "paymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "???????????? 0:???????????????1??????????????????2??????????????????3???????????????", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????? 11??????????????? 12??????????????? 13????????????????????????????????? 14?????????????????? 15: ???????????? 16??????????????????", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "????????????", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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

        // ????????????
        ResponseUtil.exportLendMoneyExcelFile(dataList, EasyExcelUtil.getOutputStream("?????????????????????", response));
    }

    @ApiOperation(value = "?????????????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "???????????????", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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
        //??????????????????
        boolean weChat = DeviceTools.iswxWork(request);
        if (mobileDevice && !weChat) {
            throw new Exception("?????????????????????????????????????????????");
        }
        //?????????????????????
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
        //????????????????????????
        if (codeRequest == null) {
            codeRequest = FkCodeUtil.getGunRequest(request);
        }
        if (codeRequest == null) {
            throw new Exception("??????????????????????????????");
        }
        //????????????
        WbUser user = this.userService.getByEmpNo(codeRequest.getEmpNo());
        UserThreadLocal.set(user);
        BudgetLendmoney bean = null;
        int success = this.budgetLendmoneyService.updateLendPayStatus(codeRequest, bean);
        String content = "???????????????";
        if (success > 0 && null != bean) {
            content = "????????????" + bean.getLendmoneycode() + "??????????????????";
        }
        messageSender.sendQywxMsg(new QywxTextMsg(codeRequest.getEmpNo(), null, null, 0, content, 0));
        UserThreadLocal.remove();
        return;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * ?????????????????????????????????
     */
    @ApiOperation(value = "?????????????????????????????????", httpMethod = "GET")
    @NoLoginAnno
    @PostMapping(value = "/getBudgetLendMoneyList")
    public Map<String, Object> getLendMoneyList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(5);
        try {
            result.put("data", this.budgetLendmoneyService.getBudgetLendMoneyList(params));
            result.put("code", 0);
            result.put("message", "??????");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * ????????????????????????????????????
     */
    @ApiOperation(value = "????????????????????????????????????", httpMethod = "GET")
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
            result.put("message", "??????");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", httpMethod = "GET")
    @NoLoginAnno
    @GetMapping(value = "/callback/repayMoney")
    public Map<String, Object> repayMoney(@RequestParam(value = "lendMoneyId") Long lendMoneyId,
                                          @RequestParam(value = "repayMoney") String repayMoney) throws Exception {
        Map<String, Object> result = new HashMap<>(2);
        try {
            this.budgetLendmoneyService.moblieRepayMoney(lendMoneyId, repayMoney);
            result.put("code", 0);
            result.put("message", "??????");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", e.getMessage());
        }
        return result;
    }

}
