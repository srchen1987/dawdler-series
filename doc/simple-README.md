
#### 简单的调用实例

以一个HelloService的服务调用过程来了解dawdler.

1、定义服务接口与实体(dto或entity)

```java
//定义一个Message的dto
public class Message implements Serializable {
 private static final long serialVersionUID = 4726982442137628060L;
 private int id;
 private String text;

 public int getId() {
  return id;
 }

 public void setId(int id) {
  this.id = id;
 }

 public String getText() {
  return text;
 }

 public void setText(String text) {
  this.text = text;
 }
}
```

```java
//定义一个Hello服务的接口
@RemoteService("simple-service")
public interface HelloService {
 
 //@RemoteServiceAssistant(async = true)
 public String say(String text);
 
 public List<Message> responseList(Map<String, Object> data);
}
```

2、服务提供者

```java
public class HelloServiceImpl implements HelloService{

 @Override
 public String say(String text) {
  System.out.println(new Date()+":"+text);
  return "hi,"+text;
 }

 @Override
 public List<Message> responseList(Map<String, Object> data) {
  System.out.println(data);
  List<Message> list = new ArrayList<>();
  Message message = new Message();
  message.setId(1);
  message.setText("text1");
  list.add(message);
  return list;
 }
}
```

3、调用者

```java
public static void main(String[] args) {
  HelloService hs = ServiceFactory.getService(HelloService.class);
  Map<String, Object> param = new HashMap<>();
  param.put("name", "jackson.song");
  try {
    List<Message> messageList = hs.responseList(param);
    System.out.println(messageList);
  } catch (Throwable e) {
   System.out.println("exception:"+e.getMessage());
  }
  ConnectionPool.shutdown();//释放资源
}
```
