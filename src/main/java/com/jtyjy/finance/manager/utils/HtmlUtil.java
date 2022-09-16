package com.jtyjy.finance.manager.utils;

import javax.servlet.http.HttpServletResponse;

/**
 * html工具类
 * @author User
 *
 */
public class HtmlUtil {
	
	/**
	 * <p>直接输出页面</p>
	 * 作者 konglingcheng
	 * date 2020年6月19日 
	 * <p>@param message 渲染内容，可以是html
	 * <p>@param response
	 * <p>@throws Exception</p>
	 */
	public static final void draw(String message, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(message);
		response.getWriter().flush();
		response.getWriter().close();
	}
	
	/**
	 * 统一提示
	 * @param title 标题
	 * @param optName 操作
	 * @param result 操作结果
	 * @param detail 详情
	 * @param time 时间
	 * @return
	 */
	public static final String html(String title, String optName, String result, String detail, String time) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>").append("\r\n");
		sb.append("<html>").append("\r\n");
		sb.append("<head>").append("\r\n");
		sb.append("<meta charset=UTF-8\">").append("\r\n");
		sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">").append("\r\n");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">").append("\r\n");
		sb.append("<title>"+title+"</title>").append("\r\n");
		sb.append("<style>").append("\r\n");
		sb.append("*{margin:0;padding:0;}").append("\r\n");
		sb.append("#content-wrap{width:80%;border-radius: 10px;padding:10px;margin:50px  auto 0;box-shadow: 0px 4px 8px rgba(181, 181, 181, 0.3);font-size: 14px;color:#333;}").append("\r\n");
		sb.append(".title{color:#999;width: 90px;display: inline-block;}").append("\r\n");
		sb.append(".content{width:calc(100% - 90px);display: inline-block;vertical-align: top;}").append("\r\n");
		sb.append(".time{text-align: right;}").append("\r\n");
		sb.append("p{margin-bottom: 10px;}").append("\r\n");
		sb.append("</style>").append("\r\n");
		sb.append("</head>").append("\r\n");
		sb.append("<body>").append("\r\n");
		sb.append("<div id=\"content-wrap\">").append("\r\n");
		sb.append("<p><span class=\"title\">您正在进行：</span><span class=\"content\">"+optName+"</span></p>").append("\r\n");
		sb.append("<p><span class=\"title\">本次结果：</span><span class=\"content\">"+result+"</span></p>").append("\r\n");
		sb.append("<p><span class=\"title\">详情：</span><span class=\"content\">"+detail+"</span></p>").append("\r\n");
		sb.append("<p class=\"time\">"+time+"</p>").append("\r\n");
		sb.append(" </div>").append("\r\n");
		sb.append("</body>").append("\r\n");
		sb.append("</html>").append("\r\n");
		return sb.toString();
	}
	
	/**
	 * 异议消息
	 * @return
	 */
	public static final String msgHtml(String title, String serverUrl, String id, String detail, Boolean isObjection) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>").append("\r\n");
		sb.append("<html>").append("\r\n");
		sb.append("<head>").append("\r\n");
		sb.append("<script src=\"https://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js\"></script>").append("\r\n");
		sb.append(" <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" rel=\"stylesheet\">").append("\r\n");
		sb.append("<script src=\"https://cdn.staticfile.org/twitter-bootstrap/3.3.7/js/bootstrap.min.js\"></script>").append("\r\n");
		sb.append("<meta charset=UTF-8\">").append("\r\n");
		sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">").append("\r\n");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">").append("\r\n");
		sb.append("<title>"+title+"</title>").append("\r\n");		
		sb.append("</head>").append("\r\n");

		sb.append("<input type=\"hidden\" id=\"id\" desc='"+id+"'>").append("\r\n");
		sb.append("<input type=\"hidden\" id=\"myUrl\" desc='"+serverUrl+"'>").append("\r\n");
		sb.append("<input type=\"hidden\" id=\"isObjection\" desc='"+isObjection+"'>").append("\r\n");

		sb.append("<body>").append("\r\n");
		sb.append("<div>").append("\r\n");
		sb.append(detail);
		sb.append(" </div>").append("\r\n");


		sb.append("<div style=\"position: absolute;bottom: 5rem;width: 100%\">").append("\r\n");
		sb.append("<div style=\"width: 5rem;margin:0 auto\">").append("\r\n");
		sb.append("<button style=\"width: 8rem; display: inline-block;text-align: center\" class=\"btn btn-primary\" data-toggle=\"modal\" data-target=\"#myModal\" >").append("\r\n");
		sb.append("异议").append("\r\n");
		sb.append("</button>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("<div class=\"modal fade\" id=\"myModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"myModalLabel\" aria-hidden=\"true\" style=\"margin-top: 20rem\">").append("\r\n");
		sb.append("<div class=\"modal-dialog\">").append("\r\n");
		sb.append("<div class=\"modal-content\">").append("\r\n");
		sb.append("<div class=\"modal-header\">").append("\r\n");
		sb.append("<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>").append("\r\n");
		sb.append("<h4 class=\"modal-title\" id=\"myModalLabel\">提出异议</h4>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append(" <textarea style=\"width: 100%;\" class=\"remark\"></textarea>").append("\r\n");
		sb.append("<div class=\"modal-footer\">").append("\r\n");
		sb.append("<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">关闭</button>").append("\r\n");
		sb.append("<button type=\"button\" class=\"btn btn-primary msg\">提交异议</button>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("</div>").append("\r\n");
		sb.append("</body>").append("\r\n");

		sb.append("<script>").append("\r\n");
		sb.append("$(function (){").append("\r\n");
		sb.append(" let disabled =$('#isObjection').val()").append("\r\n");
		sb.append("if(disabled==='true'){").append("\r\n");
		sb.append("$('.btn').attr('disabled','disabled')").append("\r\n");
		sb.append("}").append("\r\n");
		sb.append("let myUrl=$('#myUrl').val();").append("\r\n");
		sb.append("let id=$('#id').val();").append("\r\n");
		sb.append("$('.msg').click(function (){").append("\r\n");
		sb.append("$.ajax({").append("\r\n");
		sb.append("url:myUrl+'/api/msg/objection',").append("\r\n");
		sb.append("type:'post',").append("\r\n");
		sb.append("dataType:\"json\",").append("\r\n");
		sb.append("data:{").append("\r\n");

		sb.append("id:id,").append("\r\n");
		sb.append("remark:$('.remark').val(),").append("\r\n");


		sb.append("},").append("\r\n");
		sb.append("success:function (res){").append("\r\n");
		sb.append("if(res.code==0){").append("\r\n");
		sb.append("alert('提交成功')").append("\r\n");
		sb.append("$('#myModal').modal('hide');").append("\r\n");
		sb.append("$('.btn').attr('disabled','disabled')").append("\r\n");
		sb.append("}").append("\r\n");
		sb.append("},").append("\r\n");
		sb.append("error:function (res){").append("\r\n");
		sb.append("alert(res.msg)}").append("\r\n");
		//

		sb.append("})").append("\r\n");
		sb.append(" });").append("\r\n");
		sb.append("})").append("\r\n");
		sb.append("</script>").append("\r\n");

		sb.append("").append("\r\n");
		sb.append("").append("\r\n");
		sb.append("").append("\r\n");
		sb.append("").append("\r\n");

		sb.append("</html>").append("\r\n");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(html("报销审核", "票面接收", "成功", "水电费比带哦VB打副本VN无法大V那个in就", "2021-07-28 09:56:00"));
	}

}
