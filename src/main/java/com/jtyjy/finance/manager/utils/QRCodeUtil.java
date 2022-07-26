package com.jtyjy.finance.manager.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.jtyjy.common.tools.QRCodeTool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 * @author User
 *
 */
public class QRCodeUtil {
	
	/**
	 * 生成Base64二维码
	 * @param content
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static final String createBase64Qrcode(String content, String path) throws Exception {
		QRCodeTool.codeSave2Path(content, path);
		return Base64Util.getImageStr(path);
	}
}
