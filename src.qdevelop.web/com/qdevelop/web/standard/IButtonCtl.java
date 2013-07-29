package com.qdevelop.web.standard;

import javax.servlet.http.HttpServletRequest;

public interface IButtonCtl {
	public void verifyUserButtonAuthorization(String menuId,String[] rids,HttpServletRequest request);
}
