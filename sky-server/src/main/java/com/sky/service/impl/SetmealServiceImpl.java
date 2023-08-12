package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //处理DTO对象-(转换成entity实体类对象)

        Setmeal setmeal=new Setmeal();
        //拷贝属性至entity对象上
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //insert
        setmealMapper.insert(setmeal);

        //获取套餐id,使得当前DTO中所有dish在转换成单独对象之后依然与相应的套餐进行绑定
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();//

        if(setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                //绑定套餐id
                setmealDish.setSetmealId(setmealId);
            });
            //insert
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    @Override
    public SetmealVO getByIdWithDish(Long setmealId) {
        SetmealVO setmealVO = new SetmealVO();

        Setmeal setmeal = setmealMapper.getById(setmealId);
        List<SetmealDish> setmealDishes= setmealDishMapper.getBySetmealId(setmealId);
        if(setmeal!=null)
            BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealVO setmealVO) {
        Setmeal setmeal = new Setmeal();
        List<SetmealDish> setmealDishes = setmealVO.getSetmealDishes();

        Long setmealId = setmealVO.getId();

        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealIds(Arrays.asList(setmealId));
        if(setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                //绑定套餐id
                setmealDish.setSetmealId(setmealId);
            });
            //insert
            setmealDishMapper.insertBatch(setmealDishes);
        }        
        

    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> setmealIds) {
        // 删除前置条件-状态-查询
        for (Long setmealId : setmealIds){
            Setmeal setmeal = setmealMapper.getById(setmealId);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(setmealIds);
        setmealDishMapper.deleteBySetmealIds(setmealIds);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal=Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);
    }

    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
