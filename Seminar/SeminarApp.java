import ilog.concert.*;
import ilog.cplex.*;

public class SeminarApp {
	public static void main(String[] args) {
		ColumnGen col_gen = new ColumnGen("R101");
		col_gen.runColumnGeneration();
		//Model_1();
		//Model_2();
		//Model_3(10);
	}
	public static void Model_1() {
		try {
			//create new model
			IloCplex cplex = new IloCplex();
			//define variables
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE,"x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE,"y");
			//define expressions
			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);
			//define objective
			cplex.addMinimize(objective);
			//define constraints
			cplex.addGe(cplex.sum(cplex.prod(60, x),cplex.prod(60, y)),300).setName("con1");
			cplex.addGe(cplex.sum(cplex.prod(12, x),cplex.prod(6,  y)),36);
			cplex.addGe(cplex.sum(cplex.prod(10, x),cplex.prod(30, y)),90);
			//solve model
			if (cplex.solve()) {
				System.out.println("obj = "+cplex.getObjValue());
				System.out.println("x   = "+cplex.getValue(x));
				System.out.println("y   = "+cplex.getValue(y));
			}
			else {
				System.out.println("Model not solved");
			}
			cplex.end();
			Model_2();
		}
		catch (IloException exc) {
			exc.printStackTrace();
		}
	}
	public static void Model_2() {
		try {
			int n = 4; //cargos
			int m = 3; //compartments
			double[] p = {310.0, 380.0, 350.0, 285.0}; //profit
			double[] v = {480.0, 650.0, 580.0, 390.0}; //volume per ton of cargo
			double[] a = {18.0, 15.0, 23.0, 12.0}; //available weight
			double[] c = {10.0, 16.0, 8.0}; //capacity of compartment
			double[] V = {6800.0, 8700.0, 5300.0}; //volume capacity of compartment
			//create new model
			IloCplex cplex = new IloCplex();
			//define variables
			IloNumVar[][] x = new IloNumVar[n][];
			for (int i=0; i<n; i++) {
				x[i] = cplex.numVarArray(m,0,Double.MAX_VALUE);
			}
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE,"y");
			//define expressions
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i=0; i<n; i++) {
				for (int j=0; j<m; j++) {
					objective.addTerm(p[i], x[i][j]);
				}
			}
			IloLinearNumExpr[] usedWeightCapacity = new IloLinearNumExpr[m];
			IloLinearNumExpr[] usedVolumeCapacity = new IloLinearNumExpr[m];
			for (int j=0; j<m; j++) {
				usedWeightCapacity[j] = cplex.linearNumExpr();
				usedVolumeCapacity[j] = cplex.linearNumExpr();
				for (int i=0; i<n; i++) {
					usedWeightCapacity[j].addTerm(1.0,x[i][j]);
					usedVolumeCapacity[j].addTerm(v[i],x[i][j]);
				}
			}
			//define objective
			cplex.addMaximize(objective);
			//define constraints
			for (int i=0; i<n; i++) {
				cplex.addLe(cplex.sum(x[i]),a[i]);
			}
			for (int j=0; j<m; j++) {
				cplex.addLe(usedWeightCapacity[j],c[j]);
				cplex.addLe(usedVolumeCapacity[j],V[j]);
				cplex.addEq(cplex.prod(1/c[j], usedWeightCapacity[j]),y);
			}
			//solve model
			if (cplex.solve()) {
				System.out.println("obj = "+cplex.getObjValue());
			}
			else {
				System.out.println("Model not solved");
			}
			cplex.end();
		}
		catch (IloException exc) {
			exc.printStackTrace();
		}
	}
	public static void Model_3(int n) {
		try {
			//random data
			double[] xPos = new double[n];
			double[] yPos = new double[n];
			for (int i=0; i<n;i++) {
				xPos[i] = Math.random()*100;
				yPos[i] = Math.random()*100;
			}
			double[][] c = new double[n][];
			for (int i=0; i<n;i++) {
				c[i] = new double[n];
				for (int j=0; j<n; j++) {
					c[i][j]=Math.sqrt(Math.pow(xPos[i]-xPos[j], 2)+Math.pow(yPos[i]-yPos[j], 2));
				}
			}
			//model
			IloCplex cplex = new IloCplex();
			//variables
			IloIntVar[][] x = new IloIntVar[n][];
			for (int i=0; i<n ;i++) {
				x[i] = cplex.boolVarArray(n);
			}
			IloNumVar[] u = cplex.numVarArray(n,0,Double.MAX_VALUE);
			//objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					if (i!=j) {
						objective.addTerm(c[i][j], x[i][j]);
					}
				}
			}
			cplex.addMinimize(objective);
			//constraints
			for (int j=0; j<n; j++) {
				IloLinearNumExpr numExpr = cplex.linearNumExpr();
				for (int i=0; i<n; i++) {
					if (i!=j) {
						numExpr.addTerm(1.0, x[i][j]);
					}
				}
				cplex.addEq(numExpr, 1.0);
			}
			for (int i=0; i<n; i++) {
				IloLinearNumExpr numExpr = cplex.linearNumExpr();
				for (int j=0; j<n; j++) {
					if (j!=i) {
						numExpr.addTerm(1.0, x[i][j]);
					}
				}
				cplex.addEq(numExpr, 1.0);
			}
			for (int i=1; i<n; i++) {
				for (int j=1; j<n; j++) {
					if (j!=i) {
						IloLinearNumExpr numExpr = cplex.linearNumExpr();
						numExpr.addTerm( 1.0, u[i]);
						numExpr.addTerm(-1.0, u[j]);
						numExpr.addTerm( n-1, x[i][j]);
						cplex.addLe(numExpr, n-2);
					}
				}
			}
			//solve model
			if (cplex.solve()) {
				System.out.println("obj = "+cplex.getObjValue());
				for (int i=0; i<n; i++){
					for (int j=0; j<n; j++){
						if (i!=j && cplex.getValue(x[i][j])>0.9999) {
							System.out.println(i+","+j);
						}
					}
				}
			}
			else {
				System.out.println("Model not solved");
			}
			cplex.end();
		}
		catch (IloException exc) {
			exc.printStackTrace();
		}
	}
}
