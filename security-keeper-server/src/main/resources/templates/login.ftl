<!DOCTYPE html>
<html>
    <head>
		<meta charset="utf-8" />
		<meta name="description" content="登录页" />
		<title>security-keeper登录页</title>
	</head>

	<body>
		<div>
			<h2>登录信息</h2>
			<div>
				<form id="loginForm" action="/security/sso/login" method="post">
					<input type="hidden" id="redirect_url" name="redirect_url" value="${redirect_url}" />
					<input id="user_name" name="user_name" type="text" placeholder="登录名" />
					<input id="password" name="password" type="password" placeholder="密码" />
					<input id="loginButton" type="submit", value="登录" />
				</form>
			</div>
		</div>
	</body>
</html>
