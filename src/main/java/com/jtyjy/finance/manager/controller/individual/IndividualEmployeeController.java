package com.jtyjy.finance.manager.controller.individual;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
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
    @ApiOperation(value = "分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<IndividualEmployeeFilesVO>> selectPage(@ModelAttribute IndividualFilesQuery query) throws Exception {
        IPage<IndividualEmployeeFilesVO> page = filesService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /**
     * 员工个体户  新增
     */
    @ApiOperation(value = "员工个体户  新增", httpMethod = "POST")
    @PostMapping("/add")
    public ResponseEntity addIndividual(@RequestBody IndividualEmployeeFilesDTO dto) throws Exception {

        try {
            filesService.addIndividual(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 户名是否重复 查询
     */
    @ApiOperation(value = "员工个体户  新增 返回数量 >0 就存在", httpMethod = "POST")
    @PostMapping("/findRepeat")
    public ResponseEntity findRepeat(@RequestBody IndividualRepeatDTO dto) throws Exception {
        try {
           Integer exist = filesService.findRepeat(dto);
            return ResponseEntity.ok(exist);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    /**
     * 员工个体户  修改信息
     */
    @ApiOperation(value = "员工个体户  修改信息", httpMethod = "POST")
    @PostMapping("/update")
    public ResponseEntity updateIndividual(@RequestBody IndividualEmployeeFiles file) {
        try {
            filesService.updateIndividual(file);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 员工个体户  修改状态  停用 启用。
     */
    @ApiOperation(value = "员工个体户  修改状态  停用 启用。", httpMethod = "POST")
    @PostMapping("/updateStatus")
    public ResponseEntity updateIndividualStatus(@RequestBody IndividualEmployeeFilesStatusDTO statusDTO) {
        try {
            filesService.updateIndividualStatus(statusDTO);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     *  新增发票。获取基础信息
     */
    @ApiOperation(value = "新增发票。获取基础信息", httpMethod = "GET")
    @GetMapping("/getIndividualInfoList")
    public ResponseEntity getIndividualInfoList(@RequestParam String name) throws Exception {
            IndividualFilesQuery query = new IndividualFilesQuery();
            query.setAccountName(name);
            query.setPage(1);
            query.setRows(-1);
        return this.selectPage(query);
    }

    /**
     * 导出
     */
    @ApiOperation(value = "员工个体户  导出", httpMethod = "GET")
    @GetMapping("/exportIndividual")
    public ResponseEntity exportIndividual(@ModelAttribute IndividualFilesQuery query,HttpServletResponse response) throws Exception {
        // writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz)
        //   @ExcelProperty("字符串标题")

        try {
            List<IndividualExportDTO> exportDTOList  =  filesService.exportIndividual(query);
//            for (int i = 1; i < exportDTOList.size()+1; i++) {
//                IndividualExportDTO individualExportDTO = exportDTOList.get(0);
//                individualExportDTO.setId(i);
//            }
            EasyExcelUtil.writeExcel(response,exportDTOList,"员工个体户信息","员工个体户信息",IndividualExportDTO.class);
//            return null;
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导入。
     */
    @SneakyThrows
    @ApiOperation(value = "员工个体户 导入", httpMethod = "POST")
    @PostMapping("/importIndividual")
    public ResponseEntity importIndividual(@RequestParam("file") MultipartFile multipartFile,HttpServletResponse response) {
        List<IndividualImportErrorDTO> errorDTOList = new ArrayList<>();
        try {
             errorDTOList = filesService.importIndividual(multipartFile);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        if(CollectionUtils.isNotEmpty(errorDTOList)) {
            EasyExcelUtil.writeExcel(response, errorDTOList, "员工个体户错误明细", "员工个体户错误明细", IndividualImportErrorDTO.class);
            return null;
        }
        return ResponseEntity.ok();
    }



    /**
     * 下载模板。
     */
    @ApiOperation(value = "员工个体户  下载模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("员工个体户信息模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualImportDTO.class).sheet("员工个体户信息模板").doWrite(new ArrayList<>());
    }
}
