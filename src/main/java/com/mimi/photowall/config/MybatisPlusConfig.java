package com.mimi.photowall.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 * 配置分页插件和自动填充
 * <p>
 * 数据隔离说明：
 * 本项目采用 BaseService 方式实现数据隔离，而非 MyBatis-Plus 数据权限插件。
 * 原因：更简单直观，易于理解和维护。
 * <p>
 * 使用方式：
 * 1. Service 类继承 BaseService
 * 2. 查询时使用 createUserQuery() 方法，自动添加 user_id 条件
 * 3. 查询详情时使用 checkDataOwnership() 方法，校验数据归属
 * <p>
 * 示例：
 * <pre>
 * public class PhotoService extends BaseService {
 *     public List<Photo> getUserPhotos() {
 *         QueryWrapper<Photo> wrapper = createUserQuery();
 *         return photoMapper.selectList(wrapper);
 *     }
 *
 *     public Photo getPhoto(Long id) {
 *         Photo photo = photoMapper.selectById(id);
 *         checkDataOwnership(photo.getUserId());
 *         return photo;
 *     }
 * }
 * </pre>
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
