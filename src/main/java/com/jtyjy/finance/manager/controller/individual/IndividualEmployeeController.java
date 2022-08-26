package com.jtyjy.finance.manager.controller.individual;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRule;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.query.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/25.
 * Time: 15:28
 */
@Api(tags = {"员工个体户档案"})
@RestController
@RequestMapping("/api/individualEmployee")
public class IndividualEmployeeController {
    //员工个体户
    private  final IndividualEmployeeFilesService filesService;

    public IndividualEmployeeController(IndividualEmployeeFilesService filesService) {
        this.filesService = filesService;
    }

    /**
     * 员工个体户 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "POST")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<IndividualEmployeeFilesVO>> selectPage(IndividualFilesQuery query) throws Exception {
        Page<IndividualEmployeeFilesVO> page = filesService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

}
