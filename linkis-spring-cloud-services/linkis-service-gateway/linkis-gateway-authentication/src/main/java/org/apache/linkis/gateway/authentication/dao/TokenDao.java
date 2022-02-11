package org.apache.linkis.gateway.authentication.dao;

import org.apache.linkis.gateway.authentication.entity.TokenEntity;

import java.util.List;


public interface TokenDao {
    Boolean insertToken(TokenEntity token); //TODO

    Boolean updateToken(TokenEntity token); //TODO

    Boolean removeToken(TokenEntity token); //TODO

    List<TokenEntity> selectTokenByName(String tokenName);

    List<TokenEntity> getAllTokens();
}
