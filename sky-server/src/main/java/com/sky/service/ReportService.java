package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @Author Cheems
 * @Date 2023/8/22 22:20
 * @PackageName:com.sky.service
 * @ClassName: ReportServiceImpl
 * @Description:
 * @Version 1.0
 */

public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesRankingTop10(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}
