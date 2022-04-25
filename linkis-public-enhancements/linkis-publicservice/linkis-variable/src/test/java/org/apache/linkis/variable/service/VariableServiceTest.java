package org.apache.linkis.variable.service;

import org.apache.linkis.protocol.variable.ResponseQueryVariable;
import org.apache.linkis.variable.dao.VarMapper;
import org.apache.linkis.variable.entity.VarKeyValueVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@ExtendWith(MockitoExtension.class)
public class VariableServiceTest {
    @InjectMocks
    VariableServiceImpl variableService;

    @Mock
    VarMapper varMapper;

    @Test
    void testQueryGolbalVariable(){
        Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
        assertEquals(new ResponseQueryVariable().getClass(),variableService.queryGolbalVariable("tom1").getClass());
    }

    @Test
    void testQueryAppVariable(){
        Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
        assertEquals(new ResponseQueryVariable().getClass(),variableService.queryAppVariable("tom1","bob","link").getClass());
    }

    @Test
    void testListGlobalVariable(){
        Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
        assertEquals(new ArrayList<>().getClass(),variableService.listGlobalVariable("tom1").getClass());
    }

    @Test
    void testSaveGlobalVaraibles(){
//        Mockito.when(varMapper.listGlobalVariable("tom1")).thenReturn(new ArrayList<>());
//        variableService.saveGlobalVaraibles(new ArrayList<>(),new ArrayList<>(),"tom");
    }
}
