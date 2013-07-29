package com.qdevelop.web.action;

import com.qdevelop.bean.ResultBean;
import com.qdevelop.lang.QDevelopException;

public class WorkFlowAction extends QDevelopAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2093865792681369518L;
	private String index,operate;
	private ResultBean rb;
	private WFResult wfr;
	
	public String execute() throws QDevelopException{
		if(operate.equals("list")){
			
			return "list";
		}else{

			wfr = new WFResult(true);
			return "operate";
		}
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	/**
	 * 工作流操作 三种值：agree:同意是|refuse:拒绝|list:意见列表
	 * @return
	 */
	public String getOperate() {
		return operate;
	}
	public void setOperate(String operate) {
		this.operate = operate;
	}
	public ResultBean getRb() {
		return rb;
	}
	public void setRb(ResultBean rb) {
		this.rb = rb;
	}
	public WFResult getWfr() {
		return wfr;
	}
	public void setWfr(WFResult wfr) {
		this.wfr = wfr;
	}
	
}
class WFResult{
	public WFResult(boolean isSuccess){
		this.isSuccess = isSuccess;
	}
	public boolean isSuccess;

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
}
