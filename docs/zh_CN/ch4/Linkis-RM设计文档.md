# Linkis RM设计文档
## 背景
在微服务场景下，各种服务所需要消耗和占用的资源具有多样性，不像传统大型应用一样容易管理。Linkis RM提供对资源的统一分配和回收的服务，在大量服务高频率启动和关闭的情况下，保证服务对资源的消耗不超出限制。
## 产品设计
### 总体架构图
RM维护引擎管理器上报的可用资源信息，处理引擎提出的资源申请，并记录成功申请后的实际资源使用信息。

1. 引擎管理器，简称EM：处理启动引擎请求的微服务。EM作为资源的提供者，负责向RM注册资源(register)和下线资源(unregister)。同时，EM作为引擎的管理者，负责代替引擎向RM申请资源。每一个EM实例，均在RM中有一条对应的资源记录，包含它提供的总资源、保护资源等信息，并动态更新已使用资源。

1. 引擎，Engine，又称应用：执行用户作业的微服务。同时，引擎作为资源的实际使用者，负责向RM上报实际使用资源和释放资源。每一个Engine，均在RM中有一条对应的资源记录：在启动过程中，体现为锁定资源；在运行过程中，体现为已使用资源；在被结束之后，该资源记录随之被删除。

### 数据库表结构设计
```

用户资源记录表：
linkis_user_resource_meta_data: 
	id
	user
	ticket_id
	creator
	em_application_name
	em_instance
	engine_application_name
	engine_instance
	user_locked_resource: 直接存储json
	user_used_resource: json
	resource_type
	locked_time
	used_time

模块资源记录表：
linkis_em_resource_meta_data: 
	id
	em_application_name
	em_instance
	total_resource:json
	protected_resource:json
	resource_policy
	used_resource:json
	left_resource:json
	locked_resource:json
	register_time: long

模块policy表：
linkis_em_meta_data: 
	id
	em_name
	resource_request_policy

锁：该表需要添加unique constraint：（scope，user, module_application_name, module_instance），用来保证锁不被强制多次同时获取。
linkis_resource_lock: 
	id
	user
	em_application_name
	em_instance

```

### 资源的类型与格式

如上图所示，所有的资源类均实现一个顶层的Resource接口，该接口定义了所有资源类均需要支持的计算和比较的方法，并进行相应的数学运算符的重载，使得资源之间能够像数字一样直接被计算和比较。

|运算符|对应方法|运算符|对应方法|
|:----    |:---|:----- |:-----   |
|+ |add  |> |moreThan   |
|- |minus  |< | lessThan    |
|*     |multiply  |= | equals    |
| /     |divide  |>= | notLessThan    |
| <=     |notMoreThan  |


当前支持的资源类型如下表所示，所有的资源都有对应的json序列化与反序列化方法，能够通过json格式进行存储和在网络间传递：

|资源类型|	描述|
|:----    |:---|
|MemoryResource |MemoryResource  |
|CPUResource	|CPU资源|
|LoadResource	|同时具备内存与CPU的资源|
|YarnResource	|Yarn队列资源（队列，队列内存，队列CPU，队列实例数）|
|LoadInstanceResource	|服务器资源（内存，CPU，实例数）|
|DriverAndYarnResource	|驱动器与执行器资源（同时具备服务器资源，Yarn队列资源）|
|SpecialResource	|其它自定义资源|


## 记录EM上报的可用资源

1. 持有资源的EM在启动时，将通过RPC调用register接口，传入json格式的资源来进行资源注册。需要向register接口提供的参数如下：
	1)	总资源：该EM能够提供的资源总数。
	2)	保护资源：当剩余资源小于该资源时，不再允许继续分配资源。
	3)	资源类型：如LoadResource，DriverAndYarnResource等类型名称。
	4)	EM名称：如sparkEngineManager等提供资源的EM名称。
	5)	EM实例：机器名加端口名。
1. RM在收到资源注册请求后，在表linkis_module_resource_meta_data中新增一条记录，内容与接口的参数信息一致。
1. 持有资源的EM在关闭时，将通过RPC调用unregister接口，传入自己的EM实例信息作为参数，来进行资源的下线。
1. RM在收到资源下线请求后，在linkis_module_resource_meta_data表中找到EM实例信息对应的那一行，进行删除处理；同时在linkis_user_resource_meta_data表中，找到该EM实例对应的所有行，进行删除处理。

## 资源的分配与回收
1.	接收用户的资源申请。
	a)	RM提供requestResource接口给EM上报资源申请，该接口接受EM实例、用户、Creator和Resource对象作为参数。requestResource接受一个可选的时间参数，当处理事件超出该时间参数的限制时，该资源申请将自动作为失败处理。
2.	判断是否有足够的资源。
	a)	根据EM实例信息，找到该EM提供的资源类型，再找到对应的RequestResourceService（有多个子类，每个子类均与一种或多种资源类型对应，有各自的处理逻辑）。
	b)	RequestResourceService从多个维度统计剩余的可用资源。
	&ensp;&ensp;i.	根据该EM的总资源，减去已用资源和保护资源后，得出剩余的EM可用资源。
	&ensp;&ensp;ii.	根据该Creator允许使用的资源上限，减去该creator已使用的资源后，得出剩余的Creator可用资源。
	&ensp;&ensp;iii.	根据该用户允许使用的资源上限，减去该用户已使用的资源后，得出剩余的用户可用资源。
	&ensp;&ensp;iv.	根据该用户全局的实例数上限，减去该用户已启动的引擎个数，得出剩余的可用实例数。
	c)	分步骤将剩余可用数量与申请的资源进行比较。
	&ensp;&ensp;i.	按b中所列的顺序，一旦某个步骤的剩余可用数量小于申请的数量时，立刻判定无足够资源，返回NotEnoughResource以及相应的提示信息，不再进行后续步骤的判定。
	&ensp;&ensp;ii.	以上步骤里，如果直到最后剩余可用数量都大于申请的数量，则判定有足够资源，进行下一步锁定资源。
