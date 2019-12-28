package cn.itsource.hrm.service.impl;

import cn.itsource.hrm.client.RedisClient;
import cn.itsource.hrm.domain.CourseType;
import cn.itsource.hrm.mapper.CourseTypeMapper;
import cn.itsource.hrm.service.ICourseTypeService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
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

    @Autowired
    private RedisClient redisClient;
    private final String COURSE_TYPE = "hrm:course_type:treeData";

    @Override
    public List<CourseType> localTree() {
        //从redis中查询数据
        String courseTypesStr = redisClient.get(COURSE_TYPE);
        List<CourseType> list = null;
        //判断
        if(StringUtils.isNotEmpty(courseTypesStr)){
            //如果存在
            //json字符串转java集合
            list = JSONObject.parseArray(courseTypesStr, CourseType.class);
        }else{
            //如果不存在，则查询数据库
            list = localTreeData();
            //list集合转json字符串
            String jsonStr = JSONObject.toJSONString(list);
            //保存到redis中
            redisClient.set(COURSE_TYPE, jsonStr);
        }
        //返回数据
        return list;
    }



    /**
     * map+for循环
     * @return
     */

    public List<CourseType> localTreeData(){
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

    /**
     * 增删改同步操作
     */
    private void synOperate() {
        List<CourseType> list = localTreeData();
        //list集合转json字符串
        String jsonStr = JSONObject.toJSONString(list);
        //保存到redis中
        redisClient.set(COURSE_TYPE, jsonStr);
    }


    @Override
    public boolean save(CourseType entity) {
        super.save(entity);
        synOperate();
        return true;
    }


    @Override
    public boolean removeById(Serializable id) {
        super.removeById(id);
        synOperate();
        return true;
    }

    @Override
    public boolean updateById(CourseType entity) {
        super.updateById(entity);
        synOperate();
        return true;
    }


}
