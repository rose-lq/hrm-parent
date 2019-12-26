package cn.itsource.hrm.service.impl;

import cn.itsource.hrm.domain.CourseType;
import cn.itsource.hrm.mapper.CourseTypeMapper;
import cn.itsource.hrm.service.ICourseTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程目录 服务实现类
 * </p>
 *
 * @author luqiao
 * @since 2019-12-26
 */
@Service
public class CourseTypeServiceImpl extends ServiceImpl<CourseTypeMapper, CourseType> implements ICourseTypeService {
    @Override
    public List<CourseType> localTree(){
        //存放父级
        List<CourseType> firstTypes = new ArrayList<>();
        //查询所有类型
        List<CourseType> courseTypes = baseMapper.selectList(null);
        //将courseTypes的数据存入map中
        Map<Long,CourseType> map=new HashMap<>();
        for (CourseType courseType : courseTypes) {
            map.put(courseType.getId(),courseType);
        }
        //分配一级和非一级
        for (CourseType courseType : courseTypes) {
            if (courseType.getPid()==0L){
                firstTypes.add(courseType);
            }else {
                CourseType parent = map.get(courseType.getPid());
                if (parent!=null){
                    parent.getChildren().add(courseType);
                }
            }
        }

        return firstTypes;
    }

}
