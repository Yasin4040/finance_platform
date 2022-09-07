package com.jtyjy.finance.manager.controller.individual;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketPageVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/25.
 * Time: 15:28
 */
@Api(tags = {"员工个体户收票信息"})
@RestController
@RequestMapping("/api/individualTicket")
public class IndividualEmployeeTicketController {
    //员工个体户收票信息
    //员工个体户
    private  final IndividualEmployeeTicketReceiptInfoService ticketService;

    //员工个体户收票信息
    //员工个体户
    private  final IndividualEmployeeTicketReceiptService mainService;

    public IndividualEmployeeTicketController(IndividualEmployeeTicketReceiptInfoService ticketService, IndividualEmployeeTicketReceiptService mainService) {
        this.ticketService = ticketService;
        this.mainService = mainService;
    }

    /**
     * 员工个体户 档案 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<IndividualTicketVO>> selectPage(@ModelAttribute IndividualTicketQuery query) throws Exception {
        IPage<IndividualTicketVO> page = ticketService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /**
     * 员工个体户 档案 分页模糊查询
     */
    @ApiOperation(value = "新 分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectMainPage")
    public ResponseEntity<PageResult<IndividualTicketPageVO>> selectMainPage(@ModelAttribute IndividualTicketQuery query) throws Exception {
        IPage<IndividualTicketPageVO> page = mainService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }
    /**
     * 获取个体户 收票明细
     */
    @ApiOperation(value = "获取个体户 收票明细", httpMethod = "GET")
    //individualId
    @GetMapping("/getIndividualInfo")
    public ResponseEntity<IndividualTicketInfoDTO> getIndividualInfo(@RequestParam String ticketId){
        IndividualTicketInfoDTO dto  = ticketService.getIndividualInfo(ticketId);
        return ResponseEntity.ok(dto);
    }

    /**
     * add 新增收票信息
     */
    @ApiOperation(value = " add 新增收票信息", httpMethod = "POST")
    @PostMapping("/addTicket")
    public ResponseEntity addTicket(@RequestBody IndividualTicketDTO dto) {

        try {
            ticketService.addTicket(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }




    /**
     * 员工个体户  修改信息
     */
    @ApiOperation(value = "员工个体户  修改信息", httpMethod = "POST")
    @PostMapping("/updateTicket")
    public ResponseEntity updateTicket(@RequestBody IndividualTicketDTO dto) {
        try {
            ticketService.updateTicket(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 删除主表
     */
    @ApiOperation(value = "删除主表", httpMethod = "POST")
    @PostMapping("/delMainTicket")
    public ResponseEntity delMainTicket(@RequestBody List<Long> ids) {
        try {
            ticketService.delTicket(ids);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 员工个体户发票维护  删除
     */
    @ApiOperation(value = "个体户收票信息模板    删除信息", httpMethod = "POST")
    @PostMapping("/deleteTicket")
    public ResponseEntity deleteTicket(String id) {
        try {
            ticketService.removeById(id);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导出
     */
    @ApiOperation(value = "个体户收票信息  导出", httpMethod = "GET")
    @GetMapping("/exportTicket")
    public ResponseEntity exportTicket(@ModelAttribute IndividualTicketQuery query, HttpServletResponse response) throws Exception {
        try {
            query.setPageNum(1);
            query.setPageSize(-1);
            IPage<IndividualTicketVO> individualTicketVOIPage = ticketService.selectPage(query);
            List<IndividualTicketVO> records = individualTicketVOIPage.getRecords();

            EasyExcelUtil.writeExcel(response,records,"员工个体户信息","员工个体户信息", IndividualTicketVO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导入。
     */
    @ApiOperation(value = "个体户收票信息 导入", httpMethod = "POST")
    @PostMapping("/importTicket")
    public ResponseEntity importTicket(@RequestParam("file") MultipartFile multipartFile,HttpServletResponse response) throws Exception {
        try {
            List<IndividualTicketImportErrorDTO> errorDTOList = ticketService.importTicket(multipartFile);
            if (CollectionUtils.isNotEmpty(errorDTOList)) {
                EasyExcelUtil.writeExcel(response, errorDTOList, "个体户收票信息错误明细", "个体户收票信息错误明细", IndividualImportErrorDTO.class);
                return null;
            }
        }catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 下载模板。
     */
    @ApiOperation(value = "个体户收票信息模板  下载模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("个体户收票信息模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualTicketImportDTO.class).sheet("个体户收票信息模板").doWrite(new ArrayList<>());
    }

}
