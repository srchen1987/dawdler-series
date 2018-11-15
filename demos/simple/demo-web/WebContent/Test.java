import com.anywide.dawdler.clientplug.web.VelocityToolBox;

public class Test extends VelocityToolBox {

	public Test(String name) {
		super(name);
	}
	public String sayHello() {
		return "hello";
	}
}
