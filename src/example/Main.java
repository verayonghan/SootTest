package example;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

public class Main
{	
	public static void main(String[] args) {
	   List<String> argsList = new ArrayList<String>(Arrays.asList(args));
	   argsList.addAll(Arrays.asList(new String[]{
			   "-w", //whole program
			   "-process-path",
			   "bin",
			   "-soot-classpath",
			   "bin",//:/Users/vera/Downloads/rt.jar",
			   "-main-class",
			   //"testers.CallGraphs",//main-class
			   //"testers.CallGraphs",//argument classes
			   //"testers.A"
	   }));

	   //String classpath = System.getProperty("java.class.path");
	   //System.out.println(classpath);
	   //String[] args2 = new String[argsList.size()];
       //args2 = argsList.toArray(args2);
       //Options.v().set_soot_classpath(".:rt.jar");
	   //System.out.println(Options.v().soot_classpath());
       //Options.v().set_help(true);
	   //Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
	   //Options.v().set_prepend_classpath(true);
	   //Options.v().set_main_class("testers.CallGraphs");
	   //Options.v().set_process_dir();
	   
	    PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

		@Override
		protected void internalTransform(String phaseName, Map options) {
		       CHATransformer.v().transform();
               //SootClass a = Scene.v().getSootClass("testers.A");
		       String methodName = "testInsertWithFailingEventPersistence";
		       SootMethod src = Scene.v().getMainClass().getMethodByName(methodName);
		       CallGraph cg = Scene.v().getCallGraph();
		       
//		       System.out.println("**************   ICFG of " + src.toString() + "   *************");
//		       JimpleBasedInterproceduralCFG icfg =  new JimpleBasedInterproceduralCFG();
//		       getEdgesOfICFG(icfg, src);
		       
		       System.out.println("**************   Serialize CallGraph  ************");
		       serializeCallGraph(cg, "dotOutput" + File.separator + methodName);
		       
		       System.out.println("**************   CallGraph of " + src.toString() + "   ************");
               getEdges(cg, src);
		}
		
		private void getEdgesOfICFG(JimpleBasedInterproceduralCFG icfg, SootMethod src) {
			DirectedGraph<Unit> unitGraph = icfg.getOrCreateUnitGraph(src); //get src's unit graph
			Iterator<Unit> unitIterator = unitGraph.iterator();
		    while(unitIterator.hasNext()) {  // iterate unit
		    	Unit nextUnit = unitIterator.next();
		    	System.out.println(src.toString() + ": " + nextUnit.toString());
		    	Collection<SootMethod> callees = icfg.getCalleesOfCallAt(nextUnit); // which methods are called by unit 
		    	for (SootMethod callee: callees) {
		    		getEdgesOfICFG(icfg,callee);
		    	}
	    	   }
		}
		
		
		private void getEdges(CallGraph cg, SootMethod src) {
			Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
			while (targets.hasNext()) {
		           SootMethod tgt = (SootMethod)targets.next();
		           System.out.println(src + " may call " + tgt);
		           getEdges(cg, tgt);
			}
		}

	   }));

       args = argsList.toArray(new String[0]);
       soot.Main.v().autoSetOptions();
	   System.setProperty("sun.boot.class.path",
				   "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/lib/rt.jar");
	   System.setProperty("java.ext.dirs",
				"/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/lib/ext");
       System.out.println(System.getProperty("sun.boot.class.path"));
       soot.Main.main(args);
	}
	
	//write to a dot file
	public static void serializeCallGraph(CallGraph graph, String fileName) { 
		if (fileName == null) { 
			fileName = soot.SourceLocator.v().getOutputDir(); 
			}
//		if (fileName.length() > 0) { 
//				fileName = fileName + File.separator; 
//			} 
		fileName = fileName + "-call-graph" + DotGraph.DOT_EXTENSION; 
		
		System.out.println("writing to file " + fileName); 
		DotGraph canvas = new DotGraph("call-graph"); 
		QueueReader<Edge> listener = graph.listener(); 
		while (listener.hasNext()) { 
			Edge next = listener.next(); 
			MethodOrMethodContext src = next.getSrc(); 
			MethodOrMethodContext tgt = next.getTgt(); 
			String srcString = src.toString(); 
			String tgtString = tgt.toString(); 
			if ((!srcString.startsWith("<java.") && !srcString.startsWith("<sun.") 
					&& !srcString.startsWith("<org.") && !srcString.startsWith("<com.") 
					&& !srcString.startsWith("<jdk.") && !srcString.startsWith("<javax."))
					||
				(!tgtString.startsWith("<java.") && !tgtString.startsWith("<sun.")
					&& !tgtString.startsWith("<org.") && !tgtString.startsWith("<com.")
					&& !tgtString.startsWith("<jdk.") && !tgtString.startsWith("<javax."))) {
				canvas.drawNode(src.toString());
				canvas.drawNode(tgt.toString());
				canvas.drawEdge(src.toString(), tgt.toString());
				//System.out.println("src = " + srcString);
				//System.out.println("tgt = " + tgtString);
			}
		}
		canvas.plot(fileName);
		return;
	}
	
	
	
}
