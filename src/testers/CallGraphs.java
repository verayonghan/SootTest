package testers;

public class CallGraphs
{
	public static void main(String[] args) { 
		int i = 0;
		doStuff(1);
	}
	
	public static void doStuff(int x) {
		int i = 0;
		int j = 1;
		i++;
		j--;
		new A().foo();
		new CallGraphs2().foo2();
	}
}

class A
{
	public void foo() {
		bar();
	}
	
	public void bar() {
	}
}