3.	为成功申请到资源的请求锁定资源。确认资源足够后，为该申请提前锁定资源，并生成唯一标识。
	a)	为了保证并发场景下的正确性，进行锁定操作之前，需要加两个锁（锁机制的具体实现在另外章节中描述）：EM锁和用户锁。
	&ensp;&ensp;i.	EM锁。获得该锁以后，将不允许其它针对该EM的资源操作。
	&ensp;&ensp;ii.	用户锁。获得该锁以后，将不允许该用户的其它资源操作。
	b)	在两个锁均成功获得后，将再次重复判断一遍资源是否足够，如果依然足够，则继续进行后续步骤。
	c)	为该资源申请生成一个UUID，并在linkis_user_resource_meta_data表中插入一条用户资源记录（pre_used_resource为申请的资源数量，used_resource为null）。
	d)	在linkis_module_resource_meta_data表中更新对应的EM资源记录字段（locked_resource,left_resource）。
	e)	提交一个定时任务，该任务如果不被取消，则在固定时间后回滚c、d两步的操作，并将UUID作废，以便未被实际使用的已锁定资源不会被无限占据。
	f)	将UUID返回给资源申请方。
	g)	无论以上步骤中发生了什么，都在最后释放a中获得的两个锁。
4.	接收用户上报的实际使用资源。
	a)	提供resourceInited接口，接受UUID、用户名、EM实例信息、实际使用Resource对象和引擎实例信息作为参数。
	b)	接收到上报信息后，获得EM锁和用户锁。
	c)	根据UUID查询到对应的锁定资源的记录，将pre_used_resource更新为null，将实际使用的资源填写used_resource。
	d)	更新对应的模块资源记录(恢复locked_resource,新增used_resource)。
	e)	异常情况：如果找不到对应的UUID，则认为已经丢失对资源的锁定，返回异常信息。
5.	接收用户释放资源的请求。
	a)	提供resourceReleased接口，接受UUID、用户名、EM实例作为参数。
	b)	接收到请求后，获得EM锁和用户锁。
	c)	根据UUID查询到对应的用户资源记录，删除该行。
	d)	更新对应的模块资源记录(清理used_resource，恢复left_resource)。

## EM锁与用户锁的实现
通过linkis_resource_lock表来实现锁，利用数据库本身的unique constraint机制保证数据不被抢写。
1.	EM锁：针对全局锁住对某个EM的某个实例的操作。
	a)	获得锁：
	&ensp;&ensp;i.	检查是否存在user为null、且application和instance栏位为对应值的记录，若有，则说明该锁已被其它实例获得，轮询等待。
	&ensp;&ensp;ii.	当发现没有对应记录时，插入一条记录，若插入成功，则说明成功获得锁；若插入遇到违反UniqueConstraint错误，则记录轮询等待，直到timeout。
	b)	释放锁：
	&ensp;&ensp;i.	删除自己所持有的那行记录。
2.	用户锁：针对某个用户锁住对某个EM的操作。
	a)	获得锁：
	&ensp;&ensp;i.	检查是否存在user，application和instance栏位为对应值的记录，若有，则说明该锁已被其它实例获得，轮询等待。
	&ensp;&ensp;ii.	当发现没有对应记录时，插入一条记录，若插入成功，则说明成功获得锁；若插入失败，则记录轮询等待，直到timeout。
	b)	释放锁：
	&ensp;&ensp;i.	删除自己所持有的记录。

## RM客户端
以jar包的形式，提供客户端给资源使用方和资源提供方，包含以下内容：
1.	所有资源类型的Java类（Resource类的子类），以及相应的json序列化方法。
2.	所有资源分配结果的Java类（ResultResource类的子类），以及相应的json序列化方法。
3.	封装好的RM接口（资源注册、下线、申请以及可用资源和释放资源的请求）。
调用客户端的接口后，客户端将生成对应的RPC命令，通过Sender传递给RM的一个微服务进行处理。RM处理完毕后，同样通过RPC将结果返回给客户端。

## 多实例状态同步
&ensp;&ensp;由于RM属于关键的底层服务，为了防止因为某个RM实例出现异常而影响所有服务的资源分配，必须保证同时有多个RM实例处于服务状态，并保证一个请求无论是被哪个实例处理，均能保证结果的一致性。
&ensp;&ensp;用户在请求RM的服务时，必须通过网关服务的转发来请求，而无法直接请求某一台固定的RM实例。网关服务通过服务注册与发现机制，识别出正常提供服务的RM实例，从而将RPC请求转发给其中一个实例。这就保证了所有请求均会被正常状态的RM实例处理。
&ensp;&ensp;RM所有的资源记录均存储在同一个数据库中，所有的RM实例均不维护自身的状态。RM在处理请求时，凡是涉及状态变化的，都会在加锁后，从数据库实时获取状态信息，完成处理逻辑后立刻将状态更新回数据库，再释放锁。这就保证了多个RM同时处理请求时，总能基于最新的状态。

