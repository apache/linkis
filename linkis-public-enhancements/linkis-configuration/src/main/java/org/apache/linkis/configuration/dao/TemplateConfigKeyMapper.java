package org.apache.linkis.configuration.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.linkis.configuration.entity.TemplateConfigKey;

/**
 * template_config_key表的dao接口类
 * @Description
 * @version 1.0
 * @author webank
 */
public interface TemplateConfigKeyMapper {

    /**
     * 根据主键删除数据库的记录
     *
     * @param id
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 新写入数据库记录
     *
     * @param templateConfigKey
     */
    int insert(TemplateConfigKey templateConfigKey);

    /**
     * 批量插入记录，建议List一次不要超过1000条
     *
     * @param list
     */
    int batchInsertList(List<TemplateConfigKey> list);

    /**
     * 动态字段,写入数据库记录
     *
     * @param templateConfigKey
     */
    int insertSelective(TemplateConfigKey templateConfigKey);

    /**
     * 根据指定主键获取一条数据库记录
     *
     * @param id
     */
    TemplateConfigKey selectByPrimaryKey(Long id);

    /**
     * 查询分页数据条数 - 示例方法
     *
     * @param id
     */
    int selectCountByPage(Long id);

    /**
     * 查询分页数据列表 - 示例方法
     * public DataPage<TemplateConfigKey> selectByPage(Id id, int pageNo, int pageSize) {
     *        if (pageNo > 100) {
     *            pageNo = 100;
     *        }
     *        if (pageNo < 1) {
     *            pageNo = 1;
     *        }
     *        if (pageSize > 50) {
     *            pageSize = 50;
     *        }
     *        if (pageSize < 1) {
     *            pageSize = 1;
     *        }
     *        int totalCount = templateConfigKeyDAO.selectCountByPage(id);
     *        List<CreditLogEntity> list = templateConfigKeyDAO.selectListByPage(id, pageNo, pageSize);
     *        DataPage<TemplateConfigKey> dp = new DataPage<>(list, pageSize, pageNo, totalCount);
     *        return dp;
     *    }
     *
     * @param id
     * @param pageNo
     * @param pageSize
     */
    List<TemplateConfigKey> selectListByPage(@Param("id") Long id, @Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 动态字段,根据主键来更新符合条件的数据库记录
     *
     * @param templateConfigKey
     */
    int updateByPrimaryKeySelective(TemplateConfigKey templateConfigKey);

    /**
     * 根据主键来更新符合条件的数据库记录
     *
     * @param templateConfigKey
     */
    int updateByPrimaryKey(TemplateConfigKey templateConfigKey);

    // === 下方为用户自定义模块,下次生成会保留 ===
    List<TemplateConfigKey> selectListByTemplateUuid(@Param("templateUuid") String templateUuid);


    int deleteByTemplateUuidAndKeyIdList(@Param("templateUuid") String templateUuid, @Param("KeyIdList")List<Long> KeyIdList);
}
