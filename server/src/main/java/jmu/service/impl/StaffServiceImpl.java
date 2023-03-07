package jmu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jmu.mapper.StaffMapper;
import jmu.pojo.Staff;
import jmu.service.StaffService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
}
