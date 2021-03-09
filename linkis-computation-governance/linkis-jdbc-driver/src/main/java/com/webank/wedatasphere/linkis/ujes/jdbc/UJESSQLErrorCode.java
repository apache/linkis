package com.webank.wedatasphere.linkis.ujes.jdbc;

/**
 * Created by leebai on 2019/8/8.
 * Modified by owenxu on 2019/8/8.
 */
public enum UJESSQLErrorCode {
    BAD_URL(80000,"bad url"),
    NOSUPPORT_DRIVER(80001,"this method not supported in driver"),
    NOSUPPORT_CONNECTION(80002, "this method not supported in connection"),
    NOSUPPORT_STATEMENT(80003,"this method not supported in statement"),
    CONNECTION_CLOSED(80004,"Connection is closed!"),
    STATEMENT_CLOSED(80005,"statement is closed!"),
    SCHEMA_EMPTY(80006,"schema is empty!"),
    SCHEMA_FAILED(80007,"Get schema failed!"),
    QUERY_TIMEOUT(80008,"query has been timeout!"),
    FILETYPE_ERROR(80009,"file type error"),
    METADATATYPE_ERROR(80010,"metadata type error"),
    NOSUPPORT_METADATA(80011, "this method not supported in DatabaseMetaData"),
    NOPERMITION(80012,"This user has no permission to read this file!"),
    PARAMS_NOT_FOUND(80013, "Parameter not found"),
    ERRORINFO_FROM_JOBINFO(80014,"get errorinfo from jobInfo"),
    RESULTSET_ROWERROR(80015,"row message error"),
    NOSUPPORT_RESULTSET(80016,"this method not supported in resultSet"),
    RESULTSET_NULL(80017,"resultset is null,try to run next() firstly to init ResultSet and MetaData"),
    PREPARESTATEMENT_TYPEERROR(80018,"parameter type error"),
    METADATA_EMPTY(80019,"data is empty")
    ;
    private String msg;
    private int code;

    UJESSQLErrorCode(int code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }
}
