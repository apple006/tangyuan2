# 使用说明
---

### 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-mongo</artifactId>
	    <version>1.2.0</version>
	</dependency>

> b. 添加服务组件

在tangyuan总配置文件(tangyuan.xml)添加mongo组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/component.xsd">
		
		<!--添加Mongo服务组件 -->
		<component resource="component-mongo.xml" type="mongo" />
		
	</tangyuan-component>

> c. 配置组件

tangyuan-mongo组件本身的配置(component-mongo.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<mongo-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mongo/component.xsd">
		
		<!-- Mongo数据源 -->
		<dataSource id="mongods">
			<property name="url" value="mongodb://127.0.0.1:27017/mdb" />
		</dataSource>
		
		<!-- Mongo服务插件 -->
		<plugin resource="test-mongo.xml"/>
		
	</mongo-component>

> d. 编写Mongo服务

在服务插件`test-mongo.xml`文件中：

	<?xml version="1.0" encoding="UTF-8"?>
	<mongoservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mongo/service.xsd" ns="mdemo">
	
		<selectSet id="getUser" dsKey="mongods"><![CDATA[
			select * from user where sex = '女' and age >= 18 and age <= 28
		 ]]></selectSet>
		
	</mongoservices>

> e. 单元测试 

	@Test
	public void testSMS() {
		XCO request = new XCO();
		// set
		Object obj = ServiceActuator.execute("mdemo/getUser", request);
		System.out.println(obj);
	}

### 2. 数据源配置


tangyuan-mongo组件中数据源配置分为两种，一种是普通数据源，适用于普通的数据库应用项目；另一种是数据源组，适用于数据量和数据并发访问量大的应用场景，同时需要配合分库分表模块共同使用。数据源的配置位于`component-mongo.xml`中。

#### 2.1 普通数据源

> 配置示例

	<dataSource id="mongods">
		<property name="url" value="mongodb://127.0.0.1:27017/mdb" />
	</dataSource>

> dataSource节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 此数据源的唯一标识，不可重复 |Y|用户定义，但是不能出现”.”|
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 |N|true/false|

> property节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 属性名称 |Y|用户定义|
| value | 属性值 |Y|用户定义|

> property节点属性名称和属性值说明

| 属性名 | 用途及说明 | 取值 | 默认值 |
| :-- | :--| :-- | :-- |
| url | mongo连接协议 | String | |
| socketKeepAlive | 是否保持长链接 | boolean | true |
| maxWaitTime | 长链接的最大等待时间 | int | 10 * 60 * 1000 |
| connectTimeout | 链接超时时间 | int | 60 * 1000 |
| socketTimeout | the socket timeout, in milliseconds | int | 60 * 1000 |
| connectionsPerHost | maximum number of connections | int | 30 |
| minConnectionsPerHost | minimum number of connections | int | |
| maxConnectionIdleTime |  the maximum idle time, in milliseconds, which must be > 0 | int | |
| maxConnectionLifeTime | the maximum life time, in milliseconds, which must be > 0 | int | |
| minHeartbeatFrequency | the minimum heartbeat frequency, in milliseconds, which must be > 0 | int | |
| serverSelectionTimeout |  the server selection timeout, in milliseconds | int | |
| sslInvalidHostNameAllowed | whether invalid host names are allowed in SSL certificates. | boolean | |
| sslEnabled | set to true if using SSL | boolean | |
| requiredReplicaSetName | Sets the required replica set name for the cluster. | String |  |
| writeConcern | 保障write operation的可靠性 | String |  |
| readConcern | 保障read operation的可靠性 | String |  |

#### 2.2 数据源组

> 配置示例

	<dataSourceGroup groupId="dsGourp" start="0" end="9">
		<property name="url" value="mongodb://127.0.0.1:27017/mdb{}" />
	</dataSourceGroup>

> 说明

数据源的本质是基于用户设置的开始和结束索引，创建多个数据源，上面代码代表创建了100个数据源

	mongodb://127.0.0.1:27017/mdb0
	mongodb://127.0.0.1:27017/mdb1
	...
	mongodb://127.0.0.1:27017/mdb9

> dataSourceGroup节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 此数据源的唯一标识，不可重复 |Y|用户定义，但是不能出现”.”|
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 |N|true/false|
| start | 开始索引，默认为0 |N|整数类型，用户定义|
| end | 结束索引 |Y|整数类型，用户定义|

> property节点

除了`url`属性可以使用占位符，其他同`dataSource`中的`property`节点。

### 3. 插件配置

tangyuan-mongo组件中可以通过插件进行服务的定义、管理和功能的扩展；按用途可分为三种：

1. 结果映射插件
2. 分库分表插件
3. MONGO服务插件
	
结果映射插件主要负责返回结果映射的配置，分库分表插件，顾名思义就是对分库分表功能的配置；而服务插件，则是定义具体的MONGO服务的。结果映射插件和分库分表插件都是最多只能有一个，服务插件则可由多个；配置位于组件配置文件`component-mongo.xml`中。

> 配置示例

	<!-- Mongo结果映射插件 -->
	<mapper  	resource="mapper-mongo.xml" />
	<!-- Mongo分库分表插件 -->
	<sharding 	resource="sharding-mongo.xml" />
	<!-- Mongo服务插件 -->
	<plugin 	resource="test-mongo.xml"/>

> mapper、sharding、plugin节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :--: | :-- |
| resource | 插件的资源文件路径 | Y | 用户定义 |

### 4. 返回结果映射

参考tangyuan-sql组件数据映射章节的返回结果映射篇<http://www.xson.org/project/sql/1.2.0/mapper.html>

> schema

<http://xson.org/schema/tangyuan/mongo/mapper.xsd>

### 5. MONGO服务

由于tangyuan-mongo组件中支持使用SQL语法访问Mongo，所以从使用角度来讲，二者非常相似，因此在这里只说明tangyuan-mongo组件区别于tangyuan-sql组件的地方，其他的内容可参考SQL服务<http://www.xson.org/project/sql/1.2.0/sql.html>

> 区别

1. MONGO服务插件的schema和根节点和SQL服务不同，可参考上文中`编写Mongo服务`处；
2. tangyuan-mongo组件中的组合服务是通过`<mongo-service>`节点来定义，而tangyuan-sql组件中的组合服务是通过`<sql-service>`节点来定义；
3. tangyuan-mongo组件中数据源是mongo数据源，和tangyuan-sql组件中数据源无关；
4. tangyuan-mongo组件中无事务概念，所以各个服务标签中也不存在`txRef`属性；

> schema

<http://xson.org/schema/tangyuan/mongo/service.xsd>


### 6. 分库分表

参考tangyuan-sql组件分库分表章节<http://www.xson.org/project/sql/1.2.0/sharding.html>

> schema

<http://xson.org/schema/tangyuan/sql/sharding.xsd>