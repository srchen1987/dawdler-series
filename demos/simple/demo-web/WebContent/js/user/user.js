$(document).ready(function() {
	User.init();
});

var User = {
		init: function(){
			User.addRule();
			User.initEvent();
		},
		initEvent: function(){
			$('#userSubmit').on('click', User.saveUser);//添加用户
			$('.deleteUser').on('click', User.deleteUser);//删除用户
		},
		saveUser: function(){
			if (sir_validate.validateAll()) {
				$("#userForm").ajaxSubmit({
					url : 'save.do',
					type : 'post',
					dataType:"json",
					success : function(data) {
						if(data.validate_error){//后台验证返回
							for(var msg in data.validate_error){
				 				alert(data.validate_error[msg]);
				 				return false;
				 			}
						}else{
							if(data.status){
								alert(data.message);
								//window.location.href = "list.html";
							}else{
								alert(data.message);
								return false;
							}
						}
					}
				});
			}
		},
		deleteUser:function(){
			var userid = $(this).attr('data-id');
			$.ajax({
				url:"del.do",
				type:"post",
				dataType:"json",
				data:{
					userid:userid
				},
				success:function(data){
					if(data.status==1){
						window.location.href = "list.html";
					}else{
						alert(data.message);
					}
				}
			});
		},
		addRule:function(){
			sir_validate.addRule([
				{"id" : "userame","viewname" : "用户名称","validaterule" : "notEmpty&maxsize:45"}, 
				{"id" : "age","viewname" : "年龄","validaterule" : "notEmpty&number"}
			]);
		}
}