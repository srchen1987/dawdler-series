/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.serverplug.db.dao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.anywide.dawdler.serverplug.load.bean.Page;
import com.anywide.dawdler.serverplug.transaction.LocalConnectionFacotry;
/**
 * 
 * @Title:  SuperDAO.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年04月15日       
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public  class SuperDAO implements BaseData{
	protected Connection con;
	private BaseData basedata;
	
	public SuperDAO(Connection con) {
		this.con=con;
		basedata = new BaseDataImpl(this.con);
	}
	public SuperDAO() {
		basedata = new BaseDataImpl();
	}
	public Connection getReadConnection() throws SQLException{
		if(con!=null)return con;
		return LocalConnectionFacotry.getReadConnection();
	}
	public Connection getWriteConnection(){
		if(con!=null)return con;
		return LocalConnectionFacotry.getWriteConnection();
	}
	public <T> List<T> queryList(String sql, Class<T> c) throws SQLException {
		return basedata.queryList(sql, c);
	}
	public <T> List<T> queryListPrepare(String sql, Class<T> c,
			Object... values) throws SQLException {
		return basedata.queryListPrepare(sql, c, values);
	}
	public int update(String sql) throws SQLException {
		return basedata.update(sql);
	}
	public int updatePrepare(String sql, Object... values) throws SQLException {
		return basedata.updatePrepare(sql, values);
	}
	public int insert(String sql) throws SQLException {
		return basedata.insert(sql);
	}
	public int insertPrepare(String sql, Object... values) throws SQLException {
		return basedata.insertPrepare(sql, values);
	}
	public int delete(String sql) throws SQLException {
		return basedata.delete(sql);
	}
	public int deletePrepare(String sql, Object... values) throws SQLException {
		return basedata.deletePrepare(sql, values);
	}
	public int queryCount(String sql) throws SQLException {
		return basedata.queryCount(sql);
	}
	public int queryCountPrepare(String sql, Object... values)
			throws SQLException {
		return basedata.queryCountPrepare(sql, values);
	}
	public <T> List<T> queryListPage(String countsql,String sql,int pageon,int row,Class<T> c) throws SQLException{
		int rowcount = queryCount(countsql);
		if(pageon<=0)pageon=1;
		if(row<=0)row=1;
		int pagecount=rowcount%row==0?rowcount/row:rowcount/row+1;
		pageon=pageon<pagecount?pageon:pagecount;
		if(rowcount==0)return new ArrayList<T>();
		return basedata.queryList(sql+" limit "+((pageon-1)*row)+","+row, c);
	}
	public <T> List<T> queryListPagePrepare(String countsql,String sql,int pageon,int row,Class<T> c,Object... values) throws SQLException{
		int rowcount = queryCountPrepare(countsql,values);
		if(pageon<=0)pageon=1;
		if(row<=0)row=1;
		int pagecount=rowcount%row==0?rowcount/row:rowcount/row+1;
		pageon=pageon<pagecount?pageon:pagecount;
		if(pageon==0)pageon=1;
		if(rowcount==0)return new ArrayList<T>();
		return basedata.queryListPrepare(sql+" limit ?,? ", c, setPage(((pageon-1)*row),row, values));
	}
	public <T> List<T> queryListPage(String countsql,String sql,int pageon,int row,Page page,Class<T> c) throws SQLException{
//		page.rowcount = queryCount(countsql);
		page.setRow(row);
		page.setPageon(pageon);
		page.setRowcountAndCompute(queryCount(countsql));
//		if(pageon<=0)pageon=1;
//		if(row<=0)row=1;
		/*page.pagecount=page.rowcount%row==0?page.rowcount/row:page.rowcount/row+1;
		page.pageon=pageon<page.pagecount?pageon:page.pagecount;
		if(page.pageon==0)page.pageon=1;
		page.row=row;*/
		if(page.getRowcount()==0)return new ArrayList<T>();
		return basedata.queryList(sql+" limit "+((page.getPageon()-1)*page.getRow())+","+page.getRow(), c);
	}
	public <T> List<T> queryListPagePrepare(String countsql,String sql,int pageon,int row,Page page,Class<T> c,Object... values) throws SQLException{
		page.setRow(row);
		page.setPageon(pageon);
		page.setRowcountAndCompute(queryCountPrepare(countsql,values));
		
		/*page.rowcount = queryCountPrepare(countsql,values);
		if(pageon<=0)pageon=1;
		if(row<=0)row=1;
		page.pagecount=page.rowcount%row==0?page.rowcount/row:page.rowcount/row+1;
		page.pageon=pageon<page.pagecount?pageon:page.pagecount;
		if(page.pageon==0)page.pageon=1;
		page.row=row;*/
		if(page.getRowcount()==0)return new ArrayList<T>();
		return basedata.queryListPrepare(sql+" limit ?,? ", c, setPage(((page.getPageon()-1)*page.getRow()),page.getRow(), values));
	}
	
	
	private  Object[] setPage(int pageon,int row,Object ...values){
		if(values==null){
			Object [] tem = {pageon,row};
			return tem;
		}
		Object[] tem = new Object[values.length+2];
		for(int i=0;i<values.length;i++){
			tem[i]=values[i];
		}
		tem[values.length]=pageon;
		tem[values.length+1]=row;
		return tem;
	}
	
	public int insertPrepareGetKey(String sql, Object... values)
			throws SQLException {
		return basedata.insertPrepareGetKey(sql, values);
	}
	
	public List<Map<String, Object>> queryListMaps(String sql)
			throws SQLException {
		return basedata.queryListMaps(sql);
	}
	
	public List<Map<String, Object>> queryListMapsPrepare(String sql,
			Object... values) throws SQLException {
		return basedata.queryListMapsPrepare(sql, values);
	}
	
	public List<Map<String, Object>> queryListMapsPage(String countsql,String sql,int pageon,int row,Page page,Object... values) throws SQLException {
		page.setRow(row);
		page.setPageon(pageon);
		page.setRowcountAndCompute(queryCountPrepare(countsql,values));
		return basedata.queryListMaps(sql+" limit "+((page.getPageon()-1)*row)+","+row);
	}
	public List<Map<String, Object>> queryListMapsPagePrepare(String countsql,String sql,int pageon,int row,Page page,Object... values) throws SQLException {
		page.setRow(row);
		page.setPageon(pageon);
		page.setRowcountAndCompute(queryCountPrepare(countsql,values));
		return basedata.queryListMapsPrepare(sql+" limit ?,? ", setPage(((page.getPageon()-1)*page.getRow()),page.getRow(), values));
	}
	
	public Page newPage(){
		return new Page();
	}
	@Override
	public int insertMap(String tableName, Map<String, Object> datas) throws SQLException {
		return basedata.insertMap(tableName, datas);
	}
	@Override
	public int insertMapGetKey(String tableName, Map<String, Object> datas) throws SQLException {
		return basedata.insertMapGetKey(tableName, datas);
	}
	
	@Override
	public <T> T queryObject(String sql, Class<T> c) throws SQLException {
		return basedata.queryObject(sql, c);
	}
	@Override
	public <T> T queryObjectPrepare(String sql, Class<T> c, Object... values) throws SQLException {
		return basedata.queryObjectPrepare(sql,c,values);
	}
}