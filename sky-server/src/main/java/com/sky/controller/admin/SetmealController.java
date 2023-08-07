package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "套餐相关接口")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询: {}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐: {}",setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable("id") Long setmealId){
        log.info("新增套餐: {}",setmealId);
        SetmealVO setmealVO = setmealService.getByIdWithDish(setmealId);
        return Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealVO setmealVO){
        log.info("修改套餐: {}",setmealVO);
        setmealService.updateWithDish(setmealVO);
        return Result.success();
    }
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam("ids") List<Long> setmealIds){
        log.info("修改套餐: {}",setmealIds);
        setmealService.deleteBatch(setmealIds);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("套餐起售、停售: {},{}",status,id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }


}
