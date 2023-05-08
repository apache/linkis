package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.ErrorCodeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PsErrorCodeMapperTest extends BaseDaoTest {

    @Autowired
    PsErrorCodeMapper psErrorCodeMapper;

    ErrorCodeEntity insert() {
        ErrorCodeEntity errorCodeEntity = new ErrorCodeEntity();
        errorCodeEntity.setErrorCode("errorCode");
        errorCodeEntity.setErrorDesc("errorDesc");
        errorCodeEntity.setErrorType(1);
        errorCodeEntity.setErrorRegex("errorRegex");
        psErrorCodeMapper.insert(errorCodeEntity);
        return errorCodeEntity;
    }

    @Test
    void getListByPage() {
        insert();
        List<ErrorCodeEntity> list = psErrorCodeMapper.getListByPage("error");
        assertTrue(list.size() > 0);
    }

}