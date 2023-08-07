package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void saveWithDish(SetmealDTO setmealDTO);

    SetmealVO getByIdWithDish(Long setmealId);

    void updateWithDish(SetmealVO setmealVO);

    void deleteBatch(@Param("setmealIds") List<Long> setmealIds);

    void startOrStop(Integer status, Long id);

}
