package com.jtyjy.finance.manager.controller.individual;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.query.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public IndividualEmployeeTicketController(IndividualEmployeeTicketReceiptInfoService ticketService) {
        this.ticketService = ticketService;
    }

//    /**
//     * 员工个体户 分页模糊查询
//     */
//    @ApiOperation(value = "分页模糊查询", httpMethod = "GET")
//    @GetMapping("/selectPage")
//    public ResponseEntity<PageResult<IndividualEmployeeFilesVO>> selectPage(@ModelAttribute IndividualFilesQuery query) throws Exception {
//        IPage<IndividualEmployeeFilesVO> page = ticketService.selectPage(query);
//        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
//    }
}
